package io.micronaut.management.endpoint.env;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.env.Environment;
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
class $EnvironmentEndpoint$Definition extends AbstractInitializableBeanDefinition<EnvironmentEndpoint> implements BeanFactory<EnvironmentEndpoint> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      EnvironmentEndpoint.class,
      "<init>",
      new Argument[]{
         Argument.of(Environment.class, "environment"),
         Argument.of(
            EnvironmentEndpointFilter.class,
            "environmentFilter",
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
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );

   @Override
   public EnvironmentEndpoint build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      EnvironmentEndpoint var4 = new EnvironmentEndpoint(
         (Environment)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (EnvironmentEndpointFilter)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (EnvironmentEndpoint)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         EnvironmentEndpoint var4 = (EnvironmentEndpoint)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   public $EnvironmentEndpoint$Definition() {
      this(EnvironmentEndpoint.class, $CONSTRUCTOR);
   }

   protected $EnvironmentEndpoint$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $EnvironmentEndpoint$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $EnvironmentEndpoint$Definition$Exec(),
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
