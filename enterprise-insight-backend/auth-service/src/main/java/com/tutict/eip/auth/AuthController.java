package com.tutict.eip.auth;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.security.JwtUtils;
import com.tutict.eip.common.security.RoleConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final String jwtSecret;
    private final long jwtTtlSeconds;

    public AuthController(
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${security.jwt.ttl-seconds:3600}") long jwtTtlSeconds
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtTtlSeconds = jwtTtlSeconds;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        List<String> roles = request.username().equalsIgnoreCase("admin")
                ? List.of(RoleConstants.ADMIN, RoleConstants.ANALYST)
                : List.of(RoleConstants.ANALYST);
        String subject = UUID.randomUUID().toString();
        String token = JwtUtils.generateToken(subject, request.username(), "demo-tenant", roles, jwtSecret, jwtTtlSeconds);
        Instant expiresAt = Instant.now().plusSeconds(jwtTtlSeconds);
        return ApiResponse.ok("login success", new LoginResponse(token, expiresAt, roles));
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfile> profile(@RequestParam(defaultValue = "demo") String username) {
        UserProfile profile = new UserProfile(username, List.of(RoleConstants.ADMIN, RoleConstants.ANALYST), "demo-tenant");
        return ApiResponse.ok(profile);
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String token, Instant expiresAt, List<String> roles) {
    }

    public record UserProfile(String username, List<String> roles, String tenant) {
    }
}
