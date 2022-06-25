package io.micronaut.context;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.ExecutableMethodsDefinition;
import io.micronaut.inject.annotation.AbstractEnvironmentAnnotationMetadata;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Internal
public abstract class AbstractExecutableMethodsDefinition<T> implements ExecutableMethodsDefinition<T>, EnvironmentConfigurable {
   private final AbstractExecutableMethodsDefinition.MethodReference[] methodsReferences;
   private final AbstractExecutableMethodsDefinition.DispatchedExecutableMethod<T, ?>[] executableMethods;
   private Environment environment;
   private List<AbstractExecutableMethodsDefinition.DispatchedExecutableMethod<T, ?>> executableMethodsList;
   private Method[] reflectiveMethods;

   protected AbstractExecutableMethodsDefinition(AbstractExecutableMethodsDefinition.MethodReference[] methodsReferences) {
      this.methodsReferences = methodsReferences;
      this.executableMethods = new AbstractExecutableMethodsDefinition.DispatchedExecutableMethod[methodsReferences.length];
   }

   @Override
   public void configure(Environment environment) {
      this.environment = environment;

      for(AbstractExecutableMethodsDefinition.DispatchedExecutableMethod<T, ?> executableMethod : this.executableMethods) {
         if (executableMethod != null) {
            executableMethod.configure(environment);
         }
      }

   }

   @Override
   public Collection<ExecutableMethod<T, ?>> getExecutableMethods() {
      if (this.executableMethodsList == null) {
         int i = 0;

         for(int methodsReferencesLength = this.methodsReferences.length; i < methodsReferencesLength; ++i) {
            this.getExecutableMethodByIndex(i);
         }

         this.executableMethodsList = Arrays.asList(this.executableMethods);
      }

      return this.executableMethodsList;
   }

   @Override
   public <R> Optional<ExecutableMethod<T, R>> findMethod(String name, Class<?>... argumentTypes) {
      return Optional.ofNullable(this.getMethod(name, argumentTypes));
   }

   @Override
   public <R> Stream<ExecutableMethod<T, R>> findPossibleMethods(String name) {
      return IntStream.range(0, this.methodsReferences.length)
         .filter(i -> this.methodsReferences[i].methodName.equals(name))
         .mapToObj(this::getExecutableMethodByIndex);
   }

   public <R> ExecutableMethod<T, R> getExecutableMethodByIndex(int index) {
      AbstractExecutableMethodsDefinition.DispatchedExecutableMethod<T, R> executableMethod = this.executableMethods[index];
      if (executableMethod == null) {
         AbstractExecutableMethodsDefinition.MethodReference methodsReference = this.methodsReferences[index];
         executableMethod = new AbstractExecutableMethodsDefinition.DispatchedExecutableMethod<>(
            this, index, methodsReference, methodsReference.annotationMetadata
         );
         if (this.environment != null) {
            executableMethod.configure(this.environment);
         }

         this.executableMethods[index] = executableMethod;
      }

      return executableMethod;
   }

   @Nullable
   protected <R> ExecutableMethod<T, R> getMethod(String name, Class<?>... argumentTypes) {
      for(int i = 0; i < this.methodsReferences.length; ++i) {
         AbstractExecutableMethodsDefinition.MethodReference methodReference = this.methodsReferences[i];
         if (methodReference.methodName.equals(name)
            && methodReference.arguments.length == argumentTypes.length
            && this.argumentsTypesMatch(argumentTypes, methodReference.arguments)) {
            return this.getExecutableMethodByIndex(i);
         }
      }

      return null;
   }

   protected Object dispatch(int index, T target, Object[] args) {
      throw this.unknownDispatchAtIndexException(index);
   }

   protected abstract Method getTargetMethodByIndex(int index);

   protected final Method getAccessibleTargetMethodByIndex(int index) {
      if (this.reflectiveMethods == null) {
         this.reflectiveMethods = new Method[this.methodsReferences.length];
      }

      Method method = this.reflectiveMethods[index];
      if (method == null) {
         method = this.getTargetMethodByIndex(index);
         if (ClassUtils.REFLECTION_LOGGER.isDebugEnabled()) {
            ClassUtils.REFLECTION_LOGGER.debug("Reflectively accessing method {} of type {}", method, method.getDeclaringClass());
         }

         method.setAccessible(true);
      }

      return method;
   }

   protected final Throwable unknownMethodAtIndexException(int index) {
      return new IllegalStateException("Unknown method at index: " + index);
   }

   protected final RuntimeException unknownDispatchAtIndexException(int index) {
      return new IllegalStateException("Unknown dispatch at index: " + index);
   }

   protected final boolean methodAtIndexMatches(int index, String name, Class[] argumentTypes) {
      AbstractExecutableMethodsDefinition.MethodReference methodReference = this.methodsReferences[index];
      Argument<?>[] arguments = methodReference.arguments;
      return arguments.length == argumentTypes.length && methodReference.methodName.equals(name) ? this.argumentsTypesMatch(argumentTypes, arguments) : false;
   }

   private boolean argumentsTypesMatch(Class[] argumentTypes, Argument<?>[] arguments) {
      for(int i = 0; i < arguments.length; ++i) {
         if (!argumentTypes[i].equals(arguments[i].getType())) {
            return false;
         }
      }

      return true;
   }

