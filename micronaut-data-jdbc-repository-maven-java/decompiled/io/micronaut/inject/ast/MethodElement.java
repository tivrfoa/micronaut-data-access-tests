package io.micronaut.inject.ast;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.ast.beans.BeanElementBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface MethodElement extends MemberElement {
   @NonNull
   ClassElement getReturnType();

   default List<? extends GenericPlaceholderElement> getDeclaredTypeVariables() {
      return Collections.emptyList();
   }

   default Optional<ClassElement> getReceiverType() {
      return Optional.empty();
   }

   @NonNull
   default ClassElement[] getThrownTypes() {
      return ClassElement.ZERO_CLASS_ELEMENTS;
   }

   @NonNull
   ParameterElement[] getParameters();

   @NonNull
   MethodElement withNewParameters(@NonNull ParameterElement... newParameters);

   @NonNull
   default BeanElementBuilder addAssociatedBean(@NonNull ClassElement type) {
      throw new UnsupportedOperationException("Only classes being processed from source code can define associated beans");
   }

   @NonNull
   default ParameterElement[] getSuspendParameters() {
      return this.getParameters();
   }

   default boolean hasParameters() {
      return this.getParameters().length > 0;
   }

   default boolean isSuspend() {
      return false;
   }

   default boolean isDefault() {
      return false;
   }

   @NonNull
   default ClassElement getGenericReturnType() {
      return this.getReturnType();
   }

   @NonNull
   @Override
   default String getDescription(boolean simple) {
      String typeString = simple ? this.getReturnType().getSimpleName() : this.getReturnType().getName();
      String args = (String)Arrays.stream(this.getParameters())
         .map(arg -> simple ? arg.getType().getSimpleName() : arg.getType().getName() + " " + arg.getName())
         .collect(Collectors.joining(","));
      return typeString + " " + this.getName() + "(" + args + ")";
   }

   default boolean overrides(@NonNull MethodElement overridden) {
      return false;
   }

   @NonNull
   static MethodElement of(
      @NonNull ClassElement declaredType,
      @NonNull AnnotationMetadata annotationMetadata,
      @NonNull ClassElement returnType,
      @NonNull ClassElement genericReturnType,
      @NonNull String name,
      ParameterElement... parameterElements
   ) {
      return new MethodElement() {
         @NonNull
         @Override
         public ClassElement getReturnType() {
            return returnType;
         }

         @NonNull
         @Override
         public ClassElement getGenericReturnType() {
            return genericReturnType;
         }

         @Override
         public ParameterElement[] getParameters() {
            return parameterElements;
         }

         @Override
         public MethodElement withNewParameters(ParameterElement... newParameters) {
            return MethodElement.of(declaredType, annotationMetadata, returnType, genericReturnType, name, ArrayUtils.concat(parameterElements, newParameters));
         }

         @NonNull
         @Override
         public AnnotationMetadata getAnnotationMetadata() {
            return annotationMetadata;
         }

         @Override
         public ClassElement getDeclaringType() {
            return declaredType;
         }

         @NonNull
         @Override
         public String getName() {
            return name;
         }

         @Override
         public boolean isPackagePrivate() {
            return false;
         }

         @Override
         public boolean isProtected() {
            return false;
         }

         @Override
         public boolean isPublic() {
            return true;
         }

         @NonNull
         @Override
         public Object getNativeType() {
            throw new UnsupportedOperationException("No native method type present");
         }
      };
   }
}
