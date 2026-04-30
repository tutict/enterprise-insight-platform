package com.tutict.eip.gateway.security;

import com.tutict.eip.common.security.JwtClaims;
import com.tutict.eip.common.security.JwtUtils;
import com.tutict.eip.common.security.RoleConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/actuator/health",
            "/actuator/info"
    );

    private static final Map<String, List<String>> ROLE_RULES = Map.of(
            "/api/metadata/templates", List.of(RoleConstants.ANALYST),
            "/api/metadata/agents", List.of(RoleConstants.ANALYST),
            "/api/prompt-compiler", List.of(RoleConstants.ANALYST),
            "/api/agent-adapter", List.of(RoleConstants.ANALYST),
            "/api/harness", List.of(RoleConstants.ANALYST),
            "/api/ai", List.of(RoleConstants.ANALYST)
    );

    private final String jwtSecret;

    public JwtAuthFilter(@Value("${security.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring("Bearer ".length());
        JwtClaims claims;
        try {
            claims = JwtUtils.parseToken(token, jwtSecret);
        } catch (Exception ex) {
            return unauthorized(exchange);
        }

        if (!isAllowed(path, claims.roles())) {
            return forbidden(exchange);
        }

        String rolesHeader = claims.roles() == null ? "" : String.join(",", claims.roles());
        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder
                        .header("X-User-Id", claims.subject())
                        .header("X-User-Name", claims.username() == null ? "" : claims.username())
                        .header("X-User-Roles", rolesHeader)
                        .header("X-Tenant", claims.tenant() == null ? "" : claims.tenant())
                )
                .build();
        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isAllowed(String path, List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, List<String>> entry : ROLE_RULES.entrySet()) {
            String rulePath = entry.getKey();
            if (path.startsWith(rulePath)) {
                return roles.stream().anyMatch(entry.getValue()::contains);
            }
        }
        return true;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
