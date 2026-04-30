package com.tutict.eip.ai.security;

import com.tutict.eip.common.ErrorCodes;
import com.tutict.eip.common.security.AccessDeniedException;
import com.tutict.eip.common.security.RequireRoles;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleGuardInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRoles requireRoles = handlerMethod.getMethodAnnotation(RequireRoles.class);
        if (requireRoles == null) {
            requireRoles = handlerMethod.getBeanType().getAnnotation(RequireRoles.class);
        }
        if (requireRoles == null || requireRoles.value().length == 0) {
            return true;
        }

        String rolesHeader = request.getHeader("X-User-Roles");
        if (rolesHeader == null || rolesHeader.isBlank()) {
            throw new AccessDeniedException("Roles header missing", ErrorCodes.FORBIDDEN);
        }

        Set<String> userRoles = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .collect(Collectors.toSet());

        boolean allowed = Arrays.stream(requireRoles.value()).anyMatch(userRoles::contains);
        if (!allowed) {
            throw new AccessDeniedException("Insufficient roles", ErrorCodes.FORBIDDEN);
        }
        return true;
    }
}
