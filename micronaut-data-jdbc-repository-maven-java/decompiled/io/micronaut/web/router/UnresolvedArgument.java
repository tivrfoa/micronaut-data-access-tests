package io.micronaut.web.router;

import io.micronaut.core.bind.ArgumentBinder;
import java.util.function.Supplier;

@FunctionalInterface
public interface UnresolvedArgument<T> extends Supplier<ArgumentBinder.BindingResult<T>> {
}
