package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class StdArraySerializers {
   protected static final HashMap<String, JsonSerializer<?>> _arraySerializers = new HashMap();

   protected StdArraySerializers() {
   }

   public static JsonSerializer<?> findStandardImpl(Class<?> cls) {
      return (JsonSerializer<?>)_arraySerializers.get(cls.getName());
   }

   static {
      _arraySerializers.put(boolean[].class.getName(), new StdArraySerializers.BooleanArraySerializer());
      _arraySerializers.put(byte[].class.getName(), new ByteArraySerializer());
      _arraySerializers.put(char[].class.getName(), new StdArraySerializers.CharArraySerializer());
      _arraySerializers.put(short[].class.getName(), new StdArraySerializers.ShortArraySerializer());
      _arraySerializers.put(int[].class.getName(), new StdArraySerializers.IntArraySerializer());
      _arraySerializers.put(long[].class.getName(), new StdArraySerializers.LongArraySerializer());
      _arraySerializers.put(float[].class.getName(), new StdArraySerializers.FloatArraySerializer());
      _arraySerializers.put(double[].class.getName(), new StdArraySerializers.DoubleArraySerializer());
   }

   @JacksonStdImpl
   public static class BooleanArraySerializer extends ArraySerializerBase<boolean[]> {
      private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Boolean.class);

      public BooleanArraySerializer() {
         super(boolean[].class);
      }

      protected BooleanArraySerializer(StdArraySerializers.BooleanArraySerializer src, BeanProperty prop, Boolean unwrapSingle) {
         super(src, prop, unwrapSingle);
      }

      @Override
      public JsonSerializer<?> _withResolved(BeanProperty prop, Boolean unwrapSingle) {
         return new StdArraySerializers.BooleanArraySerializer(this, prop, unwrapSingle);
      }

      @Override
      public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
         return this;
      }

      @Override
      public JavaType getContentType() {
         return VALUE_TYPE;
      }

      @Override
      public JsonSerializer<?> getContentSerializer() {
         return null;
      }

      public boolean isEmpty(SerializerProvider prov, boolean[] value) {
         return value.length == 0;
      }

      public boolean hasSingleElement(boolean[] value) {
         return value.length == 1;
      }

      public final void serialize(boolean[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int len = value.length;
         if (len == 1 && this._shouldUnwrapSingle(provider)) {
            this.serializeContents(value, g, provider);
         } else {
            g.writeStartArray(value, len);
            this.serializeContents(value, g, provider);
            g.writeEndArray();
         }
      }

      public void serializeContents(boolean[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            g.writeBoolean(value[i]);
         }

      }

      @Override
      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         o.set("items", this.createSchemaNode("boolean"));
         return o;
      }

      @Override
      public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
         this.visitArrayFormat(visitor, typeHint, JsonFormatTypes.BOOLEAN);
      }
   }

   @JacksonStdImpl
   public static class CharArraySerializer extends StdSerializer<char[]> {
      public CharArraySerializer() {
         super(char[].class);
      }

      public boolean isEmpty(SerializerProvider prov, char[] value) {
         return value.length == 0;
      }

      public void serialize(char[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         if (provider.isEnabled(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)) {
            g.writeStartArray(value, value.length);
            this._writeArrayContents(g, value);
            g.writeEndArray();
         } else {
            g.writeString(value, 0, value.length);
         }

      }

      public void serializeWithType(char[] value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
         boolean asArray = provider.isEnabled(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS);
         WritableTypeId typeIdDef;
         if (asArray) {
            typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, JsonToken.START_ARRAY));
            this._writeArrayContents(g, value);
         } else {
            typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, JsonToken.VALUE_STRING));
            g.writeString(value, 0, value.length);
         }

         typeSer.writeTypeSuffix(g, typeIdDef);
      }

      private final void _writeArrayContents(JsonGenerator g, char[] value) throws IOException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            g.writeString(value, i, 1);
         }

      }

      @Override
      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         ObjectNode itemSchema = this.createSchemaNode("string");
         itemSchema.put("type", "string");
         return o.set("items", itemSchema);
      }

      @Override
      public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
         this.visitArrayFormat(visitor, typeHint, JsonFormatTypes.STRING);
      }
   }

   @JacksonStdImpl
   public static class DoubleArraySerializer extends ArraySerializerBase<double[]> {
      private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Double.TYPE);

      public DoubleArraySerializer() {
         super(double[].class);
      }

      protected DoubleArraySerializer(StdArraySerializers.DoubleArraySerializer src, BeanProperty prop, Boolean unwrapSingle) {
         super(src, prop, unwrapSingle);
      }

      @Override
      public JsonSerializer<?> _withResolved(BeanProperty prop, Boolean unwrapSingle) {
         return new StdArraySerializers.DoubleArraySerializer(this, prop, unwrapSingle);
      }

      @Override
      public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
         return this;
      }

      @Override
      public JavaType getContentType() {
         return VALUE_TYPE;
      }

      @Override
      public JsonSerializer<?> getContentSerializer() {
         return null;
      }

      public boolean isEmpty(SerializerProvider prov, double[] value) {
         return value.length == 0;
      }

      public boolean hasSingleElement(double[] value) {
         return value.length == 1;
      }

      public final void serialize(double[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int len = value.length;
         if (len == 1 && this._shouldUnwrapSingle(provider)) {
            this.serializeContents(value, g, provider);
         } else {
            g.writeArray(value, 0, value.length);
         }
      }

      public void serializeContents(double[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            g.writeNumber(value[i]);
         }

      }

      @Override
      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("array", true).set("items", this.createSchemaNode("number"));
      }

      @Override
      public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
         this.visitArrayFormat(visitor, typeHint, JsonFormatTypes.NUMBER);
      }
   }

   @JacksonStdImpl
   public static class FloatArraySerializer extends StdArraySerializers.TypedPrimitiveArraySerializer<float[]> {
      private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Float.TYPE);

      public FloatArraySerializer() {
         super(float[].class);
      }

      public FloatArraySerializer(StdArraySerializers.FloatArraySerializer src, BeanProperty prop, Boolean unwrapSingle) {
         super(src, prop, unwrapSingle);
      }

      @Override
      public JsonSerializer<?> _withResolved(BeanProperty prop, Boolean unwrapSingle) {
         return new StdArraySerializers.FloatArraySerializer(this, prop, unwrapSingle);
      }

      @Override
      public JavaType getContentType() {
         return VALUE_TYPE;
      }

      @Override
      public JsonSerializer<?> getContentSerializer() {
         return null;
      }

      public boolean isEmpty(SerializerProvider prov, float[] value) {
         return value.length == 0;
      }

      public boolean hasSingleElement(float[] value) {
         return value.length == 1;
      }

      public final void serialize(float[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int len = value.length;
         if (len == 1 && this._shouldUnwrapSingle(provider)) {
            this.serializeContents(value, g, provider);
         } else {
            g.writeStartArray(value, len);
            this.serializeContents(value, g, provider);
            g.writeEndArray();
         }
      }

      public void serializeContents(float[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            g.writeNumber(value[i]);
         }

      }

      @Override
      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("array", true).set("items", this.createSchemaNode("number"));
      }

      @Override
      public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
         this.visitArrayFormat(visitor, typeHint, JsonFormatTypes.NUMBER);
      }
   }

   @JacksonStdImpl
   public static class IntArraySerializer extends ArraySerializerBase<int[]> {
      private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Integer.TYPE);

      public IntArraySerializer() {
         super(int[].class);
      }

      protected IntArraySerializer(StdArraySerializers.IntArraySerializer src, BeanProperty prop, Boolean unwrapSingle) {
         super(src, prop, unwrapSingle);
      }

      @Override
      public JsonSerializer<?> _withResolved(BeanProperty prop, Boolean unwrapSingle) {
         return new StdArraySerializers.IntArraySerializer(this, prop, unwrapSingle);
      }

      @Override
      public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
         return this;
      }

      @Override
      public JavaType getContentType() {
         return VALUE_TYPE;
      }

      @Override
      public JsonSerializer<?> getContentSerializer() {
         return null;
      }

      public boolean isEmpty(SerializerProvider prov, int[] value) {
         return value.length == 0;
      }

      public boolean hasSingleElement(int[] value) {
         return value.length == 1;
      }

      public final void serialize(int[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int len = value.length;
         if (len == 1 && this._shouldUnwrapSingle(provider)) {
            this.serializeContents(value, g, provider);
         } else {
            g.writeArray(value, 0, value.length);
         }
      }

      public void serializeContents(int[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            g.writeNumber(value[i]);
         }

      }

      @Override
      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("array", true).set("items", this.createSchemaNode("integer"));
      }

      @Override
      public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
         this.visitArrayFormat(visitor, typeHint, JsonFormatTypes.INTEGER);
      }
   }

   @JacksonStdImpl
   public static class LongArraySerializer extends StdArraySerializers.TypedPrimitiveArraySerializer<long[]> {
      private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Long.TYPE);

      public LongArraySerializer() {
         super(long[].class);
      }

      public LongArraySerializer(StdArraySerializers.LongArraySerializer src, BeanProperty prop, Boolean unwrapSingle) {
         super(src, prop, unwrapSingle);
      }

      @Override
      public JsonSerializer<?> _withResolved(BeanProperty prop, Boolean unwrapSingle) {
         return new StdArraySerializers.LongArraySerializer(this, prop, unwrapSingle);
      }

      @Override
      public JavaType getContentType() {
         return VALUE_TYPE;
      }

      @Override
      public JsonSerializer<?> getContentSerializer() {
         return null;
      }

      public boolean isEmpty(SerializerProvider prov, long[] value) {
         return value.length == 0;
      }

      public boolean hasSingleElement(long[] value) {
         return value.length == 1;
      }

      public final void serialize(long[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int len = value.length;
         if (len == 1 && this._shouldUnwrapSingle(provider)) {
            this.serializeContents(value, g, provider);
         } else {
            g.writeArray(value, 0, value.length);
         }
      }

      public void serializeContents(long[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            g.writeNumber(value[i]);
         }

      }

      @Override
      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         return this.createSchemaNode("array", true).set("items", this.createSchemaNode("number", true));
      }

      @Override
      public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
         this.visitArrayFormat(visitor, typeHint, JsonFormatTypes.NUMBER);
      }
   }

   @JacksonStdImpl
   public static class ShortArraySerializer extends StdArraySerializers.TypedPrimitiveArraySerializer<short[]> {
      private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Short.TYPE);

      public ShortArraySerializer() {
         super(short[].class);
      }

      public ShortArraySerializer(StdArraySerializers.ShortArraySerializer src, BeanProperty prop, Boolean unwrapSingle) {
         super(src, prop, unwrapSingle);
      }

      @Override
      public JsonSerializer<?> _withResolved(BeanProperty prop, Boolean unwrapSingle) {
         return new StdArraySerializers.ShortArraySerializer(this, prop, unwrapSingle);
      }

      @Override
      public JavaType getContentType() {
         return VALUE_TYPE;
      }

      @Override
      public JsonSerializer<?> getContentSerializer() {
         return null;
      }

      public boolean isEmpty(SerializerProvider prov, short[] value) {
         return value.length == 0;
      }

      public boolean hasSingleElement(short[] value) {
         return value.length == 1;
      }

      public final void serialize(short[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int len = value.length;
         if (len == 1 && this._shouldUnwrapSingle(provider)) {
            this.serializeContents(value, g, provider);
         } else {
            g.writeStartArray(value, len);
            this.serializeContents(value, g, provider);
            g.writeEndArray();
         }
      }

      public void serializeContents(short[] value, JsonGenerator g, SerializerProvider provider) throws IOException {
         int i = 0;

         for(int len = value.length; i < len; ++i) {
            g.writeNumber(value[i]);
         }

      }

      @Override
      public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
         ObjectNode o = this.createSchemaNode("array", true);
         return o.set("items", this.createSchemaNode("integer"));
      }

      @Override
      public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
         this.visitArrayFormat(visitor, typeHint, JsonFormatTypes.INTEGER);
      }
   }

   protected abstract static class TypedPrimitiveArraySerializer<T> extends ArraySerializerBase<T> {
      protected TypedPrimitiveArraySerializer(Class<T> cls) {
         super(cls);
      }

      protected TypedPrimitiveArraySerializer(StdArraySerializers.TypedPrimitiveArraySerializer<T> src, BeanProperty prop, Boolean unwrapSingle) {
         super(src, prop, unwrapSingle);
      }

      @Override
      public final ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
         return this;
      }
   }
}
