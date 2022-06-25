package io.micronaut.transaction.jdbc;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.sql.Connection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;

// $FF: synthetic class
@Generated
class $TransactionalConnectionInterceptor$Definition
   extends AbstractInitializableBeanDefinition<TransactionalConnectionInterceptor>
   implements BeanFactory<TransactionalConnectionInterceptor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      TransactionalConnectionInterceptor.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanContext.class, "beanContext"), Argument.of(Qualifier.class, "qualifier", null, Argument.ofTypeVariable(DataSource.class, "T"))
      },
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.aop.Interceptor",
      new Argument[]{Argument.of(Connection.class, "T"), Argument.of(Object.class, "R")},
      "io.micronaut.aop.MethodInterceptor",
      new Argument[]{Argument.of(Connection.class, "T"), Argument.of(Object.class, "R")}
   );

   @Override
   public TransactionalConnectionInterceptor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      TransactionalConnectionInterceptor var4 = new TransactionalConnectionInterceptor(
         var2, (Qualifier<DataSource>)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (TransactionalConnectionInterceptor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      TransactionalConnectionInterceptor var4 = (TransactionalConnectionInterceptor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $TransactionalConnectionInterceptor$Definition() {
      this(TransactionalConnectionInterceptor.class, $CONSTRUCTOR);
   }

   protected $TransactionalConnectionInterceptor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $TransactionalConnectionInterceptor$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.of("io.micronaut.context.annotation.Prototype"),
         false,
         false,
         false,
         false,
         false,
         false,
         false,
         false
      );
   }
}
