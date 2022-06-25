package io.micronaut.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;
import io.micronaut.core.util.ArrayUtils;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

@Internal
abstract class AbstractExecutable implements Executable {
   protected final Class declaringType;
   protected final String methodName;
   protected final Class[] argTypes;
   private Argument[] arguments;
   private Method method;

   AbstractExecutable(Class declaringType, String methodName, Argument[] arguments) {
      Objects.requireNonNull(declaringType, "Declaring type cannot be null");
      Objects.requireNonNull(methodName, "Method name cannot be null");
      this.argTypes = Argument.toClassArray(arguments);
      this.declaringType = declaringType;
      this.methodName = methodName;
      if (ArrayUtils.isNotEmpty(arguments)) {
         this.arguments = arguments;
      } else {
         this.arguments = Argument.ZERO_ARGUMENTS;
      }

   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof AbstractExecutable)) {
         return false;
      } else {
         AbstractExecutable that = (AbstractExecutable)o;
         return Objects.equals(this.declaringType, that.declaringType)
            && Objects.equals(this.methodName, that.methodName)
            && Arrays.equals(this.argTypes, that.argTypes);
      }
   }

   public int hashCode() {
      int result = Objects.hash(new Object[]{this.declaringType, this.methodName});
      return 31 * result + Arrays.hashCode(this.argTypes);
   }

   @Override
   public Argument<?>[] getArguments() {
      return this.arguments;
   }

   public final Method getTargetMethod() {
      if (this.method == null) {
         Method resolvedMethod = this.resolveTargetMethod();
         resolvedMethod.setAccessible(true);
         this.method = resolvedMethod;
      }

      return this.method;
   }

   @NonNull
   protected Method resolveTargetMethod() {
      return ReflectionUtils.getRequiredMethod(this.declaringType, this.methodName, this.argTypes);
   }
}
