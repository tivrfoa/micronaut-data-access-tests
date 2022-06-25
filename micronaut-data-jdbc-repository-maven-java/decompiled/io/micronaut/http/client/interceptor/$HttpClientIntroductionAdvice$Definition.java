package io.micronaut.http.client.interceptor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientRegistry;
import io.micronaut.http.client.ReactiveClientResultTransformer;
import io.micronaut.http.client.bind.HttpClientBinderRegistry;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.json.codec.JsonMediaTypeCodec;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HttpClientIntroductionAdvice$Definition
   extends AbstractInitializableBeanDefinition<HttpClientIntroductionAdvice>
   implements BeanFactory<HttpClientIntroductionAdvice> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HttpClientIntroductionAdvice.class,
      "<init>",
      new Argument[]{
         Argument.of(HttpClientRegistry.class, "clientFactory", null, Argument.ofTypeVariable(HttpClient.class, "T")),
         Argument.of(JsonMediaTypeCodec.class, "jsonMediaTypeCodec"),
         Argument.of(List.class, "transformers", null, Argument.ofTypeVariable(ReactiveClientResultTransformer.class, "E")),
         Argument.of(HttpClientBinderRegistry.class, "binderRegistry"),
         Argument.of(
            ConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               ConversionService.class, "Impl", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         )
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.aop.Interceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")},
      "io.micronaut.aop.MethodInterceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")}
   );

   @Override
   public HttpClientIntroductionAdvice build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HttpClientIntroductionAdvice var4 = new HttpClientIntroductionAdvice(
         (HttpClientRegistry<?>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (JsonMediaTypeCodec)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (List<ReactiveClientResultTransformer>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 2, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[2].getTypeParameters()[0], null
         ),
         (HttpClientBinderRegistry)super.getBeanForConstructorArgument(var1, var2, 3, null),
         (ConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 4, null)
      );
      return (HttpClientIntroductionAdvice)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      HttpClientIntroductionAdvice var4 = (HttpClientIntroductionAdvice)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $HttpClientIntroductionAdvice$Definition() {
      this(HttpClientIntroductionAdvice.class, $CONSTRUCTOR);
   }

   protected $HttpClientIntroductionAdvice$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HttpClientIntroductionAdvice$Definition$Reference.$ANNOTATION_METADATA,
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
