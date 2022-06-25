package io.micronaut.inject.writer;

import io.micronaut.asm.AnnotationVisitor;
import io.micronaut.asm.ClassVisitor;
import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.MethodVisitor;
import io.micronaut.asm.Opcodes;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.asm.commons.Method;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.annotation.AnnotationMetadataReference;
import io.micronaut.inject.annotation.AnnotationMetadataWriter;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.ast.GenericPlaceholderElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.ast.TypedElement;
import io.micronaut.inject.processing.JavaModelUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Internal
public abstract class AbstractClassFileWriter implements Opcodes, OriginatingElements, ClassOutputWriter {
   protected static final Type TYPE_ARGUMENT = Type.getType(Argument.class);
   protected static final Type TYPE_ARGUMENT_ARRAY = Type.getType(Argument[].class);
   protected static final String ZERO_ARGUMENTS_CONSTANT = "ZERO_ARGUMENTS";
   protected static final String CONSTRUCTOR_NAME = "<init>";
   protected static final String DESCRIPTOR_DEFAULT_CONSTRUCTOR = "()V";
   protected static final Method METHOD_DEFAULT_CONSTRUCTOR = new Method("<init>", "()V");
   protected static final Type TYPE_OBJECT = Type.getType(Object.class);
   protected static final Type TYPE_CLASS = Type.getType(Class.class);
   protected static final int DEFAULT_MAX_STACK = 13;
   protected static final Type TYPE_GENERATED = Type.getType(Generated.class);
   protected static final Pattern ARRAY_PATTERN = Pattern.compile("(\\[\\])+$");
   protected static final Method METHOD_CREATE_ARGUMENT_SIMPLE = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(Argument.class, "of", Class.class, String.class)
   );
   protected static final Method METHOD_GENERIC_PLACEHOLDER_SIMPLE = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(Argument.class, "ofTypeVariable", Class.class, String.class, String.class)
   );
   protected static final Method METHOD_CREATE_TYPE_VARIABLE_SIMPLE = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(Argument.class, "ofTypeVariable", Class.class, String.class)
   );
   private static final Method METHOD_CREATE_ARGUMENT_WITH_ANNOTATION_METADATA_GENERICS = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(Argument.class, "of", Class.class, String.class, AnnotationMetadata.class, Argument[].class)
   );
   private static final Method METHOD_CREATE_TYPE_VAR_WITH_ANNOTATION_METADATA_GENERICS = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(Argument.class, "ofTypeVariable", Class.class, String.class, AnnotationMetadata.class, Argument[].class)
   );
   private static final Method METHOD_CREATE_GENERIC_PLACEHOLDER_WITH_ANNOTATION_METADATA_GENERICS = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(
         Argument.class, "ofTypeVariable", Class.class, String.class, String.class, AnnotationMetadata.class, Argument[].class
      )
   );
   private static final Method METHOD_CREATE_ARGUMENT_WITH_ANNOTATION_METADATA_CLASS_GENERICS = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(Argument.class, "of", Class.class, AnnotationMetadata.class, Class[].class)
   );
   private static final Type ANNOTATION_UTIL_TYPE = Type.getType(AnnotationUtil.class);
   private static final Type MAP_TYPE = Type.getType(Map.class);
   private static final String EMPTY_MAP = "EMPTY_MAP";
   private static final Method[] MAP_OF = new Method[11];
   private static final Method MAP_BY_ARRAY;
   private static final Method INTERN_MAP_OF_METHOD;
   protected final OriginatingElements originatingElements;

   @Deprecated
   protected AbstractClassFileWriter(Element originatingElement) {
      this(OriginatingElements.of(originatingElement));
   }

   protected AbstractClassFileWriter(Element... originatingElements) {
      this(OriginatingElements.of(originatingElements));
   }

   protected AbstractClassFileWriter(OriginatingElements originatingElements) {
      this.originatingElements = (OriginatingElements)Objects.requireNonNull(originatingElements, "The originating elements cannot be null");
   }

   @NonNull
   @Override
   public Element[] getOriginatingElements() {
      return this.originatingElements.getOriginatingElements();
   }

   @Override
   public void addOriginatingElement(@NonNull Element element) {
      this.originatingElements.addOriginatingElement(element);
   }

   protected static void pushTypeArgumentElements(
      Type owningType,
      ClassWriter owningTypeWriter,
      GeneratorAdapter generatorAdapter,
      String declaringElementName,
      Map<String, ClassElement> types,
      Map<String, Integer> defaults,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      if (types != null && !types.isEmpty()) {
         Set<String> visitedTypes = new HashSet(5);
         pushTypeArgumentElements(owningType, owningTypeWriter, generatorAdapter, declaringElementName, types, visitedTypes, defaults, loadTypeMethods);
      } else {
         generatorAdapter.visitInsn(1);
      }
   }

   private static void pushTypeArgumentElements(
      Type owningType,
      ClassWriter declaringClassWriter,
      GeneratorAdapter generatorAdapter,
      String declaringElementName,
      Map<String, ClassElement> types,
      Set<String> visitedTypes,
      Map<String, Integer> defaults,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      if (visitedTypes.contains(declaringElementName)) {
         generatorAdapter.getStatic(TYPE_ARGUMENT, "ZERO_ARGUMENTS", TYPE_ARGUMENT_ARRAY);
      } else {
         visitedTypes.add(declaringElementName);
         int len = types.size();
         pushNewArray(generatorAdapter, Argument.class, len);
         int i = 0;

         for(Entry<String, ClassElement> entry : types.entrySet()) {
            generatorAdapter.push(i);
            String argumentName = (String)entry.getKey();
            ClassElement classElement = (ClassElement)entry.getValue();
            Type classReference = JavaModelUtils.getTypeReference(classElement);
            Map<String, ClassElement> typeArguments = classElement.getTypeArguments();
            if (!CollectionUtils.isNotEmpty(typeArguments) && classElement.getAnnotationMetadata() == AnnotationMetadata.EMPTY_METADATA) {
               buildArgument(generatorAdapter, argumentName, classElement);
            } else {
               buildArgumentWithGenerics(
                  owningType,
                  declaringClassWriter,
                  generatorAdapter,
                  argumentName,
                  classReference,
                  classElement,
                  typeArguments,
                  visitedTypes,
                  defaults,
                  loadTypeMethods
               );
            }

            generatorAdapter.visitInsn(83);
            if (i != len - 1) {
               generatorAdapter.visitInsn(89);
            }

            ++i;
         }
      }

   }

   protected static void buildArgument(GeneratorAdapter generatorAdapter, String argumentName, Type objectType) {
      generatorAdapter.push(objectType);
      generatorAdapter.push(argumentName);
      invokeInterfaceStaticMethod(generatorAdapter, Argument.class, METHOD_CREATE_ARGUMENT_SIMPLE);
   }

   protected static void buildArgument(GeneratorAdapter generatorAdapter, String argumentName, ClassElement objectType) {
      generatorAdapter.push(getTypeReference(objectType));
      generatorAdapter.push(argumentName);
      boolean isTypeVariable = objectType instanceof GenericPlaceholderElement || objectType.isTypeVariable();
      if (isTypeVariable) {
         String variableName = argumentName;
         if (objectType instanceof GenericPlaceholderElement) {
            GenericPlaceholderElement gpe = (GenericPlaceholderElement)objectType;
            variableName = gpe.getVariableName();
         }

         boolean hasVariable = !variableName.equals(argumentName);
         if (hasVariable) {
            generatorAdapter.push(variableName);
         }

         invokeInterfaceStaticMethod(generatorAdapter, Argument.class, hasVariable ? METHOD_GENERIC_PLACEHOLDER_SIMPLE : METHOD_CREATE_TYPE_VARIABLE_SIMPLE);
      } else {
         invokeInterfaceStaticMethod(generatorAdapter, Argument.class, METHOD_CREATE_ARGUMENT_SIMPLE);
      }

   }

   protected static void buildArgumentWithGenerics(
      Type owningType,
      ClassWriter owningClassWriter,
      GeneratorAdapter generatorAdapter,
      String argumentName,
      Type typeReference,
      ClassElement classElement,
      Map<String, ClassElement> typeArguments,
      Set<String> visitedTypes,
      Map<String, Integer> defaults,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      generatorAdapter.push(typeReference);
      generatorAdapter.push(argumentName);
      AnnotationMetadata annotationMetadata = classElement.getAnnotationMetadata();
      boolean hasAnnotationMetadata = annotationMetadata != AnnotationMetadata.EMPTY_METADATA;
      if (!hasAnnotationMetadata && typeArguments.isEmpty()) {
         invokeInterfaceStaticMethod(generatorAdapter, Argument.class, METHOD_CREATE_ARGUMENT_SIMPLE);
      } else {
         if (!hasAnnotationMetadata) {
            generatorAdapter.visitInsn(1);
         } else {
            AnnotationMetadataWriter.instantiateNewMetadata(
               owningType, owningClassWriter, generatorAdapter, (DefaultAnnotationMetadata)annotationMetadata, defaults, loadTypeMethods
            );
         }

         pushTypeArgumentElements(
            owningType, owningClassWriter, generatorAdapter, classElement.getName(), typeArguments, visitedTypes, defaults, loadTypeMethods
         );
         invokeInterfaceStaticMethod(
            generatorAdapter,
            Argument.class,
            classElement.isTypeVariable() ? METHOD_CREATE_TYPE_VAR_WITH_ANNOTATION_METADATA_GENERICS : METHOD_CREATE_ARGUMENT_WITH_ANNOTATION_METADATA_GENERICS
         );
      }
   }

   protected static void buildArgumentWithGenerics(
      GeneratorAdapter generatorAdapter, Type type, AnnotationMetadataReference annotationMetadata, ClassElement[] generics
   ) {
      generatorAdapter.push(type);
      AnnotationMetadataWriter.pushAnnotationMetadataReference(generatorAdapter, annotationMetadata);
      pushNewArray(generatorAdapter, Class.class, generics.length);
      int len = generics.length;

      for(int i = 0; i < len; ++i) {
         ClassElement generic = generics[i];
         pushStoreInArray(generatorAdapter, i, len, () -> generatorAdapter.push(getTypeReference(generic)));
      }

      invokeInterfaceStaticMethod(generatorAdapter, Argument.class, METHOD_CREATE_ARGUMENT_WITH_ANNOTATION_METADATA_CLASS_GENERICS);
   }

   protected static void pushBuildArgumentsForMethod(
      String declaringElementName,
      Type owningType,
      ClassWriter declaringClassWriter,
      GeneratorAdapter generatorAdapter,
      Collection<ParameterElement> argumentTypes,
      Map<String, Integer> defaults,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      int len = argumentTypes.size();
      pushNewArray(generatorAdapter, Argument.class, len);
      int i = 0;

      for(ParameterElement entry : argumentTypes) {
         generatorAdapter.push(i);
         ClassElement classElement = entry.getGenericType();
         String argumentName = entry.getName();
         AnnotationMetadata annotationMetadata = entry.getAnnotationMetadata();
         Map<String, ClassElement> typeArguments = classElement.getTypeArguments();
         pushCreateArgument(
            declaringElementName,
            owningType,
            declaringClassWriter,
            generatorAdapter,
            argumentName,
            classElement,
            annotationMetadata,
            typeArguments,
            defaults,
            loadTypeMethods
         );
         generatorAdapter.visitInsn(83);
         if (i != len - 1) {
            generatorAdapter.visitInsn(89);
         }

         ++i;
      }

   }

   protected void pushReturnTypeArgument(
      Type owningType,
      ClassWriter classWriter,
      GeneratorAdapter generatorAdapter,
      String declaringTypeName,
      ClassElement argument,
      Map<String, Integer> defaults,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      Type type = Type.getType(Argument.class);
      if (argument.isPrimitive() && !argument.isArray()) {
         String constantName = argument.getName().toUpperCase(Locale.ENGLISH);
         generatorAdapter.getStatic(type, constantName, type);
      } else {
         if (!argument.isArray()
            && String.class.getName().equals(argument.getType().getName())
            && argument.getName().equals(argument.getType().getName())
            && argument.getAnnotationMetadata().isEmpty()) {
            generatorAdapter.getStatic(type, "STRING", type);
            return;
         }

         pushCreateArgument(
            declaringTypeName,
            owningType,
            classWriter,
            generatorAdapter,
            argument.getName(),
            argument,
            AnnotationMetadata.EMPTY_METADATA,
            argument.getTypeArguments(),
            defaults,
            loadTypeMethods
         );
      }

   }

   protected static void pushCreateArgument(
      String declaringTypeName,
      Type owningType,
      ClassWriter declaringClassWriter,
      GeneratorAdapter generatorAdapter,
      String argumentName,
      TypedElement typedElement,
      AnnotationMetadata annotationMetadata,
      Map<String, ClassElement> typeArguments,
      Map<String, Integer> defaults,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      Type argumentType = JavaModelUtils.getTypeReference(typedElement);
      generatorAdapter.push(argumentType);
      generatorAdapter.push(argumentName);
      boolean hasAnnotations = !annotationMetadata.isEmpty() && annotationMetadata instanceof DefaultAnnotationMetadata;
      boolean hasTypeArguments = typeArguments != null && !typeArguments.isEmpty();
      boolean isGenericPlaceholder = typedElement instanceof GenericPlaceholderElement;
      boolean isTypeVariable = isGenericPlaceholder || typedElement instanceof ClassElement && ((ClassElement)typedElement).isTypeVariable();
      String variableName = argumentName;
      if (isGenericPlaceholder) {
         variableName = ((GenericPlaceholderElement)typedElement).getVariableName();
      }

      boolean hasVariableName = !variableName.equals(argumentName);
      if (!hasAnnotations && !hasTypeArguments && !isTypeVariable) {
         invokeInterfaceStaticMethod(generatorAdapter, Argument.class, METHOD_CREATE_ARGUMENT_SIMPLE);
      } else {
         if (isTypeVariable && hasVariableName) {
            generatorAdapter.push(variableName);
         }

         if (hasAnnotations) {
            AnnotationMetadataWriter.instantiateNewMetadata(
               owningType, declaringClassWriter, generatorAdapter, (DefaultAnnotationMetadata)annotationMetadata, defaults, loadTypeMethods
            );
         } else {
            generatorAdapter.visitInsn(1);
         }

         if (hasTypeArguments) {
            pushTypeArgumentElements(owningType, declaringClassWriter, generatorAdapter, declaringTypeName, typeArguments, defaults, loadTypeMethods);
         } else {
            generatorAdapter.visitInsn(1);
         }

         if (isTypeVariable) {
            invokeInterfaceStaticMethod(
               generatorAdapter,
               Argument.class,
               hasVariableName ? METHOD_CREATE_GENERIC_PLACEHOLDER_WITH_ANNOTATION_METADATA_GENERICS : METHOD_CREATE_TYPE_VAR_WITH_ANNOTATION_METADATA_GENERICS
            );
         } else {
            invokeInterfaceStaticMethod(generatorAdapter, Argument.class, METHOD_CREATE_ARGUMENT_WITH_ANNOTATION_METADATA_GENERICS);
         }

      }
   }

   public void writeTo(File targetDir) throws IOException {
      this.accept(this.newClassWriterOutputVisitor(targetDir));
   }

   protected void writeBooleanMethod(ClassWriter classWriter, String methodName, Supplier<Boolean> valueSupplier) {
      GeneratorAdapter isSingletonMethod = this.startPublicMethodZeroArgs(classWriter, Boolean.TYPE, methodName);
      isSingletonMethod.loadThis();
      isSingletonMethod.push(valueSupplier.get());
      isSingletonMethod.returnValue();
      isSingletonMethod.visitMaxs(1, 1);
      isSingletonMethod.visitEnd();
   }

   @Nullable
   public Element getOriginatingElement() {
      Element[] originatingElements = this.getOriginatingElements();
      return ArrayUtils.isNotEmpty(originatingElements) ? originatingElements[0] : null;
   }

   protected void implementInterceptedTypeMethod(Type interceptedType, ClassWriter classWriter) {
      GeneratorAdapter getTargetTypeMethod = this.startPublicMethodZeroArgs(classWriter, Class.class, "getInterceptedType");
      getTargetTypeMethod.loadThis();
      getTargetTypeMethod.push(interceptedType);
      getTargetTypeMethod.returnValue();
      getTargetTypeMethod.visitMaxs(1, 1);
      getTargetTypeMethod.visitEnd();
   }

   protected static String getTypeDescriptor(TypedElement type) {
      return JavaModelUtils.getTypeReference(type).getDescriptor();
   }

   protected static String getTypeDescriptor(Class<?> type) {
      return Type.getDescriptor(type);
   }

   protected static String getTypeDescriptor(String type) {
      return getTypeDescriptor(type);
   }

   protected static Type getTypeReferenceForName(String className, String... genericTypes) {
      String referenceString = getTypeDescriptor(className, genericTypes);
      return Type.getType(referenceString);
   }

   protected static Type getTypeReference(TypedElement type) {
      return JavaModelUtils.getTypeReference(type);
   }

   protected static void pushBoxPrimitiveIfNecessary(Type fieldType, MethodVisitor injectMethodVisitor) {
      Optional<Class> pt = ClassUtils.getPrimitiveType(fieldType.getClassName());
      Class<?> wrapperType = (Class)pt.map(ReflectionUtils::getWrapperType).orElse(null);
      if (wrapperType != null && wrapperType != Void.class) {
         Type wrapper = Type.getType(wrapperType);
         String primitiveName = fieldType.getClassName();
         String sig = wrapperType.getName() + " valueOf(" + primitiveName + ")";
         Method valueOfMethod = Method.getMethod(sig);
         injectMethodVisitor.visitMethodInsn(184, wrapper.getInternalName(), "valueOf", valueOfMethod.getDescriptor(), false);
      }

   }

   protected static void pushBoxPrimitiveIfNecessary(Class<?> fieldType, MethodVisitor injectMethodVisitor) {
      Class<?> wrapperType = ReflectionUtils.getWrapperType(fieldType);
      if (wrapperType != null && wrapperType != Void.class) {
         Type wrapper = Type.getType(wrapperType);
         String primitiveName = fieldType.getName();
         String sig = wrapperType.getName() + " valueOf(" + primitiveName + ")";
         Method valueOfMethod = Method.getMethod(sig);
         injectMethodVisitor.visitMethodInsn(184, wrapper.getInternalName(), "valueOf", valueOfMethod.getDescriptor(), false);
      }

   }

   protected static void pushBoxPrimitiveIfNecessary(TypedElement fieldType, MethodVisitor injectMethodVisitor) {
      ClassElement type = fieldType.getType();
      if (type.isPrimitive() && !type.isArray()) {
         String primitiveName = type.getName();
         Optional<Class> pt = ClassUtils.getPrimitiveType(primitiveName);
         Class<?> wrapperType = (Class)pt.map(ReflectionUtils::getWrapperType).orElse(null);
         if (wrapperType != null && wrapperType != Void.class) {
            Type wrapper = Type.getType(wrapperType);
            String sig = wrapperType.getName() + " valueOf(" + primitiveName + ")";
            Method valueOfMethod = Method.getMethod(sig);
            injectMethodVisitor.visitMethodInsn(184, wrapper.getInternalName(), "valueOf", valueOfMethod.getDescriptor(), false);
         }
      }

   }

   protected static void pushCastToType(MethodVisitor methodVisitor, Type type) {
      String internalName = getInternalNameForCast(type);
      methodVisitor.visitTypeInsn(192, internalName);
      Type primitiveType = null;
      Optional<Class> pt = ClassUtils.getPrimitiveType(type.getClassName());
      if (pt.isPresent()) {
         primitiveType = Type.getType((Class<?>)pt.get());
      }

      pushPrimitiveCastIfRequired(methodVisitor, internalName, primitiveType);
   }

   private static void pushPrimitiveCastIfRequired(MethodVisitor methodVisitor, String internalName, Type primitiveType) {
      if (primitiveType != null) {
         Method valueMethod = null;
         switch(primitiveType.getSort()) {
            case 1:
               valueMethod = Method.getMethod("boolean booleanValue()");
               break;
            case 2:
               valueMethod = Method.getMethod("char charValue()");
               break;
            case 3:
               valueMethod = Method.getMethod("byte byteValue()");
               break;
            case 4:
               valueMethod = Method.getMethod("short shortValue()");
               break;
            case 5:
               valueMethod = Method.getMethod("int intValue()");
               break;
            case 6:
               valueMethod = Method.getMethod("float floatValue()");
               break;
            case 7:
               valueMethod = Method.getMethod("long longValue()");
               break;
            case 8:
               valueMethod = Method.getMethod("double doubleValue()");
         }

         if (valueMethod != null) {
            methodVisitor.visitMethodInsn(182, internalName, valueMethod.getName(), valueMethod.getDescriptor(), false);
         }
      }

   }

   protected static void pushCastToType(MethodVisitor methodVisitor, TypedElement type) {
      String internalName = getInternalNameForCast(type);
      methodVisitor.visitTypeInsn(192, internalName);
      Type primitiveType = null;
      if (type.isPrimitive() && !type.isArray()) {
         Optional<Class> pt = ClassUtils.getPrimitiveType(type.getType().getName());
         if (pt.isPresent()) {
            primitiveType = Type.getType((Class<?>)pt.get());
         }
      }

      pushPrimitiveCastIfRequired(methodVisitor, internalName, primitiveType);
   }

   protected static void pushCastToType(MethodVisitor methodVisitor, Class<?> type) {
      String internalName = getInternalNameForCast(type);
      methodVisitor.visitTypeInsn(192, internalName);
      Type primitiveType = null;
      if (type.isPrimitive()) {
         primitiveType = Type.getType(type);
      }

      pushPrimitiveCastIfRequired(methodVisitor, internalName, primitiveType);
   }

   protected static void pushReturnValue(MethodVisitor methodVisitor, TypedElement type) {
      Class<?> primitiveTypeClass = null;
      if (type.isPrimitive() && !type.isArray()) {
         primitiveTypeClass = (Class)ClassUtils.getPrimitiveType(type.getType().getName()).orElse(null);
      }

      if (primitiveTypeClass == null) {
         methodVisitor.visitInsn(176);
      } else {
         Type primitiveType = Type.getType(primitiveTypeClass);
         switch(primitiveType.getSort()) {
            case 0:
               methodVisitor.visitInsn(177);
               break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
               methodVisitor.visitInsn(172);
               break;
            case 6:
               methodVisitor.visitInsn(174);
               break;
            case 7:
               methodVisitor.visitInsn(173);
               break;
            case 8:
               methodVisitor.visitInsn(175);
         }
      }

   }

   protected static void pushMethodNameAndTypesArguments(GeneratorAdapter methodVisitor, String methodName, Collection<ClassElement> argumentTypes) {
      methodVisitor.visitLdcInsn(methodName);
      int argTypeCount = argumentTypes.size();
      if (!argumentTypes.isEmpty()) {
         pushNewArray(methodVisitor, Class.class, argTypeCount);
         Iterator<ClassElement> argIterator = argumentTypes.iterator();

         for(int i = 0; i < argTypeCount; ++i) {
            pushStoreTypeInArray(methodVisitor, i, argTypeCount, (ClassElement)argIterator.next());
         }
      } else {
         pushNewArray(methodVisitor, Class.class, 0);
      }

   }

   protected static void pushNewArray(GeneratorAdapter methodVisitor, Class<?> arrayType, int size) {
      Type t = Type.getType(arrayType);
      pushNewArray(methodVisitor, t, size);
   }

   protected static void pushNewArray(GeneratorAdapter methodVisitor, Type arrayType, int size) {
      methodVisitor.push(size);
      methodVisitor.newArray(arrayType);
      if (size > 0) {
         methodVisitor.visitInsn(89);
      }

   }

   protected static void pushStoreStringInArray(GeneratorAdapter methodVisitor, int index, int size, String string) {
      methodVisitor.push(index);
      methodVisitor.push(string);
      methodVisitor.visitInsn(83);
      if (index != size - 1) {
         methodVisitor.dup();
      }

   }

   protected static void pushStoreInArray(GeneratorAdapter methodVisitor, int index, int size, Runnable runnable) {
      pushStoreInArray(methodVisitor, TYPE_OBJECT, index, size, runnable);
   }

   protected static void pushStoreInArray(GeneratorAdapter methodVisitor, Type type, int index, int size, Runnable runnable) {
      methodVisitor.push(index);
      runnable.run();
      methodVisitor.arrayStore(type);
      if (index != size - 1) {
         methodVisitor.dup();
      }

   }

   protected static void pushStoreTypeInArray(GeneratorAdapter methodVisitor, int index, int size, ClassElement type) {
      methodVisitor.push(index);
      if (type.isPrimitive()) {
         Class<?> typeClass = (Class)ClassUtils.getPrimitiveType(type.getName()).orElse(null);
         if (typeClass != null) {
            if (type.isArray()) {
               Type arrayType = Type.getType(Array.newInstance(typeClass, 0).getClass());
               methodVisitor.push(arrayType);
            } else {
               Type wrapperType = Type.getType(ReflectionUtils.getWrapperType(typeClass));
               methodVisitor.visitFieldInsn(178, wrapperType.getInternalName(), "TYPE", Type.getDescriptor(Class.class));
            }
         } else {
            methodVisitor.push(JavaModelUtils.getTypeReference(type));
         }
      } else {
         methodVisitor.push(JavaModelUtils.getTypeReference(type));
      }

      methodVisitor.arrayStore(TYPE_CLASS);
      if (index < size - 1) {
         methodVisitor.dup();
      }

   }

   protected Type[] getTypes(Collection<ClassElement> types) {
      Type[] converted = new Type[types.size()];
      Iterator<ClassElement> iter = types.iterator();

      for(int i = 0; i < converted.length; ++i) {
         ClassElement type = (ClassElement)iter.next();
         converted[i] = JavaModelUtils.getTypeReference(type);
      }

      return converted;
   }

   protected static Type getObjectType(Object type) {
      if (type instanceof TypedElement) {
         String name = ((TypedElement)type).getType().getName();
         String internalName = getTypeDescriptor(name);
         return Type.getType(internalName);
      } else if (type instanceof Class) {
         return Type.getType((Class<?>)type);
      } else if (type instanceof String) {
         String className = type.toString();
         String internalName = getTypeDescriptor(className);
         return Type.getType(internalName);
      } else {
         throw new IllegalArgumentException("Type reference [" + type + "] should be a Class or a String representing the class name");
      }
   }

   protected static String getTypeDescriptor(String className, String... genericTypes) {
      if (JavaModelUtils.NAME_TO_TYPE_MAP.containsKey(className)) {
         return (String)JavaModelUtils.NAME_TO_TYPE_MAP.get(className);
      } else {
         String internalName = getInternalName(className);
         StringBuilder start = new StringBuilder(40);
         Matcher matcher = ARRAY_PATTERN.matcher(className);
         if (matcher.find()) {
            int dimensions = matcher.group(0).length() / 2;

            for(int i = 0; i < dimensions; ++i) {
               start.append('[');
            }
         }

         start.append('L').append(internalName);
         if (genericTypes != null && genericTypes.length > 0) {
            start.append('<');

            for(String genericType : genericTypes) {
               start.append(getTypeDescriptor(genericType));
            }

            start.append('>');
         }

         return start.append(';').toString();
      }
   }

   protected static String getMethodDescriptor(String returnType, String... argumentTypes) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for(String argumentType : argumentTypes) {
         builder.append(getTypeDescriptor(argumentType));
      }

      builder.append(')');
      builder.append(getTypeDescriptor(returnType));
      return builder.toString();
   }

   protected static String getMethodDescriptor(TypedElement returnType, Collection<? extends TypedElement> argumentTypes) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for(TypedElement argumentType : argumentTypes) {
         builder.append(getTypeDescriptor(argumentType));
      }

      builder.append(')');
      builder.append(getTypeDescriptor(returnType));
      return builder.toString();
   }

   protected static String getMethodDescriptorForReturnType(Type returnType, Collection<? extends TypedElement> argumentTypes) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for(TypedElement argumentType : argumentTypes) {
         builder.append(getTypeDescriptor(argumentType));
      }

      builder.append(')');
      builder.append(returnType.getDescriptor());
      return builder.toString();
   }

   protected static String getMethodDescriptor(Class<?> returnType, Collection<Class<?>> argumentTypes) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for(Class<?> argumentType : argumentTypes) {
         builder.append(Type.getDescriptor(argumentType));
      }

      builder.append(')');
      builder.append(Type.getDescriptor(returnType));
      return builder.toString();
   }

   protected static String getMethodDescriptor(Type returnType, Collection<Type> argumentTypes) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for(Type argumentType : argumentTypes) {
         builder.append(argumentType.getDescriptor());
      }

      builder.append(')');
      builder.append(returnType.getDescriptor());
      return builder.toString();
   }

   protected static String getMethodSignature(String returnTypeReference, String... argReferenceTypes) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for(String argumentType : argReferenceTypes) {
         builder.append(argumentType);
      }

      builder.append(')');
      builder.append(returnTypeReference);
      return builder.toString();
   }

   protected static String getConstructorDescriptor(Class<?>... argumentTypes) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for(Class<?> argumentType : argumentTypes) {
         builder.append(getTypeDescriptor(argumentType));
      }

      return builder.append(")V").toString();
   }

   protected static String getConstructorDescriptor(Type[] argumentTypes) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for(Type argumentType : argumentTypes) {
         builder.append(argumentType.getDescriptor());
      }

      return builder.append(")V").toString();
   }

   protected static String getConstructorDescriptor(Collection<ParameterElement> argList) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for(ParameterElement argumentType : argList) {
         builder.append(getTypeDescriptor(argumentType));
      }

      return builder.append(")V").toString();
   }

   protected void writeClassToDisk(File targetDir, ClassWriter classWriter, String className) throws IOException {
      if (targetDir != null) {
         String fileName = className.replace('.', '/') + ".class";
         File targetFile = new File(targetDir, fileName);
         targetFile.getParentFile().mkdirs();
         OutputStream outputStream = Files.newOutputStream(targetFile.toPath());
         Throwable var7 = null;

         try {
            this.writeClassToDisk(outputStream, classWriter);
         } catch (Throwable var16) {
            var7 = var16;
            throw var16;
         } finally {
            if (outputStream != null) {
               if (var7 != null) {
                  try {
                     outputStream.close();
                  } catch (Throwable var15) {
                     var7.addSuppressed(var15);
                  }
               } else {
                  outputStream.close();
               }
            }

         }
      }

   }

   protected void writeClassToDisk(OutputStream out, ClassWriter classWriter) throws IOException {
      byte[] bytes = classWriter.toByteArray();
      out.write(bytes);
   }

   protected GeneratorAdapter startConstructor(ClassVisitor classWriter) {
      MethodVisitor defaultConstructor = classWriter.visitMethod(1, "<init>", "()V", null, null);
      return new GeneratorAdapter(defaultConstructor, 1, "<init>", "()V");
   }

   protected GeneratorAdapter startConstructor(ClassVisitor classWriter, Class<?>... argumentTypes) {
      String descriptor = getConstructorDescriptor(argumentTypes);
      return new GeneratorAdapter(classWriter.visitMethod(1, "<init>", descriptor, null, null), 1, "<init>", descriptor);
   }

   protected void startClass(ClassVisitor classWriter, String className, Type superType) {
      classWriter.visit(52, 4096, className, null, superType.getInternalName(), null);
      classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
   }

   protected void startPublicClass(ClassVisitor classWriter, String className, Type superType) {
      classWriter.visit(52, 4097, className, null, superType.getInternalName(), null);
      classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
   }

   protected void startService(ClassVisitor classWriter, Class<?> serviceType, String internalClassName, Type superType) {
      this.startService(classWriter, serviceType.getName(), internalClassName, superType);
   }

   protected void startService(ClassVisitor classWriter, String serviceName, String internalClassName, Type superType, String... interfaces) {
      classWriter.visit(52, 4113, internalClassName, null, superType.getInternalName(), interfaces);
      AnnotationVisitor annotationVisitor = classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
      annotationVisitor.visit("service", serviceName);
      annotationVisitor.visitEnd();
   }

   protected void startFinalClass(ClassVisitor classWriter, String className, Type superType) {
      classWriter.visit(52, 4112, className, null, superType.getInternalName(), null);
      classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
   }

   protected void startPublicFinalClass(ClassVisitor classWriter, String className, Type superType) {
      classWriter.visit(52, 4113, className, null, superType.getInternalName(), null);
      classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
   }

   protected void startClass(ClassWriter classWriter, String className, Type superType, String genericSignature) {
      classWriter.visit(52, 4096, className, genericSignature, superType.getInternalName(), null);
      classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
   }

   protected void invokeConstructor(MethodVisitor cv, Class superClass, Class... argumentTypes) {
      try {
         Type superType = Type.getType(superClass);
         Type superConstructor = Type.getType(superClass.getDeclaredConstructor(argumentTypes));
         cv.visitMethodInsn(183, superType.getInternalName(), "<init>", superConstructor.getDescriptor(), false);
      } catch (NoSuchMethodException var6) {
         throw new ClassGenerationException("Micronaut version on compile classpath doesn't match", var6);
      }
   }

   protected static void invokeInterfaceStaticMethod(MethodVisitor visitor, Class targetType, Method method) {
      Type type = Type.getType(targetType);
      String owner = type.getSort() == 9 ? type.getDescriptor() : type.getInternalName();
      visitor.visitMethodInsn(184, owner, method.getName(), method.getDescriptor(), true);
   }

   protected GeneratorAdapter startPublicMethodZeroArgs(ClassWriter classWriter, Class returnType, String methodName) {
      Type methodType = Type.getMethodType(Type.getType(returnType));
      return new GeneratorAdapter(classWriter.visitMethod(1, methodName, methodType.getDescriptor(), null, null), 1, methodName, methodType.getDescriptor());
   }

   protected GeneratorAdapter startPublicFinalMethodZeroArgs(ClassWriter classWriter, Class returnType, String methodName) {
      Type methodType = Type.getMethodType(Type.getType(returnType));
      return new GeneratorAdapter(classWriter.visitMethod(17, methodName, methodType.getDescriptor(), null, null), 1, methodName, methodType.getDescriptor());
   }

   protected static String getInternalName(String className) {
      String newClassName = className.replace('.', '/');
      Matcher matcher = ARRAY_PATTERN.matcher(newClassName);
      if (matcher.find()) {
         newClassName = matcher.replaceFirst("");
      }

      return newClassName;
   }

   protected static String getInternalNameForCast(TypedElement type) {
      ClassElement ce = type.getType();
      if (ce.isPrimitive() && !ce.isArray()) {
         Optional<Class> pt = ClassUtils.getPrimitiveType(ce.getName());
         return pt.isPresent() ? Type.getInternalName(ReflectionUtils.getWrapperType((Class)pt.get())) : JavaModelUtils.getTypeReference(ce).getInternalName();
      } else {
         return JavaModelUtils.getTypeReference(ce).getInternalName();
      }
   }

   protected static String getInternalNameForCast(Class<?> typeClass) {
      if (typeClass.isPrimitive()) {
         typeClass = ReflectionUtils.getWrapperType(typeClass);
      }

      return Type.getInternalName(typeClass);
   }

   protected static String getInternalNameForCast(Type type) {
      Optional<Class> pt = ClassUtils.getPrimitiveType(type.getClassName());
      return pt.isPresent() ? Type.getInternalName(ReflectionUtils.getWrapperType((Class)pt.get())) : type.getInternalName();
   }

   protected String getClassFileName(String className) {
      return className.replace('.', File.separatorChar) + ".class";
   }

   protected ClassWriterOutputVisitor newClassWriterOutputVisitor(File compilationDir) {
      return new DirectoryClassWriterOutputVisitor(compilationDir);
   }

   protected void returnVoid(GeneratorAdapter overriddenMethodGenerator) {
      overriddenMethodGenerator.pop();
      overriddenMethodGenerator.visitInsn(177);
   }

   protected GeneratorAdapter visitStaticInitializer(ClassVisitor classWriter) {
      MethodVisitor mv = classWriter.visitMethod(8, "<clinit>", "()V", null, null);
      return new GeneratorAdapter(mv, 8, "<clinit>", "()V");
   }

   protected GeneratorAdapter startPublicMethod(ClassWriter writer, String methodName, String returnType, String... argumentTypes) {
      return new GeneratorAdapter(
         writer.visitMethod(1, methodName, getMethodDescriptor(returnType, argumentTypes), null, null),
         1,
         methodName,
         getMethodDescriptor(returnType, argumentTypes)
      );
   }

   protected GeneratorAdapter startPublicMethod(ClassWriter writer, Method asmMethod) {
      String methodName = asmMethod.getName();
      return new GeneratorAdapter(writer.visitMethod(1, methodName, asmMethod.getDescriptor(), null, null), 1, methodName, asmMethod.getDescriptor());
   }

   protected GeneratorAdapter startProtectedMethod(ClassWriter writer, String methodName, String returnType, String... argumentTypes) {
      return new GeneratorAdapter(
         writer.visitMethod(4, methodName, getMethodDescriptor(returnType, argumentTypes), null, null),
         4,
         methodName,
         getMethodDescriptor(returnType, argumentTypes)
      );
   }

   protected void generateServiceDescriptor(String className, GeneratedFile generatedFile) throws IOException {
      CharSequence contents = generatedFile.getTextContent();
      if (contents != null) {
         String[] entries = contents.toString().split("\\n");
         if (!Arrays.asList(entries).contains(className)) {
            BufferedWriter w = new BufferedWriter(generatedFile.openWriter());
            Throwable var6 = null;

            try {
               w.newLine();
               w.write(className);
            } catch (Throwable var30) {
               var6 = var30;
               throw var30;
            } finally {
               if (w != null) {
                  if (var6 != null) {
                     try {
                        w.close();
                     } catch (Throwable var28) {
                        var6.addSuppressed(var28);
                     }
                  } else {
                     w.close();
                  }
               }

            }
         }
      } else {
         BufferedWriter w = new BufferedWriter(generatedFile.openWriter());
         Throwable var34 = null;

         try {
            w.write(className);
         } catch (Throwable var29) {
            var34 = var29;
            throw var29;
         } finally {
            if (w != null) {
               if (var34 != null) {
                  try {
                     w.close();
                  } catch (Throwable var27) {
                     var34.addSuppressed(var27);
                  }
               } else {
                  w.close();
               }
            }

         }
      }

   }

   protected void pushNewInstance(GeneratorAdapter generatorAdapter, Type typeToInstantiate) {
      generatorAdapter.newInstance(typeToInstantiate);
      generatorAdapter.dup();
      generatorAdapter.invokeConstructor(typeToInstantiate, METHOD_DEFAULT_CONSTRUCTOR);
   }

   @NonNull
   protected ClassElement invokeMethod(@NonNull GeneratorAdapter generatorAdapter, @NonNull MethodElement method) {
      ClassElement returnType = method.getReturnType();
      Method targetMethod = new Method(method.getName(), getMethodDescriptor(returnType, Arrays.asList(method.getParameters())));
      ClassElement declaringElement = method.getDeclaringType();
      Type declaringType = JavaModelUtils.getTypeReference(declaringElement);
      if (method.isStatic()) {
         generatorAdapter.invokeStatic(declaringType, targetMethod);
      } else if (declaringElement.isInterface()) {
         generatorAdapter.invokeInterface(declaringType, targetMethod);
      } else {
         generatorAdapter.invokeVirtual(declaringType, targetMethod);
      }

      return returnType;
   }

   public static <T> void pushStringMapOf(
      GeneratorAdapter generatorAdapter, Map<? extends CharSequence, T> annotationData, boolean skipEmpty, T empty, Consumer<T> pushValue
   ) {
      Set<? extends Entry<String, T>> entrySet = annotationData != null
         ? (Set)annotationData.entrySet()
            .stream()
            .filter(e -> !skipEmpty || e.getKey() != null && e.getValue() != null)
            .map(
               e -> e.getValue() == null && empty != null
                     ? new SimpleEntry(((CharSequence)e.getKey()).toString(), empty)
                     : new SimpleEntry(((CharSequence)e.getKey()).toString(), e.getValue())
            )
            .collect(Collectors.toCollection(() -> new TreeSet(Entry.comparingByKey())))
         : null;
      if (entrySet != null && !entrySet.isEmpty()) {
         if (entrySet.size() == 1 && ((Entry)entrySet.iterator().next()).getValue() == Collections.EMPTY_MAP) {
            for(Entry<String, T> entry : entrySet) {
               generatorAdapter.push((String)entry.getKey());
               pushValue.accept(entry.getValue());
            }

            generatorAdapter.invokeStatic(ANNOTATION_UTIL_TYPE, INTERN_MAP_OF_METHOD);
         } else if (entrySet.size() < MAP_OF.length) {
            for(Entry<String, T> entry : entrySet) {
               generatorAdapter.push((String)entry.getKey());
               pushValue.accept(entry.getValue());
            }

            generatorAdapter.invokeStatic(ANNOTATION_UTIL_TYPE, MAP_OF[entrySet.size()]);
         } else {
            int totalSize = entrySet.size() * 2;
            pushNewArray(generatorAdapter, Object.class, totalSize);
            int i = 0;

            for(Entry<? extends CharSequence, T> entry : entrySet) {
               String memberName = ((CharSequence)entry.getKey()).toString();
               pushStoreStringInArray(generatorAdapter, i++, totalSize, memberName);
               pushStoreInArray(generatorAdapter, i++, totalSize, () -> pushValue.accept(entry.getValue()));
            }

            generatorAdapter.invokeStatic(ANNOTATION_UTIL_TYPE, MAP_BY_ARRAY);
         }

      } else {
         generatorAdapter.getStatic(Type.getType(Collections.class), "EMPTY_MAP", MAP_TYPE);
      }
   }

   static {
      for(int i = 1; i < MAP_OF.length; ++i) {
         Class[] mapArgs = new Class[i * 2];

         for(int k = 0; k < i * 2; k += 2) {
            mapArgs[k] = String.class;
            mapArgs[k + 1] = Object.class;
         }

         MAP_OF[i] = Method.getMethod(ReflectionUtils.getRequiredMethod(AnnotationUtil.class, "mapOf", mapArgs));
      }

      MAP_BY_ARRAY = Method.getMethod(ReflectionUtils.getRequiredMethod(AnnotationUtil.class, "mapOf", Object[].class));
      INTERN_MAP_OF_METHOD = Method.getMethod(ReflectionUtils.getRequiredInternalMethod(AnnotationUtil.class, "internMapOf", String.class, Object.class));
   }
}
