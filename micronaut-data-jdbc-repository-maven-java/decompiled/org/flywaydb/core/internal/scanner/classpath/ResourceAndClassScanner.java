package org.flywaydb.core.internal.scanner.classpath;

import java.util.Collection;
import org.flywaydb.core.api.resource.LoadableResource;

public interface ResourceAndClassScanner<I> {
   Collection<LoadableResource> scanForResources();

   Collection<Class<? extends I>> scanForClasses();
}
