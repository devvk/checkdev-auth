package ru.checkdev.auth.service;

import lombok.extern.slf4j.Slf4j;

/**
 * Циклический прерыватель - прерывает вызовы после превышения порога ошибок.
 */
@Slf4j
public class CircuitBreaker {

    private final int failureThreshold;
    private int failureCount;
    private State state = State.CLOSED;

    public CircuitBreaker(int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    private enum State {
        OPEN,
        CLOSED
    }

    @FunctionalInterface
    public interface Action<T> {
        T execute() throws Exception;
    }

    public <R> R execute(Action<R> action, R defaultValue) {
        if (state == State.CLOSED) {
            try {
                return action.execute();
            } catch (Exception e) {
                failureCount++;
                log.error("Attempt failed, failure count: {}", failureCount, e);
                if (failureCount >= failureThreshold) {
                    state = State.OPEN;
                    log.warn("Circuit Breaker OPENED due to failure threshold exceeded");
                }
                return defaultValue;
            }
        }
        log.warn("Circuit Breaker is OPEN. Skipping request.");
        throw new CircuitBreakerOpenException("Circuit Breaker is OPEN. Request skipped.");
    }

    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
