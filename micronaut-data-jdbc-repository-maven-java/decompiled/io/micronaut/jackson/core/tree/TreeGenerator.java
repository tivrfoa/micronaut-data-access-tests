package io.micronaut.jackson.core.tree;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.json.JsonStreamConfig;
import io.micronaut.json.tree.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TreeGenerator extends JsonGenerator {
   private ObjectCodec codec;
   private int generatorFeatures;
   private final Deque<TreeGenerator.StructureBuilder> structureStack = new ArrayDeque();
   private JsonNode completed = null;

   TreeGenerator() {
   }

   @Override
   public JsonGenerator setCodec(ObjectCodec oc) {
      this.codec = oc;
      return this;
   }

   @Override
   public ObjectCodec getCodec() {
      return this.codec;
   }

   @Override
   public Version version() {
      return Version.unknownVersion();
   }

   @Override
   public JsonStreamContext getOutputContext() {
      return null;
   }

   @Override
   public JsonGenerator enable(JsonGenerator.Feature f) {
      this.generatorFeatures |= f.getMask();
      return this;
   }

   @Override
   public JsonGenerator disable(JsonGenerator.Feature f) {
      this.generatorFeatures &= ~f.getMask();
      return this;
   }

   @Override
   public boolean isEnabled(JsonGenerator.Feature f) {
      return (this.generatorFeatures & f.getMask()) != 0;
   }

   @Override
   public int getFeatureMask() {
      return this.generatorFeatures;
   }

   @Override
   public JsonGenerator setFeatureMask(int values) {
      this.generatorFeatures = values;
      return this;
   }

   @Override
   public JsonGenerator useDefaultPrettyPrinter() {
      return this;
   }

   private void checkEmptyNodeStack(JsonToken token) throws JsonGenerationException {
      if (this.structureStack.isEmpty()) {
         throw new JsonGenerationException("Unexpected " + tokenType(token) + " literal", this);
      }
   }

   private static String tokenType(JsonToken token) {
      switch(token) {
         case END_OBJECT:
         case END_ARRAY:
            return "container end";
         case FIELD_NAME:
            return "field";
         case VALUE_NUMBER_INT:
            return "integer";
         case VALUE_STRING:
            return "string";
         case VALUE_NUMBER_FLOAT:
            return "float";
         case VALUE_NULL:
            return "null";
         case VALUE_TRUE:
         case VALUE_FALSE:
            return "boolean";
         default:
            return "";
      }
   }

   private void complete(JsonNode value) throws JsonGenerationException {
      if (this.completed != null) {
         throw new JsonGenerationException("Tree generator has already completed", this);
      } else {
         this.completed = value;
      }
   }

   public boolean isComplete() {
      return this.completed != null;
   }

   @NonNull
   public JsonNode getCompletedValue() {
      if (!this.isComplete()) {
         throw new IllegalStateException("Not completed");
      } else {
         return this.completed;
      }
   }

   @Override
   public void writeStartArray() throws IOException {
      this.structureStack.push(new TreeGenerator.ArrayBuilder());
   }

   private void writeEndStructure(JsonToken token) throws JsonGenerationException {
      this.checkEmptyNodeStack(token);
      TreeGenerator.StructureBuilder current = (TreeGenerator.StructureBuilder)this.structureStack.pop();
      if (this.structureStack.isEmpty()) {
         this.complete(current.build());
      } else {
         ((TreeGenerator.StructureBuilder)this.structureStack.peekFirst()).addValue(current.build());
      }

   }

   @Override
   public void writeEndArray() throws IOException {
      this.writeEndStructure(JsonToken.END_ARRAY);
   }

   @Override
   public void writeStartObject() throws IOException {
      this.structureStack.push(new TreeGenerator.ObjectBuilder());
   }

   @Override
   public void writeEndObject() throws IOException {
      this.writeEndStructure(JsonToken.END_OBJECT);
   }

   @Override
   public void writeFieldName(String name) throws IOException {
      this.checkEmptyNodeStack(JsonToken.FIELD_NAME);
      ((TreeGenerator.StructureBuilder)this.structureStack.peekFirst()).setCurrentFieldName(name);
   }

   @Override
   public void writeFieldName(SerializableString name) throws IOException {
      this.writeFieldName(name.getValue());
   }

   private void writeScalar(JsonToken token, JsonNode value) throws JsonGenerationException {
      if (this.structureStack.isEmpty()) {
         this.complete(value);
      } else {
         ((TreeGenerator.StructureBuilder)this.structureStack.peekFirst()).addValue(value);
      }

   }

   @Override
   public void writeString(String text) throws IOException {
      this.writeScalar(JsonToken.VALUE_STRING, JsonNode.createStringNode(text));
   }

   @Override
   public void writeString(char[] buffer, int offset, int len) throws IOException {
      this.writeString(new String(buffer, offset, len));
   }

   @Override
   public void writeString(SerializableString text) throws IOException {
      this.writeString(text.getValue());
   }

   @Override
   public void writeRawUTF8String(byte[] buffer, int offset, int len) throws IOException {
      this._reportUnsupportedOperation();
   }

   @Override
   public void writeUTF8String(byte[] buffer, int offset, int len) throws IOException {
      this._reportUnsupportedOperation();
   }

   @Override
   public void writeRaw(String text) throws IOException {
      this._reportUnsupportedOperation();
   }

   @Override
   public void writeRaw(String text, int offset, int len) throws IOException {
      this._reportUnsupportedOperation();
   }

   @Override
   public void writeRaw(char[] text, int offset, int len) throws IOException {
      this._reportUnsupportedOperation();
   }

   @Override
   public void writeRaw(char c) throws IOException {
      this._reportUnsupportedOperation();
   }

   @Override
   public void writeRawValue(String text) throws IOException {
      this.writeObject(text);
   }

   @Override
   public void writeRawValue(String text, int offset, int len) throws IOException {
      this.writeRawValue(text.substring(offset, len));
   }

   @Override
   public void writeRawValue(char[] text, int offset, int len) throws IOException {
      this.writeRawValue(new String(text, offset, len));
   }

   @Override
   public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {
      this._reportUnsupportedOperation();
   }

   @Override
   public int writeBinary(Base64Variant bv, InputStream data, int dataLength) throws IOException {
      this._reportUnsupportedOperation();
      return 0;
   }

   @Override
   public void writeNumber(int v) throws IOException {
      this.writeScalar(JsonToken.VALUE_NUMBER_INT, JsonNode.createNumberNode(v));
   }

   @Override
   public void writeNumber(long v) throws IOException {
      this.writeScalar(JsonToken.VALUE_NUMBER_INT, JsonNode.createNumberNode(v));
   }

   @Override
   public void writeNumber(BigInteger v) throws IOException {
      this.writeScalar(JsonToken.VALUE_NUMBER_INT, JsonNode.createNumberNode(v));
   }

   @Override
   public void writeNumber(double v) throws IOException {
      this.writeScalar(JsonToken.VALUE_NUMBER_FLOAT, JsonNode.createNumberNode(v));
   }

   @Override
   public void writeNumber(float v) throws IOException {
      this.writeScalar(JsonToken.VALUE_NUMBER_FLOAT, JsonNode.createNumberNode(v));
   }

   @Override
   public void writeNumber(BigDecimal v) throws IOException {
      this.writeScalar(JsonToken.VALUE_NUMBER_FLOAT, JsonNode.createNumberNode(v));
   }

   @Override
   public void writeNumber(String encodedValue) throws IOException {
      this._reportUnsupportedOperation();
   }

   @Override
   public void writeBoolean(boolean state) throws IOException {
      this.writeScalar(state ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE, JsonNode.createBooleanNode(state));
   }

   @Override
   public void writeNull() throws IOException {
      this.writeScalar(JsonToken.VALUE_NULL, JsonNode.nullNode());
   }

   @Override
   public void writeObject(Object pojo) throws IOException {
      this.getCodec().writeValue(this, pojo);
   }

   @Override
   public void writeTree(TreeNode rootNode) throws IOException {
      if (rootNode == null) {
         this.writeNull();
      } else if (rootNode instanceof JsonNode) {
         this.writeScalar(JsonToken.VALUE_EMBEDDED_OBJECT, (JsonNode)rootNode);
      } else {
         JsonStreamTransfer.transferNext(rootNode.traverse(), this, JsonStreamConfig.DEFAULT);
      }

   }

   @Override
   public void flush() throws IOException {
   }

   @Override
   public boolean isClosed() {
      return false;
   }

   @Override
   public void close() throws IOException {
   }

   private class ArrayBuilder implements TreeGenerator.StructureBuilder {
      final List<JsonNode> values = new ArrayList();

      private ArrayBuilder() {
      }

      @Override
      public void addValue(JsonNode value) {
         this.values.add(value);
      }

      @Override
      public void setCurrentFieldName(String currentFieldName) throws JsonGenerationException {
         throw new JsonGenerationException("Expected array value, got field name", TreeGenerator.this);
      }

      @Override
      public JsonNode build() {
         return JsonNode.createArrayNode(this.values);
      }
   }

   private class ObjectBuilder implements TreeGenerator.StructureBuilder {
      final Map<String, JsonNode> values = new LinkedHashMap();
      String currentFieldName = null;

      private ObjectBuilder() {
      }

      @Override
      public void addValue(JsonNode value) throws JsonGenerationException {
         if (this.currentFieldName == null) {
            throw new JsonGenerationException("Expected field name, got value", TreeGenerator.this);
         } else {
            this.values.put(this.currentFieldName, value);
            this.currentFieldName = null;
         }
      }

      @Override
      public void setCurrentFieldName(String currentFieldName) {
         this.currentFieldName = currentFieldName;
      }

      @Override
      public JsonNode build() {
         return JsonNode.createObjectNode(this.values);
      }
   }

   private interface StructureBuilder {
      void addValue(JsonNode value) throws JsonGenerationException;

      void setCurrentFieldName(String currentFieldName) throws JsonGenerationException;

      JsonNode build();
   }
}
