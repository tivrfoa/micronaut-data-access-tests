package io.micronaut.http.client.bind;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.MutableHttpRequest;
import java.lang.annotation.Annotation;

@BootstrapContextCompatible
@Indexed(AnnotatedClientRequestBinder.class)
public interface AnnotatedClientRequestBinder<A extends Annotation> extends ClientRequestBinder {
   void bind(@NonNull MethodInvocationContext<Object, Object> context, @NonNull ClientRequestUriContext uriContext, @NonNull MutableHttpRequest<?> request);

   @NonNull
   Class<A> getAnnotationType();
}
