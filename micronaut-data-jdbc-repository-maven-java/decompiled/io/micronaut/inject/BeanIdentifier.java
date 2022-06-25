package io.micronaut.inject;

import io.micronaut.core.naming.Named;
import java.io.Serializable;

public interface BeanIdentifier extends CharSequence, Serializable, Named {
   static BeanIdentifier of(String id) {
      return new DefaultBeanIdentifier(id);
   }
}
