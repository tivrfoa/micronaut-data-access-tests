package io.micronaut.aop;

import io.micronaut.core.annotation.AnnotationMetadataDelegate;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.attr.MutableAttributeHolder;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ArgumentValue;
import io.micronaut.core.type.Executable;
import io.micronaut.core.type.MutableArgumentValue;
import java.util.LinkedHashMap;
import java.util.Map;

public interface InvocationContext<T, R> extends Executable<T, R>, AnnotationMetadataDelegate, MutableAttributeHolder {
   @NonNull
   Map<String, MutableArgumentValue<?>> getParameters();

   @NonNull
   T getTarget();

   @Nullable
   R proceed() throws RuntimeException;

   @Nullable
   R proceed(Interceptor from) throws RuntimeException;

   @NonNull
   default InterceptorKind getKind() {
      return InterceptorKind.AROUND;
   }

   @Override
   default Class<T> getDeclaringType() {
      return this.getTarget().getClass();
   }

   default InvocationContext<T, R> setAttribute(@NonNull CharSequence name, Object value) {
      return (InvocationContext<T, R>)MutableAttributeHolder.super.setAttribute(name, value);
   }

   @NonNull
   default Object[] getParameterValues() {
      return this.getParameters().values().stream().map(ArgumentValue::getValue).toArray();
   }

   @NonNull
   default Map<String, Object> getParameterValueMap() {
      Argument<?>[] arguments = this.getArguments();
      Object[] parameterValues = this.getParameterValues();
      Map<String, Object> valueMap = new LinkedHashMap(arguments.length);

      for(int i = 0; i < parameterValues.length; ++i) {
         Object parameterValue = parameterValues[i];
         Argument arg = arguments[i];
         valueMap.put(arg.getName(), parameterValue);
      }

      return valueMap;
   }
}
