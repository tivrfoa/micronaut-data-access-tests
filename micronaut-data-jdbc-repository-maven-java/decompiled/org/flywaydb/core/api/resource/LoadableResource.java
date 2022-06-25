package org.flywaydb.core.api.resource;

import java.io.Reader;

public abstract class LoadableResource implements Resource, Comparable<LoadableResource> {
   public abstract Reader read();

   public int compareTo(LoadableResource o) {
      return this.getRelativePath().compareTo(o.getRelativePath());
   }
}
