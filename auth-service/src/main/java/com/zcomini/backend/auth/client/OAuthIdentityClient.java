package com.zcomini.backend.auth.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.zcomini.backend.auth.config.AuthProperties;
import com.zcomini.backend.shared.exception.BusinessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OAuthIdentityClient {

    private static final String DEFAULT_GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";
    private static final String DEFAULT_ZALO_TOKEN_URL = "https://oauth.zaloapp.com/v4/access_token";
    private static final String DEFAULT_ZALO_PROFILE_URL = "https://graph.zalo.me/v2.0/me";
    private static final String DEFAULT_ZALO_PHONE_URL = "https://graph.zalo.me/v2.0/me/info";

    private final RestTemplate restTemplate = new RestTemplate();
    private final AuthProperties authProperties;

    public OAuthIdentityClient(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public GoogleIdentity verifyGoogleIdToken(String idToken) {
        URI uri = UriComponentsBuilder
                .fromUriString(resolve(authProperties.googleTokenInfoUrl(), DEFAULT_GOOGLE_TOKEN_INFO_URL))
                .queryParam("id_token", idToken)
                .build()
                .toUri();

        JsonNode payload = exchangeJson(RequestEntity.get(uri).build(), "Google token is invalid");
        String subject = text(payload, "sub");
        String email = normalizeEmail(text(payload, "email"));
        boolean verifiedEmail = booleanValue(payload, "email_verified");
        String audience = text(payload, "aud");

        if (!StringUtils.hasText(subject)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Google token subject is missing");
        }
        if (!StringUtils.hasText(email) || !verifiedEmail) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Google account must provide a verified email");
        }
        validateGoogleAudience(audience);

        return new GoogleIdentity(
                subject,
                email,
                text(payload, "name"),
                text(payload, "picture"),
                verifiedEmail,
                payload
        );
    }

    public ZaloIdentity verifyZaloAccessToken(String accessToken, String phoneToken) {
        String resolvedAccessToken = accessToken;
        if (!StringUtils.hasText(resolvedAccessToken)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Zalo access token is required");
        }
        URI profileUri = UriComponentsBuilder
                .fromUriString(resolve(authProperties.zaloProfileUrl(), DEFAULT_ZALO_PROFILE_URL))
                .queryParam("fields", "id,name,picture")
                .build()
                .toUri();
        JsonNode profile = exchangeJson(zaloGet(profileUri, resolvedAccessToken), "Zalo token is invalid");
        String providerUserId = text(profile, "id");
        if (!StringUtils.hasText(providerUserId)) {
            providerUserId = text(profile.path("data"), "id");
        }
        if (!StringUtils.hasText(providerUserId)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Zalo user id is missing");
        }

        String phone = null;
        JsonNode phonePayload = null;
        if (StringUtils.hasText(phoneToken)) {
            URI phoneUri = UriComponentsBuilder
                    .fromUriString(resolve(authProperties.zaloPhoneUrl(), DEFAULT_ZALO_PHONE_URL))
                    .build()
                    .toUri();
            phonePayload = exchangeJson(zaloPost(phoneUri, resolvedAccessToken, phoneToken), "Zalo phone permission is invalid");
            phone = normalizePhone(firstText(phonePayload, "user_phone", "phone", "number"));
            if (!StringUtils.hasText(phone)) {
                phone = normalizePhone(firstText(phonePayload.path("data"), "user_phone", "phone", "number"));
            }
        }

        return new ZaloIdentity(
                providerUserId,
                phone,
                firstText(profile, "name", "display_name"),
                coalesceText(
                        firstText(profile, "avatar"),
                        firstText(profile.path("picture").path("data"), "url"),
                        firstText(profile.path("data").path("picture").path("data"), "url")
                ),
                StringUtils.hasText(phone),
                phonePayload == null ? profile : phonePayload
        );
    }

    public ZaloIdentity verifyZaloAuthorizationCode(String authCode,
                                                   String codeVerifier,
                                                   String redirectUri,
                                                   String phoneToken) {
        String accessToken = exchangeZaloAuthorizationCode(authCode, codeVerifier, redirectUri);
        return verifyZaloAccessToken(accessToken, phoneToken);
    }

    private String exchangeZaloAuthorizationCode(String authCode, String codeVerifier, String redirectUri) {
        if (!StringUtils.hasText(authCode) || !StringUtils.hasText(codeVerifier)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Zalo authorization code and code verifier are required");
        }
        if (!StringUtils.hasText(authProperties.zaloAppSecret())) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Zalo app secret is not configured");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", authCode);
        form.add("grant_type", "authorization_code");
        form.add("code_verifier", codeVerifier);
        if (StringUtils.hasText(authProperties.zaloAppId())) {
            form.add("app_id", authProperties.zaloAppId().trim());
        }
        if (StringUtils.hasText(redirectUri)) {
            form.add("redirect_uri", redirectUri.trim());
        }

        JsonNode payload = exchangeJson(
                RequestEntity.post(URI.create(resolve(authProperties.zaloTokenUrl(), DEFAULT_ZALO_TOKEN_URL)))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("secret_key", authProperties.zaloAppSecret().trim())
                        .body(form),
                "Zalo authorization code is invalid"
        );
        String accessToken = firstText(payload, "access_token");
        if (!StringUtils.hasText(accessToken)) {
            accessToken = firstText(payload.path("data"), "access_token");
        }
        if (!StringUtils.hasText(accessToken)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Zalo access token is missing");
        }
        return accessToken;
    }

    private RequestEntity<Void> zaloGet(URI uri, String accessToken) {
        return RequestEntity.get(uri)
                .header("access_token", accessToken)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
    }

    private RequestEntity<Void> zaloPost(URI uri, String accessToken, String phoneToken) {
        return RequestEntity.post(uri)
                .header("access_token", accessToken)
                .header("code", phoneToken)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
    }

    private JsonNode exchangeJson(RequestEntity<?> request, String message) {
        try {
            JsonNode body = restTemplate.exchange(request, JsonNode.class).getBody();
            if (body == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, message);
            }
            return body;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, message);
        }
    }

    private void validateGoogleAudience(String audience) {
        Set<String> allowedClientIds = Arrays.stream(resolve(authProperties.googleClientIds(), "").split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (allowedClientIds.isEmpty()) {
            return;
        }
        if (!allowedClientIds.contains(audience)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Google token audience is not allowed");
        }
    }

    private String resolve(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText(null);
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String coalesceText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean booleanValue(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || value.isNull()) {
            return false;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        return "true".equalsIgnoreCase(value.asText());
    }

    private String normalizeEmail(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : null;
    }

    private String normalizePhone(String value) {
        return StringUtils.hasText(value) ? value.replaceAll("\\D", "") : null;
    }

    public record GoogleIdentity(
            String providerUserId,
            String email,
            String fullName,
            String avatarUrl,
            boolean verifiedEmail,
            JsonNode rawPayload
    ) {
    }

    public record ZaloIdentity(
            String providerUserId,
            String phone,
            String fullName,
            String avatarUrl,
            boolean verifiedPhone,
            JsonNode rawPayload
    ) {
    }
}
