package io.micronaut.jackson.databind.convert;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $JacksonConverterRegistrar$Definition
   extends AbstractInitializableBeanDefinition<JacksonConverterRegistrar>
   implements BeanFactory<JacksonConverterRegistrar> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JacksonConverterRegistrar.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanProvider.class, "objectMapper", null, Argument.ofTypeVariable(ObjectMapper.class, "T")),
         Argument.of(
            ConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               ConversionService.class, "Impl", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
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
   public JacksonConverterRegistrar build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JacksonConverterRegistrar var4 = new JacksonConverterRegistrar(
         (BeanProvider<ObjectMapper>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (ConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (JacksonConverterRegistrar)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JacksonConverterRegistrar var4 = (JacksonConverterRegistrar)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JacksonConverterRegistrar$Definition() {
      this(JacksonConverterRegistrar.class, $CONSTRUCTOR);
   }

   protected $JacksonConverterRegistrar$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JacksonConverterRegistrar$Definition$Reference.$ANNOTATION_METADATA,
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
