package io.micronaut.json.tree;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

public abstract class JsonNode {
   JsonNode() {
   }

   @NonNull
   public static JsonNode nullNode() {
      return JsonNull.INSTANCE;
   }

   @NonNull
   public static JsonNode createArrayNode(@NonNull List<JsonNode> nodes) {
      Objects.requireNonNull(nodes, "nodes");
      return new JsonArray(nodes);
   }

   @NonNull
   public static JsonNode createObjectNode(Map<String, JsonNode> nodes) {
      Objects.requireNonNull(nodes, "nodes");
      return new JsonObject(nodes);
   }

   @NonNull
   public static JsonNode createBooleanNode(boolean value) {
      return JsonBoolean.valueOf(value);
   }

   @NonNull
   public static JsonNode createStringNode(@NonNull String value) {
      Objects.requireNonNull(value, "value");
      return new JsonString(value);
   }

   @Internal
   public static JsonNode createNumberNodeImpl(Number value) {
      Objects.requireNonNull(value, "value");
      return new JsonNumber(value);
   }

   @NonNull
   public static JsonNode createNumberNode(int value) {
      return createNumberNodeImpl(value);
   }

   @NonNull
   public static JsonNode createNumberNode(long value) {
      return createNumberNodeImpl(value);
   }

   @NonNull
   public static JsonNode createNumberNode(@NonNull BigDecimal value) {
      return createNumberNodeImpl(value);
   }

   @NonNull
   public static JsonNode createNumberNode(float value) {
      return createNumberNodeImpl(value);
   }

   @NonNull
   public static JsonNode createNumberNode(double value) {
      return createNumberNodeImpl(value);
   }

   @NonNull
   public static JsonNode createNumberNode(@NonNull BigInteger value) {
      return createNumberNodeImpl(value);
   }

   public boolean isNumber() {
      return false;
   }

   @NonNull
   public Number getNumberValue() {
      throw new IllegalStateException("Not a number");
   }

   public final int getIntValue() {
      return this.getNumberValue().intValue();
   }

   public final long getLongValue() {
      return this.getNumberValue().longValue();
   }

   public final float getFloatValue() {
      return this.getNumberValue().floatValue();
   }

   public final double getDoubleValue() {
      return this.getNumberValue().doubleValue();
   }

   @NonNull
   public final BigInteger getBigIntegerValue() {
      Number numberValue = this.getNumberValue();
      if (numberValue instanceof BigInteger) {
         return (BigInteger)numberValue;
      } else {
         return numberValue instanceof BigDecimal ? ((BigDecimal)numberValue).toBigInteger() : BigInteger.valueOf(numberValue.longValue());
      }
   }

   @NonNull
   public final BigDecimal getBigDecimalValue() {
      Number numberValue = this.getNumberValue();
      if (numberValue instanceof BigInteger) {
         return new BigDecimal((BigInteger)numberValue);
      } else if (numberValue instanceof BigDecimal) {
         return (BigDecimal)numberValue;
      } else {
         return numberValue instanceof Long ? BigDecimal.valueOf(numberValue.longValue()) : BigDecimal.valueOf(numberValue.doubleValue());
      }
   }

   public boolean isString() {
      return false;
   }

   @NonNull
   public String getStringValue() {
      throw new IllegalStateException("Not a string");
   }

   @NonNull
   public String coerceStringValue() {
      throw new IllegalStateException("Not a scalar value");
   }

   public boolean isBoolean() {
      return false;
   }

   public boolean getBooleanValue() {
      throw new IllegalStateException("Not a boolean");
   }

   public boolean isNull() {
      return false;
   }

   public abstract int size();

   @NonNull
   public abstract Iterable<JsonNode> values();

   @NonNull
   public abstract Iterable<Entry<String, JsonNode>> entries();

   public boolean isValueNode() {
      return false;
   }

   public boolean isContainerNode() {
      return false;
   }

   public boolean isArray() {
      return false;
   }

   public boolean isObject() {
      return false;
   }

   @Nullable
   public abstract JsonNode get(@NonNull String fieldName);

   @Nullable
   public abstract JsonNode get(int index);
}
