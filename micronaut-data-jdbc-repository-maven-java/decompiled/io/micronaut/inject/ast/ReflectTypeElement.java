package io.micronaut.inject.ast;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Objects;

@Internal
abstract class ReflectTypeElement<T extends Type> implements ClassElement {
   protected final T type;

   ReflectTypeElement(T type) {
      this.type = type;
   }

   final Class<?> getErasure() {
      Class<?> erasure = getErasure(this.type);

      for(int i = 0; i < this.getArrayDimensions(); ++i) {
         erasure = Array.newInstance(erasure, 0).getClass();
      }

      return erasure;
   }

   @Override
   public boolean isPrimitive() {
      return this.getErasure().isPrimitive();
   }

   @Override
   public boolean isPackagePrivate() {
      int modifiers = this.getErasure().getModifiers();
      return !Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers) && !Modifier.isPrivate(modifiers);
   }

   @Override
   public boolean isProtected() {
      return !this.isPublic();
   }

   @Override
   public boolean isPublic() {
      return Modifier.isPublic(this.getErasure().getModifiers());
   }

   public String toString() {
      return this.type.getTypeName();
   }

   @NonNull
   @Override
   public String getName() {
      Class<?> erasure = this.getErasure();

      while(erasure.isArray()) {
         erasure = erasure.getComponentType();
      }

      return erasure.getName();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ReflectClassElement that = (ReflectClassElement)o;
         return this.type.equals(that.type);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.type});
   }

   @Override
   public boolean isAssignable(Class<?> type) {
      return type.isAssignableFrom(this.getErasure());
   }

   @Override
   public boolean isAssignable(String type) {
      return false;
   }

   @Override
   public boolean isAssignable(ClassElement type) {
      return false;
   }

   @NonNull
   @Override
   public Object getNativeType() {
      return this.type;
   }

   @NonNull
   @Override
   public ClassElement getRawClassElement() {
      return ClassElement.of(this.getErasure());
   }

   @NonNull
   static Class<?> getErasure(@NonNull Type type) {
      if (type instanceof Class) {
         return (Class<?>)type;
      } else if (type instanceof GenericArrayType) {
         return Array.newInstance(getErasure(((GenericArrayType)type).getGenericComponentType()), 0).getClass();
      } else if (type instanceof ParameterizedType) {
         return getErasure(((ParameterizedType)type).getRawType());
      } else if (type instanceof TypeVariable) {
         return getErasure(((TypeVariable)type).getBounds()[0]);
      } else if (type instanceof WildcardType) {
         return getErasure(((WildcardType)type).getUpperBounds()[0]);
      } else {
         throw new IllegalArgumentException("Unsupported type: " + type.getClass());
      }
   }
}
