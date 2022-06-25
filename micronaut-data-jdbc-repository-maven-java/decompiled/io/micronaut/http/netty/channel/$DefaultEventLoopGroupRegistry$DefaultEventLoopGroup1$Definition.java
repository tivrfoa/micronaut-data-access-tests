package io.micronaut.http.netty.channel;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated
class $DefaultEventLoopGroupRegistry$DefaultEventLoopGroup1$Definition
   extends AbstractInitializableBeanDefinition<EventLoopGroup>
   implements BeanFactory<EventLoopGroup> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf("java.lang.Iterable", new Argument[]{Argument.of(EventExecutor.class, "T")});
   private static final Set $EXPOSED_TYPES = Collections.singleton(EventLoopGroup.class);

   @Override
   public EventLoopGroup build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, DefaultEventLoopGroupRegistry.class, null);
      var1.markDependentAsFactory();
      EventLoopGroup var5 = ((DefaultEventLoopGroupRegistry)var4)
         .defaultEventLoopGroup((ThreadFactory)super.getBeanForConstructorArgument(var1, var2, 0, Qualifiers.byName("netty")));
      return (EventLoopGroup)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      EventLoopGroup var4 = (EventLoopGroup)var3;
      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         DefaultEventLoopGroupRegistry.class,
         "defaultEventLoopGroup",
         new Argument[]{
            Argument.of(
               ThreadFactory.class,
               "threadFactory",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "netty")),
                  AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
                  AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "netty")),
                  AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
                  false,
                  true
               ),
               null
            )
         },
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Primary",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("missingProperty", "micronaut.netty.event-loops.default"),
                        var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                     )
                  }
               ),
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               AnnotationUtil.mapOf("typed", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
               "io.micronaut.context.annotation.BootstrapContextCompatible",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Primary",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("missingProperty", "micronaut.netty.event-loops.default"), var0
                     )
                  }
               ),
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "javax.inject.Qualifier",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary"),
               "javax.inject.Scope",
               AnnotationUtil.internListOf("javax.inject.Singleton")
            ),
            false,
            true
         ),
         false
      );
   }

   public $DefaultEventLoopGroupRegistry$DefaultEventLoopGroup1$Definition() {
      this(EventLoopGroup.class, $CONSTRUCTOR);
   }

   protected $DefaultEventLoopGroupRegistry$DefaultEventLoopGroup1$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultEventLoopGroupRegistry$DefaultEventLoopGroup1$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false,
         false
      );
   }

   @Override
   public Set getExposedTypes() {
      return $EXPOSED_TYPES;
   }
}
