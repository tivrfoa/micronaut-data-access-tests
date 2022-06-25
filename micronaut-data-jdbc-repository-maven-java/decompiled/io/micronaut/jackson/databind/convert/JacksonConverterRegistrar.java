package io.micronaut.jackson.databind.convert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.micronaut.context.BeanProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.jackson.JacksonConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@Internal
public class JacksonConverterRegistrar implements TypeConverterRegistrar {
   private final BeanProvider<ObjectMapper> objectMapper;
   private final ConversionService<?> conversionService;

   @Inject
   protected JacksonConverterRegistrar(BeanProvider<ObjectMapper> objectMapper, ConversionService<?> conversionService) {
      this.objectMapper = objectMapper;
      this.conversionService = conversionService;
   }

   @Override
   public void register(ConversionService<?> conversionService) {
      conversionService.addConverter(ArrayNode.class, Object[].class, this.arrayNodeToObjectConverter());
      conversionService.addConverter(ArrayNode.class, Iterable.class, this.arrayNodeToIterableConverter());
      conversionService.addConverter(JsonNode.class, Object.class, this.jsonNodeToObjectConverter());
      conversionService.addConverter(ObjectNode.class, ConvertibleValues.class, this.objectNodeToConvertibleValuesConverter());
      conversionService.addConverter(Object.class, JsonNode.class, this.objectToJsonNodeConverter());
      conversionService.addConverter(
         CharSequence.class,
         PropertyNamingStrategy.class,
         (TypeConverter)((charSequence, targetType, context) -> {
            Optional<PropertyNamingStrategy> propertyNamingStrategy = this.resolvePropertyNamingStrategy(charSequence);
            if (!propertyNamingStrategy.isPresent()) {
               context.reject(
                  charSequence,
                  new IllegalArgumentException(String.format("Unable to convert '%s' to a com.fasterxml.jackson.databind.PropertyNamingStrategy", charSequence))
               );
            }
   
            return propertyNamingStrategy;
         })
      );
   }

   protected TypeConverter<Object, JsonNode> objectToJsonNodeConverter() {
      return (object, targetType, context) -> {
         try {
            return Optional.of(this.objectMapper.get().valueToTree(object));
         } catch (IllegalArgumentException var5) {
            context.reject(var5);
            return Optional.empty();
         }
      };
   }

   protected TypeConverter<ObjectNode, ConvertibleValues> objectNodeToConvertibleValuesConverter() {
      return (object, targetType, context) -> Optional.of(new ObjectNodeConvertibleValues(object, this.conversionService));
   }

   protected TypeConverter<JsonNode, Object> jsonNodeToObjectConverter() {
      return (node, targetType, context) -> {
         try {
            if (CharSequence.class.isAssignableFrom(targetType) && node instanceof ObjectNode) {
               return Optional.of(node.toString());
            } else {
               Argument<Object> argument = null;
               if (node instanceof ContainerNode && context instanceof ArgumentConversionContext && targetType.getTypeParameters().length != 0) {
                  argument = ((ArgumentConversionContext)context).getArgument();
               }

               Object result;
               if (argument != null) {
                  ObjectMapper om = this.objectMapper.get();
                  JsonParser jsonParser = om.treeAsTokens(node);
                  TypeFactory typeFactory = om.getTypeFactory();
                  JavaType javaType = JacksonConfiguration.constructType(argument, typeFactory);
                  result = om.readValue(jsonParser, javaType);
               } else {
                  result = this.objectMapper.get().treeToValue(node, targetType);
               }

               return Optional.ofNullable(result);
            }
         } catch (IOException var10) {
            context.reject(var10);
            return Optional.empty();
         }
      };
   }

   protected TypeConverter<ArrayNode, Iterable> arrayNodeToIterableConverter() {
      return (node, targetType, context) -> {
         Map<String, Argument<?>> typeVariables = context.getTypeVariables();
         Class elementType = typeVariables.isEmpty() ? Map.class : ((Argument)typeVariables.values().iterator().next()).getType();
         List results = new ArrayList();
         node.elements().forEachRemaining(jsonNode -> {
            Optional converted = this.conversionService.convert(jsonNode, elementType, context);
            if (converted.isPresent()) {
               results.add(converted.get());
            }

         });
         return Optional.of(results);
      };
   }

   protected TypeConverter<ArrayNode, Object[]> arrayNodeToObjectConverter() {
      return (node, targetType, context) -> {
         try {
            Object[] result = this.objectMapper.get().treeToValue(node, targetType);
            return Optional.of(result);
         } catch (JsonProcessingException var5) {
            context.reject(var5);
            return Optional.empty();
         }
      };
   }

   @NonNull
   private Optional<PropertyNamingStrategy> resolvePropertyNamingStrategy(@Nullable CharSequence charSequence) {
      if (charSequence != null) {
         String stringValue = NameUtils.environmentName(charSequence.toString());
         if (StringUtils.isNotEmpty(stringValue)) {
            switch(stringValue) {
               case "SNAKE_CASE":
                  return Optional.of(PropertyNamingStrategies.SNAKE_CASE);
               case "UPPER_CAMEL_CASE":
                  return Optional.of(PropertyNamingStrategies.UPPER_CAMEL_CASE);
               case "LOWER_CASE":
                  return Optional.of(PropertyNamingStrategies.LOWER_CASE);
               case "KEBAB_CASE":
                  return Optional.of(PropertyNamingStrategies.KEBAB_CASE);
               case "LOWER_CAMEL_CASE":
                  return Optional.of(PropertyNamingStrategies.LOWER_CAMEL_CASE);
               case "LOWER_DOT_CASE":
                  return Optional.of(PropertyNamingStrategies.LOWER_DOT_CASE);
               default:
                  return Optional.empty();
            }
         }
      }

      return Optional.empty();
   }
}
