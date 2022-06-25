package org.flywaydb.core.internal.clazz;

import java.util.Collection;
import java.util.Collections;
import org.flywaydb.core.api.ClassProvider;

public enum NoopClassProvider implements ClassProvider {
   INSTANCE;

   @Override
   public Collection<Class<?>> getClasses() {
      return Collections.emptyList();
   }
}
