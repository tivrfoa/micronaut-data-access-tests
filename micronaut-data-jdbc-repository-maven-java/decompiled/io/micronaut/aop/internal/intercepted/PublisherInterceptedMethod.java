package io.micronaut.aop.internal.intercepted;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import org.reactivestreams.Publisher;

@Internal
class PublisherInterceptedMethod implements InterceptedMethod {
   private static final boolean AVAILABLE = ClassUtils.isPresent(
      "io.micronaut.core.async.publisher.Publishers", PublisherInterceptedMethod.class.getClassLoader()
   );
   private final ConversionService<?> conversionService = ConversionService.SHARED;
   private final MethodInvocationContext<?, ?> context;
   private final Argument<?> returnTypeValue;

   PublisherInterceptedMethod(MethodInvocationContext<?, ?> context) {
      this.context = context;
      this.returnTypeValue = (Argument)context.getReturnType().asArgument().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
   }

   @Override
   public InterceptedMethod.ResultType resultType() {
      return InterceptedMethod.ResultType.PUBLISHER;
   }

   @Override
   public Argument<?> returnTypeValue() {
      return this.returnTypeValue;
   }

   @Override
   public Publisher<?> interceptResultAsPublisher() {
      return this.convertToPublisher(this.context.proceed());
   }

   @Override
   public Publisher<?> interceptResultAsPublisher(Interceptor<?, ?> from) {
      return this.convertToPublisher(this.context.proceed(from));
   }

   @Override
   public Publisher<?> interceptResultAsPublisher(ExecutorService executorService) {
      Objects.requireNonNull(executorService);
      Publisher<?> actual = this.interceptResultAsPublisher();
      return s -> executorService.submit(() -> actual.subscribe(s));
   }

   public Publisher<?> interceptResult() {
      return this.interceptResultAsPublisher();
   }

   public Publisher<?> interceptResult(Interceptor<?, ?> from) {
      return this.interceptResultAsPublisher(from);
   }

   @Override
   public Object handleResult(Object result) {
      if (result == null) {
         result = Publishers.empty();
      }

      return this.convertPublisherResult(this.context.getReturnType(), result);
   }

   @Override
   public <E extends Throwable> Object handleException(Exception exception) throws E {
      return this.convertPublisherResult(this.context.getReturnType(), Publishers.just(exception));
   }

   static boolean isConvertibleToPublisher(Class<?> reactiveType) {
      return AVAILABLE && Publishers.isConvertibleToPublisher(reactiveType);
   }

   private Object convertPublisherResult(ReturnType<?> returnType, Object result) {
      return returnType.getType().isInstance(result)
         ? result
         : this.conversionService
            .convert(result, returnType.asArgument())
            .orElseThrow(() -> new IllegalStateException("Cannot convert publisher result: " + result + " to '" + returnType.getType().getName() + "'"));
   }

   private Publisher<?> convertToPublisher(Object result) {
      if (result == null) {
         return Publishers.empty();
      } else {
         return result instanceof Publisher
            ? (Publisher)result
            : (Publisher)this.conversionService
               .convert(result, Publisher.class)
               .orElseThrow(() -> new IllegalStateException("Cannot convert reactive type " + result + " to 'org.reactivestreams.Publisher'"));
      }
   }
}
