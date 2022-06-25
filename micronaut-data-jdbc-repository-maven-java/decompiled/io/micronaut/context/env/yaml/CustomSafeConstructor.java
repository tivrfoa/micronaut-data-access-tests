package io.micronaut.context.env.yaml;

import io.micronaut.core.annotation.Internal;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

@Internal
class CustomSafeConstructor extends SafeConstructor {
   @Override
   protected Map<Object, Object> newMap(MappingNode node) {
      return this.createDefaultMap(node.getValue().size());
   }

   @Override
   protected List<Object> newList(SequenceNode node) {
      return this.createDefaultList(node.getValue().size());
   }
}
