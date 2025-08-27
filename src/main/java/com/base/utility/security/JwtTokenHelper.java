package com.base.utility.security;

import com.base.utility.exception.type.BusinessException;
import com.base.utility.exception.utils.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Slf4j
@Component
public class JwtTokenHelper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<Jwt> getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }
        return Optional.empty();
    }

    public Optional<String> getCurrentUserId() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("sub"));
    }

    public Optional<String> getCurrentUsername() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("preferred_username"));
    }

    public Optional<String> getCurrentUserEmail() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("email"));
    }

    public List<String> getCurrentUserRoles() {
        return getCurrentJwt()
                .map(jwt -> {
                    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                    if (realmAccess != null && realmAccess.containsKey("roles")) {
                        return (List<String>) realmAccess.get("roles");
                    }
                    return List.<String>of();
                })
                .orElse(List.of());
    }

    public Optional<String> getClaimAsString(String claimName) {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString(claimName));
    }

    public <T> Optional<T> getClaim(String claimName, Class<T> claimType) {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaim(claimName));
    }

    public boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }

    public boolean hasAnyRole(String... roles) {
        List<String> userRoles = getCurrentUserRoles();
        return List.of(roles).stream().anyMatch(userRoles::contains);
    }

    public boolean isTokenExpired() {
        return getCurrentJwt()
                .map(jwt -> jwt.getExpiresAt().isBefore(java.time.Instant.now()))
                .orElse(true);
    }

    public Map<String, Object> decodeTokenPayload(String token) {
        try {
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid JWT format");
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));

            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Failed to decode JWT token", e);
        }
    }

    public void validateToken() {
        if (!getCurrentJwt().isPresent()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "No JWT token found");
        }

        if (isTokenExpired()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "JWT token has expired");
        }
    }
}
