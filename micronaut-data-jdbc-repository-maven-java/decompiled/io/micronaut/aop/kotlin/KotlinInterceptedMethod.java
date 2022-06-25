package io.micronaut.aop.kotlin;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.core.annotation.NonNull;
import kotlin.coroutines.CoroutineContext;

public interface KotlinInterceptedMethod extends InterceptedMethod {
   @NonNull
   CoroutineContext getCoroutineContext();

   void updateCoroutineContext(@NonNull CoroutineContext coroutineContext);
}
