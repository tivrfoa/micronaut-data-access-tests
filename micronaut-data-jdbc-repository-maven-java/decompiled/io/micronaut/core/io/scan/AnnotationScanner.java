package io.micronaut.core.io.scan;

import io.micronaut.core.annotation.NonNull;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public interface AnnotationScanner {
   @NonNull
   Stream<Class<?>> scan(@NonNull String annotation, @NonNull String pkg);

   @NonNull
   default Stream<Class<?>> scan(@NonNull String annotation, @NonNull Package... packages) {
      Objects.requireNonNull(annotation, "Annotation type cannot be null");
      Objects.requireNonNull(packages, "Packages to scan cannot be null");
      return ((Stream)Arrays.stream(packages).parallel()).flatMap(pkg -> this.scan(annotation, pkg.getName()));
   }

   @NonNull
   default Stream<Class<?>> scan(@NonNull Class<? extends Annotation> annotation, @NonNull Package... packages) {
      Objects.requireNonNull(annotation, "Annotation type cannot be null");
      Objects.requireNonNull(packages, "Packages to scan cannot be null");
      return this.scan(annotation.getName(), packages);
   }

   @NonNull
   default Stream<Class<?>> scan(@NonNull Class<? extends Annotation> annotation, @NonNull Package pkg) {
      return this.scan(annotation.getName(), pkg.getName());
   }

   @NonNull
   default Stream<Class<?>> scan(@NonNull String annotation, @NonNull String... packages) {
      Objects.requireNonNull(annotation, "Annotation type cannot be null");
      Objects.requireNonNull(packages, "Packages to scan cannot be null");
      Stream<String> stream = Arrays.stream(packages);
      return this.scan(annotation, stream);
   }

   @NonNull
   default Stream<Class<?>> scan(@NonNull String annotation, @NonNull Collection<String> packages) {
      Objects.requireNonNull(annotation, "Annotation type cannot be null");
      Objects.requireNonNull(packages, "Packages to scan cannot be null");
      return this.scan(annotation, packages.parallelStream());
   }

   @NonNull
   default Stream<Class<?>> scan(@NonNull Class<? extends Annotation> annotation, @NonNull Collection<String> packages) {
      Objects.requireNonNull(annotation, "Annotation type cannot be null");
      Objects.requireNonNull(packages, "Packages to scan cannot be null");
      return this.scan(annotation.getName(), packages.parallelStream());
   }

   @NonNull
   default Stream<Class<?>> scan(@NonNull String annotation, @NonNull Stream<String> packages) {
      Objects.requireNonNull(annotation, "Annotation type cannot be null");
      Objects.requireNonNull(packages, "Packages to scan cannot be null");
      return ((Stream)packages.parallel()).flatMap(pkg -> this.scan(annotation, pkg));
   }

   @NonNull
   default Stream<Class<?>> scan(@NonNull Class<? extends Annotation> annotation, @NonNull String... packages) {
      Objects.requireNonNull(annotation, "Annotation type cannot be null");
      Objects.requireNonNull(packages, "Packages to scan cannot be null");
      return this.scan(annotation.getName(), packages);
   }
}
