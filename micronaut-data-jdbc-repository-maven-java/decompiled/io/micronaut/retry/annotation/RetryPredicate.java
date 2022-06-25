package io.micronaut.retry.annotation;

import io.micronaut.core.annotation.Introspected;
import java.util.function.Predicate;

@Introspected
@FunctionalInterface
public interface RetryPredicate extends Predicate<Throwable> {
}
