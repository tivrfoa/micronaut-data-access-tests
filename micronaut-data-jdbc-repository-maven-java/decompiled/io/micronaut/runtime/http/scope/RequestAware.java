package io.micronaut.runtime.http.scope;

import io.micronaut.http.HttpRequest;

public interface RequestAware {
   void setRequest(HttpRequest<?> request);
}
