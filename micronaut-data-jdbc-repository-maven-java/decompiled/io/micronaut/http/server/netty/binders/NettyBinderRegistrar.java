package io.micronaut.http.server.netty.binders;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.netty.HttpContentProcessorResolver;
import io.micronaut.http.server.netty.multipart.MultipartBodyArgumentBinder;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Singleton
@Internal
class NettyBinderRegistrar implements BeanCreatedEventListener<RequestBinderRegistry> {
   private final ConversionService<?> conversionService;
   private final HttpContentProcessorResolver httpContentProcessorResolver;
   private final BeanLocator beanLocator;
   private final BeanProvider<HttpServerConfiguration> httpServerConfiguration;
   private final BeanProvider<ExecutorService> executorService;

   NettyBinderRegistrar(
      @Nullable ConversionService<?> conversionService,
      HttpContentProcessorResolver httpContentProcessorResolver,
      BeanLocator beanLocator,
      BeanProvider<HttpServerConfiguration> httpServerConfiguration,
      @Named("io") BeanProvider<ExecutorService> executorService
   ) {
      this.conversionService = conversionService == null ? ConversionService.SHARED : conversionService;
      this.httpContentProcessorResolver = httpContentProcessorResolver;
      this.beanLocator = beanLocator;
      this.httpServerConfiguration = httpServerConfiguration;
      this.executorService = executorService;
   }

   public RequestBinderRegistry onCreated(BeanCreatedEvent<RequestBinderRegistry> event) {
      RequestBinderRegistry registry = event.getBean();
      registry.addRequestArgumentBinder(new CompletableFutureBodyBinder(this.httpContentProcessorResolver, this.conversionService));
      registry.addRequestArgumentBinder(new MultipartBodyArgumentBinder(this.beanLocator, this.httpServerConfiguration));
      registry.addRequestArgumentBinder(new InputStreamBodyBinder(this.httpContentProcessorResolver, (ExecutorService)this.executorService.get()));
      return registry;
   }
}
