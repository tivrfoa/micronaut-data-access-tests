package io.micronaut.http.server.binding;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.server.util.locale.HttpLocaleResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $LocaleArgumentBinder$Definition extends AbstractInitializableBeanDefinition<LocaleArgumentBinder> implements BeanFactory<LocaleArgumentBinder> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      LocaleArgumentBinder.class, "<init>", new Argument[]{Argument.of(HttpLocaleResolver.class, "localeResolver")}, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.bind.ArgumentBinder",
      new Argument[]{Argument.of(Locale.class, "T"), Argument.of(HttpRequest.class, "S", null, Argument.ofTypeVariable(Object.class, "B"))},
      "io.micronaut.core.bind.TypeArgumentBinder",
      new Argument[]{Argument.of(Locale.class, "T"), Argument.of(HttpRequest.class, "S", null, Argument.ofTypeVariable(Object.class, "B"))},
      "io.micronaut.http.bind.binders.RequestArgumentBinder",
      new Argument[]{Argument.of(Locale.class, "T")},
      "io.micronaut.http.bind.binders.TypedRequestArgumentBinder",
      new Argument[]{Argument.of(Locale.class, "T")}
   );

   @Override
   public LocaleArgumentBinder build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      LocaleArgumentBinder var4 = new LocaleArgumentBinder((HttpLocaleResolver)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (LocaleArgumentBinder)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      LocaleArgumentBinder var4 = (LocaleArgumentBinder)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $LocaleArgumentBinder$Definition() {
      this(LocaleArgumentBinder.class, $CONSTRUCTOR);
   }

   protected $LocaleArgumentBinder$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $LocaleArgumentBinder$Definition$Reference.$ANNOTATION_METADATA,
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
