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
class $CompositeHttpLocaleResolver$Definition
   extends AbstractInitializableBeanDefinition<CompositeHttpLocaleResolver>
   implements BeanFactory<CompositeHttpLocaleResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      CompositeHttpLocaleResolver.class,
      "<init>",
      new Argument[]{
         Argument.of(HttpLocaleResolver[].class, "localeResolvers"), Argument.of(HttpLocaleResolutionConfiguration.class, "httpLocaleResolutionConfiguration")
      },
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
   public CompositeHttpLocaleResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      CompositeHttpLocaleResolver var4 = new CompositeHttpLocaleResolver(
         (HttpLocaleResolver[])super.getBeansOfTypeForConstructorArgument(var1, var2, 0, Argument.of(HttpLocaleResolver.class, null), null)
            .toArray(new HttpLocaleResolver[0]),
         (HttpLocaleResolutionConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (CompositeHttpLocaleResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      CompositeHttpLocaleResolver var4 = (CompositeHttpLocaleResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $CompositeHttpLocaleResolver$Definition() {
      this(CompositeHttpLocaleResolver.class, $CONSTRUCTOR);
   }

   protected $CompositeHttpLocaleResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $CompositeHttpLocaleResolver$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false,
         false
      );
   }
}
