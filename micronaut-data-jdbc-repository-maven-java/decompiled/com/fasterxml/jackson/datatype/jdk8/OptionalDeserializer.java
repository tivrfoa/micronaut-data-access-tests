package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.ReferenceTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.util.Optional;

final class OptionalDeserializer extends ReferenceTypeDeserializer<Optional<?>> {
   private static final long serialVersionUID = 1L;

   public OptionalDeserializer(JavaType fullType, ValueInstantiator inst, TypeDeserializer typeDeser, JsonDeserializer<?> deser) {
      super(fullType, inst, typeDeser, deser);
   }

   public OptionalDeserializer withResolved(TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser) {
      return new OptionalDeserializer(this._fullType, this._valueInstantiator, typeDeser, valueDeser);
   }

   public Optional<?> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
      return Optional.ofNullable(this._valueDeserializer.getNullValue(ctxt));
   }

   @Override
   public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
      return this.getNullValue(ctxt);
   }

   public Optional<?> referenceValue(Object contents) {
      return Optional.ofNullable(contents);
   }

   public Object getReferenced(Optional<?> reference) {
      return reference.orElse(null);
   }

   public Optional<?> updateReference(Optional<?> reference, Object contents) {
      return Optional.ofNullable(contents);
   }
}