   private static final class DispatchedExecutableMethod<T, R> implements ExecutableMethod<T, R>, ReturnType<R>, EnvironmentConfigurable {
      private final AbstractExecutableMethodsDefinition dispatcher;
      private final int index;
      private final AbstractExecutableMethodsDefinition.MethodReference methodReference;
      private AnnotationMetadata annotationMetadata;

      private DispatchedExecutableMethod(
         AbstractExecutableMethodsDefinition dispatcher,
         int index,
         AbstractExecutableMethodsDefinition.MethodReference methodReference,
         AnnotationMetadata annotationMetadata
      ) {
         this.dispatcher = dispatcher;
         this.index = index;
         this.methodReference = methodReference;
         this.annotationMetadata = annotationMetadata;
      }

      @Override
      public void configure(Environment environment) {
         if (this.annotationMetadata.hasPropertyExpressions()) {
            this.annotationMetadata = new AbstractExecutableMethodsDefinition.MethodAnnotationMetadata(this.annotationMetadata, environment);
         }

      }

      @Override
      public boolean hasPropertyExpressions() {
         return this.annotationMetadata.hasPropertyExpressions();
      }

      @Override
      public boolean isAbstract() {
         return this.methodReference.isAbstract;
      }

      @Override
      public boolean isSuspend() {
         return this.methodReference.isSuspend;
      }

      @Override
      public Class<T> getDeclaringType() {
         return this.methodReference.declaringType;
      }

      @Override
      public String getMethodName() {
         return this.methodReference.methodName;
      }

      @Override
      public Argument<?>[] getArguments() {
         return this.methodReference.arguments;
      }

      @Override
      public Method getTargetMethod() {
         return this.dispatcher.getTargetMethodByIndex(this.index);
      }

      @Override
      public ReturnType<R> getReturnType() {
         return this;
      }

      @Override
      public Class<R> getType() {
         return this.methodReference.returnArgument == null ? Void.TYPE : this.methodReference.returnArgument.getType();
      }

      @Override
      public boolean isSuspended() {
         return this.methodReference.isSuspend;
      }

      @NonNull
      @Override
      public AnnotationMetadata getAnnotationMetadata() {
         return this.annotationMetadata;
      }

      @Override
      public Argument[] getTypeParameters() {
         return this.methodReference.returnArgument != null ? this.methodReference.returnArgument.getTypeParameters() : Argument.ZERO_ARGUMENTS;
      }

      @Override
      public Map<String, Argument<?>> getTypeVariables() {
         return this.methodReference.returnArgument != null ? this.methodReference.returnArgument.getTypeVariables() : Collections.emptyMap();
      }

      @NonNull
      @Override
      public Argument asArgument() {
         Map<String, Argument<?>> typeVariables = this.getTypeVariables();
         Collection<Argument<?>> values = typeVariables.values();
         AnnotationMetadata annotationMetadata = this.getAnnotationMetadata();
         return Argument.of(this.getType(), annotationMetadata, (Argument<?>[])values.toArray(Argument.ZERO_ARGUMENTS));
      }

      @Override
      public R invoke(T instance, Object... arguments) {
         ArgumentUtils.validateArguments(this, this.methodReference.arguments, arguments);
         return (R)this.dispatcher.dispatch(this.index, instance, arguments);
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof AbstractExecutableMethodsDefinition.DispatchedExecutableMethod)) {
            return false;
         } else {
            AbstractExecutableMethodsDefinition.DispatchedExecutableMethod that = (AbstractExecutableMethodsDefinition.DispatchedExecutableMethod)o;
            return Objects.equals(this.methodReference.declaringType, that.methodReference.declaringType)
               && Objects.equals(this.methodReference.methodName, that.methodReference.methodName)
               && Arrays.equals(this.methodReference.arguments, that.methodReference.arguments);
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.methodReference.declaringType, this.methodReference.methodName, Arrays.hashCode(this.methodReference.arguments)});
      }

      public String toString() {
         String text = Argument.toString(this.getArguments());
         return this.getReturnType().getType().getSimpleName() + " " + this.getMethodName() + "(" + text + ")";
      }
   }

   private static final class MethodAnnotationMetadata extends AbstractEnvironmentAnnotationMetadata {
      private final Environment environment;

      MethodAnnotationMetadata(AnnotationMetadata targetMetadata, Environment environment) {
         super(targetMetadata);
         this.environment = environment;
      }

      @Nullable
      @Override
      protected Environment getEnvironment() {
         return this.environment;
      }
   }

   @Internal
   public static final class MethodReference {
      final AnnotationMetadata annotationMetadata;
      final Class<?> declaringType;
      final String methodName;
      @Nullable
      final Argument<?> returnArgument;
      final Argument<?>[] arguments;
      final boolean isAbstract;
      final boolean isSuspend;

      public MethodReference(
         Class<?> declaringType,
         AnnotationMetadata annotationMetadata,
         String methodName,
         Argument<?> returnArgument,
         Argument<?>[] arguments,
         boolean isAbstract,
         boolean isSuspend
      ) {
         this.declaringType = declaringType;
         this.annotationMetadata = annotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : annotationMetadata;
         this.methodName = methodName;
         this.returnArgument = returnArgument;
         this.arguments = arguments == null ? Argument.ZERO_ARGUMENTS : arguments;
         this.isAbstract = isAbstract;
         this.isSuspend = isSuspend;
      }
   }
}
