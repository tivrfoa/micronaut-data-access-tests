package io.micronaut.http.server.exceptions.response;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.json.JsonConfiguration;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HateoasErrorResponseProcessor$Definition
   extends AbstractInitializableBeanDefinition<HateoasErrorResponseProcessor>
   implements BeanFactory<HateoasErrorResponseProcessor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HateoasErrorResponseProcessor.class, "<init>", new Argument[]{Argument.of(JsonConfiguration.class, "jacksonConfiguration")}, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.http.server.exceptions.response.ErrorResponseProcessor", new Argument[]{Argument.of(JsonError.class, "T")}
   );

   @Override
   public HateoasErrorResponseProcessor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HateoasErrorResponseProcessor var4 = new HateoasErrorResponseProcessor((JsonConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (HateoasErrorResponseProcessor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      HateoasErrorResponseProcessor var4 = (HateoasErrorResponseProcessor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $HateoasErrorResponseProcessor$Definition() {
      this(HateoasErrorResponseProcessor.class, $CONSTRUCTOR);
   }

   protected $HateoasErrorResponseProcessor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HateoasErrorResponseProcessor$Definition$Reference.$ANNOTATION_METADATA,
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
