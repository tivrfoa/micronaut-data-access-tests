package io.micronaut.json.bind;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import java.util.Optional;

@Internal
public interface JsonBeanPropertyBinderExceptionHandler {
   Optional<ConversionErrorException> toConversionError(@Nullable Object object, @NonNull Exception e);
}
