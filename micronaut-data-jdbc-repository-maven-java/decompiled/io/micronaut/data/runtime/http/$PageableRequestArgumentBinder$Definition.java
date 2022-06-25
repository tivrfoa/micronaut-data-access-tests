package io.micronaut.data.runtime.http;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.runtime.config.DataConfiguration;
import io.micronaut.http.HttpRequest;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $PageableRequestArgumentBinder$Definition
   extends AbstractInitializableBeanDefinition<PageableRequestArgumentBinder>
   implements BeanFactory<PageableRequestArgumentBinder> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      PageableRequestArgumentBinder.class, "<init>", new Argument[]{Argument.of(DataConfiguration.PageableConfiguration.class, "configuration")}, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.bind.ArgumentBinder",
      new Argument[]{Argument.of(Pageable.class, "T"), Argument.of(HttpRequest.class, "S", null, Argument.ofTypeVariable(Object.class, "B"))},
      "io.micronaut.core.bind.TypeArgumentBinder",
      new Argument[]{Argument.of(Pageable.class, "T"), Argument.of(HttpRequest.class, "S", null, Argument.ofTypeVariable(Object.class, "B"))},
      "io.micronaut.http.bind.binders.RequestArgumentBinder",
      new Argument[]{Argument.of(Pageable.class, "T")},
      "io.micronaut.http.bind.binders.TypedRequestArgumentBinder",
      new Argument[]{Argument.of(Pageable.class, "T")}
   );

   @Override
   public PageableRequestArgumentBinder build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      PageableRequestArgumentBinder var4 = new PageableRequestArgumentBinder(
         (DataConfiguration.PageableConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null)
      );
      return (PageableRequestArgumentBinder)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      PageableRequestArgumentBinder var4 = (PageableRequestArgumentBinder)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $PageableRequestArgumentBinder$Definition() {
      this(PageableRequestArgumentBinder.class, $CONSTRUCTOR);
   }

   protected $PageableRequestArgumentBinder$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $PageableRequestArgumentBinder$Definition$Reference.$ANNOTATION_METADATA,
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
