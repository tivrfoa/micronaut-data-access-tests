package io.micronaut.core.type;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public interface ReturnType<T> extends TypeInformation<T>, AnnotationMetadataProvider, ArgumentCoercible<T> {
   @NonNull
   @Override
   default Argument<T> asArgument() {
      Collection<Argument<?>> values = this.getTypeVariables().values();
      return Argument.of(this.getType(), (Argument<?>[])values.toArray(Argument.ZERO_ARGUMENTS));
   }

   default boolean isSuspended() {
      return false;
   }

   default boolean isSingleResult() {
      if (this.isSpecifiedSingle()) {
         return true;
      } else if (this.isReactive()) {
         Class<T> returnType = this.getType();
         return RuntimeTypeInformation.isSingle(returnType);
      } else {
         return true;
      }
   }

   static <T1> ReturnType<T1> of(Class<T1> type, Argument<?>... typeArguments) {
      final Map<String, Argument<?>> argumentMap = new LinkedHashMap(typeArguments.length);

      for(Argument<?> argument : typeArguments) {
         argumentMap.put(argument.getName(), argument);
      }

      return new ReturnType<T1>() {
         @Override
         public Class<T1> getType() {
            return type;
         }

         @Override
         public Argument[] getTypeParameters() {
            return typeArguments;
         }

         @Override
         public Map<String, Argument<?>> getTypeVariables() {
            return argumentMap;
         }
      };
   }
}
