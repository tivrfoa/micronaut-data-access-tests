package io.micronaut.context.env;

import io.micronaut.core.annotation.Internal;
import java.util.List;

@Internal
public final class ConstantPropertySources {
   private final List<PropertySource> sources;

   public ConstantPropertySources(List<PropertySource> sources) {
      this.sources = sources;
   }

   List<PropertySource> getSources() {
      return this.sources;
   }
}
