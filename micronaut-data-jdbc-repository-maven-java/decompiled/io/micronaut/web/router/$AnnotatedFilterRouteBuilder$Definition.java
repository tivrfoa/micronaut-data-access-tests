package io.micronaut.web.router;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.context.ServerContextPathProvider;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $AnnotatedFilterRouteBuilder$Definition
   extends AbstractInitializableBeanDefinition<AnnotatedFilterRouteBuilder>
   implements BeanFactory<AnnotatedFilterRouteBuilder> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      AnnotatedFilterRouteBuilder.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanContext.class, "beanContext"),
         Argument.of(ExecutionHandleLocator.class, "executionHandleLocator"),
         Argument.of(RouteBuilder.UriNamingStrategy.class, "uriNamingStrategy"),
         Argument.of(
            ConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               ConversionService.class, "Impl", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         ),
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
      new Argument[]{Argument.of(Filter.class, "A"), Argument.of(BeanContext.class, "T")},
      "io.micronaut.context.processor.BeanDefinitionProcessor",
      new Argument[]{Argument.of(Filter.class, "A")}
   );

   @Override
   public AnnotatedFilterRouteBuilder build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      AnnotatedFilterRouteBuilder var4 = new AnnotatedFilterRouteBuilder(
         var2,
         (ExecutionHandleLocator)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (RouteBuilder.UriNamingStrategy)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (ConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 3, null),
         (ServerContextPathProvider)super.getBeanForConstructorArgument(var1, var2, 4, null)
      );
      return (AnnotatedFilterRouteBuilder)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      AnnotatedFilterRouteBuilder var4 = (AnnotatedFilterRouteBuilder)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $AnnotatedFilterRouteBuilder$Definition() {
      this(AnnotatedFilterRouteBuilder.class, $CONSTRUCTOR);
   }

   protected $AnnotatedFilterRouteBuilder$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $AnnotatedFilterRouteBuilder$Definition$Reference.$ANNOTATION_METADATA,
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
