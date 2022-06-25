package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import java.io.Serializable;

public abstract class BaseJsonNode extends JsonNode implements Serializable {
   private static final long serialVersionUID = 1L;

   Object writeReplace() {
      return NodeSerialization.from(this);
   }

   protected BaseJsonNode() {
   }

   @Override
   public final JsonNode findPath(String fieldName) {
      JsonNode value = this.findValue(fieldName);
      return (JsonNode)(value == null ? MissingNode.getInstance() : value);
   }

   public abstract int hashCode();

   @Override
   public JsonNode required(String fieldName) {
      return this._reportRequiredViolation("Node of type `%s` has no fields", new Object[]{this.getClass().getSimpleName()});
   }

   @Override
   public JsonNode required(int index) {
      return this._reportRequiredViolation("Node of type `%s` has no indexed values", new Object[]{this.getClass().getSimpleName()});
   }

   @Override
   public JsonParser traverse() {
      return new TreeTraversingParser(this);
   }

   @Override
   public JsonParser traverse(ObjectCodec codec) {
      return new TreeTraversingParser(this, codec);
   }

   @Override
   public abstract JsonToken asToken();

   @Override
   public JsonParser.NumberType numberType() {
      return null;
   }

   @Override
   public abstract void serialize(JsonGenerator var1, SerializerProvider var2) throws IOException;

   @Override
   public abstract void serializeWithType(JsonGenerator var1, SerializerProvider var2, TypeSerializer var3) throws IOException;

   @Override
   public String toString() {
      return InternalNodeMapper.nodeToString(this);
   }

   @Override
   public String toPrettyString() {
      return InternalNodeMapper.nodeToPrettyString(this);
   }
}
