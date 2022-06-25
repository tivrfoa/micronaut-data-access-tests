package io.micronaut.http.server.util;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.runtime.server.EmbeddedServer;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultHttpHostResolver$Definition extends AbstractInitializableBeanDefinition<DefaultHttpHostResolver> implements BeanFactory<DefaultHttpHostResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultHttpHostResolver.class,
      "<init>",
      new Argument[]{
         Argument.of(HttpServerConfiguration.class, "serverConfiguration"),
         Argument.of(
            BeanProvider.class,
            "embeddedServer",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            Argument.ofTypeVariable(EmbeddedServer.class, "T")
         )
      },
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );

   @Override
   public DefaultHttpHostResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultHttpHostResolver var4 = new DefaultHttpHostResolver(
         (HttpServerConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (BeanProvider<EmbeddedServer>)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (DefaultHttpHostResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultHttpHostResolver var4 = (DefaultHttpHostResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultHttpHostResolver$Definition() {
      this(DefaultHttpHostResolver.class, $CONSTRUCTOR);
   }

   protected $DefaultHttpHostResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultHttpHostResolver$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
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
