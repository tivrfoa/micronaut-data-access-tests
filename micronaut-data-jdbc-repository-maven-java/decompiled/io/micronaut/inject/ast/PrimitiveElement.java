package io.micronaut.inject.ast;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;

public final class PrimitiveElement implements ArrayableClassElement {
   public static final PrimitiveElement VOID = new PrimitiveElement("void");
   public static final PrimitiveElement BOOLEAN = new PrimitiveElement("boolean");
   public static final PrimitiveElement INT = new PrimitiveElement("int");
   public static final PrimitiveElement CHAR = new PrimitiveElement("char");
   public static final PrimitiveElement LONG = new PrimitiveElement("long");
   public static final PrimitiveElement FLOAT = new PrimitiveElement("float");
   public static final PrimitiveElement DOUBLE = new PrimitiveElement("double");
   public static final PrimitiveElement SHORT = new PrimitiveElement("short");
   public static final PrimitiveElement BYTE = new PrimitiveElement("byte");
   private static final PrimitiveElement[] PRIMITIVES = new PrimitiveElement[]{INT, CHAR, BOOLEAN, LONG, FLOAT, DOUBLE, SHORT, BYTE, VOID};
   private final String typeName;
   private final int arrayDimensions;

   private PrimitiveElement(String name) {
      this(name, 0);
   }

   private PrimitiveElement(String name, int arrayDimensions) {
      this.typeName = name;
      this.arrayDimensions = arrayDimensions;
   }

   @Override
   public boolean isAssignable(String type) {
      return this.typeName.equals(type);
   }

   @Override
   public boolean isAssignable(ClassElement type) {
      return this.typeName.equals(type.getName());
   }

   @Override
   public boolean isArray() {
      return this.arrayDimensions > 0;
   }

   @Override
   public int getArrayDimensions() {
      return this.arrayDimensions;
   }

   @NonNull
   @Override
   public String getName() {
      return this.typeName;
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
      throw new UnsupportedOperationException("There is no native types for primitives");
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return AnnotationMetadata.EMPTY_METADATA;
   }

   @Override
   public ClassElement withArrayDimensions(int arrayDimensions) {
      return new PrimitiveElement(this.typeName, arrayDimensions);
   }

   @Override
   public boolean isPrimitive() {
      return true;
   }

   public static PrimitiveElement valueOf(String name) {
      for(PrimitiveElement element : PRIMITIVES) {
         if (element.getName().equalsIgnoreCase(name)) {
            return element;
         }
      }

      throw new IllegalArgumentException(String.format("No primitive found for name: %s", name));
   }
}
