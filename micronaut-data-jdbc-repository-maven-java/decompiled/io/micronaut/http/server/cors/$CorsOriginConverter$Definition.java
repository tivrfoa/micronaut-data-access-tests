package io.micronaut.http.server.cors;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $CorsOriginConverter$Definition extends AbstractInitializableBeanDefinition<CorsOriginConverter> implements BeanFactory<CorsOriginConverter> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      CorsOriginConverter.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.convert.TypeConverter",
      new Argument[]{
         Argument.of(Map.class, "S", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V")),
         Argument.of(CorsOriginConfiguration.class, "T")
      }
   );

   @Override
   public CorsOriginConverter build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      CorsOriginConverter var4 = new CorsOriginConverter();
      return (CorsOriginConverter)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      CorsOriginConverter var4 = (CorsOriginConverter)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $CorsOriginConverter$Definition() {
      this(CorsOriginConverter.class, $CONSTRUCTOR);
   }

   protected $CorsOriginConverter$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $CorsOriginConverter$Definition$Reference.$ANNOTATION_METADATA,
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
