package com.zcomini.backend.auth.client;

import com.zcomini.backend.auth.client.dto.HostAppIdentity;

public interface HostAppTokenVerifier {
    HostAppIdentity verify(String hostAccessToken);
}
