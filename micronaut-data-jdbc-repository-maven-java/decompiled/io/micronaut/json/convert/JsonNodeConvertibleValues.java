package io.micronaut.json.convert;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.json.tree.JsonNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

@Internal
public class JsonNodeConvertibleValues<V> implements ConvertibleValues<V> {
   private final JsonNode objectNode;
   private final ConversionService<?> conversionService;

   public JsonNodeConvertibleValues(JsonNode objectNode, ConversionService<?> conversionService) {
      if (!objectNode.isObject()) {
         throw new IllegalArgumentException("Expected object node");
      } else {
         this.objectNode = objectNode;
         this.conversionService = conversionService;
      }
   }

   @Override
   public Set<String> names() {
      Set<String> set = new LinkedHashSet();

      for(Entry<String, JsonNode> entry : this.objectNode.entries()) {
         set.add(entry.getKey());
      }

      return Collections.unmodifiableSet(set);
   }

   @Override
   public Collection<V> values() {
      List<V> values = new ArrayList();
      this.objectNode.values().forEach(v -> values.add(v));
      return Collections.unmodifiableCollection(values);
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      String fieldName = name.toString();
      JsonNode jsonNode = this.objectNode.get(fieldName);
      return jsonNode == null ? Optional.empty() : this.conversionService.convert(jsonNode, conversionContext);
   }
}
