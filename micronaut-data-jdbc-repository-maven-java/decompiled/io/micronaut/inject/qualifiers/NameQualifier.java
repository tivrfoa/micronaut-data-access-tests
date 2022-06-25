package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.naming.NameResolver;
import io.micronaut.core.naming.Named;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanType;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Internal
class NameQualifier<T> implements Qualifier<T>, Named {
   protected final Class<? extends Annotation> annotationType;
   private final String name;

   NameQualifier(AnnotationMetadata annotationMetadata, String name) {
      this.annotationType = annotationMetadata != null ? (Class)annotationMetadata.getAnnotationType(name).orElse(null) : null;
      this.name = (String)Objects.requireNonNull(this.annotationType == null ? name : this.annotationType.getSimpleName(), "Argument [name] cannot be null");
   }

   NameQualifier(Class<? extends Annotation> annotationType) {
      this.name = (String)Objects.requireNonNull(annotationType.getSimpleName(), "Argument [name] cannot be null");
      this.annotationType = annotationType;
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      ArgumentUtils.check("beanType", (T)beanType).notNull();
      ArgumentUtils.check("candidates", (T)candidates).notNull();
      return candidates.filter(candidate -> {
         if (!QualifierUtils.matchType(beanType, candidate)) {
            return false;
         } else if (QualifierUtils.matchAny(beanType, candidate)) {
            return true;
         } else {
            AnnotationMetadata annotationMetadata = candidate.getAnnotationMetadata();
            String thisName = (String)annotationMetadata.findDeclaredAnnotation("javax.inject.Named").flatMap(AnnotationValue::stringValue).orElse(null);
            if (thisName == null && candidate instanceof BeanDefinition) {
               Qualifier<?> qualifier = ((BeanDefinition)candidate).getDeclaredQualifier();
               if (qualifier != null && qualifier.contains(this)) {
                  return true;
               }
            }

            if (thisName == null) {
               if (candidate instanceof NameResolver) {
                  Optional<String> resolvedName = ((NameResolver)candidate).resolveName();
                  thisName = (String)resolvedName.orElse(candidate.getBeanType().getSimpleName());
               } else {
                  thisName = candidate.getBeanType().getSimpleName();
               }
            }

            return thisName.equalsIgnoreCase(this.name) || thisName.equalsIgnoreCase(this.name + beanType.getSimpleName());
         }
      });
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && NameQualifier.class.isAssignableFrom(o.getClass())) {
         NameQualifier<?> that = (NameQualifier)o;
         return this.name.equals(that.name);
      } else {
         return false;
      }
   }

   public String toString() {
      return "@Named('" + this.name + "')";
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   @Override
   public String getName() {
      return this.name;
   }
}
