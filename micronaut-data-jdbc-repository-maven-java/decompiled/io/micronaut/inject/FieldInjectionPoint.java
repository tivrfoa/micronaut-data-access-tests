package io.micronaut.inject;

import io.micronaut.core.annotation.AnnotatedElement;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.type.ArgumentCoercible;
import java.lang.reflect.Field;

public interface FieldInjectionPoint<B, T> extends InjectionPoint<B>, AnnotationMetadataProvider, AnnotatedElement, ArgumentCoercible<T> {
   @Override
   String getName();

   Field getField();

   Class<T> getType();

   void set(T instance, Object object);
}
