package io.micronaut.http.server.websocket;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.web.router.RouteBuilder;
import io.micronaut.websocket.annotation.ServerWebSocket;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ServerWebSocketProcessor$Definition
   extends AbstractInitializableBeanDefinition<ServerWebSocketProcessor>
   implements BeanFactory<ServerWebSocketProcessor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ServerWebSocketProcessor.class,
      "<init>",
      new Argument[]{
         Argument.of(ExecutionHandleLocator.class, "executionHandleLocator"),
         Argument.of(RouteBuilder.UriNamingStrategy.class, "uriNamingStrategy"),
         Argument.of(
            ConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               ConversionService.class, "Impl", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         )
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.processor.AnnotationProcessor",
      new Argument[]{
         Argument.of(ServerWebSocket.class, "A"),
         Argument.of(ExecutableMethod.class, "T", null, Argument.ofTypeVariable(Object.class, "T"), Argument.ofTypeVariable(Object.class, "R"))
      },
      "io.micronaut.context.processor.ExecutableMethodProcessor",
      new Argument[]{Argument.of(ServerWebSocket.class, "A")}
   );

   @Override
   public ServerWebSocketProcessor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ServerWebSocketProcessor var4 = new ServerWebSocketProcessor(
         (ExecutionHandleLocator)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (RouteBuilder.UriNamingStrategy)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (ConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (ServerWebSocketProcessor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ServerWebSocketProcessor var4 = (ServerWebSocketProcessor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ServerWebSocketProcessor$Definition() {
      this(ServerWebSocketProcessor.class, $CONSTRUCTOR);
   }

   protected $ServerWebSocketProcessor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ServerWebSocketProcessor$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.of("javax.inject.Singleton"),
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
