package io.micronaut.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.BasicDeserializerFactory;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.CreatorCollector;

final class ResilientBeanDeserializerFactory extends BeanDeserializerFactory {
   public ResilientBeanDeserializerFactory(DeserializerFactoryConfig config) {
      super(config);
   }

   @Override
   public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
      return new ResilientBeanDeserializerFactory(config);
   }

   @Override
   protected ValueInstantiator _constructDefaultValueInstantiator(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
      try {
         return super._constructDefaultValueInstantiator(ctxt, beanDesc);
      } catch (IllegalArgumentException var5) {
         if (var5.getMessage().startsWith("Failed to access RecordComponents of type ")) {
            DeserializationConfig config = ctxt.getConfig();
            return (new BasicDeserializerFactory.CreatorCollectionState(
                  ctxt,
                  beanDesc,
                  config.getDefaultVisibilityChecker(beanDesc.getBeanClass(), beanDesc.getClassInfo()),
                  new CreatorCollector(beanDesc, config),
                  this._findCreatorsFromProperties(ctxt, beanDesc)
               ))
               .creators
               .constructValueInstantiator(ctxt);
         } else {
            throw var5;
         }
      }
   }
}
