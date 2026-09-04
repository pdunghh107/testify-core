package com.zcomini.backend.shared.api.enums;

public enum ApiErrorCode {

    // [400]
    BAD_REQUEST(400, "400"),
    UNAUTHORIZED(401, "401"),
    PAYMENT_REQUIRED(402, "402"),
    FORBIDDEN(403, "403"),
    NOT_FOUND(404, "404"),
    METHOD_NOT_ALLOWED(405, "405"),
    NOT_ACCEPTABLE(406, "406"),
    PROXY_AUTHENTICATION_REQUIRED(407, "407"),
    REQUEST_TIMEOUT(408, "408"),
    CONFLICT(409, "409"),
    GONE(410, "410"),
    LENGTH_REQUIRED(411, "411"),
    PRECONDITION_FAILED(412, "412"),
    PAYLOAD_TOO_LARGE(413, "413"),
    URI_TOO_LONG(414, "414"),
    UNSUPPORTED_MEDIA_TYPE(415, "415"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "416"),
    EXPECTATION_FAILED(417, "417"),
    I_AM_A_TEAPOT(418, "418"),
    UNPROCESSABLE_CONTENT(422, "422"),
    LOCKED(423, "423"),
    FAILED_DEPENDENCY(424, "424"),
    TOO_EARLY(425, "425"),
    UPGRADE_REQUIRED(426, "426"),
    PRECONDITION_REQUIRED(428, "428"),
    TOO_MANY_REQUESTS(429, "429"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "431"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "451"),

    // [500]
    INTERNAL_SERVER_ERROR(500, "500"),
    NOT_IMPLEMENTED(501, "501"),
    BAD_GATEWAY(502, "502"),
    SERVICE_UNAVAILABLE(503, "503"),
    GATEWAY_TIMEOUT(504, "504"),
    HTTP_VERSION_NOT_SUPPORTED(505, "505"),
    VARIANT_ALSO_NEGOTIATES(506, "506"),
    INSUFFICIENT_STORAGE(507, "507"),
    LOOP_DETECTED(508, "508"),
    NOT_EXTENDED(510, "510"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "511"),

    // TODO: Remove
    // [CUSTOM]
    // [AUTH_SERVICE]
    AUTH_TOKEN_EXPIRED(401, "AUTH.TOKEN_EXPIRED"),
    AUTH_TOKEN_INVALID(401, "AUTH.TOKEN_INVALID"),
    AUTH_TOKEN_REVOKED(401, "AUTH.TOKEN_REVOKED"),
    AUTH_REFRESH_TOKEN_INVALID(401, "AUTH.REFRESH_TOKEN_INVALID"),
    AUTH_CREDENTIALS_INVALID(401, "AUTH.CREDENTIALS_INVALID"),
    AUTH_ACCOUNT_LOCKED(423, "AUTH.ACCOUNT_LOCKED");

    private final int status;
    private final String code;

    ApiErrorCode(int status, String code) {
        this.status = status;
        this.code = code;
    }

    public int status() {
        return status;
    }

    public String value() {
        return code;
    }

    public static String defaultForStatus(int status) {
        return switch (status) {
            case 400 -> BAD_REQUEST.value();
            case 401 -> UNAUTHORIZED.value();
            case 402 -> PAYMENT_REQUIRED.value();
            case 403 -> FORBIDDEN.value();
            case 404 -> NOT_FOUND.value();
            case 405 -> METHOD_NOT_ALLOWED.value();
            case 406 -> NOT_ACCEPTABLE.value();
            case 409 -> CONFLICT.value();
            case 415 -> UNSUPPORTED_MEDIA_TYPE.value();
            case 422 -> UNPROCESSABLE_CONTENT.value();
            case 429 -> TOO_MANY_REQUESTS.value();
            case 502 -> BAD_GATEWAY.value();
            case 503 -> SERVICE_UNAVAILABLE.value();
            default -> INTERNAL_SERVER_ERROR.value();
        };
    }
}
