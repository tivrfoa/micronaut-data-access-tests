package io.micronaut.transaction.test;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.test.annotation.TransactionMode;
import io.micronaut.transaction.SynchronousTransactionManager;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultTestTransactionExecutionListener$Definition
   extends AbstractInitializableBeanDefinition<DefaultTestTransactionExecutionListener>
   implements BeanFactory<DefaultTestTransactionExecutionListener> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;

   @Override
   public DefaultTestTransactionExecutionListener build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultTestTransactionExecutionListener var4 = new DefaultTestTransactionExecutionListener(
         (SynchronousTransactionManager<Object>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         super.getPropertyValueForConstructorArgument(var1, var2, 1, "micronaut.test.rollback", null),
         (TransactionMode)super.getPropertyValueForConstructorArgument(var1, var2, 2, "micronaut.test.transaction-mode", null)
      );
      return (DefaultTestTransactionExecutionListener)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultTestTransactionExecutionListener var4 = (DefaultTestTransactionExecutionListener)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         DefaultTestTransactionExecutionListener.class,
         "<init>",
         new Argument[]{
            Argument.of(SynchronousTransactionManager.class, "transactionManager", null, Argument.ofTypeVariable(Object.class, "T")),
            Argument.of(
               Boolean.TYPE,
               "rollback",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property",
                              AnnotationUtil.mapOf("defaultValue", "true", "name", "micronaut.test.rollback"),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Property")
                           )
                        }
                     )
                  ),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", AnnotationUtil.mapOf("defaultValue", "true")),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", AnnotationUtil.mapOf("defaultValue", "true")),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("defaultValue", "true", "name", "micronaut.test.rollback"), var0
                           )
                        }
                     )
                  ),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_LIST),
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               TransactionMode.class,
               "transactionMode",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property",
                              AnnotationUtil.mapOf("defaultValue", "SEPARATE_TRANSACTIONS", "name", "micronaut.test.transaction-mode"),
                              var0
                           )
                        }
                     )
                  ),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", AnnotationUtil.mapOf("defaultValue", "SEPARATE_TRANSACTIONS")),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", AnnotationUtil.mapOf("defaultValue", "SEPARATE_TRANSACTIONS")),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property",
                              AnnotationUtil.mapOf("defaultValue", "SEPARATE_TRANSACTIONS", "name", "micronaut.test.transaction-mode"),
                              var0
                           )
                        }
                     )
                  ),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_LIST),
                  false,
                  true
               ),
               null
            )
         },
         null,
         false
      );
   }

   public $DefaultTestTransactionExecutionListener$Definition() {
      this(DefaultTestTransactionExecutionListener.class, $CONSTRUCTOR);
   }

   protected $DefaultTestTransactionExecutionListener$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultTestTransactionExecutionListener$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         true,
         true,
         false,
         true,
         false,
         false
      );
   }
}
