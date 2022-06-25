package io.micronaut.json.convert;

import io.micronaut.context.BeanProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.BeanPropertyBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonArray;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

@Singleton
public final class JsonConverterRegistrar implements TypeConverterRegistrar {
   private final BeanProvider<JsonMapper> objectCodec;
   private final ConversionService<?> conversionService;
   private final BeanProvider<BeanPropertyBinder> beanPropertyBinder;

   @Inject
   public JsonConverterRegistrar(
      BeanProvider<JsonMapper> objectCodec, ConversionService<?> conversionService, BeanProvider<BeanPropertyBinder> beanPropertyBinder
   ) {
      this.objectCodec = objectCodec;
      this.conversionService = conversionService;
      this.beanPropertyBinder = beanPropertyBinder;
   }

   @Override
   public void register(ConversionService<?> conversionService) {
      conversionService.addConverter(JsonArray.class, Object[].class, this.arrayNodeToObjectConverter());
      conversionService.addConverter(JsonNode.class, ConvertibleValues.class, this.objectNodeToConvertibleValuesConverter());
      conversionService.addConverter(JsonArray.class, Iterable.class, this.arrayNodeToIterableConverter());
      conversionService.addConverter(JsonNode.class, Object.class, this.jsonNodeToObjectConverter());
      conversionService.addConverter(Map.class, Object.class, this.mapToObjectConverter());
      conversionService.addConverter(Object.class, JsonNode.class, this.objectToJsonNodeConverter());
   }

   @Internal
   public TypeConverter<JsonNode, ConvertibleValues> objectNodeToConvertibleValuesConverter() {
      return (object, targetType, context) -> object.isObject() ? Optional.of(new JsonNodeConvertibleValues(object, this.conversionService)) : Optional.empty();
   }

   public TypeConverter<JsonArray, Iterable> arrayNodeToIterableConverter() {
      return (node, targetType, context) -> {
         Collection<Object> results;
         if (targetType.isAssignableFrom(ArrayList.class)) {
            results = new ArrayList();
         } else {
            if (!targetType.isAssignableFrom(LinkedHashSet.class)) {
               return Optional.empty();
            }

            results = new LinkedHashSet();
         }

         Map<String, Argument<?>> typeVariables = context.getTypeVariables();
         Class elementType = typeVariables.isEmpty() ? Map.class : ((Argument)typeVariables.values().iterator().next()).getType();

         for(int i = 0; i < node.size(); ++i) {
            Optional<?> converted = this.conversionService.convert(node.get(i), elementType, context);
            converted.ifPresent(results::add);
         }

         return Optional.of(results);
      };
   }

   @Internal
   public TypeConverter<JsonArray, Object[]> arrayNodeToObjectConverter() {
      return (node, targetType, context) -> {
         try {
            JsonMapper om = this.objectCodec.get();
            Object[] result = om.readValueFromTree(node, targetType);
            return Optional.of(result);
         } catch (IOException var6) {
            context.reject(var6);
            return Optional.empty();
         }
      };
   }

   protected TypeConverter<Map, Object> mapToObjectConverter() {
      return (map, targetType, context) -> {
         ArgumentConversionContext<Object> conversionContext;
         if (context instanceof ArgumentConversionContext) {
            conversionContext = (ArgumentConversionContext)context;
         } else {
            conversionContext = ConversionContext.of(targetType);
         }

         ArgumentBinder binder = this.beanPropertyBinder.get();
         ArgumentBinder.BindingResult result = binder.bind(conversionContext, this.correctKeys(map));
         return result.getValue();
      };
   }

   private Map correctKeys(Map<?, ?> map) {
      Map mapWithExtraProps = new LinkedHashMap(map.size());

      for(Entry entry : map.entrySet()) {
         Object key = entry.getKey();
         Object value = this.correctKeys(entry.getValue());
         mapWithExtraProps.put(NameUtils.decapitalize(NameUtils.dehyphenate(key.toString())), value);
      }

      return mapWithExtraProps;
   }

   private List correctKeys(List list) {
      List newList = new ArrayList(list.size());

      for(Object o : list) {
         newList.add(this.correctKeys(o));
      }

      return newList;
   }

   private Object correctKeys(Object o) {
      if (o instanceof List) {
         return this.correctKeys((List)o);
      } else {
         return o instanceof Map ? this.correctKeys((Map<?, ?>)o) : o;
      }
   }

   protected TypeConverter<Object, JsonNode> objectToJsonNodeConverter() {
      return (object, targetType, context) -> {
         try {
            return Optional.of(this.objectCodec.get().writeValueToTree(object));
         } catch (IOException | IllegalArgumentException var5) {
            context.reject(var5);
            return Optional.empty();
         }
      };
   }

   protected TypeConverter<JsonNode, Object> jsonNodeToObjectConverter() {
      return (node, targetType, context) -> {
         try {
            if (CharSequence.class.isAssignableFrom(targetType) && node.isObject()) {
               return Optional.of(new String(this.objectCodec.get().writeValueAsBytes(node), StandardCharsets.UTF_8));
            } else {
               Argument<?> argument = null;
               if (context instanceof ArgumentConversionContext) {
                  argument = ((ArgumentConversionContext)context).getArgument();
                  if (targetType != argument.getType()) {
                     argument = null;
                  }
               }

               if (argument == null) {
                  argument = Argument.of(targetType);
               }

               JsonMapper om = this.objectCodec.get();
               return Optional.ofNullable(om.readValueFromTree(node, argument));
            }
         } catch (IOException var6) {
            context.reject(var6);
            return Optional.empty();
         }
      };
   }
}
