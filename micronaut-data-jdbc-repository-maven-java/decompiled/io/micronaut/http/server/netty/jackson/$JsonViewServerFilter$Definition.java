package io.micronaut.http.server.netty.jackson;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

// $FF: synthetic class
@Generated
class $JsonViewServerFilter$Definition extends AbstractInitializableBeanDefinition<JsonViewServerFilter> implements BeanFactory<JsonViewServerFilter> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JsonViewServerFilter.class,
      "<init>",
      new Argument[]{
         Argument.of(JsonViewCodecResolver.class, "jsonViewCodecResolver"),
         Argument.of(
            ExecutorService.class,
            "executorService",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
               false,
               true
            ),
            null
         )
      },
      null,
      false
   );

   @Override
   public JsonViewServerFilter build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JsonViewServerFilter var4 = new JsonViewServerFilter(
         (JsonViewCodecResolver)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (ExecutorService)super.getBeanForConstructorArgument(var1, var2, 1, Qualifiers.byName("io"))
      );
      return (JsonViewServerFilter)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JsonViewServerFilter var4 = (JsonViewServerFilter)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JsonViewServerFilter$Definition() {
      this(JsonViewServerFilter.class, $CONSTRUCTOR);
   }

   protected $JsonViewServerFilter$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JsonViewServerFilter$Definition$Reference.$ANNOTATION_METADATA,
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
         false,
         false,
         false,
         false
      );
   }
}
