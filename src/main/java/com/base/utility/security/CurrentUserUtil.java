package com.base.utility.security;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserUtil {
    private final JwtTokenHelper jwtTokenHelper;

    public Optional<CurrentUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return jwtTokenHelper.getCurrentJwt()
                .map(jwt -> CurrentUser.builder()
                        .userId(jwt.getClaimAsString("sub"))
                        .username(jwt.getClaimAsString("preferred_username"))
                        .email(jwt.getClaimAsString("email"))
                        .firstName(jwt.getClaimAsString("given_name"))
                        .lastName(jwt.getClaimAsString("family_name"))
                        .roles(jwtTokenHelper.getCurrentUserRoles())
                        .authorities(authentication.getAuthorities().stream()
                                .map(authority -> authority.getAuthority())
                                .toList())
                        .build());
    }
    public String getCurrentUserId() {
        return getCurrentUser()
                .map(CurrentUser::userId)
                .orElse("system");
    }
    public String getCurrentUsername() {
        return getCurrentUser()
                .map(CurrentUser::username)
                .orElse("anonymous");
    }
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    @Builder
    @With
    public record CurrentUser(
            String userId,
            String username,
            String email,
            String firstName,
            String lastName,
            List<String> roles,
            List<String> authorities
    ) {
    }
}
