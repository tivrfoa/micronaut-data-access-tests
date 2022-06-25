package io.micronaut.data.model.runtime;

import io.micronaut.core.beans.BeanProperty;
import io.micronaut.data.model.Embedded;

class RuntimeEmbedded<T> extends RuntimeAssociation<T> implements Embedded {
   RuntimeEmbedded(RuntimePersistentEntity owner, BeanProperty<T, ?> property, boolean constructorArg) {
      super(owner, property, constructorArg);
   }
}
