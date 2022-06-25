package io.micronaut.http.netty.channel;

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
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultEventLoopGroupConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DefaultEventLoopGroupConfiguration>
   implements BeanFactory<DefaultEventLoopGroupConfiguration>,
   ParametrizedBeanFactory<DefaultEventLoopGroupConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;

   @Override
   public DefaultEventLoopGroupConfiguration doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      DefaultEventLoopGroupConfiguration var5 = new DefaultEventLoopGroupConfiguration(
         (String)var4.get("name"),
         super.getPropertyValueForConstructorArgument(var1, var2, 1, "micronaut.netty.event-loops.*.num-threads", null),
         (Integer)super.getPropertyValueForConstructorArgument(var1, var2, 2, "micronaut.netty.event-loops.*.io-ratio", null),
         super.getPropertyValueForConstructorArgument(var1, var2, 3, "micronaut.netty.event-loops.*.prefer-native-transport", null),
         (String)super.getPropertyValueForConstructorArgument(var1, var2, 4, "micronaut.netty.event-loops.*.executor", null),
         (Duration)super.getPropertyValueForConstructorArgument(var1, var2, 5, "micronaut.netty.event-loops.*.shutdown-quiet-period", null),
         (Duration)super.getPropertyValueForConstructorArgument(var1, var2, 6, "micronaut.netty.event-loops.*.shutdown-timeout", null)
      );
      return (DefaultEventLoopGroupConfiguration)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultEventLoopGroupConfiguration var4 = (DefaultEventLoopGroupConfiguration)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         DefaultEventLoopGroupConfiguration.class,
         "<init>",
         new Argument[]{
            Argument.of(
               String.class,
               "name",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
                  AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
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
               Integer.TYPE,
               "numThreads",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property",
                              AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.num-threads"),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Property")
                           )
                        }
                     ),
                     "io.micronaut.core.bind.annotation.Bindable",
                     AnnotationUtil.mapOf("defaultValue", "0")
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.num-threads"), var0
                           )
                        }
                     ),
                     "io.micronaut.core.bind.annotation.Bindable",
                     AnnotationUtil.mapOf("defaultValue", "0")
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               Integer.class,
               "ioRatio",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.io-ratio"), var0
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
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.io-ratio"), var0
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
               Boolean.TYPE,
               "preferNativeTransport",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property",
                              AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.prefer-native-transport"),
                              var0
                           )
                        }
                     ),
                     "io.micronaut.core.bind.annotation.Bindable",
                     AnnotationUtil.mapOf("defaultValue", "false")
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property",
                              AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.prefer-native-transport"),
                              var0
                           )
                        }
                     ),
                     "io.micronaut.core.bind.annotation.Bindable",
                     AnnotationUtil.mapOf("defaultValue", "false")
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               String.class,
               "executor",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.executor"), var0
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
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.executor"), var0
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
               Duration.class,
               "shutdownQuietPeriod",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property",
                              AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.shutdown-quiet-period"),
                              var0
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
                              "io.micronaut.context.annotation.Property",
                              AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.shutdown-quiet-period"),
                              var0
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
               Duration.class,
               "shutdownTimeout",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.shutdown-timeout"), var0
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
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.netty.event-loops.*.shutdown-timeout"), var0
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

   public $DefaultEventLoopGroupConfiguration$Definition() {
      this(DefaultEventLoopGroupConfiguration.class, $CONSTRUCTOR);
   }

   protected $DefaultEventLoopGroupConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultEventLoopGroupConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
