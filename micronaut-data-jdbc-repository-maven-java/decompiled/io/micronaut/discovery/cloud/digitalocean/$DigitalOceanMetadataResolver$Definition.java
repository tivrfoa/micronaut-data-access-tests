package io.micronaut.discovery.cloud.digitalocean;

import com.fasterxml.jackson.core.JsonFactory;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.json.JsonMapper;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DigitalOceanMetadataResolver$Definition
   extends AbstractInitializableBeanDefinition<DigitalOceanMetadataResolver>
   implements BeanFactory<DigitalOceanMetadataResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DigitalOceanMetadataResolver.class,
      "<init>",
      new Argument[]{
         Argument.of(DigitalOceanMetadataConfiguration.class, "configuration"),
         Argument.of(JsonFactory.class, "jsonFactory"),
         Argument.of(JsonMapper.class, "mapper")
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
   public DigitalOceanMetadataResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DigitalOceanMetadataResolver var4 = new DigitalOceanMetadataResolver(
         (DigitalOceanMetadataConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (JsonFactory)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (JsonMapper)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (DigitalOceanMetadataResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DigitalOceanMetadataResolver var4 = (DigitalOceanMetadataResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DigitalOceanMetadataResolver$Definition() {
      this(DigitalOceanMetadataResolver.class, $CONSTRUCTOR);
   }

   protected $DigitalOceanMetadataResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DigitalOceanMetadataResolver$Definition$Reference.$ANNOTATION_METADATA,
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
