package io.micronaut.data.jdbc.config;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.InitializingBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $SchemaGenerator$Definition
   extends AbstractInitializableBeanDefinition<SchemaGenerator>
   implements BeanFactory<SchemaGenerator>,
   InitializingBeanDefinition<SchemaGenerator> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      SchemaGenerator.class,
      "<init>",
      new Argument[]{Argument.of(List.class, "configurations", null, Argument.ofTypeVariable(DataJdbcConfiguration.class, "E"))},
      null,
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(
         SchemaGenerator.class,
         "createSchema",
         new Argument[]{Argument.of(BeanLocator.class, "beanLocator")},
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Context", Collections.EMPTY_MAP, "io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Context", Collections.EMPTY_MAP, "io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "javax.inject.Scope",
                  AnnotationUtil.internListOf("javax.inject.Singleton"),
                  "javax.inject.Singleton",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Context")
               ),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.PostConstruct", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.PostConstruct", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false,
         true,
         false
      )
   };

   @Override
   public SchemaGenerator build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      SchemaGenerator var4 = new SchemaGenerator(
         (List<DataJdbcConfiguration>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
         )
      );
      var4 = (SchemaGenerator)this.injectBean(var1, var2, var4);
      SchemaGenerator var10001 = (SchemaGenerator)this.initialize(var1, var2, var4);
      return var4;
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      SchemaGenerator var4 = (SchemaGenerator)var3;
      return super.injectBean(var1, var2, var3);
   }

   @Override
   public SchemaGenerator initialize(BeanResolutionContext var1, BeanContext var2, SchemaGenerator var3) {
      SchemaGenerator var4 = (SchemaGenerator)var3;
      super.postConstruct(var1, var2, var3);
      var4.createSchema(super.getBeanForMethodArgument(var1, var2, 0, 0, null));
      return var4;
   }

   public $SchemaGenerator$Definition() {
      this(SchemaGenerator.class, $CONSTRUCTOR);
   }

   protected $SchemaGenerator$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $SchemaGenerator$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
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
