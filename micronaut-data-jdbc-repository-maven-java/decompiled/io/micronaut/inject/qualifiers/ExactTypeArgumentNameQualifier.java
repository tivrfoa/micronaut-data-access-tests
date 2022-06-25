package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;

final class ExactTypeArgumentNameQualifier<T> implements Qualifier<T> {
   private static final Logger LOG = ClassUtils.getLogger(TypeArgumentQualifier.class);
   private final String typeName;

   ExactTypeArgumentNameQualifier(String typeName) {
      this.typeName = (String)Objects.requireNonNull(typeName, "Type name cannot be null");
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates.filter(candidate -> beanType.isAssignableFrom(candidate.getBeanType()))
         .filter(
            candidate -> {
               List<Class<?>> typeArguments = this.getTypeArguments(beanType, (BT)candidate);
               boolean result = this.areTypesCompatible(typeArguments);
               if (LOG.isTraceEnabled() && !result) {
                  LOG.trace(
                     "Bean type {} is not compatible with candidate generic types [{}] of candidate {}",
                     beanType,
                     CollectionUtils.toString(typeArguments),
                     candidate
                  );
               }
      
               return result;
            }
         );
   }

   private boolean areTypesCompatible(List<Class<?>> typeArguments) {
      if (typeArguments.isEmpty()) {
         return true;
      } else {
         if (typeArguments.size() == 1) {
            for(Class<?> typeArgument : typeArguments) {
               if (this.typeName.equals(typeArgument.getTypeName())) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private <BT extends BeanType<T>> List<Class<?>> getTypeArguments(Class<T> beanType, BT candidate) {
      if (candidate instanceof BeanDefinition) {
         BeanDefinition<BT> definition = (BeanDefinition)candidate;
         return (List<Class<?>>)definition.getTypeArguments(beanType).stream().map(TypeInformation::getType).collect(Collectors.toList());
      } else {
         return beanType.isInterface()
            ? Arrays.asList(GenericTypeUtils.resolveInterfaceTypeArguments(candidate.getBeanType(), beanType))
            : Arrays.asList(GenericTypeUtils.resolveSuperTypeGenericArguments(candidate.getBeanType(), beanType));
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ExactTypeArgumentNameQualifier<?> that = (ExactTypeArgumentNameQualifier)o;
         return this.generify(this.typeName).equals(this.generify(that.typeName));
      } else {
         return false;
      }
   }

   private String generify(String typeName) {
      return "<" + typeName + ">";
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.generify(this.typeName)});
   }

   public String toString() {
      return this.generify(this.typeName);
   }
}
