package io.micronaut.configuration.jdbc.hikari;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DatasourceFactory$Definition
   extends AbstractInitializableBeanDefinition<DatasourceFactory>
   implements BeanFactory<DatasourceFactory>,
   DisposableBeanDefinition<DatasourceFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DatasourceFactory.class, "<init>", new Argument[]{Argument.of(ApplicationContext.class, "applicationContext")}, null, false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(
         DatasourceFactory.class,
         "close",
         null,
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Factory", Collections.EMPTY_MAP, "jakarta.inject.Singleton", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Factory", Collections.EMPTY_MAP, "jakarta.inject.Singleton", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false,
         false,
         true
      )
   };

   @Override
   public DatasourceFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DatasourceFactory var4 = new DatasourceFactory(var2);
      return (DatasourceFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DatasourceFactory var4 = (DatasourceFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   @Override
   public DatasourceFactory dispose(BeanResolutionContext var1, BeanContext var2, DatasourceFactory var3) {
      DatasourceFactory var4 = (DatasourceFactory)var3;
      super.preDestroy(var1, var2, var3);
      var4.close();
      return var4;
   }

   public $DatasourceFactory$Definition() {
      this(DatasourceFactory.class, $CONSTRUCTOR);
   }

   protected $DatasourceFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DatasourceFactory$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         null,
         Optional.empty(),
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
