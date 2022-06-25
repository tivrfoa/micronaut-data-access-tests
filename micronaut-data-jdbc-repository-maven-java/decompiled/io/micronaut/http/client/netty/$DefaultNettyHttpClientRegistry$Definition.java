package io.micronaut.http.client.netty;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.LoadBalancerResolver;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.client.StreamingHttpClient;
import io.micronaut.http.client.netty.ssl.NettyClientSslBuilder;
import io.micronaut.http.client.sse.SseClient;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.filter.HttpClientFilterResolver;
import io.micronaut.http.netty.channel.EventLoopGroupFactory;
import io.micronaut.http.netty.channel.EventLoopGroupRegistry;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.json.JsonMapper;
import io.micronaut.scheduling.instrument.InvocationInstrumenterFactory;
import io.micronaut.websocket.WebSocketClient;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated
class $DefaultNettyHttpClientRegistry$Definition
   extends AbstractInitializableBeanDefinition<DefaultNettyHttpClientRegistry>
   implements BeanFactory<DefaultNettyHttpClientRegistry>,
   DisposableBeanDefinition<DefaultNettyHttpClientRegistry> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultNettyHttpClientRegistry.class,
      "<init>",
      new Argument[]{
         Argument.of(HttpClientConfiguration.class, "defaultHttpClientConfiguration"),
         Argument.of(HttpClientFilterResolver.class, "httpClientFilterResolver", null, Argument.ofTypeVariable(AnnotationMetadataProvider.class, "T")),
         Argument.of(LoadBalancerResolver.class, "loadBalancerResolver"),
         Argument.of(NettyClientSslBuilder.class, "nettyClientSslBuilder"),
         Argument.of(ThreadFactory.class, "threadFactory"),
         Argument.of(MediaTypeCodecRegistry.class, "codecRegistry"),
         Argument.of(EventLoopGroupRegistry.class, "eventLoopGroupRegistry"),
         Argument.of(EventLoopGroupFactory.class, "eventLoopGroupFactory"),
         Argument.of(BeanContext.class, "beanContext"),
         Argument.of(List.class, "invocationInstrumenterFactories", null, Argument.ofTypeVariable(InvocationInstrumenterFactory.class, "E")),
         Argument.of(JsonMapper.class, "jsonMapper")
      },
      null,
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultNettyHttpClientRegistry.class,
         "close",
         null,
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.BootstrapContextCompatible",
                  Collections.EMPTY_MAP,
                  "io.micronaut.context.annotation.Factory",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.BootstrapContextCompatible",
                  Collections.EMPTY_MAP,
                  "io.micronaut.context.annotation.Factory",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false,
         false,
         true
      )
   };
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.http.client.HttpClientRegistry",
      new Argument[]{Argument.of(HttpClient.class, "T")},
      "io.micronaut.http.client.ProxyHttpClientRegistry",
      new Argument[]{Argument.of(ProxyHttpClient.class, "P")},
      "io.micronaut.http.client.StreamingHttpClientRegistry",
      new Argument[]{Argument.of(StreamingHttpClient.class, "S")},
      "io.micronaut.http.client.sse.SseClientRegistry",
      new Argument[]{Argument.of(SseClient.class, "E")},
      "io.micronaut.websocket.WebSocketClientRegistry",
      new Argument[]{Argument.of(WebSocketClient.class, "W")}
   );

   @Override
   public DefaultNettyHttpClientRegistry build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultNettyHttpClientRegistry var4 = new DefaultNettyHttpClientRegistry(
         (HttpClientConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (HttpClientFilterResolver)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (LoadBalancerResolver)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (NettyClientSslBuilder)super.getBeanForConstructorArgument(var1, var2, 3, null),
         (ThreadFactory)super.getBeanForConstructorArgument(var1, var2, 4, null),
         (MediaTypeCodecRegistry)super.getBeanForConstructorArgument(var1, var2, 5, null),
         (EventLoopGroupRegistry)super.getBeanForConstructorArgument(var1, var2, 6, null),
         (EventLoopGroupFactory)super.getBeanForConstructorArgument(var1, var2, 7, null),
         var2,
         (List<InvocationInstrumenterFactory>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 9, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[9].getTypeParameters()[0], null
         ),
         (JsonMapper)super.getBeanForConstructorArgument(var1, var2, 10, null)
      );
      return (DefaultNettyHttpClientRegistry)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultNettyHttpClientRegistry var4 = (DefaultNettyHttpClientRegistry)var3;
      return super.injectBean(var1, var2, var3);
   }

   @Override
   public DefaultNettyHttpClientRegistry dispose(BeanResolutionContext var1, BeanContext var2, DefaultNettyHttpClientRegistry var3) {
      DefaultNettyHttpClientRegistry var4 = (DefaultNettyHttpClientRegistry)var3;
      super.preDestroy(var1, var2, var3);
      var4.close();
      return var4;
   }

   public $DefaultNettyHttpClientRegistry$Definition() {
      this(DefaultNettyHttpClientRegistry.class, $CONSTRUCTOR);
   }

   protected $DefaultNettyHttpClientRegistry$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultNettyHttpClientRegistry$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.empty(),
         false,
         false,
         false,
         true,
         false,
         false,
         false,
         false
      );
   }
}
