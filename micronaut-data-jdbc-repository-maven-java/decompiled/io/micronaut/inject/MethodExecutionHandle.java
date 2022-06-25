package io.micronaut.inject;

import io.micronaut.core.annotation.NonNull;

public interface MethodExecutionHandle<T, R> extends ExecutionHandle<T, R>, MethodReference {
   @NonNull
   ExecutableMethod<?, R> getExecutableMethod();
}
