package io.micronaut.inject.ast;

import io.micronaut.core.annotation.NonNull;
import java.util.Collections;
import java.util.Set;

public interface MemberElement extends Element {
   ClassElement getDeclaringType();

   default ClassElement getOwningType() {
      return this.getDeclaringType();
   }

   @Override
   default Set<ElementModifier> getModifiers() {
      return Collections.emptySet();
   }

   default boolean isReflectionRequired() {
      return this.isReflectionRequired(this.getOwningType());
   }

   default boolean isReflectionRequired(@NonNull ClassElement callingType) {
      if (this.isPublic()) {
         return false;
      } else if (!this.isPackagePrivate() && !this.isProtected()) {
         return true;
      } else {
         ClassElement declaringType = this.getDeclaringType();
         return !declaringType.getPackageName().equals(callingType.getPackageName());
      }
   }
}
