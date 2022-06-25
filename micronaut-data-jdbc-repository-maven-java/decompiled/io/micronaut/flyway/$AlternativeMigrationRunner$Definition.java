package io.micronaut.flyway;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $AlternativeMigrationRunner$Definition
   extends AbstractInitializableBeanDefinition<AlternativeMigrationRunner>
   implements BeanFactory<AlternativeMigrationRunner> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      AlternativeMigrationRunner.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationContext.class, "applicationContext"),
         Argument.of(ApplicationEventPublisher.class, "eventPublisher", null, Argument.ofTypeVariable(Object.class, "T"))
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.BeanCreatedEventListener", new Argument[]{Argument.of(FlywayConfigurationProperties.class, "T")}
   );

   @Override
   public AlternativeMigrationRunner build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      AlternativeMigrationRunner var4 = new AlternativeMigrationRunner(
         var2, (ApplicationEventPublisher)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (AlternativeMigrationRunner)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      AlternativeMigrationRunner var4 = (AlternativeMigrationRunner)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $AlternativeMigrationRunner$Definition() {
      this(AlternativeMigrationRunner.class, $CONSTRUCTOR);
   }

   protected $AlternativeMigrationRunner$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $AlternativeMigrationRunner$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $AlternativeMigrationRunner$Definition$Exec(),
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
