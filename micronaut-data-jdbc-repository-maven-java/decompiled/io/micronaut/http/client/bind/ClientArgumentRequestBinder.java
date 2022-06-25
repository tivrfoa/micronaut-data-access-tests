package io.micronaut.http.client.bind;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.http.MutableHttpRequest;

@BootstrapContextCompatible
@Indexed(ClientArgumentRequestBinder.class)
public interface ClientArgumentRequestBinder<T> extends ClientRequestBinder {
   void bind(
      @NonNull ArgumentConversionContext<T> context, @NonNull ClientRequestUriContext uriContext, @NonNull T value, @NonNull MutableHttpRequest<?> request
   );
}
