package io.micronaut.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

// $FF: synthetic class
@Generated
class $JacksonConfiguration$Definition extends AbstractInitializableBeanDefinition<JacksonConfiguration> implements BeanFactory<JacksonConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JacksonConfiguration.class, "<init>", null, null, false
   );

   @Override
   public JacksonConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JacksonConfiguration var4 = new JacksonConfiguration();
      return (JacksonConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         JacksonConfiguration var4 = (JacksonConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "jackson.bean-introspection-module")) {
            var4.setBeanIntrospectionModule(
               super.getPropertyValueForSetter(
                  var1, var2, "setBeanIntrospectionModule", Argument.of(Boolean.TYPE, "beanIntrospectionModule"), "jackson.bean-introspection-module", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "jackson.module-scan")) {
            var4.setModuleScan(
               super.getPropertyValueForSetter(var1, var2, "setModuleScan", Argument.of(Boolean.TYPE, "moduleScan"), "jackson.module-scan", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "jackson.date-format")) {
            var4.setDateFormat(
               (String)super.getPropertyValueForSetter(var1, var2, "setDateFormat", Argument.of(String.class, "dateFormat"), "jackson.date-format", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "jackson.locale")) {
            var4.setLocale((Locale)super.getPropertyValueForSetter(var1, var2, "setLocale", Argument.of(Locale.class, "locale"), "jackson.locale", null));
         }

         if (this.containsPropertyValue(var1, var2, "jackson.time-zone")) {
            var4.setTimeZone(
               (TimeZone)super.getPropertyValueForSetter(var1, var2, "setTimeZone", Argument.of(TimeZone.class, "timeZone"), "jackson.time-zone", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "jackson.array-size-threshold")) {
            var4.setArraySizeThreshold(
               super.getPropertyValueForSetter(
                  var1, var2, "setArraySizeThreshold", Argument.of(Integer.TYPE, "arraySizeThreshold"), "jackson.array-size-threshold", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "jackson.serialization")) {
            var4.setSerialization(
               (Map<SerializationFeature, Boolean>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setSerialization",
                  Argument.of(Map.class, "serialization", null, Argument.of(SerializationFeature.class, "K"), Argument.ofTypeVariable(Boolean.class, "V")),
                  "jackson.serialization",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "jackson.deserialization")) {
            var4.setDeserialization(
               (Map<DeserializationFeature, Boolean>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setDeserialization",
                  Argument.of(Map.class, "deserialization", null, Argument.of(DeserializationFeature.class, "K"), Argument.ofTypeVariable(Boolean.class, "V")),
                  "jackson.deserialization",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "jackson.mapper")) {
            var4.setMapper(
               (Map<MapperFeature, Boolean>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMapper",
                  Argument.of(Map.class, "mapper", null, Argument.of(MapperFeature.class, "K"), Argument.ofTypeVariable(Boolean.class, "V")),
                  "jackson.mapper",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "jackson.parser")) {
            var4.setParser(
               (Map<JsonParser.Feature, Boolean>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setParser",
                  Argument.of(Map.class, "parser", null, Argument.of(JsonParser.Feature.class, "K"), Argument.ofTypeVariable(Boolean.class, "V")),
                  "jackson.parser",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "jackson.generator")) {
            var4.setGenerator(
               (Map<JsonGenerator.Feature, Boolean>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setGenerator",
                  Argument.of(Map.class, "generator", null, Argument.of(JsonGenerator.Feature.class, "K"), Argument.ofTypeVariable(Boolean.class, "V")),
                  "jackson.generator",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "jackson.factory")) {
            var4.setFactory(
               (Map<JsonFactory.Feature, Boolean>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setFactory",
                  Argument.of(Map.class, "factory", null, Argument.of(JsonFactory.Feature.class, "K"), Argument.ofTypeVariable(Boolean.class, "V")),
                  "jackson.factory",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "jackson.serialization-inclusion")) {
            var4.setSerializationInclusion(
               (JsonInclude.Include)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setSerializationInclusion",
                  Argument.of(JsonInclude.Include.class, "serializationInclusion"),
                  "jackson.serialization-inclusion",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "jackson.default-typing")) {
            var4.setDefaultTyping(
               (ObjectMapper.DefaultTyping)super.getPropertyValueForSetter(
                  var1, var2, "setDefaultTyping", Argument.of(ObjectMapper.DefaultTyping.class, "defaultTyping"), "jackson.default-typing", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "jackson.property-naming-strategy")) {
            var4.setPropertyNamingStrategy(
               (PropertyNamingStrategy)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setPropertyNamingStrategy",
                  Argument.of(PropertyNamingStrategy.class, "propertyNamingStrategy"),
                  "jackson.property-naming-strategy",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "jackson.always-serialize-errors-as-list")) {
            var4.setAlwaysSerializeErrorsAsList(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setAlwaysSerializeErrorsAsList",
                  Argument.of(Boolean.TYPE, "alwaysSerializeErrorsAsList"),
                  "jackson.always-serialize-errors-as-list",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "jackson.trim-strings")) {
            var4.setTrimStrings(
               super.getPropertyValueForSetter(var1, var2, "setTrimStrings", Argument.of(Boolean.TYPE, "trimStrings"), "jackson.trim-strings", null)
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $JacksonConfiguration$Definition() {
      this(JacksonConfiguration.class, $CONSTRUCTOR);
   }

   protected $JacksonConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JacksonConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false
      );
   }
}
