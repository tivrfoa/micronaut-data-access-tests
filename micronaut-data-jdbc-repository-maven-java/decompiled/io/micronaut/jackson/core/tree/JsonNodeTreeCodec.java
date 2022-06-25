package io.micronaut.jackson.core.tree;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.micronaut.json.JsonStreamConfig;
import io.micronaut.json.tree.JsonNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class JsonNodeTreeCodec {
   private static final JsonNodeTreeCodec INSTANCE = new JsonNodeTreeCodec(JsonStreamConfig.DEFAULT);
   private final JsonStreamConfig config;

   private JsonNodeTreeCodec(JsonStreamConfig config) {
      this.config = config;
   }

   public static JsonNodeTreeCodec getInstance() {
      return INSTANCE;
   }

   public JsonNodeTreeCodec withConfig(JsonStreamConfig config) {
      return new JsonNodeTreeCodec(config);
   }

   public JsonNode readTree(JsonParser p) throws IOException {
      switch(p.hasCurrentToken() ? p.currentToken() : p.nextToken()) {
         case START_OBJECT:
            Map<String, JsonNode> map = new LinkedHashMap();

            while(p.nextToken() != JsonToken.END_OBJECT) {
               String name = p.currentName();
               p.nextToken();
               map.put(name, this.readTree(p));
            }

            return JsonNode.createObjectNode(map);
         case START_ARRAY:
            List<JsonNode> list = new ArrayList();

            while(p.nextToken() != JsonToken.END_ARRAY) {
               list.add(this.readTree(p));
            }

            return JsonNode.createArrayNode(list);
         case VALUE_STRING:
            return JsonNode.createStringNode(p.getText());
         case VALUE_NUMBER_INT:
            if (this.config.useBigIntegerForInts()) {
               return JsonNode.createNumberNode(p.getBigIntegerValue());
            }

            return JsonNode.createNumberNodeImpl(p.getNumberValue());
         case VALUE_NUMBER_FLOAT:
            if (this.config.useBigDecimalForFloats()) {
               if (p.isNaN()) {
                  return JsonNode.createNumberNode(p.getFloatValue());
               }

               return JsonNode.createNumberNode(p.getDecimalValue());
            }

            return JsonNode.createNumberNodeImpl(p.getNumberValue());
         case VALUE_TRUE:
            return JsonNode.createBooleanNode(true);
         case VALUE_FALSE:
            return JsonNode.createBooleanNode(false);
         case VALUE_NULL:
            return JsonNode.nullNode();
         default:
            throw new UnsupportedOperationException("Unsupported token: " + p.currentToken());
      }
   }

   public void writeTree(JsonGenerator generator, JsonNode tree) throws IOException {
      if (tree.isObject()) {
         generator.writeStartObject();

         for(Entry<String, JsonNode> entry : tree.entries()) {
            generator.writeFieldName((String)entry.getKey());
            this.writeTree(generator, (JsonNode)entry.getValue());
         }

         generator.writeEndObject();
      } else if (tree.isArray()) {
         generator.writeStartArray();

         for(JsonNode value : tree.values()) {
            this.writeTree(generator, value);
         }

         generator.writeEndArray();
      } else if (tree.isBoolean()) {
         generator.writeBoolean(tree.getBooleanValue());
      } else if (tree.isNull()) {
         generator.writeNull();
      } else if (tree.isNumber()) {
         Number value = tree.getNumberValue();
         if (value instanceof Integer) {
            generator.writeNumber(value.intValue());
         } else if (value instanceof Long) {
            generator.writeNumber(value.longValue());
         } else if (value instanceof Double) {
            generator.writeNumber(value.doubleValue());
         } else if (value instanceof Float) {
            generator.writeNumber(value.floatValue());
         } else if (value instanceof BigDecimal) {
            generator.writeNumber((BigDecimal)value);
         } else if (!(value instanceof Byte) && !(value instanceof Short)) {
            if (!(value instanceof BigInteger)) {
               throw new IllegalStateException("Unknown number type " + value.getClass().getName());
            }

            generator.writeNumber((BigInteger)value);
         } else {
            generator.writeNumber(value.shortValue());
         }
      } else {
         if (!tree.isString()) {
            throw new AssertionError();
         }

         generator.writeString(tree.getStringValue());
      }

   }

   public JsonParser treeAsTokens(JsonNode node) {
      return new JsonNodeTraversingParser(node);
   }

   public TreeGenerator createTreeGenerator() {
      return new TreeGenerator();
   }
}
