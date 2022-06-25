package io.micronaut.jackson.serialize;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.http.hateoas.Resource;
import io.micronaut.jackson.modules.BeanIntrospectionModule;
import java.util.List;

@Internal
@Requires(
   missingBeans = {BeanIntrospectionModule.class}
)
public class ResourceDeserializerModifier extends BeanDeserializerModifier {
   @Override
   public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
      if (Resource.class.isAssignableFrom(beanDesc.getBeanClass())) {
         for(int i = 0; i < propDefs.size(); ++i) {
            BeanPropertyDefinition definition = (BeanPropertyDefinition)propDefs.get(i);
            if (definition.getName().equals("embedded")) {
               propDefs.set(i, definition.withSimpleName("_embedded"));
            }

            if (definition.getName().equals("links")) {
               propDefs.set(i, definition.withSimpleName("_links"));
            }
         }

         return propDefs;
      } else {
         return super.updateProperties(config, beanDesc, propDefs);
      }
   }
}
