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
class $CookieLocaleResolver$Definition extends AbstractInitializableBeanDefinition<CookieLocaleResolver> implements BeanFactory<CookieLocaleResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      CookieLocaleResolver.class,
      "<init>",
      new Argument[]{Argument.of(HttpLocaleResolutionConfiguration.class, "httpLocaleResolutionConfiguration")},
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.util.LocaleResolver",
      new Argument[]{Argument.of(HttpRequest.class, "T", null, Argument.ofTypeVariable(Object.class, "B"))},
      "io.micronaut.core.util.locale.AbstractLocaleResolver",
      new Argument[]{Argument.of(HttpRequest.class, "T", null, Argument.ofTypeVariable(Object.class, "B"))}
   );

   @Override
   public CookieLocaleResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      CookieLocaleResolver var4 = new CookieLocaleResolver((HttpLocaleResolutionConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (CookieLocaleResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      CookieLocaleResolver var4 = (CookieLocaleResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $CookieLocaleResolver$Definition() {
      this(CookieLocaleResolver.class, $CONSTRUCTOR);
   }

   protected $CookieLocaleResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $CookieLocaleResolver$Definition$Reference.$ANNOTATION_METADATA,
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
