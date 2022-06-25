package io.micronaut.jackson.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.codec.CodecConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.ApplicationConfiguration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $JsonStreamMediaTypeCodec$Definition
   extends AbstractInitializableBeanDefinition<JsonStreamMediaTypeCodec>
   implements BeanFactory<JsonStreamMediaTypeCodec> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JsonStreamMediaTypeCodec.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanProvider.class, "objectMapper", null, Argument.ofTypeVariable(ObjectMapper.class, "T")),
         Argument.of(ApplicationConfiguration.class, "applicationConfiguration"),
         Argument.of(
            CodecConfiguration.class,
            "codecConfiguration",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP, "javax.inject.Named", AnnotationUtil.mapOf("value", "json-stream")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP, "javax.inject.Named", AnnotationUtil.mapOf("value", "json-stream")),
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
   private static final Set $EXPOSED_TYPES = new HashSet(Arrays.asList(JsonStreamMediaTypeCodec.class, JacksonMediaTypeCodec.class));

   @Override
   public JsonStreamMediaTypeCodec build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JsonStreamMediaTypeCodec var4 = new JsonStreamMediaTypeCodec(
         (BeanProvider<ObjectMapper>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (ApplicationConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (CodecConfiguration)super.getBeanForConstructorArgument(var1, var2, 2, Qualifiers.byName("json-stream"))
      );
      return (JsonStreamMediaTypeCodec)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JsonStreamMediaTypeCodec var4 = (JsonStreamMediaTypeCodec)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JsonStreamMediaTypeCodec$Definition() {
      this(JsonStreamMediaTypeCodec.class, $CONSTRUCTOR);
   }

   protected $JsonStreamMediaTypeCodec$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JsonStreamMediaTypeCodec$Definition$Reference.$ANNOTATION_METADATA,
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

   @Override
   public Set getExposedTypes() {
      return $EXPOSED_TYPES;
   }
}
