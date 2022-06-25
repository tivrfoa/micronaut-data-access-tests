package io.micronaut.scheduling.executor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.ValidatedBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated
class $UserExecutorConfiguration$Definition
   extends AbstractInitializableBeanDefinition<UserExecutorConfiguration>
   implements BeanFactory<UserExecutorConfiguration>,
   ParametrizedBeanFactory<UserExecutorConfiguration>,
   ValidatedBeanDefinition<UserExecutorConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;

   @Override
   public UserExecutorConfiguration doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      UserExecutorConfiguration var5 = new UserExecutorConfiguration(
         (String)var4.get("name"),
         (Integer)super.getPropertyValueForConstructorArgument(var1, var2, 1, "micronaut.executors.*.n-threads", null),
         (ExecutorType)super.getPropertyValueForConstructorArgument(var1, var2, 2, "micronaut.executors.*.type", null),
         (Integer)super.getPropertyValueForConstructorArgument(var1, var2, 3, "micronaut.executors.*.parallelism", null),
         (Integer)super.getPropertyValueForConstructorArgument(var1, var2, 4, "micronaut.executors.*.core-pool-size", null),
         (Class<? extends ThreadFactory>)super.getPropertyValueForConstructorArgument(var1, var2, 5, "micronaut.executors.*.thread-factory-class", null)
      );
      return (UserExecutorConfiguration)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         UserExecutorConfiguration var4 = (UserExecutorConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.executors.*.n-threads")) {
            var4.nThreads = (Integer)super.getPropertyValueForField(var1, var2, Argument.of(Integer.class, "nThreads"), "micronaut.executors.*.n-threads", null);
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.executors.*.name")) {
            var4.setName((String)super.getPropertyValueForSetter(var1, var2, "setName", Argument.of(String.class, "name"), "micronaut.executors.*.name", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.executors.*.type")) {
            var4.setType(
               (ExecutorType)super.getPropertyValueForSetter(var1, var2, "setType", Argument.of(ExecutorType.class, "type"), "micronaut.executors.*.type", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.executors.*.parallelism")) {
            var4.setParallelism(
               (Integer)super.getPropertyValueForSetter(
                  var1, var2, "setParallelism", Argument.of(Integer.class, "parallelism"), "micronaut.executors.*.parallelism", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.executors.*.number-of-threads")) {
            var4.setNumberOfThreads(
               (Integer)super.getPropertyValueForSetter(
                  var1, var2, "setNumberOfThreads", Argument.of(Integer.class, "nThreads"), "micronaut.executors.*.number-of-threads", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.executors.*.core-pool-size")) {
            var4.setCorePoolSize(
               (Integer)super.getPropertyValueForSetter(
                  var1, var2, "setCorePoolSize", Argument.of(Integer.class, "corePoolSize"), "micronaut.executors.*.core-pool-size", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.executors.*.thread-factory-class")) {
            var4.setThreadFactoryClass(
               (Class<? extends ThreadFactory>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setThreadFactoryClass",
                  Argument.of(Class.class, "threadFactoryClass", null, Argument.ofTypeVariable(ThreadFactory.class, "T")),
                  "micronaut.executors.*.thread-factory-class",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         UserExecutorConfiguration.class,
         "<init>",
         new Argument[]{
            Argument.of(
               String.class,
               "name",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP, "javax.annotation.Nullable", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP, "javax.annotation.Nullable", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf(
                     "io.micronaut.core.bind.annotation.Bindable",
                     AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                     "javax.inject.Qualifier",
                     AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
                  ),
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               Integer.class,
               "nThreads",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property",
                              AnnotationUtil.mapOf("name", "micronaut.executors.*.n-threads"),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Property")
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.executors.*.n-threads"), var0
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               ExecutorType.class,
               "type",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.executors.*.type"), var0)
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.executors.*.type"), var0)
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               Integer.class,
               "parallelism",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.executors.*.parallelism"), var0
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.executors.*.parallelism"), var0
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               Integer.class,
               "corePoolSize",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.executors.*.core-pool-size"), var0
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.executors.*.core-pool-size"), var0
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               Class.class,
               "threadFactoryClass",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.executors.*.thread-factory-class"), var0
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.executors.*.thread-factory-class"), var0
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               Argument.ofTypeVariable(ThreadFactory.class, "T")
            )
         },
         new DefaultAnnotationMetadata(
            AnnotationUtil.internMapOf("io.micronaut.context.annotation.ConfigurationInject", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("io.micronaut.core.annotation.Creator", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("io.micronaut.core.annotation.Creator", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("io.micronaut.context.annotation.ConfigurationInject", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("io.micronaut.core.annotation.Creator", AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationInject")),
            false,
            true
         ),
         false
      );
   }

   public $UserExecutorConfiguration$Definition() {
      this(UserExecutorConfiguration.class, $CONSTRUCTOR);
   }

   protected $UserExecutorConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $UserExecutorConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
