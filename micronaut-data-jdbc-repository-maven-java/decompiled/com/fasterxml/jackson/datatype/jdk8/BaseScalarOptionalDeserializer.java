package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

public abstract class BaseScalarOptionalDeserializer<T> extends StdScalarDeserializer<T> {
   protected final T _empty;

   protected BaseScalarOptionalDeserializer(Class<T> cls, T empty) {
      super(cls);
      this._empty = empty;
   }

   @Override
   public T getNullValue(DeserializationContext ctxt) {
      return this._empty;
   }
}
