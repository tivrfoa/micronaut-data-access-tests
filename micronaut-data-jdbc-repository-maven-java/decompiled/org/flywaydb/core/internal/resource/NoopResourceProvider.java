package org.flywaydb.core.internal.resource;

import java.util.Collection;
import java.util.Collections;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;

public enum NoopResourceProvider implements ResourceProvider {
   INSTANCE;

   @Override
   public LoadableResource getResource(String name) {
      return null;
   }

   @Override
   public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
      return Collections.emptyList();
   }
}
