package io.micronaut.http.server.netty.binders;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.server.netty.HttpContentProcessorResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;
import org.reactivestreams.Publisher;

// $FF: synthetic class
@Generated
class $PublisherBodyBinder$Definition extends AbstractInitializableBeanDefinition<PublisherBodyBinder> implements BeanFactory<PublisherBodyBinder> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      PublisherBodyBinder.class,
      "<init>",
      new Argument[]{
         Argument.of(
            ConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               ConversionService.class, "Impl", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         ),
         Argument.of(HttpContentProcessorResolver.class, "httpContentProcessorResolver")
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.bind.ArgumentBinder",
      new Argument[]{
         Argument.of(Publisher.class, "T", null, Argument.ofTypeVariable(Object.class, "T")),
         Argument.of(HttpRequest.class, "S", null, Argument.ofTypeVariable(Object.class, "B"))
      },
      "io.micronaut.core.bind.TypeArgumentBinder",
      new Argument[]{
         Argument.of(Publisher.class, "T", null, Argument.ofTypeVariable(Object.class, "T")),
         Argument.of(HttpRequest.class, "S", null, Argument.ofTypeVariable(Object.class, "B"))
      },
      "io.micronaut.core.bind.annotation.AnnotatedArgumentBinder",
      new Argument[]{
         Argument.of(Body.class, "A"),
         Argument.of(Publisher.class, "T", null, Argument.ofTypeVariable(Object.class, "T")),
         Argument.of(HttpRequest.class, "S", null, Argument.ofTypeVariable(Object.class, "B"))
      },
      "io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder",
      new Argument[]{Argument.of(Body.class, "A"), Argument.of(Publisher.class, "T", null, Argument.ofTypeVariable(Object.class, "T"))},
      "io.micronaut.http.bind.binders.BodyArgumentBinder",
      new Argument[]{Argument.of(Publisher.class, "T", null, Argument.ofTypeVariable(Object.class, "T"))},
      "io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder",
      new Argument[]{Argument.of(Publisher.class, "T", null, Argument.ofTypeVariable(Object.class, "T"))},
      "io.micronaut.http.bind.binders.NonBlockingBodyArgumentBinder",
      new Argument[]{Argument.of(Publisher.class, "T", null, Argument.ofTypeVariable(Object.class, "T"))},
      "io.micronaut.http.bind.binders.RequestArgumentBinder",
      new Argument[]{Argument.of(Publisher.class, "T", null, Argument.ofTypeVariable(Object.class, "T"))},
      "io.micronaut.http.bind.binders.TypedRequestArgumentBinder",
      new Argument[]{Argument.of(Publisher.class, "T", null, Argument.ofTypeVariable(Object.class, "T"))}
   );

   @Override
   public PublisherBodyBinder build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      PublisherBodyBinder var4 = new PublisherBodyBinder(
         (ConversionService)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (HttpContentProcessorResolver)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (PublisherBodyBinder)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      PublisherBodyBinder var4 = (PublisherBodyBinder)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $PublisherBodyBinder$Definition() {
      this(PublisherBodyBinder.class, $CONSTRUCTOR);
   }

   protected $PublisherBodyBinder$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $PublisherBodyBinder$Definition$Reference.$ANNOTATION_METADATA,
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
