package io.micronaut.data.model;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;

public interface PersistentElement extends Named, AnnotationMetadataProvider {
   @NonNull
   String getPersistedName();
}
