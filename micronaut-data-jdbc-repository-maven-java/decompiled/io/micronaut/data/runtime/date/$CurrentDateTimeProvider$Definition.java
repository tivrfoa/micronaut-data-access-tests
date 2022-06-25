package io.micronaut.data.runtime.date;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $CurrentDateTimeProvider$Definition extends AbstractInitializableBeanDefinition<CurrentDateTimeProvider> implements BeanFactory<CurrentDateTimeProvider> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      CurrentDateTimeProvider.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.data.runtime.date.DateTimeProvider", new Argument[]{Argument.of(OffsetDateTime.class, "T")}
   );

   @Override
   public CurrentDateTimeProvider build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      CurrentDateTimeProvider var4 = new CurrentDateTimeProvider();
      return (CurrentDateTimeProvider)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      CurrentDateTimeProvider var4 = (CurrentDateTimeProvider)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $CurrentDateTimeProvider$Definition() {
      this(CurrentDateTimeProvider.class, $CONSTRUCTOR);
   }

   protected $CurrentDateTimeProvider$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $CurrentDateTimeProvider$Definition$Reference.$ANNOTATION_METADATA,
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
