package io.micronaut.context;

import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodExecutionHandle;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ExecutionHandleLocator {
   ExecutionHandleLocator EMPTY = new ExecutionHandleLocator() {
   };

   default <T, R> Optional<MethodExecutionHandle<T, R>> findExecutionHandle(Class<T> beanType, String method, Class... arguments) {
      return Optional.empty();
   }

   default <T, R> Optional<MethodExecutionHandle<T, R>> findExecutionHandle(Class<T> beanType, Qualifier<?> qualifier, String method, Class... arguments) {
      return Optional.empty();
   }

   default <T, R> Optional<MethodExecutionHandle<T, R>> findExecutionHandle(T bean, String method, Class... arguments) {
      return Optional.empty();
   }

   default <T, R> Optional<ExecutableMethod<T, R>> findExecutableMethod(Class<T> beanType, String method, Class... arguments) {
      return Optional.empty();
   }

   default <T, R> Optional<ExecutableMethod<T, R>> findProxyTargetMethod(Class<T> beanType, String method, Class... arguments) {
      return Optional.empty();
   }

   default <T, R> Optional<ExecutableMethod<T, R>> findProxyTargetMethod(Class<T> beanType, Qualifier<T> qualifier, String method, Class... arguments) {
      return Optional.empty();
   }

   default <T, R> Optional<ExecutableMethod<T, R>> findProxyTargetMethod(Argument<T> beanType, Qualifier<T> qualifier, String method, Class... arguments) {
      return Optional.empty();
   }

   default <T, R> ExecutableMethod<T, R> getExecutableMethod(Class<T> beanType, String method, Class... arguments) throws NoSuchMethodException {
      Optional<ExecutableMethod<T, R>> executableMethod = this.findExecutableMethod(beanType, method, arguments);
      return (ExecutableMethod<T, R>)executableMethod.orElseThrow(
         () -> new NoSuchMethodException(
               "No such method ["
                  + method
                  + "("
                  + (String)Arrays.stream(arguments).map(Class::getName).collect(Collectors.joining(","))
                  + ") ] for bean ["
                  + beanType.getName()
                  + "]"
            )
      );
   }

   default <T, R> ExecutableMethod<T, R> getProxyTargetMethod(Class<T> beanType, String method, Class... arguments) throws NoSuchMethodException {
      Optional<ExecutableMethod<T, R>> executableMethod = this.findProxyTargetMethod(beanType, method, arguments);
      return (ExecutableMethod<T, R>)executableMethod.orElseThrow(
         () -> new NoSuchMethodException(
               "No such method ["
                  + method
                  + "("
                  + (String)Arrays.stream(arguments).map(Class::getName).collect(Collectors.joining(","))
                  + ") ] for bean ["
                  + beanType.getName()
                  + "]"
            )
      );
   }

   default <T, R> ExecutableMethod<T, R> getProxyTargetMethod(Class<T> beanType, Qualifier<T> qualifier, String method, Class... arguments) throws NoSuchMethodException {
      Optional<ExecutableMethod<T, R>> executableMethod = this.findProxyTargetMethod(beanType, qualifier, method, arguments);
      return (ExecutableMethod<T, R>)executableMethod.orElseThrow(
         () -> new NoSuchMethodException(
               "No such method ["
                  + method
                  + "("
                  + (String)Arrays.stream(arguments).map(Class::getName).collect(Collectors.joining(","))
                  + ") ] for bean ["
                  + beanType.getName()
                  + "]"
            )
      );
   }

   default <T, R> ExecutableMethod<T, R> getProxyTargetMethod(Argument<T> beanType, Qualifier<T> qualifier, String method, Class... arguments) throws NoSuchMethodException {
      Optional<ExecutableMethod<T, R>> executableMethod = this.findProxyTargetMethod(beanType, qualifier, method, arguments);
      return (ExecutableMethod<T, R>)executableMethod.orElseThrow(
         () -> new NoSuchMethodException(
               "No such method ["
                  + method
                  + "("
                  + (String)Arrays.stream(arguments).map(Class::getName).collect(Collectors.joining(","))
                  + ") ] for bean ["
                  + beanType.getName()
                  + "]"
            )
      );
   }

   default <T, R> MethodExecutionHandle<T, R> getExecutionHandle(Class<T> beanType, String method, Class... arguments) throws NoSuchMethodException {
      return (MethodExecutionHandle<T, R>)this.findExecutionHandle(beanType, method, arguments)
         .orElseThrow(
            () -> new NoSuchMethodException(
                  "No such method ["
                     + method
                     + "("
                     + (String)Arrays.stream(arguments).map(Class::getName).collect(Collectors.joining(","))
                     + ") ] for bean ["
                     + beanType.getName()
                     + "]"
               )
         );
   }

   default <T, R> MethodExecutionHandle<T, R> getExecutionHandle(T bean, String method, Class... arguments) throws NoSuchMethodException {
      return (MethodExecutionHandle<T, R>)this.findExecutionHandle(bean, method, arguments)
         .orElseThrow(
            () -> new NoSuchMethodException(
                  "No such method ["
                     + method
                     + "("
                     + (String)Arrays.stream(arguments).map(Class::getName).collect(Collectors.joining(","))
                     + ") ] for bean ["
                     + bean
                     + "]"
               )
         );
   }

   default MethodExecutionHandle<?, Object> createExecutionHandle(BeanDefinition<?> beanDefinition, ExecutableMethod<Object, ?> method) {
      throw new UnsupportedOperationException(
         "No such method ["
            + method
            + "("
            + (String)Arrays.stream(method.getArgumentTypes()).map(Class::getName).collect(Collectors.joining(","))
            + ") ] for bean ["
            + beanDefinition.getBeanType()
            + "]"
      );
   }
}
