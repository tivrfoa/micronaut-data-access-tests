package io.micronaut.http.server.exceptions;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $URISyntaxHandler$Definition extends AbstractInitializableBeanDefinition<URISyntaxHandler> implements BeanFactory<URISyntaxHandler> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      URISyntaxHandler.class,
      "<init>",
      new Argument[]{Argument.of(ErrorResponseProcessor.class, "responseProcessor", null, Argument.ofTypeVariable(Object.class, "T"))},
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
      "io.micronaut.http.server.exceptions.ExceptionHandler",
      new Argument[]{Argument.of(URISyntaxException.class, "T"), Argument.of(HttpResponse.class, "R", null, Argument.ofTypeVariable(Object.class, "B"))}
   );

   @Override
   public URISyntaxHandler build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      URISyntaxHandler var4 = new URISyntaxHandler((ErrorResponseProcessor<?>)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (URISyntaxHandler)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      URISyntaxHandler var4 = (URISyntaxHandler)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $URISyntaxHandler$Definition() {
      this(URISyntaxHandler.class, $CONSTRUCTOR);
   }

   protected $URISyntaxHandler$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $URISyntaxHandler$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $URISyntaxHandler$Definition$Exec(),
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
