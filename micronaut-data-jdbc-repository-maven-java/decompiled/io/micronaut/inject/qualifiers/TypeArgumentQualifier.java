package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;

@Internal
public class TypeArgumentQualifier<T> implements Qualifier<T> {
   private static final Logger LOG = ClassUtils.getLogger(TypeArgumentQualifier.class);
   private final Class[] typeArguments;

   TypeArgumentQualifier(Class... typeArguments) {
      this.typeArguments = typeArguments;
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates.filter(candidate -> beanType.isAssignableFrom(candidate.getBeanType()))
         .filter(
            candidate -> {
               List<Class> typeArguments = this.getTypeArguments(beanType, (BT)candidate);
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

   public Class[] getTypeArguments() {
      return this.typeArguments;
   }

   protected boolean areTypesCompatible(List<Class> classes) {
      Class[] typeArguments = this.typeArguments;
      return areTypesCompatible(typeArguments, classes);
   }

   protected <BT extends BeanType<T>> List<Class> getTypeArguments(Class<T> beanType, BT candidate) {
      if (candidate instanceof BeanDefinition) {
         BeanDefinition<BT> definition = (BeanDefinition)candidate;
         return (List<Class>)definition.getTypeArguments(beanType).stream().map(TypeInformation::getType).collect(Collectors.toList());
      } else {
         return beanType.isInterface()
            ? Arrays.asList(GenericTypeUtils.resolveInterfaceTypeArguments(candidate.getBeanType(), beanType))
            : Arrays.asList(GenericTypeUtils.resolveSuperTypeGenericArguments(candidate.getBeanType(), beanType));
      }
   }

   public static boolean areTypesCompatible(Class[] typeArguments, List<Class> classes) {
      if (classes.isEmpty()) {
         return true;
      } else if (classes.size() != typeArguments.length) {
         return false;
      } else {
         for(int i = 0; i < classes.size(); ++i) {
            Class left = (Class)classes.get(i);
            Class right = typeArguments[i];
            if (right != Object.class && left != right && !left.isAssignableFrom(right)) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TypeArgumentQualifier<?> that = (TypeArgumentQualifier)o;
         return Arrays.equals(this.typeArguments, that.typeArguments);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.typeArguments);
   }

   public String toString() {
      return "<" + (String)Arrays.stream(this.typeArguments).map(Class::getSimpleName).collect(Collectors.joining(",")) + ">";
   }
}
