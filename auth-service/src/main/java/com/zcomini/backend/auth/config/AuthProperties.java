package com.zcomini.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String jwtSecret,
        String jwtIssuer,
        long accessTokenMinutes,
        long refreshTokenDays,
        boolean exposeDevSecrets,
        String seedUserEmail,
        String seedUserPassword,
        String internalServiceKey,
        String googleClientIds,
        String googleTokenInfoUrl,
        String zaloAppId,
        String zaloAppSecret,
        String zaloTokenUrl,
        String zaloProfileUrl,
        String zaloPhoneUrl,
        String hostAppProfileUrl,
        String hostAppTokenVerifyUrl,
        String hostAppAudience,
        int hostAppTimeoutMillis
) {
}
