package io.micronaut.asm.tree.analysis;

import io.micronaut.asm.Type;
import java.util.List;

public class SimpleVerifier extends BasicVerifier {
   private final Type currentClass;
   private final Type currentSuperClass;
   private final List<Type> currentClassInterfaces;
   private final boolean isInterface;
   private ClassLoader loader = this.getClass().getClassLoader();

   public SimpleVerifier() {
      this(null, null, false);
   }

   public SimpleVerifier(Type currentClass, Type currentSuperClass, boolean isInterface) {
      this(currentClass, currentSuperClass, null, isInterface);
   }

   public SimpleVerifier(Type currentClass, Type currentSuperClass, List<Type> currentClassInterfaces, boolean isInterface) {
      this(589824, currentClass, currentSuperClass, currentClassInterfaces, isInterface);
      if (this.getClass() != SimpleVerifier.class) {
         throw new IllegalStateException();
      }
   }

   protected SimpleVerifier(int api, Type currentClass, Type currentSuperClass, List<Type> currentClassInterfaces, boolean isInterface) {
      super(api);
      this.currentClass = currentClass;
      this.currentSuperClass = currentSuperClass;
      this.currentClassInterfaces = currentClassInterfaces;
      this.isInterface = isInterface;
   }

   public void setClassLoader(ClassLoader loader) {
      this.loader = loader;
   }

   @Override
   public BasicValue newValue(Type type) {
      if (type == null) {
         return BasicValue.UNINITIALIZED_VALUE;
      } else {
         boolean isArray = type.getSort() == 9;
         if (isArray) {
            switch(type.getElementType().getSort()) {
               case 1:
               case 2:
               case 3:
               case 4:
                  return new BasicValue(type);
            }
         }

         BasicValue value = super.newValue(type);
         if (BasicValue.REFERENCE_VALUE.equals(value)) {
            if (isArray) {
               value = this.newValue(type.getElementType());
               StringBuilder descriptor = new StringBuilder();

               for(int i = 0; i < type.getDimensions(); ++i) {
                  descriptor.append('[');
               }

               descriptor.append(value.getType().getDescriptor());
               value = new BasicValue(Type.getType(descriptor.toString()));
            } else {
               value = new BasicValue(type);
            }
         }

         return value;
      }
   }

   @Override
   protected boolean isArrayValue(BasicValue value) {
      Type type = value.getType();
      return type != null && (type.getSort() == 9 || type.equals(NULL_TYPE));
   }

   @Override
   protected BasicValue getElementValue(BasicValue objectArrayValue) throws AnalyzerException {
      Type arrayType = objectArrayValue.getType();
      if (arrayType != null) {
         if (arrayType.getSort() == 9) {
            return this.newValue(Type.getType(arrayType.getDescriptor().substring(1)));
         }

         if (arrayType.equals(NULL_TYPE)) {
            return objectArrayValue;
         }
      }

      throw new AssertionError();
   }

   @Override
   protected boolean isSubTypeOf(BasicValue value, BasicValue expected) {
      Type expectedType = expected.getType();
      Type type = value.getType();
      switch(expectedType.getSort()) {
         case 5:
         case 6:
         case 7:
         case 8:
            return type.equals(expectedType);
         case 9:
         case 10:
            if (type.equals(NULL_TYPE)) {
               return true;
            } else if (type.getSort() != 10 && type.getSort() != 9) {
               return false;
            } else if (this.isAssignableFrom(expectedType, type)) {
               return true;
            } else {
               if (this.getClass(expectedType).isInterface()) {
                  return Object.class.isAssignableFrom(this.getClass(type));
               }

               return false;
            }
         default:
            throw new AssertionError();
      }
   }

   @Override
   public BasicValue merge(BasicValue value1, BasicValue value2) {
      if (value1.equals(value2)) {
         return value1;
      } else {
         Type type1 = value1.getType();
         Type type2 = value2.getType();
         if (type1 != null && (type1.getSort() == 10 || type1.getSort() == 9) && type2 != null && (type2.getSort() == 10 || type2.getSort() == 9)) {
            if (type1.equals(NULL_TYPE)) {
               return value2;
            } else if (type2.equals(NULL_TYPE)) {
               return value1;
            } else if (this.isAssignableFrom(type1, type2)) {
               return value1;
            } else if (this.isAssignableFrom(type2, type1)) {
               return value2;
            } else {
               int numDimensions = 0;
               if (type1.getSort() == 9
                  && type2.getSort() == 9
                  && type1.getDimensions() == type2.getDimensions()
                  && type1.getElementType().getSort() == 10
                  && type2.getElementType().getSort() == 10) {
                  numDimensions = type1.getDimensions();
                  type1 = type1.getElementType();
                  type2 = type2.getElementType();
               }

               while(type1 != null && !this.isInterface(type1)) {
                  type1 = this.getSuperClass(type1);
                  if (this.isAssignableFrom(type1, type2)) {
                     return this.newArrayValue(type1, numDimensions);
                  }
               }

               return this.newArrayValue(Type.getObjectType("java/lang/Object"), numDimensions);
            }
         } else {
            return BasicValue.UNINITIALIZED_VALUE;
         }
      }
   }

   private BasicValue newArrayValue(Type type, int dimensions) {
      if (dimensions == 0) {
         return this.newValue(type);
      } else {
         StringBuilder descriptor = new StringBuilder();

         for(int i = 0; i < dimensions; ++i) {
            descriptor.append('[');
         }

         descriptor.append(type.getDescriptor());
         return this.newValue(Type.getType(descriptor.toString()));
      }
   }

   protected boolean isInterface(Type type) {
      return this.currentClass != null && this.currentClass.equals(type) ? this.isInterface : this.getClass(type).isInterface();
   }

   protected Type getSuperClass(Type type) {
      if (this.currentClass != null && this.currentClass.equals(type)) {
         return this.currentSuperClass;
      } else {
         Class<?> superClass = this.getClass(type).getSuperclass();
         return superClass == null ? null : Type.getType(superClass);
      }
   }

   protected boolean isAssignableFrom(Type type1, Type type2) {
      if (type1.equals(type2)) {
         return true;
      } else if (this.currentClass != null && this.currentClass.equals(type1)) {
         if (this.getSuperClass(type2) == null) {
            return false;
         } else if (!this.isInterface) {
            return this.isAssignableFrom(type1, this.getSuperClass(type2));
         } else {
            return type2.getSort() == 10 || type2.getSort() == 9;
         }
      } else if (this.currentClass == null || !this.currentClass.equals(type2)) {
         return this.getClass(type1).isAssignableFrom(this.getClass(type2));
      } else if (this.isAssignableFrom(type1, this.currentSuperClass)) {
         return true;
      } else {
         if (this.currentClassInterfaces != null) {
            for(Type currentClassInterface : this.currentClassInterfaces) {
               if (this.isAssignableFrom(type1, currentClassInterface)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   protected Class<?> getClass(Type type) {
      try {
         return type.getSort() == 9
            ? Class.forName(type.getDescriptor().replace('/', '.'), false, this.loader)
            : Class.forName(type.getClassName(), false, this.loader);
      } catch (ClassNotFoundException var3) {
         throw new TypeNotPresentException(var3.toString(), var3);
      }
   }
}
