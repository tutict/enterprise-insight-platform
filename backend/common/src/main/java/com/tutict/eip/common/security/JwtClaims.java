package com.tutict.eip.common.security;

import java.time.Instant;
import java.util.List;

public record JwtClaims(
        String subject,
        String username,
        String tenant,
        List<String> roles,
        Instant expiresAt
) {}
