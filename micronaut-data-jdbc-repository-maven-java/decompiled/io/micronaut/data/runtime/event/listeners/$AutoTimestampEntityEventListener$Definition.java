package io.micronaut.data.runtime.event.listeners;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.date.DateTimeProvider;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $AutoTimestampEntityEventListener$Definition
   extends AbstractInitializableBeanDefinition<AutoTimestampEntityEventListener>
   implements BeanFactory<AutoTimestampEntityEventListener> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      AutoTimestampEntityEventListener.class,
      "<init>",
      new Argument[]{
         Argument.of(DateTimeProvider.class, "dateTimeProvider", null, Argument.ofTypeVariable(Object.class, "T")),
         Argument.of(
            DataConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               DataConversionService.class, "Impl", null, Argument.ofTypeVariable(DataConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         )
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.data.event.EntityEventListener",
      new Argument[]{Argument.of(Object.class, "T")},
      "io.micronaut.data.model.runtime.PropertyAutoPopulator",
      new Argument[]{Argument.of(DateUpdated.class, "T")}
   );

   @Override
   public AutoTimestampEntityEventListener build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      AutoTimestampEntityEventListener var4 = new AutoTimestampEntityEventListener(
         (DateTimeProvider<?>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (DataConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (AutoTimestampEntityEventListener)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      AutoTimestampEntityEventListener var4 = (AutoTimestampEntityEventListener)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $AutoTimestampEntityEventListener$Definition() {
      this(AutoTimestampEntityEventListener.class, $CONSTRUCTOR);
   }

   protected $AutoTimestampEntityEventListener$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $AutoTimestampEntityEventListener$Definition$Reference.$ANNOTATION_METADATA,
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
