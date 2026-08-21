package com.zcomini.backend.auth.client;

public interface HostAppTokenVerifier {
    HostAppIdentity verify(String hostAccessToken);
}
