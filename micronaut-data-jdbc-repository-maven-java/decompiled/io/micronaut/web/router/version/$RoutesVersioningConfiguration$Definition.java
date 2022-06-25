package io.micronaut.web.router.version;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $RoutesVersioningConfiguration$Definition
   extends AbstractInitializableBeanDefinition<RoutesVersioningConfiguration>
   implements BeanFactory<RoutesVersioningConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      RoutesVersioningConfiguration.class, "<init>", null, null, false
   );

   @Override
   public RoutesVersioningConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      RoutesVersioningConfiguration var4 = new RoutesVersioningConfiguration();
      return (RoutesVersioningConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         RoutesVersioningConfiguration var4 = (RoutesVersioningConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.router.versioning.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.router.versioning.enabled", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.router.versioning.default-version")) {
            var4.setDefaultVersion(
               (String)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setDefaultVersion",
                  Argument.of(
                     String.class,
                     "defaultVersion",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.router.versioning.default-version",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $RoutesVersioningConfiguration$Definition() {
      this(RoutesVersioningConfiguration.class, $CONSTRUCTOR);
   }

   protected $RoutesVersioningConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $RoutesVersioningConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         null,
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
