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
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.jdbc.DataSourceResolver;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;

// $FF: synthetic class
@Generated
class $DataSourceMigrationRunner$Definition
   extends AbstractInitializableBeanDefinition<DataSourceMigrationRunner>
   implements BeanFactory<DataSourceMigrationRunner> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DataSourceMigrationRunner.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationContext.class, "applicationContext"),
         Argument.of(ApplicationEventPublisher.class, "eventPublisher", null, Argument.ofTypeVariable(Object.class, "T")),
         Argument.of(
            DataSourceResolver.class,
            "dataSourceResolver",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         )
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.BeanCreatedEventListener", new Argument[]{Argument.of(DataSource.class, "T")}
   );

   @Override
   public DataSourceMigrationRunner build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DataSourceMigrationRunner var4 = new DataSourceMigrationRunner(
         var2,
         (ApplicationEventPublisher)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (DataSourceResolver)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (DataSourceMigrationRunner)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DataSourceMigrationRunner var4 = (DataSourceMigrationRunner)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DataSourceMigrationRunner$Definition() {
      this(DataSourceMigrationRunner.class, $CONSTRUCTOR);
   }

   protected $DataSourceMigrationRunner$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DataSourceMigrationRunner$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $DataSourceMigrationRunner$Definition$Exec(),
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
