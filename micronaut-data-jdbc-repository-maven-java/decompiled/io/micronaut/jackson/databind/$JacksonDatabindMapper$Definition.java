package io.micronaut.jackson.databind;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $JacksonDatabindMapper$Definition extends AbstractInitializableBeanDefinition<JacksonDatabindMapper> implements BeanFactory<JacksonDatabindMapper> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JacksonDatabindMapper.class,
      "<init>",
      new Argument[]{Argument.of(ObjectMapper.class, "objectMapper")},
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf("io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP, "javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.mapOf("io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP, "javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );

   @Override
   public JacksonDatabindMapper build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JacksonDatabindMapper var4 = new JacksonDatabindMapper((ObjectMapper)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (JacksonDatabindMapper)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JacksonDatabindMapper var4 = (JacksonDatabindMapper)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JacksonDatabindMapper$Definition() {
      this(JacksonDatabindMapper.class, $CONSTRUCTOR);
   }

   protected $JacksonDatabindMapper$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JacksonDatabindMapper$Definition$Reference.$ANNOTATION_METADATA,
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
