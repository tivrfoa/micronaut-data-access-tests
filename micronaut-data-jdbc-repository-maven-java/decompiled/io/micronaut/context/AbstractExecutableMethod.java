package io.micronaut.context;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.annotation.AbstractEnvironmentAnnotationMetadata;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Internal
public abstract class AbstractExecutableMethod extends AbstractExecutable implements ExecutableMethod, EnvironmentConfigurable {
   private final ReturnType returnType;
   private final Argument<?> genericReturnType;
   private final int hashCode;
   private Environment environment;
   private AnnotationMetadata methodAnnotationMetadata;

   protected AbstractExecutableMethod(Class<?> declaringType, String methodName, Argument genericReturnType, Argument... arguments) {
      super(declaringType, methodName, arguments);
      this.genericReturnType = genericReturnType;
      this.returnType = new AbstractExecutableMethod.ReturnTypeImpl();
      int result = Objects.hash(new Object[]{declaringType, methodName});
      result = 31 * result + Arrays.hashCode(this.argTypes);
      this.hashCode = result;
   }

   protected AbstractExecutableMethod(Class<?> declaringType, String methodName, Argument genericReturnType) {
      this(declaringType, methodName, genericReturnType, Argument.ZERO_ARGUMENTS);
   }

   protected AbstractExecutableMethod(Class<?> declaringType, String methodName) {
      this(declaringType, methodName, Argument.OBJECT_ARGUMENT, Argument.ZERO_ARGUMENTS);
   }

   @Override
   public boolean hasPropertyExpressions() {
      return this.getAnnotationMetadata().hasPropertyExpressions();
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      if (this.methodAnnotationMetadata == null) {
         this.methodAnnotationMetadata = this.initializeAnnotationMetadata();
      }

      return this.methodAnnotationMetadata;
   }

   @Override
   public void configure(Environment environment) {
      this.environment = environment;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AbstractExecutableMethod that = (AbstractExecutableMethod)o;
         return Objects.equals(this.declaringType, that.declaringType)
            && Objects.equals(this.methodName, that.methodName)
            && Arrays.equals(this.argTypes, that.argTypes);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   public String toString() {
      String text = Argument.toString(this.getArguments());
      return this.getReturnType().getType().getSimpleName() + " " + this.getMethodName() + "(" + text + ")";
   }

   @Override
   public ReturnType getReturnType() {
      return this.returnType;
   }

   @Override
   public Class[] getArgumentTypes() {
      return this.argTypes;
   }

   @Override
   public Class getDeclaringType() {
      return this.declaringType;
   }

   @Override
   public String getMethodName() {
      return this.methodName;
   }

   @Override
   public final Object invoke(Object instance, Object... arguments) {
      ArgumentUtils.validateArguments(this, this.getArguments(), arguments);
      return this.invokeInternal(instance, arguments);
   }

   protected abstract Object invokeInternal(Object instance, Object[] arguments);

   protected AnnotationMetadata resolveAnnotationMetadata() {
      return AnnotationMetadata.EMPTY_METADATA;
   }

   private AnnotationMetadata initializeAnnotationMetadata() {
      AnnotationMetadata annotationMetadata = this.resolveAnnotationMetadata();
      if (annotationMetadata != AnnotationMetadata.EMPTY_METADATA) {
         return (AnnotationMetadata)(annotationMetadata.hasPropertyExpressions()
            ? new AbstractExecutableMethod.MethodAnnotationMetadata(annotationMetadata)
            : annotationMetadata);
      } else {
         return AnnotationMetadata.EMPTY_METADATA;
      }
   }

   private final class MethodAnnotationMetadata extends AbstractEnvironmentAnnotationMetadata {
      MethodAnnotationMetadata(AnnotationMetadata targetMetadata) {
         super(targetMetadata);
      }

      @Nullable
      @Override
      protected Environment getEnvironment() {
         return AbstractExecutableMethod.this.environment;
      }
   }

   class ReturnTypeImpl implements ReturnType {
      @Override
      public Class<?> getType() {
         return AbstractExecutableMethod.this.genericReturnType != null ? AbstractExecutableMethod.this.genericReturnType.getType() : Void.TYPE;
      }

      @Override
      public boolean isSuspended() {
         return AbstractExecutableMethod.this.isSuspend();
      }

      @NonNull
      @Override
      public AnnotationMetadata getAnnotationMetadata() {
         return AbstractExecutableMethod.this.getAnnotationMetadata();
      }

      @Override
      public Argument[] getTypeParameters() {
         return AbstractExecutableMethod.this.genericReturnType != null
            ? AbstractExecutableMethod.this.genericReturnType.getTypeParameters()
            : Argument.ZERO_ARGUMENTS;
      }

      @Override
      public Map<String, Argument<?>> getTypeVariables() {
         return AbstractExecutableMethod.this.genericReturnType != null
            ? AbstractExecutableMethod.this.genericReturnType.getTypeVariables()
            : Collections.emptyMap();
      }

      @NonNull
      @Override
      public Argument asArgument() {
         Map<String, Argument<?>> typeVariables = this.getTypeVariables();
         Collection<Argument<?>> values = typeVariables.values();
         AnnotationMetadata annotationMetadata = this.getAnnotationMetadata();
         return Argument.of(this.getType(), annotationMetadata, (Argument<?>[])values.toArray(Argument.ZERO_ARGUMENTS));
      }
   }
}
