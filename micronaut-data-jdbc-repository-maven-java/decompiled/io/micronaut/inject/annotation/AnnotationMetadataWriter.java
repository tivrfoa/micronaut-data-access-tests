package io.micronaut.inject.annotation;

import io.micronaut.asm.ClassVisitor;
import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;
import io.micronaut.asm.MethodVisitor;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.asm.commons.Method;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.writer.AbstractClassFileWriter;
import io.micronaut.inject.writer.ClassGenerationException;
import io.micronaut.inject.writer.ClassWriterOutputVisitor;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Internal
public class AnnotationMetadataWriter extends AbstractClassFileWriter {
   private static final Type TYPE_DEFAULT_ANNOTATION_METADATA = Type.getType(DefaultAnnotationMetadata.class);
   private static final Type TYPE_DEFAULT_ANNOTATION_METADATA_HIERARCHY = Type.getType(AnnotationMetadataHierarchy.class);
   private static final Type TYPE_ANNOTATION_CLASS_VALUE = Type.getType(AnnotationClassValue.class);
   private static final Method METHOD_LIST_OF = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(AnnotationUtil.class, "internListOf", Object[].class)
   );
   private static final Method METHOD_REGISTER_ANNOTATION_DEFAULTS = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(DefaultAnnotationMetadata.class, "registerAnnotationDefaults", AnnotationClassValue.class, Map.class)
   );
   private static final Method METHOD_REGISTER_ANNOTATION_TYPE = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(DefaultAnnotationMetadata.class, "registerAnnotationType", AnnotationClassValue.class)
   );
   private static final Method METHOD_REGISTER_REPEATABLE_ANNOTATIONS = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(DefaultAnnotationMetadata.class, "registerRepeatableAnnotations", Map.class)
   );
   private static final Method METHOD_GET_DEFAULT_VALUES = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(AnnotationMetadataSupport.class, "getDefaultValues", String.class)
   );
   private static final Method CONSTRUCTOR_ANNOTATION_METADATA = Method.getMethod(
      ReflectionUtils.getRequiredInternalConstructor(
         DefaultAnnotationMetadata.class, Map.class, Map.class, Map.class, Map.class, Map.class, Boolean.TYPE, Boolean.TYPE
      )
   );
   private static final Method CONSTRUCTOR_ANNOTATION_METADATA_HIERARCHY = Method.getMethod(
      ReflectionUtils.getRequiredInternalConstructor(AnnotationMetadataHierarchy.class, AnnotationMetadata[].class)
   );
   private static final Method CONSTRUCTOR_ANNOTATION_VALUE_AND_MAP = Method.getMethod(
      ReflectionUtils.getRequiredInternalConstructor(AnnotationValue.class, String.class, Map.class, Map.class)
   );
   private static final Method CONSTRUCTOR_CLASS_VALUE = Method.getMethod(
      ReflectionUtils.getRequiredInternalConstructor(AnnotationClassValue.class, String.class)
   );
   private static final Method CONSTRUCTOR_CLASS_VALUE_WITH_CLASS = Method.getMethod(
      ReflectionUtils.getRequiredInternalConstructor(AnnotationClassValue.class, Class.class)
   );
   private static final Method CONSTRUCTOR_CLASS_VALUE_WITH_INSTANCE = Method.getMethod(
      ReflectionUtils.getRequiredInternalConstructor(AnnotationClassValue.class, Object.class)
   );
   private static final Type ANNOTATION_UTIL_TYPE = Type.getType(AnnotationUtil.class);
   private static final Type LIST_TYPE = Type.getType(List.class);
   private static final String EMPTY_LIST = "EMPTY_LIST";
   private static final String LOAD_CLASS_PREFIX = "$micronaut_load_class_value_";
   private final String className;
   private final AnnotationMetadata annotationMetadata;
   private final boolean writeAnnotationDefaults;

   @Deprecated
   public AnnotationMetadataWriter(String className, ClassElement originatingElement, AnnotationMetadata annotationMetadata, boolean writeAnnotationDefaults) {
      super((Element)originatingElement);
      this.className = className + "$$AnnotationMetadata";
      if (annotationMetadata instanceof DefaultAnnotationMetadata) {
         this.annotationMetadata = annotationMetadata;
      } else {
         if (!(annotationMetadata instanceof AnnotationMetadataHierarchy)) {
            throw new ClassGenerationException("Compile time metadata required to generate class: " + className);
         }

         AnnotationMetadataHierarchy hierarchy = (AnnotationMetadataHierarchy)annotationMetadata;
         this.annotationMetadata = hierarchy.getDeclaredMetadata();
      }

      this.writeAnnotationDefaults = writeAnnotationDefaults;
   }

   @Deprecated
   public AnnotationMetadataWriter(String className, ClassElement originatingElement, AnnotationMetadata annotationMetadata) {
      this(className, originatingElement, annotationMetadata, false);
   }

   public String getClassName() {
      return this.className;
   }

   @Override
   public void accept(ClassWriterOutputVisitor outputVisitor) throws IOException {
      ClassWriter classWriter = this.generateClassBytes();
      if (classWriter != null) {
         OutputStream outputStream = outputVisitor.visitClass(this.className, this.getOriginatingElements());
         Throwable var4 = null;

         try {
            outputStream.write(classWriter.toByteArray());
         } catch (Throwable var13) {
            var4 = var13;
            throw var13;
         } finally {
            if (outputStream != null) {
               if (var4 != null) {
                  try {
                     outputStream.close();
                  } catch (Throwable var12) {
                     var4.addSuppressed(var12);
                  }
               } else {
                  outputStream.close();
               }
            }

         }
      }

   }

   public void writeTo(OutputStream outputStream) {
      try {
         ClassWriter classWriter = this.generateClassBytes();
         this.writeClassToDisk(outputStream, classWriter);
      } catch (Throwable var3) {
         throw new ClassGenerationException("Error generating annotation metadata: " + var3.getMessage(), var3);
      }
   }

   @Internal
   public static void instantiateNewMetadata(
      Type owningType,
      ClassWriter declaringClassWriter,
      GeneratorAdapter generatorAdapter,
      DefaultAnnotationMetadata annotationMetadata,
      Map<String, Integer> defaultsStorage,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      instantiateInternal(owningType, declaringClassWriter, generatorAdapter, annotationMetadata, true, defaultsStorage, loadTypeMethods);
   }

   @Internal
   public static void instantiateNewMetadataHierarchy(
      Type owningType,
      ClassWriter classWriter,
      GeneratorAdapter generatorAdapter,
      AnnotationMetadataHierarchy hierarchy,
      Map<String, Integer> defaultsStorage,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      if (hierarchy.isEmpty()) {
         generatorAdapter.getStatic(Type.getType(AnnotationMetadata.class), "EMPTY_METADATA", Type.getType(AnnotationMetadata.class));
      } else {
         List<AnnotationMetadata> notEmpty = (List)CollectionUtils.iterableToList(hierarchy).stream().filter(h -> !h.isEmpty()).collect(Collectors.toList());
         if (notEmpty.size() == 1) {
            pushNewAnnotationMetadataOrReference(
               owningType, classWriter, generatorAdapter, defaultsStorage, loadTypeMethods, (AnnotationMetadata)notEmpty.get(0)
            );
         } else {
            generatorAdapter.visitTypeInsn(187, TYPE_DEFAULT_ANNOTATION_METADATA_HIERARCHY.getInternalName());
            generatorAdapter.visitInsn(89);
            pushNewArray(generatorAdapter, AnnotationMetadata.class, 2);
            pushStoreInArray(generatorAdapter, 0, 2, () -> {
               AnnotationMetadata rootMetadata = hierarchy.getRootMetadata();
               pushNewAnnotationMetadataOrReference(owningType, classWriter, generatorAdapter, defaultsStorage, loadTypeMethods, rootMetadata);
            });
            pushStoreInArray(generatorAdapter, 1, 2, () -> {
               AnnotationMetadata declaredMetadata = hierarchy.getDeclaredMetadata();
               pushNewAnnotationMetadataOrReference(owningType, classWriter, generatorAdapter, defaultsStorage, loadTypeMethods, declaredMetadata);
            });
            generatorAdapter.invokeConstructor(TYPE_DEFAULT_ANNOTATION_METADATA_HIERARCHY, CONSTRUCTOR_ANNOTATION_METADATA_HIERARCHY);
         }
      }
   }

   @Internal
   public static void pushAnnotationMetadataReference(GeneratorAdapter generatorAdapter, AnnotationMetadataReference annotationMetadata) {
      String className = annotationMetadata.getClassName();
      Type type = getTypeReferenceForName(className, new String[0]);
      generatorAdapter.getStatic(type, "$ANNOTATION_METADATA", Type.getType(AnnotationMetadata.class));
   }

   @Internal
   private static void pushNewAnnotationMetadataOrReference(
      Type owningType,
      ClassWriter classWriter,
      GeneratorAdapter generatorAdapter,
      Map<String, Integer> defaultsStorage,
      Map<String, GeneratorAdapter> loadTypeMethods,
      AnnotationMetadata annotationMetadata
   ) {
      if (annotationMetadata instanceof DefaultAnnotationMetadata) {
         instantiateNewMetadata(owningType, classWriter, generatorAdapter, (DefaultAnnotationMetadata)annotationMetadata, defaultsStorage, loadTypeMethods);
      } else if (annotationMetadata instanceof AnnotationMetadataReference) {
         pushAnnotationMetadataReference(generatorAdapter, (AnnotationMetadataReference)annotationMetadata);
      } else {
         generatorAdapter.getStatic(Type.getType(AnnotationMetadata.class), "EMPTY_METADATA", Type.getType(AnnotationMetadata.class));
      }

   }

   @Internal
   public static void writeAnnotationDefaults(
      DefaultAnnotationMetadata annotationMetadata,
      ClassWriter classWriter,
      Type owningType,
      Map<String, Integer> defaultsStorage,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      Map<String, Map<CharSequence, Object>> annotationDefaultValues = annotationMetadata.annotationDefaultValues;
      if (CollectionUtils.isNotEmpty(annotationDefaultValues)) {
         MethodVisitor si = classWriter.visitMethod(8, "<clinit>", "()V", null, null);
         GeneratorAdapter staticInit = new GeneratorAdapter(si, 8, "<clinit>", "()V");
         writeAnnotationDefaults(owningType, classWriter, staticInit, annotationMetadata, defaultsStorage, loadTypeMethods);
         staticInit.visitInsn(177);
         staticInit.visitMaxs(1, 1);
         staticInit.visitEnd();
      }

   }

   @Internal
   public static void writeAnnotationDefaults(
      Type owningType,
      ClassWriter classWriter,
      GeneratorAdapter staticInit,
      DefaultAnnotationMetadata annotationMetadata,
      Map<String, Integer> defaultsStorage,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      Map<String, Map<CharSequence, Object>> annotationDefaultValues = annotationMetadata.annotationDefaultValues;
      if (CollectionUtils.isNotEmpty(annotationDefaultValues)) {
         for(Entry<String, Map<CharSequence, Object>> entry : annotationDefaultValues.entrySet()) {
            Map<CharSequence, Object> annotationValues = (Map)entry.getValue();
            boolean typeOnly = CollectionUtils.isEmpty(annotationValues);
            String annotationName = (String)entry.getKey();
            if (!typeOnly || !AnnotationMetadataSupport.getRegisteredAnnotationType(annotationName).isPresent()) {
               invokeLoadClassValueMethod(owningType, classWriter, staticInit, loadTypeMethods, new AnnotationClassValue(annotationName));
               if (!typeOnly) {
                  pushStringMapOf(
                     staticInit, annotationValues, true, null, v -> pushValue(owningType, classWriter, staticInit, v, defaultsStorage, loadTypeMethods, true)
                  );
                  staticInit.invokeStatic(TYPE_DEFAULT_ANNOTATION_METADATA, METHOD_REGISTER_ANNOTATION_DEFAULTS);
               } else {
                  staticInit.invokeStatic(TYPE_DEFAULT_ANNOTATION_METADATA, METHOD_REGISTER_ANNOTATION_TYPE);
               }
            }
         }

         if (annotationMetadata.repeated != null && !annotationMetadata.repeated.isEmpty()) {
            Map<String, String> repeated = new HashMap();

            for(Entry<String, String> e : annotationMetadata.repeated.entrySet()) {
               repeated.put(e.getValue(), e.getKey());
            }

            AnnotationMetadataSupport.removeCoreRepeatableAnnotations(repeated);
            if (!repeated.isEmpty()) {
               pushStringMapOf(staticInit, repeated, true, null, v -> pushValue(owningType, classWriter, staticInit, v, defaultsStorage, loadTypeMethods, true));
               staticInit.invokeStatic(TYPE_DEFAULT_ANNOTATION_METADATA, METHOD_REGISTER_REPEATABLE_ANNOTATIONS);
            }
         }
      }

   }

   private static void pushListOfString(GeneratorAdapter methodVisitor, List<String> names) {
      if (names != null) {
         names = (List)names.stream().filter(Objects::nonNull).collect(Collectors.toList());
      }

      if (names != null && !names.isEmpty()) {
         int totalSize = names.size();
         pushNewArray(methodVisitor, Object.class, totalSize);
         int i = 0;

         for(String name : names) {
            pushStoreStringInArray(methodVisitor, i++, totalSize, name);
         }

         methodVisitor.invokeStatic(ANNOTATION_UTIL_TYPE, METHOD_LIST_OF);
      } else {
         methodVisitor.getStatic(Type.getType(Collections.class), "EMPTY_LIST", LIST_TYPE);
      }
   }

   private static void instantiateInternal(
      Type owningType,
      ClassWriter declaringClassWriter,
      GeneratorAdapter generatorAdapter,
      DefaultAnnotationMetadata annotationMetadata,
      boolean isNew,
      Map<String, Integer> defaultsStorage,
      Map<String, GeneratorAdapter> loadTypeMethods
   ) {
      if (isNew) {
         generatorAdapter.visitTypeInsn(187, TYPE_DEFAULT_ANNOTATION_METADATA.getInternalName());
         generatorAdapter.visitInsn(89);
      } else {
         generatorAdapter.loadThis();
      }

      pushCreateAnnotationData(
         owningType,
         declaringClassWriter,
         generatorAdapter,
         annotationMetadata.declaredAnnotations,
         defaultsStorage,
         loadTypeMethods,
         annotationMetadata.getSourceRetentionAnnotations()
      );
      pushCreateAnnotationData(
         owningType,
         declaringClassWriter,
         generatorAdapter,
         annotationMetadata.declaredStereotypes,
         defaultsStorage,
         loadTypeMethods,
         annotationMetadata.getSourceRetentionAnnotations()
      );
      pushCreateAnnotationData(
         owningType,
         declaringClassWriter,
         generatorAdapter,
         annotationMetadata.allStereotypes,
         defaultsStorage,
         loadTypeMethods,
         annotationMetadata.getSourceRetentionAnnotations()
      );
      pushCreateAnnotationData(
         owningType,
         declaringClassWriter,
         generatorAdapter,
         annotationMetadata.allAnnotations,
         defaultsStorage,
         loadTypeMethods,
         annotationMetadata.getSourceRetentionAnnotations()
      );
      pushStringMapOf(
         generatorAdapter, annotationMetadata.annotationsByStereotype, false, Collections.emptyList(), list -> pushListOfString(generatorAdapter, list)
      );
      generatorAdapter.push(annotationMetadata.hasPropertyExpressions());
      generatorAdapter.push(true);
      generatorAdapter.invokeConstructor(TYPE_DEFAULT_ANNOTATION_METADATA, CONSTRUCTOR_ANNOTATION_METADATA);
   }

   private ClassWriter generateClassBytes() {
      ClassWriter classWriter = new ClassWriter(3);
      Type owningType = getTypeReferenceForName(this.className, new String[0]);
      this.startClass(classWriter, getInternalName(this.className), TYPE_DEFAULT_ANNOTATION_METADATA);
      GeneratorAdapter constructor = this.startConstructor(classWriter);
      DefaultAnnotationMetadata annotationMetadata = (DefaultAnnotationMetadata)this.annotationMetadata;
      Map<String, Integer> defaultsStorage = new HashMap(3);
      HashMap<String, GeneratorAdapter> loadTypeMethods = new HashMap(5);
      instantiateInternal(owningType, classWriter, constructor, annotationMetadata, false, defaultsStorage, loadTypeMethods);
      constructor.visitInsn(177);
      constructor.visitMaxs(1, 1);
      constructor.visitEnd();
      if (this.writeAnnotationDefaults) {
         writeAnnotationDefaults(annotationMetadata, classWriter, owningType, defaultsStorage, loadTypeMethods);
      }

      for(GeneratorAdapter adapter : loadTypeMethods.values()) {
         adapter.visitMaxs(3, 1);
         adapter.visitEnd();
      }

      classWriter.visitEnd();
      return classWriter;
   }

   private static void pushCreateAnnotationData(
      Type declaringType,
      ClassWriter declaringClassWriter,
      GeneratorAdapter methodVisitor,
      Map<String, Map<CharSequence, Object>> annotationData,
      Map<String, Integer> defaultsStorage,
      Map<String, GeneratorAdapter> loadTypeMethods,
      Set<String> sourceRetentionAnnotations
   ) {
      if (annotationData != null) {
         annotationData = new LinkedHashMap(annotationData);

         for(String sourceRetentionAnnotation : sourceRetentionAnnotations) {
            annotationData.remove(sourceRetentionAnnotation);
         }
      }

      pushStringMapOf(
         methodVisitor,
         annotationData,
         false,
         Collections.emptyMap(),
         attributes -> pushStringMapOf(
               methodVisitor,
               attributes,
               true,
               null,
               v -> pushValue(declaringType, declaringClassWriter, methodVisitor, v, defaultsStorage, loadTypeMethods, true)
            )
      );
   }

   private static void pushValue(
      Type declaringType,
      ClassVisitor declaringClassWriter,
      GeneratorAdapter methodVisitor,
      Object value,
      Map<String, Integer> defaultsStorage,
      Map<String, GeneratorAdapter> loadTypeMethods,
      boolean boxValue
   ) {
      if (value == null) {
         throw new IllegalStateException("Cannot map null value in: " + declaringType.getClassName());
      } else {
         if (value instanceof Boolean) {
            methodVisitor.push((Boolean)value);
            if (boxValue) {
               pushBoxPrimitiveIfNecessary(Boolean.TYPE, methodVisitor);
            }
         } else if (value instanceof String) {
            methodVisitor.push(value.toString());
         } else if (value instanceof AnnotationClassValue) {
            AnnotationClassValue acv = (AnnotationClassValue)value;
            if (acv.isInstantiated()) {
               methodVisitor.visitTypeInsn(187, TYPE_ANNOTATION_CLASS_VALUE.getInternalName());
               methodVisitor.visitInsn(89);
               methodVisitor.visitTypeInsn(187, getInternalName(acv.getName()));
               methodVisitor.visitInsn(89);
               methodVisitor.invokeConstructor(
                  getTypeReferenceForName(acv.getName(), new String[0]), new Method("<init>", getConstructorDescriptor(new Class[0]))
               );
               methodVisitor.invokeConstructor(TYPE_ANNOTATION_CLASS_VALUE, CONSTRUCTOR_CLASS_VALUE_WITH_INSTANCE);
            } else {
               invokeLoadClassValueMethod(declaringType, declaringClassWriter, methodVisitor, loadTypeMethods, acv);
            }
         } else if (value instanceof Enum) {
            Enum enumObject = (Enum)value;
            Class declaringClass = enumObject.getDeclaringClass();
            Type t = Type.getType(declaringClass);
            methodVisitor.getStatic(t, enumObject.name(), t);
         } else if (value.getClass().isArray()) {
            Class<?> jt = ReflectionUtils.getPrimitiveType(value.getClass().getComponentType());
            Type componentType = Type.getType(jt);
            int len = Array.getLength(value);
            if (Object.class == jt && len == 0) {
               pushEmptyObjectsArray(methodVisitor);
            } else {
               pushNewArray(methodVisitor, jt, len);

               for(int i = 0; i < len; ++i) {
                  Object v = Array.get(value, i);
                  pushStoreInArray(
                     methodVisitor,
                     componentType,
                     i,
                     len,
                     () -> pushValue(declaringType, declaringClassWriter, methodVisitor, v, defaultsStorage, loadTypeMethods, !jt.isPrimitive())
                  );
               }
            }
         } else if (value instanceof Collection) {
            if (((Collection)value).isEmpty()) {
               pushEmptyObjectsArray(methodVisitor);
            } else {
               List array = Arrays.asList(((Collection)value).toArray());
               int len = array.size();
               boolean first = true;
               Class<?> arrayType = Object.class;

               for(int i = 0; i < len; ++i) {
                  Object v = array.get(i);
                  if (first) {
                     arrayType = v == null ? Object.class : v.getClass();
                     pushNewArray(methodVisitor, arrayType, len);
                     first = false;
                  }

                  Class<?> finalArrayType = arrayType;
                  pushStoreInArray(
                     methodVisitor,
                     Type.getType(arrayType),
                     i,
                     len,
                     () -> pushValue(declaringType, declaringClassWriter, methodVisitor, v, defaultsStorage, loadTypeMethods, !finalArrayType.isPrimitive())
                  );
               }
            }
         } else if (value instanceof Long) {
            methodVisitor.push((Long)value);
            if (boxValue) {
               pushBoxPrimitiveIfNecessary(Long.TYPE, methodVisitor);
            }
         } else if (value instanceof Double) {
            methodVisitor.push((Double)value);
            if (boxValue) {
               pushBoxPrimitiveIfNecessary(Double.TYPE, methodVisitor);
            }
         } else if (value instanceof Float) {
            methodVisitor.push((Float)value);
            if (boxValue) {
               pushBoxPrimitiveIfNecessary(Float.TYPE, methodVisitor);
            }
         } else if (value instanceof Byte) {
            methodVisitor.push((Byte)value);
            if (boxValue) {
               pushBoxPrimitiveIfNecessary(Byte.TYPE, methodVisitor);
            }
         } else if (value instanceof Short) {
            methodVisitor.push((Short)value);
            if (boxValue) {
               pushBoxPrimitiveIfNecessary(Short.TYPE, methodVisitor);
            }
         } else if (value instanceof Character) {
            methodVisitor.push((Character)value);
            if (boxValue) {
               pushBoxPrimitiveIfNecessary(Character.TYPE, methodVisitor);
            }
         } else if (value instanceof Number) {
            methodVisitor.push(((Number)value).intValue());
            if (boxValue) {
               pushBoxPrimitiveIfNecessary(ReflectionUtils.getPrimitiveType(value.getClass()), methodVisitor);
            }
         } else if (value instanceof AnnotationValue) {
            AnnotationValue data = (AnnotationValue)value;
            String annotationName = data.getAnnotationName();
            Map<CharSequence, Object> values = data.getValues();
            Type annotationValueType = Type.getType(AnnotationValue.class);
            methodVisitor.newInstance(annotationValueType);
            methodVisitor.dup();
            methodVisitor.push(annotationName);
            pushStringMapOf(
               methodVisitor, values, true, null, v -> pushValue(declaringType, declaringClassWriter, methodVisitor, v, defaultsStorage, loadTypeMethods, true)
            );
            Integer defaultIndex = (Integer)defaultsStorage.get(annotationName);
            if (defaultIndex == null) {
               methodVisitor.push(annotationName);
               methodVisitor.invokeStatic(Type.getType(AnnotationMetadataSupport.class), METHOD_GET_DEFAULT_VALUES);
               methodVisitor.dup();
               int localIndex = methodVisitor.newLocal(Type.getType(Map.class));
               methodVisitor.storeLocal(localIndex);
               defaultsStorage.put(annotationName, localIndex);
            } else {
               methodVisitor.loadLocal(defaultIndex);
            }

            methodVisitor.invokeConstructor(annotationValueType, CONSTRUCTOR_ANNOTATION_VALUE_AND_MAP);
         } else {
            methodVisitor.visitInsn(1);
         }

      }
   }

   private static void pushEmptyObjectsArray(GeneratorAdapter methodVisitor) {
      methodVisitor.getStatic(Type.getType(ArrayUtils.class), "EMPTY_OBJECT_ARRAY", Type.getType(Object[].class));
   }

   private static void invokeLoadClassValueMethod(
      Type declaringType,
      ClassVisitor declaringClassWriter,
      GeneratorAdapter methodVisitor,
      Map<String, GeneratorAdapter> loadTypeMethods,
      AnnotationClassValue acv
   ) {
      String typeName = acv.getName();
      String desc = getMethodDescriptor(AnnotationClassValue.class, Collections.emptyList());
      GeneratorAdapter loadTypeGeneratorMethod = (GeneratorAdapter)loadTypeMethods.computeIfAbsent(
         typeName,
         type -> {
            String methodName = "$micronaut_load_class_value_" + loadTypeMethods.size();
            GeneratorAdapter loadTypeGenerator = new GeneratorAdapter(
               declaringClassWriter.visitMethod(4104, methodName, desc, null, null), 4104, methodName, desc
            );
            loadTypeGenerator.visitCode();
            Label tryStart = new Label();
            Label tryEnd = new Label();
            Label exceptionHandler = new Label();
            loadTypeGenerator.visitTryCatchBlock(tryStart, tryEnd, exceptionHandler, Type.getInternalName(Throwable.class));
            loadTypeGenerator.visitLabel(tryStart);
            loadTypeGenerator.visitTypeInsn(187, TYPE_ANNOTATION_CLASS_VALUE.getInternalName());
            loadTypeGenerator.visitInsn(89);
            loadTypeGenerator.push(getTypeReferenceForName(typeName, new String[0]));
            loadTypeGenerator.invokeConstructor(TYPE_ANNOTATION_CLASS_VALUE, CONSTRUCTOR_CLASS_VALUE_WITH_CLASS);
            loadTypeGenerator.visitLabel(tryEnd);
            loadTypeGenerator.returnValue();
            loadTypeGenerator.visitLabel(exceptionHandler);
            loadTypeGenerator.visitFrame(-1, 0, new Object[0], 1, new Object[]{"java/lang/Throwable"});
            loadTypeGenerator.visitVarInsn(58, 0);
            loadTypeGenerator.visitTypeInsn(187, TYPE_ANNOTATION_CLASS_VALUE.getInternalName());
            loadTypeGenerator.visitInsn(89);
            loadTypeGenerator.push(typeName);
            loadTypeGenerator.invokeConstructor(TYPE_ANNOTATION_CLASS_VALUE, CONSTRUCTOR_CLASS_VALUE);
            loadTypeGenerator.returnValue();
            return loadTypeGenerator;
         }
      );
      methodVisitor.visitMethodInsn(184, declaringType.getInternalName(), loadTypeGeneratorMethod.getName(), desc, false);
   }
}
