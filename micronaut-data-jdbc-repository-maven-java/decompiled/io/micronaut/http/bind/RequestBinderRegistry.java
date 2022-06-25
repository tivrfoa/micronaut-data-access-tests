package io.micronaut.http.bind;

import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.http.HttpRequest;

public interface RequestBinderRegistry extends ArgumentBinderRegistry<HttpRequest<?>> {
}
