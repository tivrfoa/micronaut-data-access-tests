package io.micronaut.http.server.netty;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.discovery.metadata.ServiceInstanceMetadataContributor;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NettyEmbeddedServerInstance$Definition
   extends AbstractInitializableBeanDefinition<NettyEmbeddedServerInstance>
   implements BeanFactory<NettyEmbeddedServerInstance>,
   ParametrizedBeanFactory<NettyEmbeddedServerInstance> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyEmbeddedServerInstance.class,
      "<init>",
      new Argument[]{
         Argument.of(
            String.class,
            "id",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.bind.annotation.Bindable",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
               ),
               false,
               true
            ),
            null
         ),
         Argument.of(
            NettyHttpServer.class,
            "nettyHttpServer",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.bind.annotation.Bindable",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
               ),
               false,
               true
            ),
            null
         ),
         Argument.of(Environment.class, "environment"),
         Argument.of(BeanLocator.class, "beanLocator"),
         Argument.of(List.class, "metadataContributors", null, Argument.ofTypeVariable(ServiceInstanceMetadataContributor.class, "E"))
      },
      null,
      false
   );

   @Override
   public NettyEmbeddedServerInstance doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      NettyEmbeddedServerInstance var5 = new NettyEmbeddedServerInstance(
         (String)var4.get("id"),
         (NettyHttpServer)var4.get("nettyHttpServer"),
         (Environment)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (BeanLocator)super.getBeanForConstructorArgument(var1, var2, 3, null),
         (List<ServiceInstanceMetadataContributor>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 4, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[4].getTypeParameters()[0], null
         )
      );
      return (NettyEmbeddedServerInstance)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      NettyEmbeddedServerInstance var4 = (NettyEmbeddedServerInstance)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $NettyEmbeddedServerInstance$Definition() {
      this(NettyEmbeddedServerInstance.class, $CONSTRUCTOR);
   }

   protected $NettyEmbeddedServerInstance$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyEmbeddedServerInstance$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("io.micronaut.context.annotation.Prototype"),
         false,
         false,
         false,
         false,
         false,
         false,
         false,
         false
      );
   }
}
