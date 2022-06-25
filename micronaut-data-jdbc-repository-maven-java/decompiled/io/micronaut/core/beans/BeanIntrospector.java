package io.micronaut.core.beans;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.core.util.ArgumentUtils;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.concurrent.Immutable;

@Immutable
public interface BeanIntrospector {
   BeanIntrospector SHARED = new DefaultBeanIntrospector();

   static BeanIntrospector forClassLoader(ClassLoader classLoader) {
      return new DefaultBeanIntrospector(classLoader);
   }

   @NonNull
   Collection<BeanIntrospection<Object>> findIntrospections(@NonNull Predicate<? super BeanIntrospectionReference<?>> filter);

   @NonNull
   Collection<Class<?>> findIntrospectedTypes(@NonNull Predicate<? super BeanIntrospectionReference<?>> filter);

   @NonNull
   <T> Optional<BeanIntrospection<T>> findIntrospection(@NonNull Class<T> beanType);

   @NonNull
   default Collection<BeanIntrospection<Object>> findIntrospections(@NonNull Class<? extends Annotation> stereotype) {
      ArgumentUtils.requireNonNull("stereotype", stereotype);
      return this.findIntrospections((Predicate<? super BeanIntrospectionReference<?>>)(ref -> ref.getAnnotationMetadata().hasStereotype(stereotype)));
   }

   @NonNull
   default Collection<BeanIntrospection<Object>> findIntrospections(@NonNull Class<? extends Annotation> stereotype, @NonNull String... packageNames) {
      ArgumentUtils.requireNonNull("stereotype", stereotype);
      ArgumentUtils.requireNonNull("packageNames", packageNames);
      return this.findIntrospections(
         (Predicate<? super BeanIntrospectionReference<?>>)(ref -> ref.getAnnotationMetadata().hasStereotype(stereotype)
               && Arrays.stream(packageNames).anyMatch(s -> ref.getName().startsWith(s + ".")))
      );
   }

   @NonNull
   default <T> BeanIntrospection<T> getIntrospection(@NonNull Class<T> beanType) {
      return (BeanIntrospection<T>)this.findIntrospection(beanType)
         .orElseThrow(
            () -> new IntrospectionException(
                  "No bean introspection available for type [" + beanType + "]. Ensure the class is annotated with io.micronaut.core.annotation.Introspected"
               )
         );
   }
}
