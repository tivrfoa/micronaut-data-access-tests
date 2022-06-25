package org.flywaydb.core.api;

import java.util.Collection;

public interface ClassProvider<I> {
   Collection<Class<? extends I>> getClasses();
}
