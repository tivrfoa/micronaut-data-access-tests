package io.micronaut.inject.processing;

import io.micronaut.asm.Type;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.TypedElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

public class JavaModelUtils {
   public static final Map<String, String> NAME_TO_TYPE_MAP = new HashMap();
   private static final ElementKind RECORD_KIND = (ElementKind)ReflectionUtils.findDeclaredField(ElementKind.class, "RECORD").flatMap(field -> {
      try {
         return Optional.of((ElementKind)field.get(ElementKind.class));
      } catch (IllegalAccessException var2) {
         return Optional.empty();
      }
   }).orElse(null);
   private static final ElementKind RECORD_COMPONENT_KIND = (ElementKind)ReflectionUtils.findDeclaredField(ElementKind.class, "RECORD_COMPONENT")
      .flatMap(field -> {
         try {
            return Optional.of((ElementKind)field.get(ElementKind.class));
         } catch (IllegalAccessException var2) {
            return Optional.empty();
         }
      })
      .orElse(null);

   public static Optional<ElementKind> resolveKind(Element element) {
      if (element != null) {
         try {
            ElementKind kind = element.getKind();
            return Optional.of(kind);
         } catch (Exception var2) {
         }
      }

      return Optional.empty();
   }

   public static Optional<ElementKind> resolveKind(Element element, ElementKind expected) {
      Optional<ElementKind> elementKind = resolveKind(element);
      return elementKind.isPresent() && elementKind.get() == expected ? elementKind : Optional.empty();
   }

   public static boolean isInterface(Element element) {
      return resolveKind(element, ElementKind.INTERFACE).isPresent();
   }

   public static boolean isRecord(Element element) {
      return resolveKind(element, RECORD_KIND).isPresent();
   }

   public static boolean isClass(Element element) {
      return resolveKind(element, ElementKind.CLASS).isPresent();
   }

   public static boolean isEnum(Element element) {
      return resolveKind(element, ElementKind.ENUM).isPresent();
   }

   public static boolean isClassOrInterface(Element element) {
      return isInterface(element) || isClass(element);
   }

   public static String getClassName(TypeElement typeElement) {
      Name qualifiedName = typeElement.getQualifiedName();

      try {
         NestingKind nestingKind = typeElement.getNestingKind();
         if (nestingKind != NestingKind.MEMBER) {
            return qualifiedName.toString();
         } else {
            TypeElement enclosingElement = typeElement;

            StringBuilder builder;
            for(builder = new StringBuilder(); nestingKind == NestingKind.MEMBER; nestingKind = enclosingElement.getNestingKind()) {
               builder.insert(0, '$').insert(1, enclosingElement.getSimpleName());
               Element enclosing = enclosingElement.getEnclosingElement();
               if (!(enclosing instanceof TypeElement)) {
                  break;
               }

               enclosingElement = (TypeElement)enclosing;
            }

            Name enclosingName = enclosingElement.getQualifiedName();
            return enclosingName.toString() + builder;
         }
      } catch (RuntimeException var6) {
         return qualifiedName.toString();
      }
   }

   public static String getClassNameWithoutPackage(TypeElement typeElement) {
      try {
         NestingKind nestingKind = typeElement.getNestingKind();
         if (nestingKind != NestingKind.MEMBER) {
            return typeElement.getSimpleName().toString();
         } else {
            TypeElement enclosingElement = typeElement;

            StringBuilder builder;
            for(builder = new StringBuilder(); nestingKind == NestingKind.MEMBER; nestingKind = enclosingElement.getNestingKind()) {
               builder.insert(0, '$').insert(1, enclosingElement.getSimpleName());
               Element enclosing = enclosingElement.getEnclosingElement();
               if (!(enclosing instanceof TypeElement)) {
                  break;
               }

               enclosingElement = (TypeElement)enclosing;
            }

            Name enclosingName = enclosingElement.getSimpleName();
            return enclosingName.toString() + builder;
         }
      } catch (RuntimeException var5) {
         return typeElement.getSimpleName().toString();
      }
   }

   public static String getPackageName(TypeElement typeElement) {
      Element enclosingElement = typeElement.getEnclosingElement();

      while(enclosingElement != null && enclosingElement.getKind() != ElementKind.PACKAGE) {
         enclosingElement = enclosingElement.getEnclosingElement();
      }

      if (enclosingElement == null) {
         return "";
      } else {
         return enclosingElement instanceof PackageElement ? ((PackageElement)enclosingElement).getQualifiedName().toString() : enclosingElement.toString();
      }
   }

   public static String getClassArrayName(TypeElement typeElement) {
      return "[L" + getClassName(typeElement) + ";";
   }

   public static boolean isRecordOrRecordComponent(Element e) {
      return isRecord(e) || isRecordComponent(e);
   }

   public static boolean isRecordComponent(Element e) {
      return resolveKind(e, RECORD_COMPONENT_KIND).isPresent();
   }

   public static Type getTypeReference(TypedElement type) {
      ClassElement classElement = type.getType();
      if (type.isPrimitive()) {
         String internalName = (String)NAME_TO_TYPE_MAP.get(classElement.getName());
         if (!type.isArray()) {
            return Type.getType(internalName);
         } else {
            StringBuilder name = new StringBuilder(internalName);

            for(int i = 0; i < type.getArrayDimensions(); ++i) {
               name.insert(0, "[");
            }

            return Type.getObjectType(name.toString());
         }
      } else {
         Object nativeType = type.getNativeType();
         if (nativeType instanceof Class) {
            Class<?> t = (Class)nativeType;
            return Type.getType(t);
         } else {
            String internalName = type.getType().getName().replace('.', '/');
            if (internalName.isEmpty()) {
               return Type.getType(Object.class);
            } else if (!type.isArray()) {
               return Type.getObjectType(internalName);
            } else {
               StringBuilder name = new StringBuilder(internalName);
               name.insert(0, "L");

               for(int i = 0; i < type.getArrayDimensions(); ++i) {
                  name.insert(0, "[");
               }

               name.append(";");
               return Type.getObjectType(name.toString());
            }
         }
      }
   }

   public static String getClassname(TypedElement type) {
      return getTypeReference(type).getClassName();
   }

   static {
      NAME_TO_TYPE_MAP.put("void", "V");
      NAME_TO_TYPE_MAP.put("boolean", "Z");
      NAME_TO_TYPE_MAP.put("char", "C");
      NAME_TO_TYPE_MAP.put("int", "I");
      NAME_TO_TYPE_MAP.put("byte", "B");
      NAME_TO_TYPE_MAP.put("long", "J");
      NAME_TO_TYPE_MAP.put("double", "D");
      NAME_TO_TYPE_MAP.put("float", "F");
      NAME_TO_TYPE_MAP.put("short", "S");
   }
}
