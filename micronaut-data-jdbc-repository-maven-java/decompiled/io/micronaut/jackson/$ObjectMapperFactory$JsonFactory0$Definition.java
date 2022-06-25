package io.micronaut.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ObjectMapperFactory$JsonFactory0$Definition extends AbstractInitializableBeanDefinition<JsonFactory> implements BeanFactory<JsonFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;

   @Override
   public JsonFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, ObjectMapperFactory.class, null);
      var1.markDependentAsFactory();
      JsonFactory var5 = ((ObjectMapperFactory)var4).jsonFactory((JacksonConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (JsonFactory)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JsonFactory var4 = (JsonFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         ObjectMapperFactory.class,
         "jsonFactory",
         new Argument[]{Argument.of(JacksonConfiguration.class, "jacksonConfiguration")},
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                        var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                     )
                  }
               ),
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                        var0
                     )
                  }
               ),
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
            false,
            true
         ),
         false
      );
   }

   public $ObjectMapperFactory$JsonFactory0$Definition() {
      this(JsonFactory.class, $CONSTRUCTOR);
   }

   protected $ObjectMapperFactory$JsonFactory0$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ObjectMapperFactory$JsonFactory0$Definition$Reference.$ANNOTATION_METADATA,
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
