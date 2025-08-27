package com.base.utility.database;

import com.base.utility.security.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
@ConditionalOnClass(name = "jakarta.persistence.Entity")
@RequiredArgsConstructor
public class AuditingConfig {
    private final CurrentUserUtil currentUserUtil;

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                return Optional.of(currentUserUtil.getCurrentUsername());
            } catch (Exception e) {
                return Optional.of("system");
            }
        };
    }
}
