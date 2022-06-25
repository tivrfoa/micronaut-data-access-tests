package io.micronaut.http.client.filter;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationMetadataResolver;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultHttpClientFilterResolver$Definition
   extends AbstractInitializableBeanDefinition<DefaultHttpClientFilterResolver>
   implements BeanFactory<DefaultHttpClientFilterResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultHttpClientFilterResolver.class,
      "<init>",
      new Argument[]{
         Argument.of(AnnotationMetadataResolver.class, "annotationMetadataResolver"),
         Argument.of(List.class, "clientFilters", null, Argument.ofTypeVariable(HttpClientFilter.class, "E"))
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.http.filter.HttpClientFilterResolver",
      new Argument[]{Argument.of(ClientFilterResolutionContext.class, "T")},
      "io.micronaut.http.filter.HttpFilterResolver",
      new Argument[]{Argument.of(HttpClientFilter.class, "F"), Argument.of(ClientFilterResolutionContext.class, "T")}
   );

   @Override
   public DefaultHttpClientFilterResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultHttpClientFilterResolver var4 = new DefaultHttpClientFilterResolver(
         (AnnotationMetadataResolver)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (List<HttpClientFilter>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0], null
         )
      );
      return (DefaultHttpClientFilterResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultHttpClientFilterResolver var4 = (DefaultHttpClientFilterResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultHttpClientFilterResolver$Definition() {
      this(DefaultHttpClientFilterResolver.class, $CONSTRUCTOR);
   }

   protected $DefaultHttpClientFilterResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultHttpClientFilterResolver$Definition$Reference.$ANNOTATION_METADATA,
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
