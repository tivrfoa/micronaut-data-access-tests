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
import io.micronaut.web.router.exceptions.DuplicateRouteException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DuplicateRouteHandler$Definition extends AbstractInitializableBeanDefinition<DuplicateRouteHandler> implements BeanFactory<DuplicateRouteHandler> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DuplicateRouteHandler.class,
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
      new Argument[]{Argument.of(DuplicateRouteException.class, "T"), Argument.of(HttpResponse.class, "R", null, Argument.ofTypeVariable(Object.class, "B"))}
   );

   @Override
   public DuplicateRouteHandler build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DuplicateRouteHandler var4 = new DuplicateRouteHandler((ErrorResponseProcessor<?>)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (DuplicateRouteHandler)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DuplicateRouteHandler var4 = (DuplicateRouteHandler)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DuplicateRouteHandler$Definition() {
      this(DuplicateRouteHandler.class, $CONSTRUCTOR);
   }

   protected $DuplicateRouteHandler$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DuplicateRouteHandler$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $DuplicateRouteHandler$Definition$Exec(),
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
