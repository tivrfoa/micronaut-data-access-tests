package io.micronaut.jackson.serialize;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.util.NameTransformer;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.http.hateoas.Resource;
import io.micronaut.jackson.modules.BeanIntrospectionModule;
import jakarta.inject.Singleton;
import java.util.Iterator;
import java.util.List;

@Internal
@Singleton
@Requires(
   missingBeans = {BeanIntrospectionModule.class}
)
class ResourceSerializerModifier extends BeanSerializerModifier {
   @Override
   public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
      if (Resource.class.isAssignableFrom(beanDesc.getBeanClass())) {
         Iterator<BeanPropertyWriter> i = beanProperties.iterator();
         BeanPropertyWriter links = null;
         BeanPropertyWriter embedded = null;

         while(i.hasNext()) {
            BeanPropertyWriter writer = (BeanPropertyWriter)i.next();
            String name = writer.getName();
            if (name.equals("links")) {
               i.remove();
               links = writer;
            }

            if (name.equals("embedded")) {
               i.remove();
               embedded = writer;
            }
         }

         if (embedded != null) {
            embedded = embedded.rename(new NameTransformer() {
               @Override
               public String transform(String name) {
                  return "_embedded";
               }

               @Override
               public String reverse(String transformed) {
                  return transformed;
               }
            });
            beanProperties.add(0, embedded);
         }

         if (links != null) {
            links = links.rename(new NameTransformer() {
               @Override
               public String transform(String name) {
                  return "_links";
               }

               @Override
               public String reverse(String transformed) {
                  return transformed;
               }
            });
            beanProperties.add(0, links);
         }

         return beanProperties;
      } else {
         return super.changeProperties(config, beanDesc, beanProperties);
      }
   }
}
