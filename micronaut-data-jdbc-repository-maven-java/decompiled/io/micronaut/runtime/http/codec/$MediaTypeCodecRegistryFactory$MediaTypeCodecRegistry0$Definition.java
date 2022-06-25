package io.micronaut.runtime.http.codec;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $MediaTypeCodecRegistryFactory$MediaTypeCodecRegistry0$Definition
   extends AbstractInitializableBeanDefinition<MediaTypeCodecRegistry>
   implements BeanFactory<MediaTypeCodecRegistry> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      MediaTypeCodecRegistryFactory.class,
      "mediaTypeCodecRegistry",
      new Argument[]{Argument.of(List.class, "codecs", null, Argument.ofTypeVariable(MediaTypeCodec.class, "E"))},
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Primary",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Primary",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "javax.inject.Qualifier",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton")
         ),
         false,
         true
      ),
      false
   );

   @Override
   public MediaTypeCodecRegistry build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, MediaTypeCodecRegistryFactory.class, null);
      var1.markDependentAsFactory();
      MediaTypeCodecRegistry var5 = ((MediaTypeCodecRegistryFactory)var4)
         .mediaTypeCodecRegistry(
            (List<MediaTypeCodec>)super.getBeansOfTypeForConstructorArgument(
               var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
            )
         );
      return (MediaTypeCodecRegistry)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      MediaTypeCodecRegistry var4 = (MediaTypeCodecRegistry)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $MediaTypeCodecRegistryFactory$MediaTypeCodecRegistry0$Definition() {
      this(MediaTypeCodecRegistry.class, $CONSTRUCTOR);
   }

   protected $MediaTypeCodecRegistryFactory$MediaTypeCodecRegistry0$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $MediaTypeCodecRegistryFactory$MediaTypeCodecRegistry0$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false,
         false
      );
   }
}
