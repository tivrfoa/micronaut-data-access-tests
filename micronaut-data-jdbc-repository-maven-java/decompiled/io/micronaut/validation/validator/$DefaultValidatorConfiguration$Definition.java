package io.micronaut.validation.validator;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.MessageSource;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.validation.validator.constraints.ConstraintValidatorRegistry;
import io.micronaut.validation.validator.extractors.ValueExtractorRegistry;
import java.util.Collections;
import java.util.Optional;
import javax.validation.ClockProvider;
import javax.validation.TraversableResolver;

// $FF: synthetic class
@Generated
class $DefaultValidatorConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DefaultValidatorConfiguration>
   implements BeanFactory<DefaultValidatorConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultValidatorConfiguration.class, "<init>", null, null, false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultValidatorConfiguration.class,
         "setConstraintValidatorRegistry",
         new Argument[]{
            Argument.of(
               ConstraintValidatorRegistry.class,
               "constraintValidatorRegistry",
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
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
                  "javax.inject.Scope",
                  AnnotationUtil.internListOf("javax.inject.Singleton"),
                  "javax.inject.Singleton",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties")
               ),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultValidatorConfiguration.class,
         "setValueExtractorRegistry",
         new Argument[]{
            Argument.of(
               ValueExtractorRegistry.class,
               "valueExtractorRegistry",
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
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
                  "javax.inject.Scope",
                  AnnotationUtil.internListOf("javax.inject.Singleton"),
                  "javax.inject.Singleton",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties")
               ),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultValidatorConfiguration.class,
         "setClockProvider",
         new Argument[]{
            Argument.of(
               ClockProvider.class,
               "clockProvider",
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
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
                  "javax.inject.Scope",
                  AnnotationUtil.internListOf("javax.inject.Singleton"),
                  "javax.inject.Singleton",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties")
               ),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultValidatorConfiguration.class,
         "setTraversableResolver",
         new Argument[]{
            Argument.of(
               TraversableResolver.class,
               "traversableResolver",
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
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
                  "javax.inject.Scope",
                  AnnotationUtil.internListOf("javax.inject.Singleton"),
                  "javax.inject.Singleton",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties")
               ),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultValidatorConfiguration.class,
         "setMessageSource",
         new Argument[]{
            Argument.of(
               MessageSource.class,
               "messageSource",
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
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
                  "javax.inject.Scope",
                  AnnotationUtil.internListOf("javax.inject.Singleton"),
                  "javax.inject.Singleton",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties")
               ),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultValidatorConfiguration.class,
         "setExecutionHandleLocator",
         new Argument[]{
            Argument.of(
               ExecutionHandleLocator.class,
               "executionHandleLocator",
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
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.validator"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.validator")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
                  "javax.inject.Scope",
                  AnnotationUtil.internListOf("javax.inject.Singleton"),
                  "javax.inject.Singleton",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties")
               ),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false
      )
   };

   @Override
   public DefaultValidatorConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultValidatorConfiguration var4 = new DefaultValidatorConfiguration();
      return (DefaultValidatorConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultValidatorConfiguration var4 = (DefaultValidatorConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.validator.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.validator.enabled", null)
            );
         }

         var4.setConstraintValidatorRegistry(super.getBeanForMethodArgument(var1, var2, 0, 0, null));
         var4.setValueExtractorRegistry(super.getBeanForMethodArgument(var1, var2, 1, 0, null));
         var4.setClockProvider(super.getBeanForMethodArgument(var1, var2, 2, 0, null));
         var4.setTraversableResolver(super.getBeanForMethodArgument(var1, var2, 3, 0, null));
         var4.setMessageSource(super.getBeanForMethodArgument(var1, var2, 4, 0, null));
         var4.setExecutionHandleLocator(super.getBeanForMethodArgument(var1, var2, 5, 0, null));
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DefaultValidatorConfiguration$Definition() {
      this(DefaultValidatorConfiguration.class, $CONSTRUCTOR);
   }

   protected $DefaultValidatorConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultValidatorConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false
      );
   }
}
