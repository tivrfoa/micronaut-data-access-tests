package io.micronaut.web.router.naming;

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
class $ConfigurableUriNamingStrategy$Definition
   extends AbstractInitializableBeanDefinition<ConfigurableUriNamingStrategy>
   implements BeanFactory<ConfigurableUriNamingStrategy> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ConfigurableUriNamingStrategy.class,
      "<init>",
      new Argument[]{
         Argument.of(
            String.class,
            "contextPath",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Value", AnnotationUtil.mapOf("value", "${micronaut.server.context-path}")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Value", AnnotationUtil.mapOf("value", "${micronaut.server.context-path}")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("io.micronaut.context.annotation.Value")),
               true,
               true
            ),
            null
         )
      },
      null,
      false
   );

   @Override
   public ConfigurableUriNamingStrategy build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ConfigurableUriNamingStrategy var4 = new ConfigurableUriNamingStrategy(
         (String)super.getPropertyPlaceholderValueForConstructorArgument(var1, var2, 0, "${micronaut.server.context-path}")
      );
      return (ConfigurableUriNamingStrategy)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ConfigurableUriNamingStrategy var4 = (ConfigurableUriNamingStrategy)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ConfigurableUriNamingStrategy$Definition() {
      this(ConfigurableUriNamingStrategy.class, $CONSTRUCTOR);
   }

   protected $ConfigurableUriNamingStrategy$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ConfigurableUriNamingStrategy$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false,
         false
      );
   }
}
