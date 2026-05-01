package com.tutict.eip.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class JwtUtils {
    // JWT 工具：生成与解析 Token

    private JwtUtils() {
    }

    public static String generateToken(String subject, String username, String tenant, List<String> roles,
                                       String secret, long ttlSeconds) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttlSeconds);
        Map<String, Object> claims = Map.of(
                "username", username,
                "tenant", tenant,
                "roles", roles
        );
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .addClaims(claims)
                .signWith(resolveKey(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    public static JwtClaims parseToken(String token, String secret) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(resolveKey(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
        String subject = claims.getSubject();
        String username = claims.get("username", String.class);
        String tenant = claims.get("tenant", String.class);
        List<?> rawRoles = claims.get("roles", List.class);
        List<String> roles = rawRoles == null
                ? List.of()
                : rawRoles.stream().map(String::valueOf).toList();
        return new JwtClaims(subject, username, tenant, roles, claims.getExpiration().toInstant());
    }

    private static Key resolveKey(String secret) {
        // 支持 base64: 前缀或明文密钥
        byte[] secretBytes = secret.startsWith("base64:")
                ? Decoders.BASE64.decode(secret.substring("base64:".length()))
                : secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
