package io.micronaut.management.endpoint.health;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.management.endpoint.EndpointEnabledCondition;
import io.micronaut.management.health.aggregator.HealthAggregator;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $HealthEndpoint$Definition extends AbstractInitializableBeanDefinition<HealthEndpoint> implements BeanFactory<HealthEndpoint> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HealthEndpoint.class,
      "<init>",
      new Argument[]{
         Argument.of(HealthAggregator.class, "healthAggregator", null, Argument.ofTypeVariable(HealthResult.class, "T")),
         Argument.of(HealthIndicator[].class, "healthIndicators"),
         Argument.of(
            HealthIndicator[].class,
            "livenessHealthIndicators",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.management.health.indicator.annotation.Liveness", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("io.micronaut.management.health.indicator.annotation.Liveness", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("io.micronaut.management.health.indicator.annotation.Liveness")),
               false,
               true
            ),
            null
         )
      },
      null,
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS;
   private static final Set $INNER_CONFIGURATION_CLASSES = Collections.singleton(HealthEndpoint.StatusConfiguration.class);

   @Override
   public HealthEndpoint build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HealthEndpoint var4 = new HealthEndpoint(
         (HealthAggregator<HealthResult>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (HealthIndicator[])super.getBeansOfTypeForConstructorArgument(var1, var2, 1, Argument.of(HealthIndicator.class, null), null)
            .toArray(new HealthIndicator[0]),
         (HealthIndicator[])super.getBeansOfTypeForConstructorArgument(
               var1,
               var2,
               2,
               Argument.of(HealthIndicator.class, null),
               Qualifiers.byAnnotationSimple(
                  ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[2].getAnnotationMetadata(),
                  "io.micronaut.management.health.indicator.annotation.Liveness"
               )
            )
            .toArray(new HealthIndicator[0])
      );
      return (HealthEndpoint)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         HealthEndpoint var4 = (HealthEndpoint)var3;
         if (this.containsPropertyValue(var1, var2, "endpoints.health.details-visible")) {
            var4.setDetailsVisible(
               (DetailsVisibility)super.getPropertyValueForSetter(
                  var1, var2, "setDetailsVisible", Argument.of(DetailsVisibility.class, "detailsVisible"), "endpoints.health.details-visible", null
               )
            );
         }

         var4.setStatusConfiguration(super.getBeanForMethodArgument(var1, var2, 0, 0, null));
      }

      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
         new AbstractInitializableBeanDefinition.MethodReference(
            HealthEndpoint.class,
            "setStatusConfiguration",
            new Argument[]{Argument.of(HealthEndpoint.StatusConfiguration.class, "statusConfiguration")},
            new AnnotationMetadataHierarchy(
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.ConfigurationReader",
                     AnnotationUtil.mapOf("prefix", "endpoints.endpoints.health"),
                     "io.micronaut.management.endpoint.annotation.Endpoint",
                     AnnotationUtil.mapOf("defaultSensitive", false, "id", "health", "value", "health")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.ConfigurationReader",
                     AnnotationUtil.mapOf("prefix", "endpoints", "value", "health"),
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new EndpointEnabledCondition())),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                           )
                        }
                     ),
                     "javax.inject.Scope",
                     Collections.EMPTY_MAP,
                     "javax.inject.Singleton",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.ConfigurationReader",
                     AnnotationUtil.mapOf("prefix", "endpoints", "value", "health"),
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new EndpointEnabledCondition())),
                              var0
                           )
                        }
                     ),
                     "javax.inject.Scope",
                     Collections.EMPTY_MAP,
                     "javax.inject.Singleton",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.ConfigurationReader",
                     AnnotationUtil.mapOf("prefix", "endpoints.endpoints.health"),
                     "io.micronaut.management.endpoint.annotation.Endpoint",
                     AnnotationUtil.mapOf("defaultSensitive", false, "id", "health", "value", "health")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.ConfigurationReader",
                     AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Endpoint"),
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Endpoint"),
                     "javax.inject.Scope",
                     AnnotationUtil.internListOf("javax.inject.Singleton"),
                     "javax.inject.Singleton",
                     AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Endpoint")
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
   }

   public $HealthEndpoint$Definition() {
      this(HealthEndpoint.class, $CONSTRUCTOR);
   }

   protected $HealthEndpoint$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HealthEndpoint$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         new $HealthEndpoint$Definition$Exec(),
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

   @Override
   protected boolean isInnerConfiguration(Class var1) {
      return $INNER_CONFIGURATION_CLASSES.contains(var1);
   }
}
