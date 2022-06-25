package io.micronaut.http.client.interceptor.configuration;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NamedClientVersioningConfiguration$Definition
   extends AbstractInitializableBeanDefinition<NamedClientVersioningConfiguration>
   implements BeanFactory<NamedClientVersioningConfiguration>,
   ParametrizedBeanFactory<NamedClientVersioningConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NamedClientVersioningConfiguration.class,
      "<init>",
      new Argument[]{
         Argument.of(
            String.class,
            "clientName",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.bind.annotation.Bindable",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
               ),
               false,
               true
            ),
            null
         )
      },
      null,
      false
   );

   @Override
   public NamedClientVersioningConfiguration doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      NamedClientVersioningConfiguration var5 = new NamedClientVersioningConfiguration((String)var4.get("clientName"));
      return (NamedClientVersioningConfiguration)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         NamedClientVersioningConfiguration var4 = (NamedClientVersioningConfiguration)var3;
         if (this.containsPropertiesValue(var1, var2, "micronaut.http.client.versioning.*.headers")) {
            var4.setHeaders(
               (List<String>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setHeaders",
                  Argument.of(List.class, "headerNames", null, Argument.ofTypeVariable(String.class, "E")),
                  "micronaut.http.client.versioning.*.headers",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.http.client.versioning.*.parameters")) {
            var4.setParameters(
               (List<String>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setParameters",
                  Argument.of(List.class, "parameterNames", null, Argument.ofTypeVariable(String.class, "E")),
                  "micronaut.http.client.versioning.*.parameters",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $NamedClientVersioningConfiguration$Definition() {
      this(NamedClientVersioningConfiguration.class, $CONSTRUCTOR);
   }

   protected $NamedClientVersioningConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NamedClientVersioningConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         true,
         true,
         false,
         true,
         false,
         false
      );
   }
}
