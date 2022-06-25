package io.micronaut.validation.validator.resolver;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.List;
import java.util.Optional;
import javax.validation.TraversableResolver;

// $FF: synthetic class
@Generated
class $CompositeTraversableResolver$Definition
   extends AbstractInitializableBeanDefinition<CompositeTraversableResolver>
   implements BeanFactory<CompositeTraversableResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      CompositeTraversableResolver.class,
      "<init>",
      new Argument[]{Argument.of(List.class, "traversableResolvers", null, Argument.ofTypeVariable(TraversableResolver.class, "E"))},
      null,
      false
   );

   @Override
   public CompositeTraversableResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      CompositeTraversableResolver var4 = new CompositeTraversableResolver(
         (List<TraversableResolver>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
         )
      );
      return (CompositeTraversableResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      CompositeTraversableResolver var4 = (CompositeTraversableResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $CompositeTraversableResolver$Definition() {
      this(CompositeTraversableResolver.class, $CONSTRUCTOR);
   }

   protected $CompositeTraversableResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $CompositeTraversableResolver$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         true,
         false,
         false,
         false
      );
   }
}
