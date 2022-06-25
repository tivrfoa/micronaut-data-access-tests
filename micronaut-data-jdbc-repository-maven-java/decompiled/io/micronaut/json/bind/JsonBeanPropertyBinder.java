package io.micronaut.json.bind;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.BeanPropertyBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.json.JsonConfiguration;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Singleton
@Primary
final class JsonBeanPropertyBinder implements BeanPropertyBinder {
   private final JsonMapper jsonMapper;
   private final int arraySizeThreshhold;
   private final BeanProvider<JsonBeanPropertyBinderExceptionHandler> exceptionHandlers;

   JsonBeanPropertyBinder(JsonMapper jsonMapper, JsonConfiguration configuration, BeanProvider<JsonBeanPropertyBinderExceptionHandler> exceptionHandlers) {
      this.jsonMapper = jsonMapper;
      this.arraySizeThreshhold = configuration.getArraySizeThreshold();
      this.exceptionHandlers = exceptionHandlers;
   }

   public ArgumentBinder.BindingResult<Object> bind(ArgumentConversionContext<Object> context, Map<CharSequence, ? super Object> source) {
      try {
         JsonNode objectNode = this.buildSourceObjectNode(source.entrySet());
         Object result = this.jsonMapper.readValueFromTree(objectNode, context.getArgument());
         return () -> Optional.of(result);
      } catch (Exception var5) {
         context.reject(var5);
         return new ArgumentBinder.BindingResult<Object>() {
            @Override
            public List<ConversionError> getConversionErrors() {
               return CollectionUtils.iterableToList(context);
            }

            @Override
            public boolean isSatisfied() {
               return false;
            }

            @Override
            public Optional<Object> getValue() {
               return Optional.empty();
            }
         };
      }
   }

   @Override
   public <T2> T2 bind(Class<T2> type, Set<? extends Entry<? extends CharSequence, Object>> source) throws ConversionErrorException {
      try {
         JsonNode objectNode = this.buildSourceObjectNode(source);
         return this.jsonMapper.readValueFromTree(objectNode, type);
      } catch (Exception var4) {
         throw this.newConversionError(null, var4);
      }
   }

   @Override
   public <T2> T2 bind(T2 object, ArgumentConversionContext<T2> context, Set<? extends Entry<? extends CharSequence, Object>> source) {
      try {
         JsonNode objectNode = this.buildSourceObjectNode(source);
         this.jsonMapper.updateValueFromTree(object, objectNode);
      } catch (Exception var5) {
         context.reject(var5);
      }

      return object;
   }

   @Override
   public <T2> T2 bind(T2 object, Set<? extends Entry<? extends CharSequence, Object>> source) throws ConversionErrorException {
      try {
         JsonNode objectNode = this.buildSourceObjectNode(source);
         this.jsonMapper.updateValueFromTree(object, objectNode);
         return object;
      } catch (Exception var4) {
         throw this.newConversionError(object, var4);
      }
   }

   protected ConversionErrorException newConversionError(Object object, Exception e) {
      for(JsonBeanPropertyBinderExceptionHandler exceptionHandler : this.exceptionHandlers) {
         Optional<ConversionErrorException> handled = exceptionHandler.toConversionError(object, e);
         if (handled.isPresent()) {
            return (ConversionErrorException)handled.get();
         }
      }

      ConversionError conversionError = new ConversionError() {
         @Override
         public Exception getCause() {
            return e;
         }

         @Override
         public Optional<Object> getOriginalValue() {
            return Optional.empty();
         }
      };
      Class type = object != null ? object.getClass() : Object.class;
      return new ConversionErrorException(Argument.of(type), conversionError);
   }

