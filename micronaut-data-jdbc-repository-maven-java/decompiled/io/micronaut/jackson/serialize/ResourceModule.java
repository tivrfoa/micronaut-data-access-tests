package io.micronaut.jackson.serialize;

import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.inject.Singleton;

@Singleton
public class ResourceModule extends SimpleModule {
   public ResourceModule() {
      this.setDeserializerModifier(new ResourceDeserializerModifier());
   }
}
