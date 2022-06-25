package io.micronaut.context.visitor;

import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import java.util.Collections;
import java.util.Set;

public class ContextConfigurerVisitor implements TypeElementVisitor<ContextConfigurer, Object> {
   private static final Set<String> SUPPORTED_SERVICE_TYPES = Collections.singleton(ApplicationContextConfigurer.class.getName());

   @Override
   public TypeElementVisitor.VisitorKind getVisitorKind() {
      return TypeElementVisitor.VisitorKind.ISOLATING;
   }

   @Override
   public String getElementType() {
      return ContextConfigurer.class.getName();
   }

   @Override
   public void visitClass(ClassElement element, VisitorContext context) {
      assertNoConstructorForContextAnnotation(element);
      element.getInterfaces()
         .stream()
         .map(Element::getName)
         .filter(SUPPORTED_SERVICE_TYPES::contains)
         .forEach(serviceType -> context.visitServiceDescriptor(serviceType, element.getName(), element));
   }

   public static void assertNoConstructorForContextAnnotation(ClassElement element) {
      element.getEnclosedElements(ElementQuery.CONSTRUCTORS).stream().filter(e -> e.getParameters().length > 0).findAny().ifPresent(e -> {
         throw typeShouldNotHaveConstructorsWithArgs(element.getName());
      });
   }

   @NonNull
   private static RuntimeException typeShouldNotHaveConstructorsWithArgs(String type) {
      return new IllegalStateException(
         type
            + " is annotated with @ContextConfigurer but has at least one constructor with arguments, which isn't supported. To resolve this create a separate class with no constructor arguments annotated with @ContextConfigurer, which sole role is configuring the application context."
      );
   }
}
