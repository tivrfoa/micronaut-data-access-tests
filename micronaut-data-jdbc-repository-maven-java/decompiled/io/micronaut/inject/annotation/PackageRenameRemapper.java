package io.micronaut.inject.annotation;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.inject.visitor.VisitorContext;
import java.util.Collections;
import java.util.List;

public interface PackageRenameRemapper extends AnnotationRemapper {
   @NonNull
   String getTargetPackage();

   @Override
   default List<AnnotationValue<?>> remap(AnnotationValue<?> annotation, VisitorContext visitorContext) {
      String simpleName = NameUtils.getSimpleName(annotation.getAnnotationName());
      return Collections.singletonList(new AnnotationValue(this.getTargetPackage() + '.' + simpleName, annotation.getValues()));
   }
}
