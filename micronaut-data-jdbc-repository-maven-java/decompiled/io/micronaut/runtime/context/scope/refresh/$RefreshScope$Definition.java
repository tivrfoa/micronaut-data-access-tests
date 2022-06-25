package io.micronaut.runtime.context.scope.refresh;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.runtime.context.scope.Refreshable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $RefreshScope$Definition extends AbstractInitializableBeanDefinition<RefreshScope> implements BeanFactory<RefreshScope> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      RefreshScope.class,
      "<init>",
      new Argument[]{Argument.of(BeanContext.class, "beanContext")},
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.LifeCycle",
      new Argument[]{Argument.of(RefreshScope.class, "T")},
      "io.micronaut.context.event.ApplicationEventListener",
      new Argument[]{Argument.of(RefreshEvent.class, "E")},
      "io.micronaut.context.scope.CustomScope",
      new Argument[]{Argument.of(Refreshable.class, "A")}
   );

   @Override
   public RefreshScope build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      RefreshScope var4 = new RefreshScope(var2);
      return (RefreshScope)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      RefreshScope var4 = (RefreshScope)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $RefreshScope$Definition() {
      this(RefreshScope.class, $CONSTRUCTOR);
   }

   protected $RefreshScope$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $RefreshScope$Definition$Reference.$ANNOTATION_METADATA,
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