   private JsonNode buildSourceObjectNode(Set<? extends Entry<? extends CharSequence, Object>> source) throws IOException {
      JsonBeanPropertyBinder.ObjectBuilder rootNode = new JsonBeanPropertyBinder.ObjectBuilder();

      for(Entry<? extends CharSequence, ? super Object> entry : source) {
         CharSequence key = (CharSequence)entry.getKey();
         Object value = entry.getValue();
         String property = key.toString();
         JsonBeanPropertyBinder.ValueBuilder current = rootNode;
         String index = null;
         Iterator<String> tokenIterator = StringUtils.splitOmitEmptyStringsIterator(property, '.');

         while(tokenIterator.hasNext()) {
            String token = (String)tokenIterator.next();
            int j = token.indexOf(91);
            if (j > -1 && token.endsWith("]")) {
               index = token.substring(j + 1, token.length() - 1);
               token = token.substring(0, j);
            }

            if (!tokenIterator.hasNext()) {
               if (current instanceof JsonBeanPropertyBinder.ObjectBuilder) {
                  JsonBeanPropertyBinder.ObjectBuilder objectNode = (JsonBeanPropertyBinder.ObjectBuilder)current;
                  if (index != null) {
                     JsonBeanPropertyBinder.ValueBuilder existing = (JsonBeanPropertyBinder.ValueBuilder)objectNode.values.get(index);
                     if (!(existing instanceof JsonBeanPropertyBinder.ObjectBuilder)) {
                        existing = new JsonBeanPropertyBinder.ObjectBuilder();
                        objectNode.values.put(index, existing);
                     }

                     JsonBeanPropertyBinder.ObjectBuilder node = (JsonBeanPropertyBinder.ObjectBuilder)existing;
                     node.values.put(token, new JsonBeanPropertyBinder.FixedValue(this.jsonMapper.writeValueToTree(value)));
                     index = null;
                  } else {
                     objectNode.values.put(token, new JsonBeanPropertyBinder.FixedValue(this.jsonMapper.writeValueToTree(value)));
                  }
               } else if (current instanceof JsonBeanPropertyBinder.ArrayBuilder && index != null) {
                  JsonBeanPropertyBinder.ArrayBuilder arrayNode = (JsonBeanPropertyBinder.ArrayBuilder)current;
                  int arrayIndex = Integer.parseInt(index);
                  if (arrayIndex < this.arraySizeThreshhold) {
                     if (arrayIndex >= arrayNode.values.size()) {
                        this.expandArrayToThreshold(arrayIndex, arrayNode);
                     }

                     JsonBeanPropertyBinder.ValueBuilder jsonNode = (JsonBeanPropertyBinder.ValueBuilder)arrayNode.values.get(arrayIndex);
                     if (!(jsonNode instanceof JsonBeanPropertyBinder.ObjectBuilder)) {
                        jsonNode = new JsonBeanPropertyBinder.ObjectBuilder();
                        arrayNode.values.set(arrayIndex, jsonNode);
                     }

                     ((JsonBeanPropertyBinder.ObjectBuilder)jsonNode)
                        .values
                        .put(token, new JsonBeanPropertyBinder.FixedValue(this.jsonMapper.writeValueToTree(value)));
                  }

                  index = null;
               }
            } else if (current instanceof JsonBeanPropertyBinder.ObjectBuilder) {
               JsonBeanPropertyBinder.ObjectBuilder objectNode = (JsonBeanPropertyBinder.ObjectBuilder)current;
               JsonBeanPropertyBinder.ValueBuilder existing = (JsonBeanPropertyBinder.ValueBuilder)objectNode.values.get(token);
               if (index != null) {
                  JsonBeanPropertyBinder.ValueBuilder jsonNode;
                  if (StringUtils.isDigits(index)) {
                     int arrayIndex = Integer.parseInt(index);
                     JsonBeanPropertyBinder.ArrayBuilder arrayNode;
                     if (!(existing instanceof JsonBeanPropertyBinder.ArrayBuilder)) {
                        arrayNode = new JsonBeanPropertyBinder.ArrayBuilder();
                        objectNode.values.put(token, arrayNode);
                     } else {
                        arrayNode = (JsonBeanPropertyBinder.ArrayBuilder)existing;
                     }

                     this.expandArrayToThreshold(arrayIndex, arrayNode);
                     jsonNode = this.getOrCreateNodeAtIndex(arrayNode, arrayIndex);
                  } else {
                     if (!(existing instanceof JsonBeanPropertyBinder.ObjectBuilder)) {
                        existing = new JsonBeanPropertyBinder.ObjectBuilder();
                        objectNode.values.put(token, existing);
                     }

                     jsonNode = (JsonBeanPropertyBinder.ValueBuilder)((JsonBeanPropertyBinder.ObjectBuilder)existing).values.get(index);
                     if (!(jsonNode instanceof JsonBeanPropertyBinder.ObjectBuilder)) {
                        jsonNode = new JsonBeanPropertyBinder.ObjectBuilder();
                        ((JsonBeanPropertyBinder.ObjectBuilder)existing).values.put(index, jsonNode);
                     }
                  }

                  current = jsonNode;
                  index = null;
               } else {
                  if (!(existing instanceof JsonBeanPropertyBinder.ObjectBuilder)) {
                     existing = new JsonBeanPropertyBinder.ObjectBuilder();
                     objectNode.values.put(token, existing);
                  }

                  current = existing;
               }
            } else if (current instanceof JsonBeanPropertyBinder.ArrayBuilder && StringUtils.isDigits(index)) {
               JsonBeanPropertyBinder.ArrayBuilder arrayNode = (JsonBeanPropertyBinder.ArrayBuilder)current;
               int arrayIndex = Integer.parseInt(index);
               this.expandArrayToThreshold(arrayIndex, arrayNode);
               JsonBeanPropertyBinder.ObjectBuilder jsonNode = this.getOrCreateNodeAtIndex(arrayNode, arrayIndex);
               current = new JsonBeanPropertyBinder.ObjectBuilder();
               jsonNode.values.put(token, current);
               index = null;
            }
         }
      }

      return rootNode.build();
   }

