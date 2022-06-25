package io.micronaut.retry;

public enum CircuitState {
   OPEN,
   CLOSED,
   HALF_OPEN;
}
