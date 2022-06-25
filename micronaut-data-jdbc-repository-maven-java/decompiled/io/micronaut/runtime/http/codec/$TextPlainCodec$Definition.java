package io.micronaut.runtime.http.codec;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.codec.CodecConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $TextPlainCodec$Definition extends AbstractInitializableBeanDefinition<TextPlainCodec> implements BeanFactory<TextPlainCodec> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      TextPlainCodec.class,
      "<init>",
      new Argument[]{
         Argument.of(
            Optional.class,
            "defaultCharset",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Value", AnnotationUtil.mapOf("value", "${micronaut.application.default-charset}")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Value", AnnotationUtil.mapOf("value", "${micronaut.application.default-charset}")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("io.micronaut.context.annotation.Value")),
               true,
               true
            ),
            Argument.ofTypeVariable(Charset.class, "T")
         ),
         Argument.of(
            CodecConfiguration.class,
            "codecConfiguration",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP, "javax.inject.Named", AnnotationUtil.mapOf("value", "text")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP, "javax.inject.Named", AnnotationUtil.mapOf("value", "text")),
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
   public TextPlainCodec build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      TextPlainCodec var4 = new TextPlainCodec(
         (Optional<Charset>)super.getPropertyPlaceholderValueForConstructorArgument(var1, var2, 0, "${micronaut.application.default-charset}"),
         (CodecConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, Qualifiers.byName("text"))
      );
      return (TextPlainCodec)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      TextPlainCodec var4 = (TextPlainCodec)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $TextPlainCodec$Definition() {
      this(TextPlainCodec.class, $CONSTRUCTOR);
   }

   protected $TextPlainCodec$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $TextPlainCodec$Definition$Reference.$ANNOTATION_METADATA,
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
