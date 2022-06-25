package io.micronaut.http.server.util.locale;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.MessageSource;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.LocaleResolver;
import io.micronaut.http.HttpRequest;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HttpLocalizedMessageSource$Definition
   extends AbstractInitializableBeanDefinition<HttpLocalizedMessageSource>
   implements BeanFactory<HttpLocalizedMessageSource> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HttpLocalizedMessageSource.class,
      "<init>",
      new Argument[]{
         Argument.of(
            LocaleResolver.class, "localeResolver", null, Argument.ofTypeVariable(HttpRequest.class, "T", null, Argument.ofTypeVariable(Object.class, "B"))
         ),
         Argument.of(MessageSource.class, "messageSource")
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.AbstractLocalizedMessageSource",
      new Argument[]{Argument.of(HttpRequest.class, "T", null, Argument.ofTypeVariable(Object.class, "B"))}
   );

   @Override
   public HttpLocalizedMessageSource build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HttpLocalizedMessageSource var4 = new HttpLocalizedMessageSource(
         (LocaleResolver<HttpRequest<?>>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (MessageSource)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (HttpLocalizedMessageSource)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      HttpLocalizedMessageSource var4 = (HttpLocalizedMessageSource)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $HttpLocalizedMessageSource$Definition() {
      this(HttpLocalizedMessageSource.class, $CONSTRUCTOR);
   }

   protected $HttpLocalizedMessageSource$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HttpLocalizedMessageSource$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $HttpLocalizedMessageSource$Definition$Exec(),
         $TYPE_ARGUMENTS,
         Optional.of("io.micronaut.runtime.http.scope.RequestScope"),
         false,
         false,
         false,
         false,
         false,
         false,
         false,
         false
      );
   }
}
