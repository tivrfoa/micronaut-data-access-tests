package io.micronaut.context.visitor;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.beans.BeanElementBuilder;
import io.micronaut.inject.visitor.VisitorContext;
import java.util.Collections;
import java.util.Set;

public interface BeanImportHandler {
   @NonNull
   default Set<String> getSupportedAnnotationNames() {
      return Collections.emptySet();
   }

   void beanAdded(BeanElementBuilder beanElementBuilder, VisitorContext context);
}
