package io.micronaut.http.server.binding;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.BasicAuth;
import io.micronaut.http.HttpRequest;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $BasicAuthArgumentBinder$Definition extends AbstractInitializableBeanDefinition<BasicAuthArgumentBinder> implements BeanFactory<BasicAuthArgumentBinder> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      BasicAuthArgumentBinder.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.bind.ArgumentBinder",
      new Argument[]{Argument.of(BasicAuth.class, "T"), Argument.of(HttpRequest.class, "S", null, Argument.ofTypeVariable(Object.class, "B"))},
      "io.micronaut.core.bind.TypeArgumentBinder",
      new Argument[]{Argument.of(BasicAuth.class, "T"), Argument.of(HttpRequest.class, "S", null, Argument.ofTypeVariable(Object.class, "B"))},
      "io.micronaut.http.bind.binders.RequestArgumentBinder",
      new Argument[]{Argument.of(BasicAuth.class, "T")},
      "io.micronaut.http.bind.binders.TypedRequestArgumentBinder",
      new Argument[]{Argument.of(BasicAuth.class, "T")}
   );

   @Override
   public BasicAuthArgumentBinder build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      BasicAuthArgumentBinder var4 = new BasicAuthArgumentBinder();
      return (BasicAuthArgumentBinder)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      BasicAuthArgumentBinder var4 = (BasicAuthArgumentBinder)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $BasicAuthArgumentBinder$Definition() {
      this(BasicAuthArgumentBinder.class, $CONSTRUCTOR);
   }

   protected $BasicAuthArgumentBinder$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $BasicAuthArgumentBinder$Definition$Reference.$ANNOTATION_METADATA,
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
