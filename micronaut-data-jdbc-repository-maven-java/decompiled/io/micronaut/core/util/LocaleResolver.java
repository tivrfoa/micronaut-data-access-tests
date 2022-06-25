package io.micronaut.core.util;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.Ordered;
import java.util.Locale;
import java.util.Optional;

public interface LocaleResolver<T> extends Ordered {
   @NonNull
   Optional<Locale> resolve(@NonNull T context);

   @NonNull
   Locale resolveOrDefault(@NonNull T context);
}
