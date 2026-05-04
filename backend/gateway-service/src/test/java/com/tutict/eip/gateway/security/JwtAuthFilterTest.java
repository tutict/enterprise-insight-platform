package com.tutict.eip.gateway.security;

import com.tutict.eip.common.security.JwtUtils;
import com.tutict.eip.common.security.RoleConstants;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthFilterTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void rejectsProtectedApiWithoutBearerToken() {
        JwtAuthFilter filter = new JwtAuthFilter(SECRET);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        ServerWebExchange exchange = exchange("/api/compiler/compile", null);
        filter.filter(exchange, chain(exchangeRef -> chainCalled.set(true))).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(chainCalled).isFalse();
    }

    @Test
    void allowsAnalystToAccessGraphRuntimeAndPropagatesClaims() {
        JwtAuthFilter filter = new JwtAuthFilter(SECRET);
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        String token = JwtUtils.generateToken(
                "user-1",
                "analyst",
                "tenant-a",
                List.of(RoleConstants.ANALYST),
                SECRET,
                3600
        );

        ServerWebExchange exchange = exchange("/api/graph/run", token);
        filter.filter(exchange, chain(forwarded::set)).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
        assertThat(forwarded.get()).isNotNull();
        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("user-1");
        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-User-Roles")).isEqualTo(RoleConstants.ANALYST);
    }

    @Test
    void forbidsAuthenticatedUserWithoutRequiredRole() {
        JwtAuthFilter filter = new JwtAuthFilter(SECRET);
        String token = JwtUtils.generateToken("user-1", "viewer", "tenant-a", List.of("VIEWER"), SECRET, 3600);

        ServerWebExchange exchange = exchange("/api/graph/run", token);
        filter.filter(exchange, chain(ignored -> {
        })).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ServerWebExchange exchange(String path, String token) {
        MockServerHttpRequest.BaseBuilder<?> request = MockServerHttpRequest.get(path);
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return MockServerWebExchange.from(request);
    }

    private GatewayFilterChain chain(java.util.function.Consumer<ServerWebExchange> consumer) {
        return exchange -> {
            consumer.accept(exchange);
            return Mono.empty();
        };
    }
}
