package io.micronaut.runtime.context.scope;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.scope.AbstractConcurrentCustomScope;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ThreadLocalCustomScope$Definition extends AbstractInitializableBeanDefinition<ThreadLocalCustomScope> implements BeanFactory<ThreadLocalCustomScope> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ThreadLocalCustomScope.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.LifeCycle",
      new Argument[]{Argument.of(AbstractConcurrentCustomScope.class, "T", null, Argument.ofTypeVariable(Annotation.class, "A"))},
      "io.micronaut.context.scope.AbstractConcurrentCustomScope",
      new Argument[]{Argument.of(ThreadLocal.class, "A")},
      "io.micronaut.context.scope.CustomScope",
      new Argument[]{Argument.of(ThreadLocal.class, "A")}
   );

   @Override
   public ThreadLocalCustomScope build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ThreadLocalCustomScope var4 = new ThreadLocalCustomScope();
      return (ThreadLocalCustomScope)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ThreadLocalCustomScope var4 = (ThreadLocalCustomScope)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ThreadLocalCustomScope$Definition() {
      this(ThreadLocalCustomScope.class, $CONSTRUCTOR);
   }

   protected $ThreadLocalCustomScope$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ThreadLocalCustomScope$Definition$Reference.$ANNOTATION_METADATA,
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
