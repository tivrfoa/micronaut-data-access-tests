package io.micronaut.http.server.netty;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.inject.ExecutionHandle;
import io.micronaut.web.router.RouteMatch;
import io.micronaut.web.router.qualifier.ConsumesMediaTypeQualifier;
import jakarta.inject.Singleton;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Singleton
@Internal
class DefaultHttpContentProcessorResolver implements HttpContentProcessorResolver {
   private static final Set<Class> RAW_BODY_TYPES = CollectionUtils.setOf(String.class, byte[].class, ByteBuffer.class, InputStream.class);
   private final BeanLocator beanLocator;
   private final BeanProvider<NettyHttpServerConfiguration> serverConfiguration;
   private NettyHttpServerConfiguration nettyServerConfiguration;

   DefaultHttpContentProcessorResolver(BeanLocator beanLocator, BeanProvider<NettyHttpServerConfiguration> serverConfiguration) {
      this.beanLocator = beanLocator;
      this.serverConfiguration = serverConfiguration;
   }

   @NonNull
   @Override
   public HttpContentProcessor<?> resolve(@NonNull NettyHttpRequest<?> request, @NonNull RouteMatch<?> route) {
      Argument<?> bodyType = (Argument)route.getBodyArgument().filter(argument -> {
         AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
         if (annotationMetadata.hasAnnotation(Body.class)) {
            return !annotationMetadata.stringValue(Body.class).isPresent();
         } else {
            return false;
         }
      }).orElseGet(() -> {
         if (route instanceof ExecutionHandle) {
            for(Argument<?> argument : ((ExecutionHandle)route).getArguments()) {
               if (argument.getType() == HttpRequest.class) {
                  return argument;
               }
            }
         }

         return Argument.OBJECT_ARGUMENT;
      });
      return this.resolve(request, bodyType);
   }

   @NonNull
   @Override
   public HttpContentProcessor<?> resolve(@NonNull NettyHttpRequest<?> request, @NonNull Argument<?> bodyType) {
      if (bodyType.getType() == HttpRequest.class) {
         bodyType = (Argument)bodyType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
      }

      boolean isRaw = RAW_BODY_TYPES.contains(bodyType.getType());
      return this.resolve(request, isRaw);
   }

   @NonNull
   @Override
   public HttpContentProcessor<?> resolve(@NonNull NettyHttpRequest<?> request) {
      return this.resolve(request, false);
   }

   private HttpContentProcessor<?> resolve(NettyHttpRequest<?> request, boolean rawBodyType) {
      Supplier<DefaultHttpContentProcessor> defaultHttpContentProcessor = () -> new DefaultHttpContentProcessor(request, this.getServerConfiguration());
      if (rawBodyType) {
         return (HttpContentProcessor<?>)defaultHttpContentProcessor.get();
      } else {
         Optional<MediaType> contentType = request.getContentType();
         return (HttpContentProcessor<?>)contentType.flatMap(
               type -> this.beanLocator.findBean(HttpContentSubscriberFactory.class, new ConsumesMediaTypeQualifier(type))
            )
            .map(factory -> factory.build(request))
            .orElseGet(defaultHttpContentProcessor);
      }
   }

   private NettyHttpServerConfiguration getServerConfiguration() {
      NettyHttpServerConfiguration nettyHttpServerConfiguration = this.nettyServerConfiguration;
      if (nettyHttpServerConfiguration == null) {
         synchronized(this) {
            nettyHttpServerConfiguration = this.nettyServerConfiguration;
            if (nettyHttpServerConfiguration == null) {
               nettyHttpServerConfiguration = this.serverConfiguration.get();
               this.nettyServerConfiguration = nettyHttpServerConfiguration;
            }
         }
      }

      return nettyHttpServerConfiguration;
   }
}
