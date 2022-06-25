package io.micronaut.jackson.databind.convert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.CollectionUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Internal
public class ObjectNodeConvertibleValues<V> implements ConvertibleValues<V> {
   private final ObjectNode objectNode;
   private final ConversionService<?> conversionService;

   public ObjectNodeConvertibleValues(ObjectNode objectNode, ConversionService<?> conversionService) {
      this.objectNode = objectNode;
      this.conversionService = conversionService;
   }

   @Override
   public Set<String> names() {
      Iterator<String> fieldNames = this.objectNode.fieldNames();
      return CollectionUtils.iteratorToSet(fieldNames);
   }

   @Override
   public Collection<V> values() {
      List<V> values = new ArrayList();

      for(JsonNode jsonNode : this.objectNode) {
         values.add(jsonNode);
      }

      return Collections.unmodifiableCollection(values);
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      String fieldName = name.toString();
      JsonNode jsonNode = this.objectNode.get(fieldName);
      return jsonNode == null ? Optional.empty() : this.conversionService.convert(jsonNode, conversionContext);
   }
}
