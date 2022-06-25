package io.micronaut.management.endpoint;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.management.endpoint.annotation.Endpoint;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $EndpointSensitivityProcessor$Definition
   extends AbstractInitializableBeanDefinition<EndpointSensitivityProcessor>
   implements BeanFactory<EndpointSensitivityProcessor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      EndpointSensitivityProcessor.class,
      "<init>",
      new Argument[]{
         Argument.of(List.class, "endpointConfigurations", null, Argument.ofTypeVariable(EndpointConfiguration.class, "E")),
         Argument.of(EndpointDefaultConfiguration.class, "defaultConfiguration"),
         Argument.of(PropertyResolver.class, "propertyResolver")
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
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.processor.AnnotationProcessor",
      new Argument[]{
         Argument.of(Endpoint.class, "A"),
         Argument.of(ExecutableMethod.class, "T", null, Argument.ofTypeVariable(Object.class, "T"), Argument.ofTypeVariable(Object.class, "R"))
      },
      "io.micronaut.context.processor.ExecutableMethodProcessor",
      new Argument[]{Argument.of(Endpoint.class, "A")}
   );

   @Override
   public EndpointSensitivityProcessor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      EndpointSensitivityProcessor var4 = new EndpointSensitivityProcessor(
         (List<EndpointConfiguration>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
         ),
         (EndpointDefaultConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (PropertyResolver)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (EndpointSensitivityProcessor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      EndpointSensitivityProcessor var4 = (EndpointSensitivityProcessor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $EndpointSensitivityProcessor$Definition() {
      this(EndpointSensitivityProcessor.class, $CONSTRUCTOR);
   }

   protected $EndpointSensitivityProcessor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $EndpointSensitivityProcessor$Definition$Reference.$ANNOTATION_METADATA,
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
}
