package io.micronaut.logging;

import io.micronaut.core.annotation.Introspected;

@Introspected
public enum LogLevel {
   ALL,
   TRACE,
   DEBUG,
   INFO,
   WARN,
   ERROR,
   OFF,
   NOT_SPECIFIED;
}
