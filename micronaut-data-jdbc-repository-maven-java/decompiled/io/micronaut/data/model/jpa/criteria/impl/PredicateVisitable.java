package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;

@Internal
public interface PredicateVisitable {
   void accept(PredicateVisitor predicateVisitor);
}
