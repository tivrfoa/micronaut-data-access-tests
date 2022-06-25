package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.StreamReadCapability;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.databind.util.RawValue;
import java.io.IOException;
import java.util.Arrays;

abstract class BaseNodeDeserializer<T extends JsonNode> extends StdDeserializer<T> {
   protected final Boolean _supportsUpdates;

   public BaseNodeDeserializer(Class<T> vc, Boolean supportsUpdates) {
      super(vc);
      this._supportsUpdates = supportsUpdates;
   }

   @Override
   public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
      return typeDeserializer.deserializeTypedFromAny(p, ctxt);
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Untyped;
   }

   @Override
   public boolean isCachable() {
      return true;
   }

   @Override
   public Boolean supportsUpdate(DeserializationConfig config) {
      return this._supportsUpdates;
   }

   protected void _handleDuplicateField(
      JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory, String fieldName, ObjectNode objectNode, JsonNode oldValue, JsonNode newValue
   ) throws IOException {
      if (ctxt.isEnabled(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)) {
         ctxt.reportInputMismatch(
            JsonNode.class, "Duplicate field '%s' for `ObjectNode`: not allowed when `DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY` enabled", fieldName
         );
      }

      if (ctxt.isEnabled(StreamReadCapability.DUPLICATE_PROPERTIES)) {
         if (oldValue.isArray()) {
            ((ArrayNode)oldValue).add(newValue);
            objectNode.replace(fieldName, oldValue);
         } else {
            ArrayNode arr = nodeFactory.arrayNode();
            arr.add(oldValue);
            arr.add(newValue);
            objectNode.replace(fieldName, arr);
         }
      }

   }

   protected final ObjectNode _deserializeObjectAtName(
      JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory, BaseNodeDeserializer.ContainerStack stack
   ) throws IOException {
      ObjectNode node = nodeFactory.objectNode();

      for(String key = p.currentName(); key != null; key = p.nextFieldName()) {
         JsonToken t = p.nextToken();
         if (t == null) {
            t = JsonToken.NOT_AVAILABLE;
         }

         JsonNode value;
         switch(t.id()) {
            case 1:
               value = this._deserializeContainerNoRecursion(p, ctxt, nodeFactory, stack, nodeFactory.objectNode());
               break;
            case 3:
               value = this._deserializeContainerNoRecursion(p, ctxt, nodeFactory, stack, nodeFactory.arrayNode());
               break;
            default:
               value = this._deserializeAnyScalar(p, ctxt);
         }

         JsonNode old = node.replace(key, value);
         if (old != null) {
            this._handleDuplicateField(p, ctxt, nodeFactory, key, node, old, value);
         }
      }

      return node;
   }

   protected final JsonNode updateObject(JsonParser p, DeserializationContext ctxt, ObjectNode node, BaseNodeDeserializer.ContainerStack stack) throws IOException {
      String key;
      if (p.isExpectedStartObjectToken()) {
         key = p.nextFieldName();
      } else {
         if (!p.hasToken(JsonToken.FIELD_NAME)) {
            return this.deserialize(p, ctxt);
         }

         key = p.currentName();
      }

      for(JsonNodeFactory nodeFactory = ctxt.getNodeFactory(); key != null; key = p.nextFieldName()) {
         JsonToken t = p.nextToken();
         JsonNode old = node.get(key);
         if (old != null) {
            if (old instanceof ObjectNode) {
               if (t == JsonToken.START_OBJECT) {
                  JsonNode newValue = this.updateObject(p, ctxt, (ObjectNode)old, stack);
                  if (newValue != old) {
                     node.set(key, newValue);
                  }
                  continue;
               }
            } else if (old instanceof ArrayNode && t == JsonToken.START_ARRAY) {
               this._deserializeContainerNoRecursion(p, ctxt, nodeFactory, stack, (ArrayNode)old);
               continue;
            }
         }

         if (t == null) {
            t = JsonToken.NOT_AVAILABLE;
         }

         JsonNode value;
         switch(t.id()) {
            case 1:
               value = this._deserializeContainerNoRecursion(p, ctxt, nodeFactory, stack, nodeFactory.objectNode());
               break;
            case 2:
            case 4:
            case 5:
            case 8:
            default:
               value = this._deserializeRareScalar(p, ctxt);
               break;
            case 3:
               value = this._deserializeContainerNoRecursion(p, ctxt, nodeFactory, stack, nodeFactory.arrayNode());
               break;
            case 6:
               value = nodeFactory.textNode(p.getText());
               break;
            case 7:
               value = this._fromInt(p, ctxt, nodeFactory);
               break;
            case 9:
               value = nodeFactory.booleanNode(true);
               break;
            case 10:
               value = nodeFactory.booleanNode(false);
               break;
            case 11:
               value = nodeFactory.nullNode();
         }

         node.set(key, value);
      }

      return node;
   }

   protected final ContainerNode<?> _deserializeContainerNoRecursion(
      JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory, BaseNodeDeserializer.ContainerStack stack, ContainerNode<?> root
   ) throws IOException {
      ContainerNode<?> curr = root;
      int intCoercionFeats = ctxt.getDeserializationFeatures() & F_MASK_INT_COERCIONS;

      label86:
      do {
         if (!(curr instanceof ObjectNode)) {
            ArrayNode currArray = (ArrayNode)curr;

            label67:
            while(true) {
               JsonToken t = p.nextToken();
               if (t == null) {
                  t = JsonToken.NOT_AVAILABLE;
               }

               switch(t.id()) {
                  case 1:
                     stack.push(curr);
                     curr = nodeFactory.objectNode();
                     currArray.add((JsonNode)curr);
                     continue label86;
                  case 2:
                  case 5:
                  default:
                     currArray.add(this._deserializeRareScalar(p, ctxt));
                     break;
                  case 3:
                     stack.push(curr);
                     curr = nodeFactory.arrayNode();
                     currArray.add((JsonNode)curr);
                     continue label86;
                  case 4:
                     break label67;
                  case 6:
                     currArray.add((JsonNode)nodeFactory.textNode(p.getText()));
                     break;
                  case 7:
                     currArray.add(this._fromInt(p, intCoercionFeats, nodeFactory));
                     break;
                  case 8:
                     currArray.add(this._fromFloat(p, ctxt, nodeFactory));
                     break;
                  case 9:
                     currArray.add((JsonNode)nodeFactory.booleanNode(true));
                     break;
                  case 10:
                     currArray.add((JsonNode)nodeFactory.booleanNode(false));
                     break;
                  case 11:
                     currArray.add((JsonNode)nodeFactory.nullNode());
               }
            }
         } else {
            ObjectNode currObject = (ObjectNode)curr;

            for(String propName = p.nextFieldName(); propName != null; propName = p.nextFieldName()) {
               JsonToken t = p.nextToken();
               if (t == null) {
                  t = JsonToken.NOT_AVAILABLE;
               }

               JsonNode value;
               switch(t.id()) {
                  case 1:
                     ObjectNode newOb = nodeFactory.objectNode();
                     JsonNode old = currObject.replace(propName, newOb);
                     if (old != null) {
                        this._handleDuplicateField(p, ctxt, nodeFactory, propName, currObject, old, newOb);
                     }

                     stack.push(curr);
                     currObject = newOb;
                     curr = newOb;
                     continue;
                  case 2:
                  case 4:
                  case 5:
                  default:
                     value = this._deserializeRareScalar(p, ctxt);
                     break;
                  case 3:
                     ArrayNode newOb = nodeFactory.arrayNode();
                     JsonNode old = currObject.replace(propName, newOb);
                     if (old != null) {
                        this._handleDuplicateField(p, ctxt, nodeFactory, propName, currObject, old, newOb);
                     }

                     stack.push(curr);
                     curr = newOb;
                     continue label86;
                  case 6:
                     value = nodeFactory.textNode(p.getText());
                     break;
                  case 7:
                     value = this._fromInt(p, intCoercionFeats, nodeFactory);
                     break;
                  case 8:
                     value = this._fromFloat(p, ctxt, nodeFactory);
                     break;
                  case 9:
                     value = nodeFactory.booleanNode(true);
                     break;
                  case 10:
                     value = nodeFactory.booleanNode(false);
                     break;
                  case 11:
                     value = nodeFactory.nullNode();
               }

               JsonNode old = currObject.replace(propName, value);
               if (old != null) {
                  this._handleDuplicateField(p, ctxt, nodeFactory, propName, currObject, old, value);
               }
            }
         }

         curr = stack.popOrNull();
      } while(curr != null);

      return root;
   }

   protected final JsonNode _deserializeAnyScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNodeFactory nodeF = ctxt.getNodeFactory();
      switch(p.currentTokenId()) {
         case 2:
            return nodeF.objectNode();
         case 3:
         case 4:
         case 5:
         default:
            return (JsonNode)ctxt.handleUnexpectedToken(this.handledType(), p);
         case 6:
            return nodeF.textNode(p.getText());
         case 7:
            return this._fromInt(p, ctxt, nodeF);
         case 8:
            return this._fromFloat(p, ctxt, nodeF);
         case 9:
            return nodeF.booleanNode(true);
         case 10:
            return nodeF.booleanNode(false);
         case 11:
            return nodeF.nullNode();
         case 12:
            return this._fromEmbedded(p, ctxt);
      }
   }

   protected final JsonNode _deserializeRareScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
      switch(p.currentTokenId()) {
         case 2:
            return ctxt.getNodeFactory().objectNode();
         case 8:
            return this._fromFloat(p, ctxt, ctxt.getNodeFactory());
         case 12:
            return this._fromEmbedded(p, ctxt);
         default:
            return (JsonNode)ctxt.handleUnexpectedToken(this.handledType(), p);
      }
   }

   protected final JsonNode _fromInt(JsonParser p, int coercionFeatures, JsonNodeFactory nodeFactory) throws IOException {
      if (coercionFeatures != 0) {
         return (JsonNode)(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS.enabledIn(coercionFeatures)
            ? nodeFactory.numberNode(p.getBigIntegerValue())
            : nodeFactory.numberNode(p.getLongValue()));
      } else {
         JsonParser.NumberType nt = p.getNumberType();
         if (nt == JsonParser.NumberType.INT) {
            return nodeFactory.numberNode(p.getIntValue());
         } else {
            return (JsonNode)(nt == JsonParser.NumberType.LONG ? nodeFactory.numberNode(p.getLongValue()) : nodeFactory.numberNode(p.getBigIntegerValue()));
         }
      }
   }

   protected final JsonNode _fromInt(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
      int feats = ctxt.getDeserializationFeatures();
      JsonParser.NumberType nt;
      if ((feats & F_MASK_INT_COERCIONS) != 0) {
         if (DeserializationFeature.USE_BIG_INTEGER_FOR_INTS.enabledIn(feats)) {
            nt = JsonParser.NumberType.BIG_INTEGER;
         } else if (DeserializationFeature.USE_LONG_FOR_INTS.enabledIn(feats)) {
            nt = JsonParser.NumberType.LONG;
         } else {
            nt = p.getNumberType();
         }
      } else {
         nt = p.getNumberType();
      }

      if (nt == JsonParser.NumberType.INT) {
         return nodeFactory.numberNode(p.getIntValue());
      } else {
         return (JsonNode)(nt == JsonParser.NumberType.LONG ? nodeFactory.numberNode(p.getLongValue()) : nodeFactory.numberNode(p.getBigIntegerValue()));
      }
   }

   protected final JsonNode _fromFloat(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
      JsonParser.NumberType nt = p.getNumberType();
      if (nt == JsonParser.NumberType.BIG_DECIMAL) {
         return nodeFactory.numberNode(p.getDecimalValue());
      } else if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
         return (JsonNode)(p.isNaN() ? nodeFactory.numberNode(p.getDoubleValue()) : nodeFactory.numberNode(p.getDecimalValue()));
      } else {
         return nt == JsonParser.NumberType.FLOAT ? nodeFactory.numberNode(p.getFloatValue()) : nodeFactory.numberNode(p.getDoubleValue());
      }
   }

   protected final JsonNode _fromEmbedded(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNodeFactory nodeF = ctxt.getNodeFactory();
      Object ob = p.getEmbeddedObject();
      if (ob == null) {
         return nodeF.nullNode();
      } else {
         Class<?> type = ob.getClass();
         if (type == byte[].class) {
            return nodeF.binaryNode((byte[])ob);
         } else if (ob instanceof RawValue) {
            return nodeF.rawValueNode((RawValue)ob);
         } else {
            return (JsonNode)(ob instanceof JsonNode ? (JsonNode)ob : nodeF.pojoNode(ob));
         }
      }
   }

   static final class ContainerStack {
      private ContainerNode[] _stack;
      private int _top;
      private int _end;

      public ContainerStack() {
      }

      public int size() {
         return this._top;
      }

      public void push(ContainerNode node) {
         if (this._top < this._end) {
            this._stack[this._top++] = node;
         } else {
            if (this._stack == null) {
               this._end = 10;
               this._stack = new ContainerNode[this._end];
            } else {
               this._end += Math.min(4000, Math.max(20, this._end >> 1));
               this._stack = (ContainerNode[])Arrays.copyOf(this._stack, this._end);
            }

            this._stack[this._top++] = node;
         }
      }

      public ContainerNode popOrNull() {
         return this._top == 0 ? null : this._stack[--this._top];
      }
   }
}
