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
class $ParameterVersionResolver$Definition
   extends AbstractInitializableBeanDefinition<ParameterVersionResolver>
   implements BeanFactory<ParameterVersionResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ParameterVersionResolver.class, "<init>", new Argument[]{Argument.of(ParameterVersionResolverConfiguration.class, "configuration")}, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.web.router.version.resolution.VersionResolver",
      new Argument[]{Argument.of(HttpRequest.class, "T", null, Argument.ofTypeVariable(Object.class, "B")), Argument.of(String.class, "R")}
   );

   @Override
   public ParameterVersionResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ParameterVersionResolver var4 = new ParameterVersionResolver(
         (ParameterVersionResolverConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null)
      );
      return (ParameterVersionResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ParameterVersionResolver var4 = (ParameterVersionResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ParameterVersionResolver$Definition() {
      this(ParameterVersionResolver.class, $CONSTRUCTOR);
   }

   protected $ParameterVersionResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ParameterVersionResolver$Definition$Reference.$ANNOTATION_METADATA,
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
