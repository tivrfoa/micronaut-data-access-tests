package io.micronaut.web.router;

import io.micronaut.inject.MethodExecutionHandle;

public interface MethodBasedRoute extends Route {
   MethodExecutionHandle getTargetMethod();
}
