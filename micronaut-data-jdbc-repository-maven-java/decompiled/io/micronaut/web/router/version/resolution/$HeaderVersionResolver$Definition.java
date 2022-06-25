package io.micronaut.web.router.version.resolution;

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
class $HeaderVersionResolver$Definition extends AbstractInitializableBeanDefinition<HeaderVersionResolver> implements BeanFactory<HeaderVersionResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HeaderVersionResolver.class, "<init>", new Argument[]{Argument.of(HeaderVersionResolverConfiguration.class, "configuration")}, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.web.router.version.resolution.VersionResolver",
      new Argument[]{Argument.of(HttpRequest.class, "T", null, Argument.ofTypeVariable(Object.class, "B")), Argument.of(String.class, "R")}
   );

   @Override
   public HeaderVersionResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HeaderVersionResolver var4 = new HeaderVersionResolver((HeaderVersionResolverConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (HeaderVersionResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      HeaderVersionResolver var4 = (HeaderVersionResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $HeaderVersionResolver$Definition() {
      this(HeaderVersionResolver.class, $CONSTRUCTOR);
   }

   protected $HeaderVersionResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HeaderVersionResolver$Definition$Reference.$ANNOTATION_METADATA,
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
