package io.micronaut.web.router.resource;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.type.Argument;
import io.micronaut.http.context.ServerContextPathProvider;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $StaticResourceConfiguration$Definition
   extends AbstractInitializableBeanDefinition<StaticResourceConfiguration>
   implements BeanFactory<StaticResourceConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      StaticResourceConfiguration.class,
      "<init>",
      new Argument[]{
         Argument.of(ResourceResolver.class, "resourceResolver"),
         Argument.of(
            ServerContextPathProvider.class,
            "contextPathProvider",
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

   @Override
   public StaticResourceConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      StaticResourceConfiguration var4 = new StaticResourceConfiguration(
         (ResourceResolver)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (ServerContextPathProvider)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (StaticResourceConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         StaticResourceConfiguration var4 = (StaticResourceConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.router.static-resources.*.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(
                  var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.router.static-resources.*.enabled", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.router.static-resources.*.paths")) {
            var4.setPaths(
               (List<String>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setPaths",
                  Argument.of(List.class, "paths", null, Argument.ofTypeVariable(String.class, "E")),
                  "micronaut.router.static-resources.*.paths",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.router.static-resources.*.mapping")) {
            var4.setMapping(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setMapping", Argument.of(String.class, "mapping"), "micronaut.router.static-resources.*.mapping", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $StaticResourceConfiguration$Definition() {
      this(StaticResourceConfiguration.class, $CONSTRUCTOR);
   }

   protected $StaticResourceConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $StaticResourceConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
