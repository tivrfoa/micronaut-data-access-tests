package io.micronaut.retry;

@FunctionalInterface
public interface RetryStateBuilder {
   RetryState build();
}
