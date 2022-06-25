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
class $RequestLocaleResolver$Definition extends AbstractInitializableBeanDefinition<RequestLocaleResolver> implements BeanFactory<RequestLocaleResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      RequestLocaleResolver.class,
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
   public RequestLocaleResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      RequestLocaleResolver var4 = new RequestLocaleResolver((HttpLocaleResolutionConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (RequestLocaleResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      RequestLocaleResolver var4 = (RequestLocaleResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $RequestLocaleResolver$Definition() {
      this(RequestLocaleResolver.class, $CONSTRUCTOR);
   }

   protected $RequestLocaleResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $RequestLocaleResolver$Definition$Reference.$ANNOTATION_METADATA,
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
