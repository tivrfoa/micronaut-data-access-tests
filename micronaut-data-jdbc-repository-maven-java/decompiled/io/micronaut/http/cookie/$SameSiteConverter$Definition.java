package io.micronaut.http.cookie;

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
class $SameSiteConverter$Definition extends AbstractInitializableBeanDefinition<SameSiteConverter> implements BeanFactory<SameSiteConverter> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      SameSiteConverter.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.convert.TypeConverter", new Argument[]{Argument.of(CharSequence.class, "S"), Argument.of(SameSite.class, "T")}
   );

   @Override
   public SameSiteConverter build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      SameSiteConverter var4 = new SameSiteConverter();
      return (SameSiteConverter)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      SameSiteConverter var4 = (SameSiteConverter)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $SameSiteConverter$Definition() {
      this(SameSiteConverter.class, $CONSTRUCTOR);
   }

   protected $SameSiteConverter$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $SameSiteConverter$Definition$Reference.$ANNOTATION_METADATA,
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
