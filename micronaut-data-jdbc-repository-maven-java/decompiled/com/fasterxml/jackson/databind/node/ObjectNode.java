package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.util.RawValue;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ObjectNode extends ContainerNode<ObjectNode> implements Serializable {
   private static final long serialVersionUID = 1L;
   protected final Map<String, JsonNode> _children;

   public ObjectNode(JsonNodeFactory nc) {
      super(nc);
      this._children = new LinkedHashMap();
   }

   public ObjectNode(JsonNodeFactory nc, Map<String, JsonNode> kids) {
      super(nc);
      this._children = kids;
   }

   @Override
   protected JsonNode _at(JsonPointer ptr) {
      return this.get(ptr.getMatchingProperty());
   }

   public ObjectNode deepCopy() {
      ObjectNode ret = new ObjectNode(this._nodeFactory);

      for(Entry<String, JsonNode> entry : this._children.entrySet()) {
         ret._children.put(entry.getKey(), ((JsonNode)entry.getValue()).deepCopy());
      }

      return ret;
   }

   @Override
   public boolean isEmpty(SerializerProvider serializers) {
      return this._children.isEmpty();
   }

   @Override
   public JsonNodeType getNodeType() {
      return JsonNodeType.OBJECT;
   }

   @Override
   public final boolean isObject() {
      return true;
   }

   @Override
   public JsonToken asToken() {
      return JsonToken.START_OBJECT;
   }

   @Override
   public int size() {
      return this._children.size();
   }

   @Override
   public boolean isEmpty() {
      return this._children.isEmpty();
   }

   @Override
   public Iterator<JsonNode> elements() {
      return this._children.values().iterator();
   }

   @Override
   public JsonNode get(int index) {
      return null;
   }

   @Override
   public JsonNode get(String propertyName) {
      return (JsonNode)this._children.get(propertyName);
   }

   @Override
   public Iterator<String> fieldNames() {
      return this._children.keySet().iterator();
   }

   @Override
   public JsonNode path(int index) {
      return MissingNode.getInstance();
   }

   @Override
   public JsonNode path(String propertyName) {
      JsonNode n = (JsonNode)this._children.get(propertyName);
      return (JsonNode)(n != null ? n : MissingNode.getInstance());
   }

   @Override
   public JsonNode required(String propertyName) {
      JsonNode n = (JsonNode)this._children.get(propertyName);
      return n != null ? n : this._reportRequiredViolation("No value for property '%s' of `ObjectNode`", new Object[]{propertyName});
   }

   @Override
   public Iterator<Entry<String, JsonNode>> fields() {
      return this._children.entrySet().iterator();
   }

   public ObjectNode with(String propertyName) {
      JsonNode n = (JsonNode)this._children.get(propertyName);
      if (n != null) {
         if (n instanceof ObjectNode) {
            return (ObjectNode)n;
         } else {
            throw new UnsupportedOperationException(
               "Property '" + propertyName + "' has value that is not of type ObjectNode (but " + n.getClass().getName() + ")"
            );
         }
      } else {
         ObjectNode result = this.objectNode();
         this._children.put(propertyName, result);
         return result;
      }
   }

   public ArrayNode withArray(String propertyName) {
      JsonNode n = (JsonNode)this._children.get(propertyName);
      if (n != null) {
         if (n instanceof ArrayNode) {
            return (ArrayNode)n;
         } else {
            throw new UnsupportedOperationException(
               "Property '" + propertyName + "' has value that is not of type ArrayNode (but " + n.getClass().getName() + ")"
            );
         }
      } else {
         ArrayNode result = this.arrayNode();
         this._children.put(propertyName, result);
         return result;
      }
   }

   @Override
   public boolean equals(Comparator<JsonNode> comparator, JsonNode o) {
      if (!(o instanceof ObjectNode)) {
         return false;
      } else {
         ObjectNode other = (ObjectNode)o;
         Map<String, JsonNode> m1 = this._children;
         Map<String, JsonNode> m2 = other._children;
         int len = m1.size();
         if (m2.size() != len) {
            return false;
         } else {
            for(Entry<String, JsonNode> entry : m1.entrySet()) {
               JsonNode v2 = (JsonNode)m2.get(entry.getKey());
               if (v2 == null || !((JsonNode)entry.getValue()).equals(comparator, v2)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   @Override
   public JsonNode findValue(String propertyName) {
      for(Entry<String, JsonNode> entry : this._children.entrySet()) {
         if (propertyName.equals(entry.getKey())) {
            return (JsonNode)entry.getValue();
         }

         JsonNode value = ((JsonNode)entry.getValue()).findValue(propertyName);
         if (value != null) {
            return value;
         }
      }

      return null;
   }

   @Override
   public List<JsonNode> findValues(String propertyName, List<JsonNode> foundSoFar) {
      for(Entry<String, JsonNode> entry : this._children.entrySet()) {
         if (propertyName.equals(entry.getKey())) {
            if (foundSoFar == null) {
               foundSoFar = new ArrayList();
            }

            foundSoFar.add(entry.getValue());
         } else {
            foundSoFar = ((JsonNode)entry.getValue()).findValues(propertyName, foundSoFar);
         }
      }

      return foundSoFar;
   }

   @Override
   public List<String> findValuesAsText(String propertyName, List<String> foundSoFar) {
      for(Entry<String, JsonNode> entry : this._children.entrySet()) {
         if (propertyName.equals(entry.getKey())) {
            if (foundSoFar == null) {
               foundSoFar = new ArrayList();
            }

            foundSoFar.add(((JsonNode)entry.getValue()).asText());
         } else {
            foundSoFar = ((JsonNode)entry.getValue()).findValuesAsText(propertyName, foundSoFar);
         }
      }

      return foundSoFar;
   }

   public ObjectNode findParent(String propertyName) {
      for(Entry<String, JsonNode> entry : this._children.entrySet()) {
         if (propertyName.equals(entry.getKey())) {
            return this;
         }

         JsonNode value = ((JsonNode)entry.getValue()).findParent(propertyName);
         if (value != null) {
            return (ObjectNode)value;
         }
      }

      return null;
   }

   @Override
   public List<JsonNode> findParents(String propertyName, List<JsonNode> foundSoFar) {
      for(Entry<String, JsonNode> entry : this._children.entrySet()) {
         if (propertyName.equals(entry.getKey())) {
            if (foundSoFar == null) {
               foundSoFar = new ArrayList();
            }

            foundSoFar.add(this);
         } else {
            foundSoFar = ((JsonNode)entry.getValue()).findParents(propertyName, foundSoFar);
         }
      }

      return foundSoFar;
   }

   @Override
   public void serialize(JsonGenerator g, SerializerProvider provider) throws IOException {
      boolean trimEmptyArray = provider != null && !provider.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
      g.writeStartObject(this);

      for(Entry<String, JsonNode> en : this._children.entrySet()) {
         BaseJsonNode value = (BaseJsonNode)en.getValue();
         if (!trimEmptyArray || !value.isArray() || !value.isEmpty(provider)) {
            g.writeFieldName((String)en.getKey());
            value.serialize(g, provider);
         }
      }

      g.writeEndObject();
   }

   @Override
   public void serializeWithType(JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      boolean trimEmptyArray = provider != null && !provider.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(this, JsonToken.START_OBJECT));

      for(Entry<String, JsonNode> en : this._children.entrySet()) {
         BaseJsonNode value = (BaseJsonNode)en.getValue();
         if (!trimEmptyArray || !value.isArray() || !value.isEmpty(provider)) {
            g.writeFieldName((String)en.getKey());
            value.serialize(g, provider);
         }
      }

      typeSer.writeTypeSuffix(g, typeIdDef);
   }

   public <T extends JsonNode> T set(String propertyName, JsonNode value) {
      if (value == null) {
         value = this.nullNode();
      }

      this._children.put(propertyName, value);
      return (T)this;
   }

   public <T extends JsonNode> T setAll(Map<String, ? extends JsonNode> properties) {
      for(Entry<String, ? extends JsonNode> en : properties.entrySet()) {
         JsonNode n = (JsonNode)en.getValue();
         if (n == null) {
            n = this.nullNode();
         }

         this._children.put(en.getKey(), n);
      }

      return (T)this;
   }

   public <T extends JsonNode> T setAll(ObjectNode other) {
      this._children.putAll(other._children);
      return (T)this;
   }

   public JsonNode replace(String propertyName, JsonNode value) {
      if (value == null) {
         value = this.nullNode();
      }

      return (JsonNode)this._children.put(propertyName, value);
   }

   public <T extends JsonNode> T without(String propertyName) {
      this._children.remove(propertyName);
      return (T)this;
   }

   public <T extends JsonNode> T without(Collection<String> propertyNames) {
      this._children.keySet().removeAll(propertyNames);
      return (T)this;
   }

   @Deprecated
   public JsonNode put(String propertyName, JsonNode value) {
      if (value == null) {
         value = this.nullNode();
      }

      return (JsonNode)this._children.put(propertyName, value);
   }

   public JsonNode putIfAbsent(String propertyName, JsonNode value) {
      if (value == null) {
         value = this.nullNode();
      }

      return (JsonNode)this._children.putIfAbsent(propertyName, value);
   }

   public JsonNode remove(String propertyName) {
      return (JsonNode)this._children.remove(propertyName);
   }

   public ObjectNode remove(Collection<String> propertyNames) {
      this._children.keySet().removeAll(propertyNames);
      return this;
   }

   public ObjectNode removeAll() {
      this._children.clear();
      return this;
   }

   @Deprecated
   public JsonNode putAll(Map<String, ? extends JsonNode> properties) {
      return this.setAll(properties);
   }

   @Deprecated
   public JsonNode putAll(ObjectNode other) {
      return this.setAll(other);
   }

   public ObjectNode retain(Collection<String> propertyNames) {
      this._children.keySet().retainAll(propertyNames);
      return this;
   }

   public ObjectNode retain(String... propertyNames) {
      return this.retain(Arrays.asList(propertyNames));
   }

   public ArrayNode putArray(String propertyName) {
      ArrayNode n = this.arrayNode();
      this._put(propertyName, n);
      return n;
   }

   public ObjectNode putObject(String propertyName) {
      ObjectNode n = this.objectNode();
      this._put(propertyName, n);
      return n;
   }

   public ObjectNode putPOJO(String propertyName, Object pojo) {
      return this._put(propertyName, this.pojoNode(pojo));
   }

   public ObjectNode putRawValue(String propertyName, RawValue raw) {
      return this._put(propertyName, this.rawValueNode(raw));
   }

   public ObjectNode putNull(String propertyName) {
      this._children.put(propertyName, this.nullNode());
      return this;
   }

   public ObjectNode put(String propertyName, short v) {
      return this._put(propertyName, this.numberNode(v));
   }

   public ObjectNode put(String fieldName, Short v) {
      return this._put(fieldName, (JsonNode)(v == null ? this.nullNode() : this.numberNode(v)));
   }

   public ObjectNode put(String fieldName, int v) {
      return this._put(fieldName, this.numberNode(v));
   }

   public ObjectNode put(String fieldName, Integer v) {
      return this._put(fieldName, (JsonNode)(v == null ? this.nullNode() : this.numberNode(v)));
   }

   public ObjectNode put(String fieldName, long v) {
      return this._put(fieldName, this.numberNode(v));
   }

   public ObjectNode put(String fieldName, Long v) {
      return this._put(fieldName, (JsonNode)(v == null ? this.nullNode() : this.numberNode(v)));
   }

   public ObjectNode put(String fieldName, float v) {
      return this._put(fieldName, this.numberNode(v));
   }

   public ObjectNode put(String fieldName, Float v) {
      return this._put(fieldName, (JsonNode)(v == null ? this.nullNode() : this.numberNode(v)));
   }

   public ObjectNode put(String fieldName, double v) {
      return this._put(fieldName, this.numberNode(v));
   }

   public ObjectNode put(String fieldName, Double v) {
      return this._put(fieldName, (JsonNode)(v == null ? this.nullNode() : this.numberNode(v)));
   }

   public ObjectNode put(String fieldName, BigDecimal v) {
      return this._put(fieldName, (JsonNode)(v == null ? this.nullNode() : this.numberNode(v)));
   }

   public ObjectNode put(String fieldName, BigInteger v) {
      return this._put(fieldName, (JsonNode)(v == null ? this.nullNode() : this.numberNode(v)));
   }

   public ObjectNode put(String fieldName, String v) {
      return this._put(fieldName, (JsonNode)(v == null ? this.nullNode() : this.textNode(v)));
   }

   public ObjectNode put(String fieldName, boolean v) {
      return this._put(fieldName, this.booleanNode(v));
   }

   public ObjectNode put(String fieldName, Boolean v) {
      return this._put(fieldName, (JsonNode)(v == null ? this.nullNode() : this.booleanNode(v)));
   }

   public ObjectNode put(String fieldName, byte[] v) {
      return this._put(fieldName, (JsonNode)(v == null ? this.nullNode() : this.binaryNode(v)));
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else {
         return o instanceof ObjectNode ? this._childrenEqual((ObjectNode)o) : false;
      }
   }

   protected boolean _childrenEqual(ObjectNode other) {
      return this._children.equals(other._children);
   }

   @Override
   public int hashCode() {
      return this._children.hashCode();
   }

   protected ObjectNode _put(String fieldName, JsonNode value) {
      this._children.put(fieldName, value);
      return this;
   }
}
