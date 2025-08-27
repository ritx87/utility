package com.base.utility.security;

import com.base.utility.exception.type.BusinessException;
import com.base.utility.exception.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleCheckUtil {
    private final JwtTokenHelper jwtTokenHelper;
    private final CurrentUserUtil currentUserUtil;

    public boolean hasRole(String role) {
        return jwtTokenHelper.hasRole(role);
    }

    public boolean hasAnyRole(String... roles) {
        return jwtTokenHelper.hasAnyRole(roles);
    }

    public boolean hasAllRoles(String... roles) {
        var userRoles = jwtTokenHelper.getCurrentUserRoles();
        return Arrays.stream(roles).allMatch(userRoles::contains);
    }

    public void requireRole(String role) {
        if (!hasRole(role)) {
            throw new BusinessException(ErrorCode.FORBIDDEN,
                    "Access denied. Required role: " + role);
        }
    }

    public void requireAnyRole(String... roles) {
        if (!hasAnyRole(roles)) {
            throw new BusinessException(ErrorCode.FORBIDDEN,
                    "Access denied. Required roles: " + String.join(", ", roles));
        }
    }

    public void requireAllRoles(String... roles) {
        if (!hasAllRoles(roles)) {
            throw new BusinessException(ErrorCode.FORBIDDEN,
                    "Access denied. Required all roles: " + String.join(", ", roles));
        }
    }

    public void requireAuthentication() {
        if (!currentUserUtil.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }

    public boolean isAdmin() {
        return hasRole("admin") || hasRole("ADMIN");
    }

    public boolean isUser() {
        return hasRole("user") || hasRole("USER");
    }

    public void requireAdmin() {
        requireRole("admin");
    }

    public boolean canAccessResource(String resourceOwner) {
        if (isAdmin()) {
            return true;
        }

        String currentUserId = currentUserUtil.getCurrentUserId();
        return currentUserId.equals(resourceOwner);
    }

    public void requireResourceAccess(String resourceOwner) {
        if (!canAccessResource(resourceOwner)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to this resource");
        }
    }
}
