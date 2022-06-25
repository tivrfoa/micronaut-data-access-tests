package io.micronaut.asm.commons;

import java.util.Collections;
import java.util.Map;

public class SimpleRemapper extends Remapper {
   private final Map<String, String> mapping;

   public SimpleRemapper(Map<String, String> mapping) {
      this.mapping = mapping;
   }

   public SimpleRemapper(String oldName, String newName) {
      this.mapping = Collections.singletonMap(oldName, newName);
   }

   @Override
   public String mapMethodName(String owner, String name, String descriptor) {
      String remappedName = this.map(owner + '.' + name + descriptor);
      return remappedName == null ? name : remappedName;
   }

   @Override
   public String mapInvokeDynamicMethodName(String name, String descriptor) {
      String remappedName = this.map('.' + name + descriptor);
      return remappedName == null ? name : remappedName;
   }

   @Override
   public String mapAnnotationAttributeName(String descriptor, String name) {
      String remappedName = this.map(descriptor + '.' + name);
      return remappedName == null ? name : remappedName;
   }

   @Override
   public String mapFieldName(String owner, String name, String descriptor) {
      String remappedName = this.map(owner + '.' + name);
      return remappedName == null ? name : remappedName;
   }

   @Override
   public String map(String key) {
      return (String)this.mapping.get(key);
   }
}