   private JsonBeanPropertyBinder.ObjectBuilder getOrCreateNodeAtIndex(JsonBeanPropertyBinder.ArrayBuilder arrayNode, int arrayIndex) {
      JsonBeanPropertyBinder.ValueBuilder jsonNode = (JsonBeanPropertyBinder.ValueBuilder)arrayNode.values.get(arrayIndex);
      if (!(jsonNode instanceof JsonBeanPropertyBinder.ObjectBuilder)) {
         jsonNode = new JsonBeanPropertyBinder.ObjectBuilder();
         arrayNode.values.set(arrayIndex, jsonNode);
      }

      return (JsonBeanPropertyBinder.ObjectBuilder)jsonNode;
   }

   private void expandArrayToThreshold(int arrayIndex, JsonBeanPropertyBinder.ArrayBuilder arrayNode) {
      if (arrayIndex < this.arraySizeThreshhold) {
         while(arrayNode.values.size() != arrayIndex + 1) {
            arrayNode.values.add(JsonBeanPropertyBinder.FixedValue.NULL);
         }
      }

   }

   private static final class ArrayBuilder implements JsonBeanPropertyBinder.ValueBuilder {
      final List<JsonBeanPropertyBinder.ValueBuilder> values = new ArrayList();

      private ArrayBuilder() {
      }

      @Override
      public JsonNode build() {
         return JsonNode.createArrayNode((List<JsonNode>)this.values.stream().map(JsonBeanPropertyBinder.ValueBuilder::build).collect(Collectors.toList()));
      }
   }

   private static final class FixedValue implements JsonBeanPropertyBinder.ValueBuilder {
      static final JsonBeanPropertyBinder.FixedValue NULL = new JsonBeanPropertyBinder.FixedValue(JsonNode.nullNode());
      final JsonNode value;

      FixedValue(JsonNode value) {
         this.value = value;
      }

      @Override
      public JsonNode build() {
         return this.value;
      }
   }

   private static final class ObjectBuilder implements JsonBeanPropertyBinder.ValueBuilder {
      final Map<String, JsonBeanPropertyBinder.ValueBuilder> values = new LinkedHashMap();

      private ObjectBuilder() {
      }

      @Override
      public JsonNode build() {
         Map<String, JsonNode> built = new LinkedHashMap(this.values.size());

         for(Entry<String, JsonBeanPropertyBinder.ValueBuilder> entry : this.values.entrySet()) {
            built.put(entry.getKey(), ((JsonBeanPropertyBinder.ValueBuilder)entry.getValue()).build());
         }

         return JsonNode.createObjectNode(built);
      }
   }

   private interface ValueBuilder {
      JsonNode build();
   }
}
