package io.micronaut.http.server.netty.ssl;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.type.Argument;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.ssl.ServerSslConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.netty.handler.ssl.SslContext;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $SelfSignedSslBuilder$Definition extends AbstractInitializableBeanDefinition<SelfSignedSslBuilder> implements BeanFactory<SelfSignedSslBuilder> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      SelfSignedSslBuilder.class,
      "<init>",
      new Argument[]{
         Argument.of(HttpServerConfiguration.class, "serverConfiguration"),
         Argument.of(ServerSslConfiguration.class, "ssl"),
         Argument.of(ResourceResolver.class, "resourceResolver")
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf("io.micronaut.http.ssl.SslBuilder", new Argument[]{Argument.of(SslContext.class, "T")});

   @Override
   public SelfSignedSslBuilder build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      SelfSignedSslBuilder var4 = new SelfSignedSslBuilder(
         (HttpServerConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (ServerSslConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (ResourceResolver)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (SelfSignedSslBuilder)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      SelfSignedSslBuilder var4 = (SelfSignedSslBuilder)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $SelfSignedSslBuilder$Definition() {
      this(SelfSignedSslBuilder.class, $CONSTRUCTOR);
   }

   protected $SelfSignedSslBuilder$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $SelfSignedSslBuilder$Definition$Reference.$ANNOTATION_METADATA,
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
