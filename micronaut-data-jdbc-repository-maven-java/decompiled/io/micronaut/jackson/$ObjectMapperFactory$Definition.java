package io.micronaut.jackson;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ObjectMapperFactory$Definition extends AbstractInitializableBeanDefinition<ObjectMapperFactory> implements BeanFactory<ObjectMapperFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ObjectMapperFactory.class, "<init>", null, null, false
   );
   private static final AbstractInitializableBeanDefinition.FieldReference[] $INJECTION_FIELDS = new AbstractInitializableBeanDefinition.FieldReference[]{
      new AbstractInitializableBeanDefinition.FieldReference(
         ObjectMapperFactory.class,
         Argument.of(
            Module[].class,
            "jacksonModules",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.FieldReference(
         ObjectMapperFactory.class,
         Argument.of(
            JsonSerializer[].class,
            "serializers",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            Argument.ofTypeVariable(Object.class, "T")
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.FieldReference(
         ObjectMapperFactory.class,
         Argument.of(
            JsonDeserializer[].class,
            "deserializers",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            Argument.ofTypeVariable(Object.class, "T")
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.FieldReference(
         ObjectMapperFactory.class,
         Argument.of(
            BeanSerializerModifier[].class,
            "beanSerializerModifiers",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.FieldReference(
         ObjectMapperFactory.class,
         Argument.of(
            BeanDeserializerModifier[].class,
            "beanDeserializerModifiers",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         ),
         false
      ),
      new AbstractInitializableBeanDefinition.FieldReference(
         ObjectMapperFactory.class,
         Argument.of(
            KeyDeserializer[].class,
            "keyDeserializers",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         ),
         false
      )
   };

   @Override
   public ObjectMapperFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ObjectMapperFactory var4 = new ObjectMapperFactory();
      return (ObjectMapperFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ObjectMapperFactory var4 = (ObjectMapperFactory)var3;
      var4.jacksonModules = (Module[])((Collection)super.getBeansOfTypeForField(var1, var2, 0, Argument.of(Module.class, null), null)).toArray(new Module[0]);
      var4.serializers = (JsonSerializer[])((Collection)super.getBeansOfTypeForField(var1, var2, 1, Argument.of(JsonSerializer.class, null), null))
         .toArray(new JsonSerializer[0]);
      var4.deserializers = (JsonDeserializer[])((Collection)super.getBeansOfTypeForField(var1, var2, 2, Argument.of(JsonDeserializer.class, null), null))
         .toArray(new JsonDeserializer[0]);
      var4.beanSerializerModifiers = (BeanSerializerModifier[])((Collection)super.getBeansOfTypeForField(
            var1, var2, 3, Argument.of(BeanSerializerModifier.class, null), null
         ))
         .toArray(new BeanSerializerModifier[0]);
      var4.beanDeserializerModifiers = (BeanDeserializerModifier[])((Collection)super.getBeansOfTypeForField(
            var1, var2, 4, Argument.of(BeanDeserializerModifier.class, null), null
         ))
         .toArray(new BeanDeserializerModifier[0]);
      var4.keyDeserializers = (KeyDeserializer[])((Collection)super.getBeansOfTypeForField(var1, var2, 5, Argument.of(KeyDeserializer.class, null), null))
         .toArray(new KeyDeserializer[0]);
      return super.injectBean(var1, var2, var3);
   }

   public $ObjectMapperFactory$Definition() {
      this(ObjectMapperFactory.class, $CONSTRUCTOR);
   }

   protected $ObjectMapperFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ObjectMapperFactory$Definition$Reference.$ANNOTATION_METADATA,
         null,
         $INJECTION_FIELDS,
         null,
         null,
         null,
         Optional.empty(),
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
