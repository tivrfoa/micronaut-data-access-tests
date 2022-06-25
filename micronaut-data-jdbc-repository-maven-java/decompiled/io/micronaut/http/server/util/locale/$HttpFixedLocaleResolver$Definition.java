package io.micronaut.http.server.util.locale;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HttpFixedLocaleResolver$Definition extends AbstractInitializableBeanDefinition<HttpFixedLocaleResolver> implements BeanFactory<HttpFixedLocaleResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HttpFixedLocaleResolver.class,
      "<init>",
      new Argument[]{Argument.of(HttpLocaleResolutionConfiguration.class, "localeResolutionConfiguration")},
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.util.LocaleResolver",
      new Argument[]{Argument.of(HttpRequest.class, "T", null, Argument.ofTypeVariable(Object.class, "B"))},
      "io.micronaut.core.util.locale.FixedLocaleResolver",
      new Argument[]{Argument.of(HttpRequest.class, "T", null, Argument.ofTypeVariable(Object.class, "B"))}
   );

   @Override
   public HttpFixedLocaleResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HttpFixedLocaleResolver var4 = new HttpFixedLocaleResolver((HttpLocaleResolutionConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (HttpFixedLocaleResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      HttpFixedLocaleResolver var4 = (HttpFixedLocaleResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $HttpFixedLocaleResolver$Definition() {
      this(HttpFixedLocaleResolver.class, $CONSTRUCTOR);
   }

   protected $HttpFixedLocaleResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HttpFixedLocaleResolver$Definition$Reference.$ANNOTATION_METADATA,
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
