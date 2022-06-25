package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Internal;

@Internal
public interface Qualified<T> {
   @Internal
   void $withBeanQualifier(Qualifier<T> qualifier);
}
