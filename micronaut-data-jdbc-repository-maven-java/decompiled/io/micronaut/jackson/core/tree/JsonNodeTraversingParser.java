package io.micronaut.jackson.core.tree;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.json.tree.JsonNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map.Entry;

final class JsonNodeTraversingParser extends ParserMinimalBase {
   private final Deque<JsonNodeTraversingParser.Context> contextStack = new ArrayDeque();
   private boolean first = true;
   private ObjectCodec codec = null;

   JsonNodeTraversingParser(JsonNode node) {
      JsonNodeTraversingParser.Context root;
      if (node.isArray()) {
         root = new JsonNodeTraversingParser.ArrayContext(null, node.values().iterator());
      } else if (node.isObject()) {
         root = new JsonNodeTraversingParser.ObjectContext(null, node.entries().iterator());
      } else {
         root = new JsonNodeTraversingParser.SingleContext(node);
      }

      this.contextStack.add(root);
   }

   private JsonNode currentNodeOrNull() {
      for(JsonNodeTraversingParser.Context context : this.contextStack) {
         JsonNode node = context.currentNode();
         if (node != null) {
            return node;
         }
      }

      return null;
   }

   @Override
   public JsonToken nextToken() throws IOException {
      if (this.first) {
         this.first = false;

         assert !this.contextStack.isEmpty();

         this._currToken = ((JsonNodeTraversingParser.Context)this.contextStack.peekFirst()).currentToken();
         return this._currToken;
      } else {
         while(!this.contextStack.isEmpty()) {
            JsonNodeTraversingParser.Context context = (JsonNodeTraversingParser.Context)this.contextStack.peekFirst();
            if (!context.lastToken) {
               JsonNodeTraversingParser.Context childContext = context.next();
               if (childContext != null) {
                  this.contextStack.addFirst(childContext);
                  this._currToken = childContext.currentToken();
               } else {
                  this._currToken = context.currentToken();
               }

               return this._currToken;
            }

            this.contextStack.removeFirst();
         }

         return null;
      }
   }

   @Override
   protected void _handleEOF() throws JsonParseException {
      this._throwInternal();
   }

   @Override
   public String getCurrentName() throws IOException {
      return this.contextStack.isEmpty() ? null : ((JsonNodeTraversingParser.Context)this.contextStack.peekFirst()).getCurrentName();
   }

   @Override
   public ObjectCodec getCodec() {
      return this.codec;
   }

   @Override
   public void setCodec(ObjectCodec oc) {
      this.codec = oc;
   }

   @Override
   public Version version() {
      return Version.unknownVersion();
   }

   @Override
   public void close() throws IOException {
      this.contextStack.clear();
   }

   @Override
   public boolean isClosed() {
      return this.contextStack.isEmpty();
   }

   @Override
   public JsonStreamContext getParsingContext() {
      return (JsonStreamContext)this.contextStack.peekFirst();
   }

   @Override
   public JsonLocation getTokenLocation() {
      return JsonLocation.NA;
   }

   @Override
   public JsonLocation getCurrentLocation() {
      return JsonLocation.NA;
   }

   @Override
   public void overrideCurrentName(String name) {
      if (!this.contextStack.isEmpty()) {
         ((JsonNodeTraversingParser.Context)this.contextStack.peekFirst()).setCurrentName(name);
      }

   }

   @Override
   public String getText() throws IOException {
      return this.contextStack.isEmpty() ? null : ((JsonNodeTraversingParser.Context)this.contextStack.peekFirst()).getText();
   }

   @Override
   public char[] getTextCharacters() throws IOException {
      return this.getText().toCharArray();
   }

   @Override
   public boolean hasTextCharacters() {
      return false;
   }

   private JsonNode currentNumberNode() throws JsonParseException {
      JsonNode node = this.currentNodeOrNull();
      if (node != null && node.isNumber()) {
         return node;
      } else {
         throw new JsonParseException(this, "Not a number");
      }
   }

   @Override
   public Number getNumberValue() throws IOException {
      return this.currentNumberNode().getNumberValue();
   }

