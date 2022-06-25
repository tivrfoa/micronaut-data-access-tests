package org.flywaydb.core.api;

import java.util.Collection;
import org.flywaydb.core.api.resource.LoadableResource;

public interface ResourceProvider {
   LoadableResource getResource(String var1);

   Collection<LoadableResource> getResources(String var1, String[] var2);
}
