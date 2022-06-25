package io.micronaut.inject.visitor;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ConstructorElement;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MethodElement;
import java.util.Collections;
import java.util.Set;

public interface TypeElementVisitor<C, E> extends Ordered, Toggleable {
   default void visitClass(ClassElement element, VisitorContext context) {
   }

   default void visitMethod(MethodElement element, VisitorContext context) {
   }

   default void visitConstructor(ConstructorElement element, VisitorContext context) {
   }

   default void visitField(FieldElement element, VisitorContext context) {
   }

   default void start(VisitorContext visitorContext) {
   }

   default void finish(VisitorContext visitorContext) {
   }

   default Set<String> getSupportedAnnotationNames() {
      Class<?>[] classes = GenericTypeUtils.resolveInterfaceTypeArguments(this.getClass(), TypeElementVisitor.class);
      if (classes.length == 2) {
         Class<?> classType = classes[0];
         String classTypeName = classType.getName();
         if (classType == Object.class) {
            classTypeName = this.getClassType();
         }

         if (classTypeName.equals(Object.class.getName())) {
            return Collections.singleton("*");
         } else {
            Class<?> elementType = classes[1];
            String elementTypeName = elementType.getName();
            if (elementTypeName.equals(Object.class.getName())) {
               elementTypeName = this.getElementType();
            }

            return elementTypeName.equals(Object.class.getName())
               ? CollectionUtils.setOf(classTypeName)
               : CollectionUtils.setOf(classTypeName, elementTypeName);
         }
      } else {
         return Collections.singleton("*");
      }
   }

   default String getClassType() {
      return Object.class.getName();
   }

   default String getElementType() {
      return Object.class.getName();
   }

   default Set<String> getSupportedOptions() {
      return Collections.emptySet();
   }

   @NonNull
   default TypeElementVisitor.VisitorKind getVisitorKind() {
      return TypeElementVisitor.VisitorKind.AGGREGATING;
   }

   public static enum VisitorKind {
      ISOLATING,
      AGGREGATING;
   }
}
