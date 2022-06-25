package io.micronaut.inject.ast;

final class SimplePackageElement implements PackageElement {
   private final String packageName;

   SimplePackageElement(String packageName) {
      this.packageName = packageName;
   }

   @Override
   public String getName() {
      return this.packageName;
   }

   @Override
   public boolean isProtected() {
      return false;
   }

   @Override
   public boolean isPublic() {
      return true;
   }

   @Override
   public Object getNativeType() {
      return this.packageName;
   }
}
