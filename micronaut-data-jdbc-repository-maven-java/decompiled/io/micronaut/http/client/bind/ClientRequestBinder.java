package io.micronaut.http.client.bind;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.Indexed;

@BootstrapContextCompatible
@Indexed(ClientRequestBinder.class)
public interface ClientRequestBinder {
}
