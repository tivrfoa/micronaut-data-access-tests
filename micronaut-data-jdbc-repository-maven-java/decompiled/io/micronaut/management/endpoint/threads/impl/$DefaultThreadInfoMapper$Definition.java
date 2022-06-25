package io.micronaut.management.endpoint.threads.impl;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.lang.management.ThreadInfo;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultThreadInfoMapper$Definition extends AbstractInitializableBeanDefinition<DefaultThreadInfoMapper> implements BeanFactory<DefaultThreadInfoMapper> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultThreadInfoMapper.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.management.endpoint.threads.ThreadInfoMapper", new Argument[]{Argument.of(ThreadInfo.class, "T")}
   );

   @Override
   public DefaultThreadInfoMapper build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultThreadInfoMapper var4 = new DefaultThreadInfoMapper();
      return (DefaultThreadInfoMapper)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultThreadInfoMapper var4 = (DefaultThreadInfoMapper)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultThreadInfoMapper$Definition() {
      this(DefaultThreadInfoMapper.class, $CONSTRUCTOR);
   }

   protected $DefaultThreadInfoMapper$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultThreadInfoMapper$Definition$Reference.$ANNOTATION_METADATA,
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
