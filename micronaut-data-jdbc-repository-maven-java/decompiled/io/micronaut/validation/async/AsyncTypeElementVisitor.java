package io.micronaut.validation.async;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import io.micronaut.scheduling.annotation.Async;
import java.util.concurrent.CompletionStage;

@Internal
public final class AsyncTypeElementVisitor implements TypeElementVisitor<Object, Async> {
   @NonNull
   @Override
   public TypeElementVisitor.VisitorKind getVisitorKind() {
      return TypeElementVisitor.VisitorKind.ISOLATING;
   }

   @Override
   public void visitMethod(MethodElement element, VisitorContext context) {
      ClassElement returnType = element.getReturnType();
      boolean isValid = returnType != null
         && (
            returnType.isAssignable(CompletionStage.class)
               || returnType.isAssignable(Void.TYPE)
               || Publishers.getKnownReactiveTypes().stream().anyMatch(returnType::isAssignable)
         );
      if (!isValid) {
         context.fail("Method must return void or a subtype of CompletionStage", element);
      }

   }
}
