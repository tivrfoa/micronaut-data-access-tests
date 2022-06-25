package io.micronaut.management.endpoint.beans.impl;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.management.endpoint.beans.BeanDefinitionData;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultBeanDefinitionDataCollector$Definition
   extends AbstractInitializableBeanDefinition<DefaultBeanDefinitionDataCollector>
   implements BeanFactory<DefaultBeanDefinitionDataCollector> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultBeanDefinitionDataCollector.class,
      "<init>",
      new Argument[]{Argument.of(BeanDefinitionData.class, "beanDefinitionData", null, Argument.ofTypeVariable(Object.class, "T"))},
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.management.endpoint.beans.BeanDefinitionDataCollector",
      new Argument[]{Argument.of(Map.class, "T", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V"))}
   );

   @Override
   public DefaultBeanDefinitionDataCollector build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultBeanDefinitionDataCollector var4 = new DefaultBeanDefinitionDataCollector(
         (BeanDefinitionData)super.getBeanForConstructorArgument(var1, var2, 0, null)
      );
      return (DefaultBeanDefinitionDataCollector)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultBeanDefinitionDataCollector var4 = (DefaultBeanDefinitionDataCollector)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultBeanDefinitionDataCollector$Definition() {
      this(DefaultBeanDefinitionDataCollector.class, $CONSTRUCTOR);
   }

   protected $DefaultBeanDefinitionDataCollector$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultBeanDefinitionDataCollector$Definition$Reference.$ANNOTATION_METADATA,
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
