package io.micronaut.core.naming;

import io.micronaut.core.annotation.NonNull;

public interface Named {
   @NonNull
   String getName();
}