   @Override
   public JsonParser.NumberType getNumberType() throws IOException {
      JsonNode currentNode = this.currentNodeOrNull();
      if (currentNode != null && currentNode.isNumber()) {
         Number value = currentNode.getNumberValue();
         if (value instanceof BigDecimal) {
            return JsonParser.NumberType.BIG_DECIMAL;
         } else if (value instanceof Double) {
            return JsonParser.NumberType.DOUBLE;
         } else if (value instanceof Float) {
            return JsonParser.NumberType.FLOAT;
         } else if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            return JsonParser.NumberType.INT;
         } else if (value instanceof Long) {
            return JsonParser.NumberType.LONG;
         } else if (value instanceof BigInteger) {
            return JsonParser.NumberType.BIG_INTEGER;
         } else {
            throw new IllegalStateException("Unknown number type " + value.getClass().getName());
         }
      } else {
         return null;
      }
   }

   @Override
   public int getIntValue() throws IOException {
      return this.currentNumberNode().getIntValue();
   }

   @Override
   public long getLongValue() throws IOException {
      return this.currentNumberNode().getLongValue();
   }

   @Override
   public BigInteger getBigIntegerValue() throws IOException {
      return this.currentNumberNode().getBigIntegerValue();
   }

   @Override
   public float getFloatValue() throws IOException {
      return this.currentNumberNode().getFloatValue();
   }

   @Override
   public double getDoubleValue() throws IOException {
      return this.currentNumberNode().getDoubleValue();
   }

   @Override
   public BigDecimal getDecimalValue() throws IOException {
      return this.currentNumberNode().getBigDecimalValue();
   }

   @Override
   public int getTextLength() throws IOException {
      return this.getText().length();
   }

   @Override
   public int getTextOffset() throws IOException {
      return 0;
   }

   @Override
   public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
      JsonNode currentNode = this.currentNodeOrNull();
      if (currentNode != null && currentNode.isNull()) {
         return null;
      } else {
         String text = this.getText();
         return text != null ? b64variant.decode(text) : null;
      }
   }

   private static String nodeToText(JsonNode node) {
      if (node.isString()) {
         return node.getStringValue();
      } else if (node.isBoolean()) {
         return Boolean.toString(node.getBooleanValue());
      } else if (node.isNumber()) {
         return node.getNumberValue().toString();
      } else if (node.isNull()) {
         return "null";
      } else {
         return node.isArray() ? "[" : "{";
      }
   }

   private static JsonToken asToken(JsonNode node) {
      if (node.isString()) {
         return JsonToken.VALUE_STRING;
      } else if (node.isBoolean()) {
         return node.getBooleanValue() ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE;
      } else if (!node.isNumber()) {
         if (node.isNull()) {
            return JsonToken.VALUE_NULL;
         } else {
            return node.isArray() ? JsonToken.START_ARRAY : JsonToken.START_OBJECT;
         }
      } else {
         Number numberValue = node.getNumberValue();
         return !(numberValue instanceof Float) && !(numberValue instanceof Double) && !(numberValue instanceof BigDecimal)
            ? JsonToken.VALUE_NUMBER_INT
            : JsonToken.VALUE_NUMBER_FLOAT;
      }
   }

   private static class ArrayContext extends JsonNodeTraversingParser.Context {
      final Iterator<JsonNode> iterator;
      JsonNode currentNode = null;

      ArrayContext(JsonNodeTraversingParser.Context parent, Iterator<JsonNode> iterator) {
         super(parent);
         this._type = 1;
         this.iterator = iterator;
      }

      @Nullable
      @Override
      JsonNodeTraversingParser.Context next() {
         if (this.iterator.hasNext()) {
            this.currentNode = (JsonNode)this.iterator.next();
            return this.createSubContextIfContainer(this.currentNode);
         } else {
            this.currentNode = null;
            this.lastToken = true;
            return null;
         }
      }

      @Override
      JsonNode currentNode() {
         return this.currentNode;
      }

      @Override
      public String getCurrentName() {
         return null;
      }

      @Override
      void setCurrentName(String currentName) {
      }

      @Override
      JsonToken currentToken() {
         if (this.currentNode == null) {
            return this.lastToken ? JsonToken.END_ARRAY : JsonToken.START_ARRAY;
         } else {
            return JsonNodeTraversingParser.asToken(this.currentNode);
         }
      }

      @Override
      String getText() {
         return this.currentNode != null ? JsonNodeTraversingParser.nodeToText(this.currentNode) : this.currentToken().asString();
      }
   }

   private abstract static class Context extends JsonStreamContext {
      boolean lastToken = false;
      private final JsonNodeTraversingParser.Context parent;

      Context(JsonNodeTraversingParser.Context parent) {
         this.parent = parent;
      }

      protected JsonNodeTraversingParser.Context createSubContextIfContainer(JsonNode node) {
         if (node.isArray()) {
            return new JsonNodeTraversingParser.ArrayContext(this, node.values().iterator());
         } else {
            return node.isObject() ? new JsonNodeTraversingParser.ObjectContext(this, node.entries().iterator()) : null;
         }
      }

      public final JsonNodeTraversingParser.Context getParent() {
         return this.parent;
      }

      @Nullable
      abstract JsonNodeTraversingParser.Context next();

      @Nullable
      abstract JsonNode currentNode();

      @Override
      public abstract String getCurrentName();

      abstract void setCurrentName(String currentName);

      abstract JsonToken currentToken();

      abstract String getText();
   }

   private static class ObjectContext extends JsonNodeTraversingParser.Context {
      final Iterator<Entry<String, JsonNode>> iterator;
      @Nullable
      String currentName = null;
      @Nullable
      JsonNode currentValue = null;
      boolean inFieldName = false;

      ObjectContext(JsonNodeTraversingParser.Context parent, Iterator<Entry<String, JsonNode>> iterator) {
         super(parent);
         this._type = 2;
         this.iterator = iterator;
      }

      @Nullable
      @Override
      JsonNodeTraversingParser.Context next() {
         if (this.inFieldName) {
            this.inFieldName = false;

            assert this.currentValue != null;

            return this.createSubContextIfContainer(this.currentValue);
         } else {
            if (this.iterator.hasNext()) {
               Entry<String, JsonNode> entry = (Entry)this.iterator.next();
               this.currentName = (String)entry.getKey();
               this.currentValue = (JsonNode)entry.getValue();
               this.inFieldName = true;
            } else {
               this.lastToken = true;
               this.currentName = null;
               this.currentValue = null;
            }

            return null;
         }
      }

      @Nullable
      @Override
      JsonNode currentNode() {
         return this.inFieldName ? null : this.currentValue;
      }

      @Nullable
      @Override
      public String getCurrentName() {
         return this.currentName;
      }

      @Override
      void setCurrentName(@Nullable String currentName) {
         this.currentName = currentName;
      }

      @Override
      JsonToken currentToken() {
         if (this.inFieldName) {
            return JsonToken.FIELD_NAME;
         } else if (this.currentValue != null) {
            return JsonNodeTraversingParser.asToken(this.currentValue);
         } else {
            return this.lastToken ? JsonToken.END_OBJECT : JsonToken.START_OBJECT;
         }
      }

      @Override
      String getText() {
         if (this.inFieldName) {
            return this.currentName;
         } else {
            return this.currentValue != null ? JsonNodeTraversingParser.nodeToText(this.currentValue) : this.currentToken().asString();
         }
      }
   }

   private static class SingleContext extends JsonNodeTraversingParser.Context {
      private final JsonNode value;

      SingleContext(JsonNode value) {
         super(null);
         this._type = 0;
         this.value = value;
         this.lastToken = true;
      }

      @Nullable
      @Override
      JsonNodeTraversingParser.Context next() {
         return null;
      }

      @Nullable
      @Override
      JsonNode currentNode() {
         return this.value;
      }

      @Override
      public String getCurrentName() {
         return null;
      }

      @Override
      void setCurrentName(String currentName) {
      }

      @Override
      JsonToken currentToken() {
         return JsonNodeTraversingParser.asToken(this.value);
      }

      @Override
      String getText() {
         return JsonNodeTraversingParser.nodeToText(this.value);
      }
   }
}
