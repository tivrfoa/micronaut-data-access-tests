package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Map.Entry;

abstract class NodeCursor extends JsonStreamContext {
   protected final NodeCursor _parent;
   protected String _currentName;
   protected Object _currentValue;

   public NodeCursor(int contextType, NodeCursor p) {
      this._type = contextType;
      this._index = -1;
      this._parent = p;
   }

   public final NodeCursor getParent() {
      return this._parent;
   }

   @Override
   public final String getCurrentName() {
      return this._currentName;
   }

   public void overrideCurrentName(String name) {
      this._currentName = name;
   }

   @Override
   public Object getCurrentValue() {
      return this._currentValue;
   }

   @Override
   public void setCurrentValue(Object v) {
      this._currentValue = v;
   }

   public abstract JsonToken nextToken();

   public abstract JsonNode currentNode();

   public abstract NodeCursor startObject();

   public abstract NodeCursor startArray();

   public final NodeCursor iterateChildren() {
      JsonNode n = this.currentNode();
      if (n == null) {
         throw new IllegalStateException("No current node");
      } else if (n.isArray()) {
         return new NodeCursor.ArrayCursor(n, this);
      } else if (n.isObject()) {
         return new NodeCursor.ObjectCursor(n, this);
      } else {
         throw new IllegalStateException("Current node of type " + n.getClass().getName());
      }
   }

   protected static final class ArrayCursor extends NodeCursor {
      protected Iterator<JsonNode> _contents;
      protected JsonNode _currentElement;

      public ArrayCursor(JsonNode n, NodeCursor p) {
         super(1, p);
         this._contents = n.elements();
      }

      @Override
      public JsonToken nextToken() {
         if (!this._contents.hasNext()) {
            this._currentElement = null;
            return JsonToken.END_ARRAY;
         } else {
            ++this._index;
            this._currentElement = (JsonNode)this._contents.next();
            return this._currentElement.asToken();
         }
      }

      @Override
      public JsonNode currentNode() {
         return this._currentElement;
      }

      @Override
      public NodeCursor startArray() {
         return new NodeCursor.ArrayCursor(this._currentElement, this);
      }

      @Override
      public NodeCursor startObject() {
         return new NodeCursor.ObjectCursor(this._currentElement, this);
      }
   }

   protected static final class ObjectCursor extends NodeCursor {
      protected Iterator<Entry<String, JsonNode>> _contents;
      protected Entry<String, JsonNode> _current;
      protected boolean _needEntry;

      public ObjectCursor(JsonNode n, NodeCursor p) {
         super(2, p);
         this._contents = ((ObjectNode)n).fields();
         this._needEntry = true;
      }

      @Override
      public JsonToken nextToken() {
         if (this._needEntry) {
            if (!this._contents.hasNext()) {
               this._currentName = null;
               this._current = null;
               return JsonToken.END_OBJECT;
            } else {
               ++this._index;
               this._needEntry = false;
               this._current = (Entry)this._contents.next();
               this._currentName = this._current == null ? null : (String)this._current.getKey();
               return JsonToken.FIELD_NAME;
            }
         } else {
            this._needEntry = true;
            return ((JsonNode)this._current.getValue()).asToken();
         }
      }

      @Override
      public JsonNode currentNode() {
         return this._current == null ? null : (JsonNode)this._current.getValue();
      }

      @Override
      public NodeCursor startArray() {
         return new NodeCursor.ArrayCursor(this.currentNode(), this);
      }

      @Override
      public NodeCursor startObject() {
         return new NodeCursor.ObjectCursor(this.currentNode(), this);
      }
   }

   protected static final class RootCursor extends NodeCursor {
      protected JsonNode _node;
      protected boolean _done = false;

      public RootCursor(JsonNode n, NodeCursor p) {
         super(0, p);
         this._node = n;
      }

      @Override
      public void overrideCurrentName(String name) {
      }

      @Override
      public JsonToken nextToken() {
         if (!this._done) {
            ++this._index;
            this._done = true;
            return this._node.asToken();
         } else {
            this._node = null;
            return null;
         }
      }

      @Override
      public JsonNode currentNode() {
         return this._done ? this._node : null;
      }

      @Override
      public NodeCursor startArray() {
         return new NodeCursor.ArrayCursor(this._node, this);
      }

      @Override
      public NodeCursor startObject() {
         return new NodeCursor.ObjectCursor(this._node, this);
      }
   }
}
