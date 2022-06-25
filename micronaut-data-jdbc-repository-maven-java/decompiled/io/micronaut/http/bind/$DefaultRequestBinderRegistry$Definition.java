package io.micronaut.http.bind;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultRequestBinderRegistry$Definition
   extends AbstractInitializableBeanDefinition<DefaultRequestBinderRegistry>
   implements BeanFactory<DefaultRequestBinderRegistry> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultRequestBinderRegistry.class,
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
         Argument.of(List.class, "binders", null, Argument.ofTypeVariable(RequestArgumentBinder.class, "E", null, Argument.ofTypeVariable(Object.class, "T")))
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
      "io.micronaut.core.bind.ArgumentBinderRegistry", new Argument[]{Argument.of(HttpRequest.class, "S", null, Argument.ofTypeVariable(Object.class, "B"))}
   );

   @Override
   public DefaultRequestBinderRegistry build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultRequestBinderRegistry var4 = new DefaultRequestBinderRegistry(
         (ConversionService)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (List<RequestArgumentBinder>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0], null
         )
      );
      return (DefaultRequestBinderRegistry)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultRequestBinderRegistry var4 = (DefaultRequestBinderRegistry)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultRequestBinderRegistry$Definition() {
      this(DefaultRequestBinderRegistry.class, $CONSTRUCTOR);
   }

   protected $DefaultRequestBinderRegistry$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultRequestBinderRegistry$Definition$Reference.$ANNOTATION_METADATA,
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
