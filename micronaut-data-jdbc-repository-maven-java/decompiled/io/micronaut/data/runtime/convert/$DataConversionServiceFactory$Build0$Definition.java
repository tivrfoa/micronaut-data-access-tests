package io.micronaut.data.runtime.convert;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $DataConversionServiceFactory$Build0$Definition
   extends AbstractInitializableBeanDefinition<DataConversionServiceImpl>
   implements BeanFactory<DataConversionServiceImpl> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DataConversionServiceFactory.class,
      "build",
      new Argument[]{
         Argument.of(
            BeanContext.class,
            "beanContext",
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
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Bean",
            AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Bean",
            AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
            "io.micronaut.core.annotation.Internal",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
         false,
         true
      ),
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.convert.ConversionService",
      new Argument[]{Argument.of(DataConversionServiceImpl.class, "Impl")},
      "io.micronaut.data.runtime.convert.DataConversionService",
      new Argument[]{Argument.of(DataConversionServiceImpl.class, "Impl")}
   );
   private static final Set $EXPOSED_TYPES = Collections.singleton(DataConversionService.class);

   @Override
   public DataConversionServiceImpl build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, DataConversionServiceFactory.class);
      DataConversionServiceImpl var5 = ((DataConversionServiceFactory)var4).build(var2);
      return (DataConversionServiceImpl)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DataConversionServiceImpl var4 = (DataConversionServiceImpl)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DataConversionServiceFactory$Build0$Definition() {
      this(DataConversionServiceImpl.class, $CONSTRUCTOR);
   }

   protected $DataConversionServiceFactory$Build0$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DataConversionServiceFactory$Build0$Definition$Reference.$ANNOTATION_METADATA,
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

   @Override
   public Set getExposedTypes() {
      return $EXPOSED_TYPES;
   }
}
