package io.micronaut.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.json.JsonConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@ConfigurationProperties("jackson")
@TypeHint({PropertyNamingStrategies.UpperCamelCaseStrategy.class, ArrayList.class, LinkedHashMap.class, HashSet.class})
public class JacksonConfiguration implements JsonConfiguration {
   public static final int DEFAULT_ARRAYSIZETHRESHOLD = 100;
   public static final String PROPERTY_MODULE_SCAN = "jackson.module-scan";
   public static final String PROPERTY_USE_BEAN_INTROSPECTION = "jackson.bean-introspection-module";
   private boolean moduleScan = true;
   private boolean beanIntrospectionModule = true;
   private String dateFormat;
   private Locale locale;
   private TimeZone timeZone;
   private int arraySizeThreshold = 100;
   private Map<SerializationFeature, Boolean> serialization = Collections.emptyMap();
   private Map<DeserializationFeature, Boolean> deserialization = Collections.emptyMap();
   private Map<MapperFeature, Boolean> mapper = Collections.emptyMap();
   private Map<JsonParser.Feature, Boolean> parser = Collections.emptyMap();
   private Map<JsonGenerator.Feature, Boolean> generator = Collections.emptyMap();
   private Map<JsonFactory.Feature, Boolean> factory = Collections.emptyMap();
   private JsonInclude.Include serializationInclusion = JsonInclude.Include.NON_EMPTY;
   private ObjectMapper.DefaultTyping defaultTyping = null;
   private PropertyNamingStrategy propertyNamingStrategy = null;
   private boolean alwaysSerializeErrorsAsList = true;
   private boolean trimStrings = false;

   public boolean isBeanIntrospectionModule() {
      return this.beanIntrospectionModule;
   }

   public void setBeanIntrospectionModule(boolean beanIntrospectionModule) {
      this.beanIntrospectionModule = beanIntrospectionModule;
   }

   public boolean isModuleScan() {
      return this.moduleScan;
   }

   public void setModuleScan(boolean moduleScan) {
      this.moduleScan = moduleScan;
   }

   public JsonInclude.Include getSerializationInclusion() {
      return this.serializationInclusion;
   }

   public ObjectMapper.DefaultTyping getDefaultTyping() {
      return this.defaultTyping;
   }

   public Locale getLocale() {
      return this.locale;
   }

   public TimeZone getTimeZone() {
      return this.timeZone;
   }

   public String getDateFormat() {
      return this.dateFormat;
   }

   public Map<SerializationFeature, Boolean> getSerializationSettings() {
      return this.serialization;
   }

   public Map<DeserializationFeature, Boolean> getDeserializationSettings() {
      return this.deserialization;
   }

   public Map<MapperFeature, Boolean> getMapperSettings() {
      return this.mapper;
   }

   public Map<JsonParser.Feature, Boolean> getParserSettings() {
      return this.parser;
   }

   public Map<JsonGenerator.Feature, Boolean> getGeneratorSettings() {
      return this.generator;
   }

   public Map<JsonFactory.Feature, Boolean> getFactorySettings() {
      return this.factory;
   }

   @Override
   public int getArraySizeThreshold() {
      return this.arraySizeThreshold;
   }

   public PropertyNamingStrategy getPropertyNamingStrategy() {
      return this.propertyNamingStrategy;
   }

   @Override
   public boolean isAlwaysSerializeErrorsAsList() {
      return this.alwaysSerializeErrorsAsList;
   }

   public boolean isTrimStrings() {
      return this.trimStrings;
   }

   public void setDateFormat(String dateFormat) {
      this.dateFormat = dateFormat;
   }

   public void setLocale(Locale locale) {
      this.locale = locale;
   }

   public void setTimeZone(TimeZone timeZone) {
      this.timeZone = timeZone;
   }

   public void setArraySizeThreshold(int arraySizeThreshold) {
      this.arraySizeThreshold = arraySizeThreshold;
   }

   public void setSerialization(Map<SerializationFeature, Boolean> serialization) {
      if (CollectionUtils.isNotEmpty(serialization)) {
         this.serialization = serialization;
      }

   }

   public void setDeserialization(Map<DeserializationFeature, Boolean> deserialization) {
      if (CollectionUtils.isNotEmpty(deserialization)) {
         this.deserialization = deserialization;
      }

   }

   public void setMapper(Map<MapperFeature, Boolean> mapper) {
      if (CollectionUtils.isNotEmpty(mapper)) {
         this.mapper = mapper;
      }

   }

   public void setParser(Map<JsonParser.Feature, Boolean> parser) {
      if (CollectionUtils.isNotEmpty(parser)) {
         this.parser = parser;
      }

   }

   public void setGenerator(Map<JsonGenerator.Feature, Boolean> generator) {
      if (CollectionUtils.isNotEmpty(generator)) {
         this.generator = generator;
      }

   }

   public void setFactory(Map<JsonFactory.Feature, Boolean> factory) {
      if (CollectionUtils.isNotEmpty(factory)) {
         this.factory = factory;
      }

   }

   public void setSerializationInclusion(JsonInclude.Include serializationInclusion) {
      if (serializationInclusion != null) {
         this.serializationInclusion = serializationInclusion;
      }

   }

   public void setDefaultTyping(ObjectMapper.DefaultTyping defaultTyping) {
      this.defaultTyping = defaultTyping;
   }

   public void setPropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
      this.propertyNamingStrategy = propertyNamingStrategy;
   }

   public void setAlwaysSerializeErrorsAsList(boolean alwaysSerializeErrorsAsList) {
      this.alwaysSerializeErrorsAsList = alwaysSerializeErrorsAsList;
   }

   public void setTrimStrings(boolean trimStrings) {
      this.trimStrings = trimStrings;
   }

   public static <T> JavaType constructType(@NonNull Argument<T> type, @NonNull TypeFactory typeFactory) {
      ArgumentUtils.requireNonNull("type", type);
      ArgumentUtils.requireNonNull("typeFactory", typeFactory);
      Map<String, Argument<?>> typeVariables = type.getTypeVariables();
      JavaType[] objects = toJavaTypeArray(typeFactory, typeVariables);
      Class<T> rawType = type.getType();
      if (ArrayUtils.isNotEmpty(objects)) {
         JavaType javaType = typeFactory.constructType(rawType);
         if (javaType.isCollectionLikeType()) {
            return typeFactory.constructCollectionLikeType(rawType, objects[0]);
         } else if (javaType.isMapLikeType()) {
            return typeFactory.constructMapLikeType(rawType, objects[0], objects[1]);
         } else {
            return javaType.isReferenceType() ? typeFactory.constructReferenceType(rawType, objects[0]) : typeFactory.constructParametricType(rawType, objects);
         }
      } else {
         return typeFactory.constructType(rawType);
      }
   }

   private static JavaType[] toJavaTypeArray(TypeFactory typeFactory, Map<String, Argument<?>> typeVariables) {
      List<JavaType> javaTypes = new ArrayList();

      for(Argument<?> argument : typeVariables.values()) {
         if (argument.hasTypeVariables()) {
            javaTypes.add(typeFactory.constructParametricType(argument.getType(), toJavaTypeArray(typeFactory, argument.getTypeVariables())));
         } else {
            javaTypes.add(typeFactory.constructType(argument.getType()));
         }
      }

      return (JavaType[])javaTypes.toArray(new JavaType[0]);
   }
}
