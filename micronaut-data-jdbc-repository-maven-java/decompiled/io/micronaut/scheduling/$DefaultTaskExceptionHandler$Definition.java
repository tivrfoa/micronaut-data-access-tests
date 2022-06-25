package io.micronaut.scheduling;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultTaskExceptionHandler$Definition
   extends AbstractInitializableBeanDefinition<DefaultTaskExceptionHandler>
   implements BeanFactory<DefaultTaskExceptionHandler> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultTaskExceptionHandler.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.exceptions.BeanExceptionHandler",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Throwable.class, "E")},
      "io.micronaut.scheduling.TaskExceptionHandler",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Throwable.class, "E")},
      "java.util.function.BiConsumer",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Throwable.class, "U")}
   );

   @Override
   public DefaultTaskExceptionHandler build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultTaskExceptionHandler var4 = new DefaultTaskExceptionHandler();
      return (DefaultTaskExceptionHandler)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultTaskExceptionHandler var4 = (DefaultTaskExceptionHandler)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultTaskExceptionHandler$Definition() {
      this(DefaultTaskExceptionHandler.class, $CONSTRUCTOR);
   }

   protected $DefaultTaskExceptionHandler$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultTaskExceptionHandler$Definition$Reference.$ANNOTATION_METADATA,
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
