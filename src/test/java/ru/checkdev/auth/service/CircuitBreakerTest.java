package ru.checkdev.auth.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class CircuitBreakerTest {

    @Test
    void whenActionIsSuccessfulThenReturnResult() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(2);

        String result = circuitBreaker.execute(() -> "success", "default");

        assertThat(result).isEqualTo("success");
    }

    @Test
    void whenFailuresReachThresholdThenOpenCircuitBreaker() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(2);
        String defaultValue = "default";

        var first = circuitBreaker.execute(() -> {
            throw new IllegalStateException("Service failure");
        }, defaultValue);
        var second = circuitBreaker.execute(() -> {
            throw new IllegalStateException("Service failure");
        }, defaultValue);

        assertThat(first).isEqualTo(defaultValue);
        assertThat(second).isEqualTo(defaultValue);
        assertThatThrownBy(() -> circuitBreaker.execute(() -> "success", "default"))
                .isExactlyInstanceOf(CircuitBreaker.CircuitBreakerOpenException.class)
                .hasMessage("Circuit Breaker is OPEN. Request skipped.");
    }

    @Test
    void whenCircuitBreakerIsOpenThenActionIsNotExecuted() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(1);
        AtomicInteger attempts = new AtomicInteger(0);

        circuitBreaker.execute(() -> {
            throw new IllegalStateException("Service failure");
        }, "default");

        assertThatThrownBy(() -> circuitBreaker.execute(() -> {
            attempts.incrementAndGet();
            return "success";
        }, "default"))
                .isInstanceOf(CircuitBreaker.CircuitBreakerOpenException.class);
        assertThat(attempts.get()).isZero();
    }
}