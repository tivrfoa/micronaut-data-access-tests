package io.micronaut.http.client.bind;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import java.util.Optional;

@BootstrapContextCompatible
public interface HttpClientBinderRegistry {
   <T> Optional<ClientArgumentRequestBinder<?>> findArgumentBinder(@NonNull Argument<T> argument);

   Optional<AnnotatedClientRequestBinder<?>> findAnnotatedBinder(@NonNull Class<?> annotationType);
}
