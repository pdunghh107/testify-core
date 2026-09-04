package com.zcomini.backend.auth.client;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import com.zcomini.backend.auth.client.dto.HostAppIdentity;
import com.zcomini.backend.auth.config.AuthProperties;
import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;

@Component
public class HttpHostAppTokenVerifier implements HostAppTokenVerifier {

    private final AuthProperties authProperties;

    public HttpHostAppTokenVerifier(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public HostAppIdentity verify(String hostAccessToken) {
        if (!StringUtils.hasText(hostAccessToken)) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    ApiErrorCode.UNAUTHORIZED.value(),
                    "Host app access token is required.");
        }

        String endpoint = resolveEndpoint();
        JsonNode payload = exchangeProfile(endpoint, hostAccessToken.trim());
        validateAudience(payload);

        JsonNode identityNode = firstObject(payload, "data", "user", "userInfo", "profile", "identity");
        if (identityNode == null) {
            identityNode = payload;
        }
        JsonNode nestedIdentityNode = firstObject(identityNode, "userInfo", "profile", "identity", "user");
        if (nestedIdentityNode != null && StringUtils.hasText(firstText(
                nestedIdentityNode,
                "cccd_number",
                "citizenIdNumber",
                "citizen_id_number",
                "soCccd",
                "soDinhDanh",
                "identityNumber"))) {
            identityNode = nestedIdentityNode;
        }

        String citizenIdNumber = normalizeDigits(firstText(
                identityNode,
                "cccd_number",
                "citizenIdNumber",
                "citizen_id_number",
                "soCccd",
                "soDinhDanh",
                "identityNumber"));
        String fullName = firstText(
                identityNode,
                "full_name",
                "fullName",
                "tenDayDu",
                "hoTen",
                "name");
        String phone = normalizeDigits(firstText(
                identityNode,
                "phone",
                "phoneNumber",
                "phone_number",
                "mobile",
                "mobilePhone",
                "mobile_phone",
                "tel",
                "telephone",
                "soDienThoai",
                "dienThoai",
                "soDienThoaiDiDong",
                "sdt"));
        String subject = firstText(identityNode, "sub", "subject", "id", "userId", "uuid");
        if (!StringUtils.hasText(subject)) {
            subject = firstText(payload, "sub", "subject", "id", "userId", "uuid");
        }

        if (citizenIdNumber.length() != 12 || !StringUtils.hasText(fullName)) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    ApiErrorCode.AUTH_TOKEN_INVALID.value(),
                    "Host app token does not contain a verified citizen identity.");
        }

        return new HostAppIdentity(
                normalizeNullable(subject),
                fullName.trim(),
                citizenIdNumber,
                phone,
                firstText(identityNode, "date_of_birth", "dateOfBirth", "ngaySinh"),
                payload);
    }

    private String resolveEndpoint() {
        if (StringUtils.hasText(authProperties.hostAppProfileUrl())) {
            return authProperties.hostAppProfileUrl().trim();
        }
        if (StringUtils.hasText(authProperties.hostAppTokenVerifyUrl())) {
            return authProperties.hostAppTokenVerifyUrl().trim();
        }
        throw new BusinessException(
                HttpStatus.SERVICE_UNAVAILABLE,
                ApiErrorCode.SERVICE_UNAVAILABLE.value(),
                "Host app token verification is not configured.");
    }

    private JsonNode exchangeProfile(String endpoint, String hostAccessToken) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(endpoint).build().toUri();
            JsonNode body = restTemplate().exchange(
                    RequestEntity.get(uri)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + hostAccessToken)
                            .header("access_token", hostAccessToken)
                            .build(),
                    JsonNode.class).getBody();
            if (body == null) {
                throw invalidToken();
            }
            if (body.has("success") && !body.path("success").asBoolean(true)) {
                throw invalidToken();
            }
            return body;
        } catch (BusinessException ex) {
            throw ex;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                throw invalidToken();
            }
            throw unavailable();
        } catch (RestClientException ex) {
            throw unavailable();
        }
    }

    private RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeout = authProperties.hostAppTimeoutMillis() > 0 ? authProperties.hostAppTimeoutMillis() : 3000;
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }

    private void validateAudience(JsonNode payload) {
        String expectedAudience = normalizeNullable(authProperties.hostAppAudience());
        if (!StringUtils.hasText(expectedAudience)) {
            return;
        }

        String audience = firstText(payload, "aud", "audience", "appId", "clientId");
        if (!expectedAudience.equals(audience)) {
            JsonNode data = payload.path("data");
            audience = firstText(data, "aud", "audience", "appId", "clientId");
        }
        if (!expectedAudience.equals(audience)) {
            throw invalidToken();
        }
    }

    private BusinessException invalidToken() {
        return new BusinessException(
                HttpStatus.UNAUTHORIZED,
                ApiErrorCode.AUTH_TOKEN_INVALID.value(),
                "Host app token is invalid.");
    }

    private BusinessException unavailable() {
        return new BusinessException(
                HttpStatus.SERVICE_UNAVAILABLE,
                ApiErrorCode.SERVICE_UNAVAILABLE.value(),
                "Host app token verification is unavailable.");
    }

    private JsonNode firstObject(JsonNode root, String... fields) {
        JsonNode current = root;
        for (String field : fields) {
            JsonNode candidate = current == null ? null : current.get(field);
            if (candidate != null && candidate.isObject()) {
                return candidate;
            }
        }

        List<String[]> nestedPaths = List.of(
                new String[] { "data", "user" },
                new String[] { "data", "userInfo" },
                new String[] { "data", "profile" },
                new String[] { "data", "identity" },
                new String[] { "payload", "user" },
                new String[] { "payload", "userInfo" },
                new String[] { "payload", "profile" },
                new String[] { "payload", "identity" },
                new String[] { "user", "profile" },
                new String[] { "user", "identity" });
        for (String[] path : nestedPaths) {
            JsonNode candidate = root;
            for (String segment : path) {
                candidate = candidate == null ? null : candidate.get(segment);
            }
            if (candidate != null && candidate.isObject()) {
                return candidate;
            }
        }
        return null;
    }

    private String firstText(JsonNode node, String... fields) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && !value.isNull()) {
                String text = value.asText(null);
                if (StringUtils.hasText(text)) {
                    return text.trim();
                }
            }
        }
        return null;
    }

    private String normalizeDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
