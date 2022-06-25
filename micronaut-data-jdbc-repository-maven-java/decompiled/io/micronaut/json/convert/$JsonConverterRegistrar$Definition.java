package io.micronaut.json.convert;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.bind.BeanPropertyBinder;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.json.JsonMapper;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $JsonConverterRegistrar$Definition extends AbstractInitializableBeanDefinition<JsonConverterRegistrar> implements BeanFactory<JsonConverterRegistrar> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JsonConverterRegistrar.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanProvider.class, "objectCodec", null, Argument.ofTypeVariable(JsonMapper.class, "T")),
         Argument.of(
            ConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               ConversionService.class, "Impl", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         ),
         Argument.of(BeanProvider.class, "beanPropertyBinder", null, Argument.ofTypeVariable(BeanPropertyBinder.class, "T"))
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
   public JsonConverterRegistrar build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JsonConverterRegistrar var4 = new JsonConverterRegistrar(
         (BeanProvider<JsonMapper>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (ConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (BeanProvider<BeanPropertyBinder>)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (JsonConverterRegistrar)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JsonConverterRegistrar var4 = (JsonConverterRegistrar)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JsonConverterRegistrar$Definition() {
      this(JsonConverterRegistrar.class, $CONSTRUCTOR);
   }

   protected $JsonConverterRegistrar$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JsonConverterRegistrar$Definition$Reference.$ANNOTATION_METADATA,
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
