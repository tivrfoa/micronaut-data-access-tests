package io.micronaut.inject.ast;

import io.micronaut.core.annotation.NonNull;
import java.util.Objects;

public interface PackageElement extends Element {
   PackageElement DEFAULT_PACKAGE = of("");

   @NonNull
   static PackageElement of(@NonNull String name) {
      Objects.requireNonNull(name, "Name cannot be null");
      return new SimplePackageElement(name);
   }
}
