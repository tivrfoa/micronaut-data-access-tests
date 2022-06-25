package io.micronaut.validation.executable;

import io.micronaut.context.annotation.Executable;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;

@Internal
public class ExecutableVisitor implements TypeElementVisitor<Object, Executable> {
   @NonNull
   @Override
   public TypeElementVisitor.VisitorKind getVisitorKind() {
      return TypeElementVisitor.VisitorKind.ISOLATING;
   }

   @Override
   public void visitMethod(MethodElement element, VisitorContext context) {
      ParameterElement[] parameters = element.getParameters();

      for(ParameterElement parameter : parameters) {
         if (parameter.getType().isPrimitive() && parameter.isNullable()) {
            context.warn("@Nullable on primitive types will allow the method to be executed at runtime with null values, causing an exception", parameter);
         }
      }

   }
}
