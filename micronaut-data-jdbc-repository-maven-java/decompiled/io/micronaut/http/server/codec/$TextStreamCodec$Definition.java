package io.micronaut.http.server.codec;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.io.buffer.ByteBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.http.codec.CodecConfiguration;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.ApplicationConfiguration;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $TextStreamCodec$Definition extends AbstractInitializableBeanDefinition<TextStreamCodec> implements BeanFactory<TextStreamCodec> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      TextStreamCodec.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationConfiguration.class, "applicationConfiguration"),
         Argument.of(ByteBufferFactory.class, "byteBufferFactory", null, Argument.ofTypeVariable(Object.class, "T"), Argument.ofTypeVariable(Object.class, "B")),
         Argument.of(BeanProvider.class, "codecRegistryProvider", null, Argument.ofTypeVariable(MediaTypeCodecRegistry.class, "T")),
         Argument.of(
            CodecConfiguration.class,
            "codecConfiguration",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP, "javax.inject.Named", AnnotationUtil.mapOf("value", "text-stream")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP, "javax.inject.Named", AnnotationUtil.mapOf("value", "text-stream")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
               false,
               true
            ),
            null
         )
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

   @Override
   public TextStreamCodec build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      TextStreamCodec var4 = new TextStreamCodec(
         (ApplicationConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (ByteBufferFactory)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (BeanProvider<MediaTypeCodecRegistry>)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (CodecConfiguration)super.getBeanForConstructorArgument(var1, var2, 3, Qualifiers.byName("text-stream"))
      );
      return (TextStreamCodec)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      TextStreamCodec var4 = (TextStreamCodec)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $TextStreamCodec$Definition() {
      this(TextStreamCodec.class, $CONSTRUCTOR);
   }

   protected $TextStreamCodec$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $TextStreamCodec$Definition$Reference.$ANNOTATION_METADATA,
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
