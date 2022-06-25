package io.micronaut.validation.internal;

import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ConstructorElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MemberElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;

public class InternalApiTypeElementVisitor implements TypeElementVisitor<Object, Object> {
   private static final String IO_MICRONAUT = "io.micronaut";
   private boolean warned = false;

   @NonNull
   @Override
   public TypeElementVisitor.VisitorKind getVisitorKind() {
      return TypeElementVisitor.VisitorKind.ISOLATING;
   }

   @Override
   public void visitClass(ClassElement element, VisitorContext context) {
      if (!element.getName().startsWith("io.micronaut")) {
         this.warn(element, context);
      }

   }

   @Override
   public void visitMethod(MethodElement element, VisitorContext context) {
      this.warnMember(element, context);
   }

   @Override
   public void visitConstructor(ConstructorElement element, VisitorContext context) {
      this.warnMember(element, context);
   }

   @Override
   public void visitField(FieldElement element, VisitorContext context) {
      this.warnMember(element, context);
   }

   private void warnMember(MemberElement element, VisitorContext context) {
      if (!element.getDeclaringType().getName().startsWith("io.micronaut")) {
         this.warn(element, context);
      }

   }

   private void warn(Element element, VisitorContext context) {
      if (element.hasAnnotation(Internal.class) || element.hasAnnotation(Experimental.class)) {
         this.warned = true;
         context.warn("Element extends or implements an internal or experimental Micronaut API", element);
      }

   }

   @Override
   public void finish(VisitorContext visitorContext) {
      if (this.warned) {
         visitorContext.warn(
            "Overriding an internal Micronaut API may result in breaking changes in minor or patch versions of the framework. Proceed with caution!", null
         );
      }

   }
}
