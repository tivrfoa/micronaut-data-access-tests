package io.micronaut.flyway;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $AbstractFlywayMigration$Definition extends AbstractInitializableBeanDefinition<AbstractFlywayMigration> implements BeanFactory<AbstractFlywayMigration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      AbstractFlywayMigration.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationContext.class, "applicationContext"),
         Argument.of(ApplicationEventPublisher.class, "eventPublisher", null, Argument.ofTypeVariable(Object.class, "T"))
      },
      null,
      false
   );

   @Override
   public AbstractFlywayMigration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      AbstractFlywayMigration var4 = new AbstractFlywayMigration(var2, (ApplicationEventPublisher)super.getBeanForConstructorArgument(var1, var2, 1, null));
      return (AbstractFlywayMigration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      AbstractFlywayMigration var4 = (AbstractFlywayMigration)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $AbstractFlywayMigration$Definition() {
      this(AbstractFlywayMigration.class, $CONSTRUCTOR);
   }

   protected $AbstractFlywayMigration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $AbstractFlywayMigration$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $AbstractFlywayMigration$Definition$Exec(),
         null,
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
