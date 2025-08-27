package com.base.utility.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class CustomMetrics {
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicInteger> gaugeValues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> longGaugeValues = new ConcurrentHashMap<>();

    // API Request Metrics
    public void incrementApiRequestCount(String endpoint, String method, String status) {
        Counter.builder("api.requests.total")
                .description("Total number of API requests")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample startApiRequestTimer(String endpoint, String method) {
        Timer timer = Timer.builder("api.request.duration")
                .description("API request duration")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .register(meterRegistry);
        return Timer.start(meterRegistry);
    }

    public void recordApiRequestDuration(Timer.Sample sample, String endpoint, String method) {
        sample.stop(Timer.builder("api.request.duration")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .register(meterRegistry));
    }

    // Database Metrics
    public void incrementDatabaseQueryCount(String operation, String table) {
        Counter.builder("database.queries.total")
                .description("Total number of database queries")
                .tag("operation", operation)
                .tag("table", table)
                .register(meterRegistry)
                .increment();
    }

    public void recordDatabaseQueryDuration(String operation, String table, Duration duration) {
        Timer.builder("database.query.duration")
                .description("Database query duration")
                .tag("operation", operation)
                .tag("table", table)
                .register(meterRegistry)
                .record(duration);
    }

    public void incrementDatabaseConnectionCount() {
        Counter.builder("database.connections.created")
                .description("Number of database connections created")
                .register(meterRegistry)
                .increment();
    }

    // External Service Metrics
    public void incrementExternalServiceCall(String service, String operation, String status) {
        Counter.builder("external.service.calls.total")
                .description("Total external service calls")
                .tag("service", service)
                .tag("operation", operation)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void recordExternalServiceDuration(String service, String operation, Duration duration) {
        Timer.builder("external.service.duration")
                .description("External service call duration")
                .tag("service", service)
                .tag("operation", operation)
                .register(meterRegistry)
                .record(duration);
    }

    // Business Metrics
    public void incrementBusinessEvent(String eventType, String category) {
        Counter.builder("business.events.total")
                .description("Total business events")
                .tag("event_type", eventType)
                .tag("category", category)
                .register(meterRegistry)
                .increment();
    }

    public void recordBusinessEventDuration(String eventType, Duration duration) {
        Timer.builder("business.event.processing.duration")
                .description("Business event processing duration")
                .tag("event_type", eventType)
                .register(meterRegistry)
                .record(duration);
    }

    // Cache Metrics
    public void incrementCacheHit(String cacheName) {
        Counter.builder("cache.requests.total")
                .description("Total cache requests")
                .tag("cache", cacheName)
                .tag("result", "hit")
                .register(meterRegistry)
                .increment();
    }

    public void incrementCacheMiss(String cacheName) {
        Counter.builder("cache.requests.total")
                .description("Total cache requests")
                .tag("cache", cacheName)
                .tag("result", "miss")
                .register(meterRegistry)
                .increment();
    }

    // Security Metrics
    public void incrementAuthenticationAttempt(String result) {
        Counter.builder("security.authentication.attempts")
                .description("Authentication attempts")
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void incrementAuthorizationFailure(String resource, String action) {
        Counter.builder("security.authorization.failures")
                .description("Authorization failures")
                .tag("resource", resource)
                .tag("action", action)
                .register(meterRegistry)
                .increment();
    }

    // Error Metrics
    public void incrementErrorCount(String errorType, String errorCode) {
        Counter.builder("errors.total")
                .description("Total errors")
                .tag("error_type", errorType)
                .tag("error_code", errorCode)
                .register(meterRegistry)
                .increment();
    }

    public void incrementValidationError(String field) {
        Counter.builder("validation.errors.total")
                .description("Total validation errors")
                .tag("field", field)
                .register(meterRegistry)
                .increment();
    }

    // Custom Gauge Metrics
    public void setGaugeValue(String name, String description, int value, String... tags) {
        AtomicInteger gaugeValue = gaugeValues.computeIfAbsent(name, k -> {
            AtomicInteger atomicInt = new AtomicInteger(0);
            Gauge.builder(name, atomicInt, AtomicInteger::get)
                    .description(description)
                    .tags(tags)
                    .register(meterRegistry);
            return atomicInt;
        });
        gaugeValue.set(value);
    }

    public void setLongGaugeValue(String name, String description, long value, String... tags) {
        AtomicLong gaugeValue = longGaugeValues.computeIfAbsent(name, k -> {
            AtomicLong atomicLong = new AtomicLong(0L);
            Gauge.builder(name, atomicLong, AtomicLong::get)
                    .description(description)
                    .tags(tags)
                    .register(meterRegistry);
            return atomicLong;
        });
        gaugeValue.set(value);
    }

    // Utility Methods
    public void incrementCounter(String name, String description, String... tags) {
        Counter.builder(name)
                .description(description)
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    public void incrementCounterBy(String name, String description, double amount, String... tags) {
        Counter.builder(name)
                .description(description)
                .tags(tags)
                .register(meterRegistry)
                .increment(amount);
    }

    public void recordTimer(String name, String description, Duration duration, String... tags) {
        Timer.builder(name)
                .description(description)
                .tags(tags)
                .register(meterRegistry)
                .record(duration);
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    // Resilience4j Integration Metrics
    public void incrementRetryAttempt(String name, String result) {
        Counter.builder("resilience4j.retry")
                .description("Retry attempts")
                .tag("name", name)
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void incrementCircuitBreakerEvent(String name, String event) {
        Counter.builder("resilience4j.circuitbreaker")
                .description("Circuit breaker events")
                .tag("name", name)
                .tag("event", event)
                .register(meterRegistry)
                .increment();
    }

    public void incrementBulkheadEvent(String name, String event) {
        Counter.builder("resilience4j.bulkhead")
                .description("Bulkhead events")
                .tag("name", name)
                .tag("event", event)
                .register(meterRegistry)
                .increment();
    }
}
