package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class Jdk8OptionalBeanPropertyWriter extends BeanPropertyWriter {
   private static final long serialVersionUID = 1L;
   protected final Object _empty;

   protected Jdk8OptionalBeanPropertyWriter(BeanPropertyWriter base, Object empty) {
      super(base);
      this._empty = empty;
   }

   protected Jdk8OptionalBeanPropertyWriter(Jdk8OptionalBeanPropertyWriter base, PropertyName newName) {
      super(base, newName);
      this._empty = base._empty;
   }

   @Override
   protected BeanPropertyWriter _new(PropertyName newName) {
      return new Jdk8OptionalBeanPropertyWriter(this, newName);
   }

   @Override
   public BeanPropertyWriter unwrappingWriter(NameTransformer unwrapper) {
      return new Jdk8UnwrappingOptionalBeanPropertyWriter(this, unwrapper, this._empty);
   }

   @Override
   public void serializeAsField(Object bean, JsonGenerator g, SerializerProvider prov) throws Exception {
      if (this._nullSerializer == null) {
         Object value = this.get(bean);
         if (value == null || value.equals(this._empty)) {
            return;
         }
      }

      super.serializeAsField(bean, g, prov);
   }
}
