package com.base.utility.config;

import com.base.utility.logging.MdcTaskDecorator;
import com.base.utility.properties.BaseUtilityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
@ConditionalOnProperty(
        prefix = "base-utility.async",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {
    private final BaseUtilityProperties properties;

    @Override
    public Executor getAsyncExecutor() {
        var asyncProps = properties.async();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProps.coreSize());
        executor.setMaxPoolSize(asyncProps.maxSize());
        executor.setQueueCapacity(asyncProps.queueCapacity());
        executor.setThreadNamePrefix(asyncProps.threadNamePrefix());
        executor.setKeepAliveSeconds(asyncProps.keepAliveSeconds());

        // Add MDC context propagation
        executor.setTaskDecorator(new MdcTaskDecorator());

        executor.initialize();
        log.info("Async executor configured with MDC context propagation - Core: {}, Max: {}",
                asyncProps.coreSize(), asyncProps.maxSize());
        return executor;
    }
}
