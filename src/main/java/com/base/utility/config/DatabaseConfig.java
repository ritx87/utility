package com.base.utility.config;

import com.base.utility.database.AuditingConfig;
import com.base.utility.database.BaseRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass(DataSource.class)
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.base"
        },
        repositoryBaseClass = BaseRepositoryImpl.class  // ✅ Use custom implementation
)
@Import({
        AuditingConfig.class
})
@RequiredArgsConstructor
public class DatabaseConfig {
}
