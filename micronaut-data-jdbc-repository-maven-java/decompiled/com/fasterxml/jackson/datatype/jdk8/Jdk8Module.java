package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public class Jdk8Module extends Module {
   protected boolean _cfgHandleAbsentAsNull = false;

   @Override
   public void setupModule(Module.SetupContext context) {
      context.addSerializers(new Jdk8Serializers());
      context.addDeserializers(new Jdk8Deserializers());
      context.addTypeModifier(new Jdk8TypeModifier());
      if (this._cfgHandleAbsentAsNull) {
         context.addBeanSerializerModifier(new Jdk8BeanSerializerModifier());
      }

   }

   @Override
   public Version version() {
      return PackageVersion.VERSION;
   }

   @Deprecated
   public Jdk8Module configureAbsentsAsNulls(boolean state) {
      this._cfgHandleAbsentAsNull = state;
      return this;
   }

   public int hashCode() {
      return this.getClass().hashCode();
   }

   public boolean equals(Object o) {
      return this == o;
   }

   @Override
   public String getModuleName() {
      return "Jdk8Module";
   }
}
