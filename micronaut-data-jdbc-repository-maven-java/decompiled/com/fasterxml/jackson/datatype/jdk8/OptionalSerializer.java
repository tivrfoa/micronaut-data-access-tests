package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.util.Optional;

public class OptionalSerializer extends ReferenceTypeSerializer<Optional<?>> {
   private static final long serialVersionUID = 1L;

   protected OptionalSerializer(ReferenceType fullType, boolean staticTyping, TypeSerializer vts, JsonSerializer<Object> ser) {
      super(fullType, staticTyping, vts, ser);
   }

   protected OptionalSerializer(
      OptionalSerializer base,
      BeanProperty property,
      TypeSerializer vts,
      JsonSerializer<?> valueSer,
      NameTransformer unwrapper,
      Object suppressableValue,
      boolean suppressNulls
   ) {
      super(base, property, vts, valueSer, unwrapper, suppressableValue, suppressNulls);
   }

   @Override
   protected ReferenceTypeSerializer<Optional<?>> withResolved(BeanProperty prop, TypeSerializer vts, JsonSerializer<?> valueSer, NameTransformer unwrapper) {
      return new OptionalSerializer(this, prop, vts, valueSer, unwrapper, this._suppressableValue, this._suppressNulls);
   }

   @Override
   public ReferenceTypeSerializer<Optional<?>> withContentInclusion(Object suppressableValue, boolean suppressNulls) {
      return new OptionalSerializer(this, this._property, this._valueTypeSerializer, this._valueSerializer, this._unwrapper, suppressableValue, suppressNulls);
   }

   protected boolean _isValuePresent(Optional<?> value) {
      return value.isPresent();
   }

   protected Object _getReferenced(Optional<?> value) {
      return value.get();
   }

   protected Object _getReferencedIfPresent(Optional<?> value) {
      return value.isPresent() ? value.get() : null;
   }
}
