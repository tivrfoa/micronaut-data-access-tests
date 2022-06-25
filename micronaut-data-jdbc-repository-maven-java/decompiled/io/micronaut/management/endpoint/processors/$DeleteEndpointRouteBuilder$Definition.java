package io.micronaut.management.endpoint.processors;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.management.endpoint.EndpointDefaultConfiguration;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.web.router.RouteBuilder;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DeleteEndpointRouteBuilder$Definition
   extends AbstractInitializableBeanDefinition<DeleteEndpointRouteBuilder>
   implements BeanFactory<DeleteEndpointRouteBuilder> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DeleteEndpointRouteBuilder.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationContext.class, "beanContext"),
         Argument.of(RouteBuilder.UriNamingStrategy.class, "uriNamingStrategy"),
         Argument.of(
            ConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               ConversionService.class, "Impl", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         ),
         Argument.of(EndpointDefaultConfiguration.class, "endpointDefaultConfiguration")
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.LifeCycle",
      new Argument[]{Argument.of(AbstractEndpointRouteBuilder.class, "T")},
      "io.micronaut.context.processor.AnnotationProcessor",
      new Argument[]{
         Argument.of(Endpoint.class, "A"),
         Argument.of(ExecutableMethod.class, "T", null, Argument.ofTypeVariable(Object.class, "T"), Argument.ofTypeVariable(Object.class, "R"))
      },
      "io.micronaut.context.processor.ExecutableMethodProcessor",
      new Argument[]{Argument.of(Endpoint.class, "A")}
   );

   @Override
   public DeleteEndpointRouteBuilder build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DeleteEndpointRouteBuilder var4 = new DeleteEndpointRouteBuilder(
         var2,
         (RouteBuilder.UriNamingStrategy)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (ConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (EndpointDefaultConfiguration)super.getBeanForConstructorArgument(var1, var2, 3, null)
      );
      return (DeleteEndpointRouteBuilder)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DeleteEndpointRouteBuilder var4 = (DeleteEndpointRouteBuilder)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DeleteEndpointRouteBuilder$Definition() {
      this(DeleteEndpointRouteBuilder.class, $CONSTRUCTOR);
   }

   protected $DeleteEndpointRouteBuilder$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DeleteEndpointRouteBuilder$Definition$Reference.$ANNOTATION_METADATA,
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
