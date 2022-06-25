package io.micronaut.inject.beans.visitor;

import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.asm.commons.Method;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.AbstractBeanIntrospectionReference;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospectionReference;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataReference;
import io.micronaut.inject.annotation.AnnotationMetadataWriter;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ConstructorElement;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.ast.TypedElement;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import io.micronaut.inject.processing.JavaModelUtils;
import io.micronaut.inject.writer.AbstractAnnotationMetadataWriter;
import io.micronaut.inject.writer.ClassWriterOutputVisitor;
import io.micronaut.inject.writer.DispatchWriter;
import io.micronaut.inject.writer.StringSwitchWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

@Internal
final class BeanIntrospectionWriter extends AbstractAnnotationMetadataWriter {
   private static final String REFERENCE_SUFFIX = "$IntrospectionRef";
   private static final String INTROSPECTION_SUFFIX = "$Introspection";
   private static final String FIELD_CONSTRUCTOR_ANNOTATION_METADATA = "$FIELD_CONSTRUCTOR_ANNOTATION_METADATA";
   private static final String FIELD_CONSTRUCTOR_ARGUMENTS = "$CONSTRUCTOR_ARGUMENTS";
   private static final String FIELD_BEAN_PROPERTIES_REFERENCES = "$PROPERTIES_REFERENCES";
   private static final String FIELD_BEAN_METHODS_REFERENCES = "$METHODS_REFERENCES";
   private static final Method PROPERTY_INDEX_OF = Method.getMethod(
      (java.lang.reflect.Method)ReflectionUtils.findMethod(BeanIntrospection.class, "propertyIndexOf", String.class).get()
   );
   private static final Method FIND_PROPERTY_BY_INDEX_METHOD = Method.getMethod(
      (java.lang.reflect.Method)ReflectionUtils.findMethod(AbstractInitializableBeanIntrospection.class, "getPropertyByIndex", Integer.TYPE).get()
   );
   private static final Method FIND_INDEXED_PROPERTY_METHOD = Method.getMethod(
      (java.lang.reflect.Method)ReflectionUtils.findMethod(AbstractInitializableBeanIntrospection.class, "findIndexedProperty", Class.class, String.class)
         .get()
   );
   private static final Method GET_INDEXED_PROPERTIES = Method.getMethod(
      (java.lang.reflect.Method)ReflectionUtils.findMethod(AbstractInitializableBeanIntrospection.class, "getIndexedProperties", Class.class).get()
   );
   private static final Method GET_BP_INDEXED_SUBSET_METHOD = Method.getMethod(
      (java.lang.reflect.Method)ReflectionUtils.findMethod(AbstractInitializableBeanIntrospection.class, "getBeanPropertiesIndexedSubset", int[].class).get()
   );
   private static final Method COLLECTIONS_EMPTY_LIST = Method.getMethod(
      (java.lang.reflect.Method)ReflectionUtils.findMethod(Collections.class, "emptyList").get()
   );
   private final ClassWriter referenceWriter;
   private final String introspectionName;
   private final Type introspectionType;
   private final Type beanType;
   private final Map<BeanIntrospectionWriter.AnnotationWithValue, String> indexByAnnotationAndValue = new HashMap(2);
   private final Map<String, Set<String>> indexByAnnotations = new HashMap(2);
   private final Map<String, String> annotationIndexFields = new HashMap(2);
   private final ClassElement classElement;
   private boolean executed = false;
   private MethodElement constructor;
   private MethodElement defaultConstructor;
   private final List<BeanIntrospectionWriter.BeanPropertyData> beanProperties = new ArrayList();
   private final List<BeanIntrospectionWriter.BeanFieldData> beanFields = new ArrayList();
   private final List<BeanIntrospectionWriter.BeanMethodData> beanMethods = new ArrayList();
   private final DispatchWriter dispatchWriter;

   BeanIntrospectionWriter(ClassElement classElement, AnnotationMetadata beanAnnotationMetadata) {
      super(computeReferenceName(classElement.getName()), classElement, beanAnnotationMetadata, true);
      String name = classElement.getName();
      this.classElement = classElement;
      this.referenceWriter = new ClassWriter(1);
      this.introspectionName = computeIntrospectionName(name);
      this.introspectionType = getTypeReferenceForName(this.introspectionName, new String[0]);
      this.beanType = getTypeReferenceForName(name, new String[0]);
      this.dispatchWriter = new DispatchWriter(this.introspectionType);
   }

   BeanIntrospectionWriter(
      String generatingType, int index, ClassElement originatingElement, ClassElement classElement, AnnotationMetadata beanAnnotationMetadata
   ) {
      super(computeReferenceName(generatingType) + index, originatingElement, beanAnnotationMetadata, true);
      String className = classElement.getName();
      this.classElement = classElement;
      this.referenceWriter = new ClassWriter(1);
      this.introspectionName = computeIntrospectionName(generatingType, className);
      this.introspectionType = getTypeReferenceForName(this.introspectionName, new String[0]);
      this.beanType = getTypeReferenceForName(className, new String[0]);
      this.dispatchWriter = new DispatchWriter(this.introspectionType);
   }

   @Nullable
   public MethodElement getConstructor() {
      return this.constructor;
   }

   public Type getBeanType() {
      return this.beanType;
   }

   void visitProperty(
      @NonNull TypedElement type,
      @NonNull TypedElement genericType,
      @NonNull String name,
      @Nullable MethodElement readMethod,
      @Nullable MethodElement writeMethod,
      boolean isReadOnly,
      @Nullable AnnotationMetadata annotationMetadata,
      @Nullable Map<String, ClassElement> typeArguments
   ) {
      DefaultAnnotationMetadata.contributeDefaults(this.annotationMetadata, annotationMetadata);
      if (typeArguments != null) {
         for(ClassElement element : typeArguments.values()) {
            DefaultAnnotationMetadata.contributeRepeatable(this.annotationMetadata, element);
         }
      }

      int readMethodIndex = -1;
      if (readMethod != null) {
         readMethodIndex = this.dispatchWriter.addMethod(this.classElement, readMethod, true);
      }

      int writeMethodIndex = -1;
      int withMethodIndex = -1;
      if (writeMethod != null) {
         writeMethodIndex = this.dispatchWriter.addMethod(this.classElement, writeMethod, true);
      }

      boolean isMutable = !isReadOnly || this.hasAssociatedConstructorArgument(name, genericType);
      if (isMutable) {
         if (writeMethod == null) {
            String prefix = (String)this.annotationMetadata.stringValue(Introspected.class, "withPrefix").orElse("with");
            ElementQuery<MethodElement> elementQuery = ElementQuery.<MethodElement>of(MethodElement.class)
               .onlyAccessible()
               .onlyDeclared()
               .onlyInstance()
               .named((Predicate<String>)(n -> n.startsWith(prefix) && n.equals(prefix + NameUtils.capitalize(name))))
               .filter(
                  methodElement -> {
                     ParameterElement[] parameters = methodElement.getParameters();
                     return parameters.length == 1
                        && methodElement.getGenericReturnType().getName().equals(this.classElement.getName())
                        && type.getType().isAssignable(parameters[0].getType());
                  }
               );
            MethodElement withMethod = (MethodElement)this.classElement.getEnclosedElement(elementQuery).orElse(null);
            if (withMethod != null) {
               withMethodIndex = this.dispatchWriter.addMethod(this.classElement, withMethod, true);
            } else {
               MethodElement constructor = this.constructor == null ? this.defaultConstructor : this.constructor;
               if (constructor != null) {
                  withMethodIndex = this.dispatchWriter.addDispatchTarget(new BeanIntrospectionWriter.CopyConstructorDispatchTarget(constructor, name));
               }
            }
         }
      } else {
         withMethodIndex = this.dispatchWriter
            .addDispatchTarget(
               new BeanIntrospectionWriter.ExceptionDispatchTarget(
                  UnsupportedOperationException.class,
                  "Cannot mutate property ["
                     + name
                     + "] that is not mutable via a setter method or constructor argument for type: "
                     + this.beanType.getClassName()
               )
            );
      }

      this.beanProperties
         .add(
            new BeanIntrospectionWriter.BeanPropertyData(
               genericType, name, annotationMetadata, typeArguments, readMethodIndex, writeMethodIndex, withMethodIndex, isReadOnly
            )
         );
   }

   public void visitBeanMethod(MethodElement element) {
      if (element != null && !element.isPrivate()) {
         int dispatchIndex = this.dispatchWriter.addMethod(this.classElement, element);
         this.beanMethods.add(new BeanIntrospectionWriter.BeanMethodData(element, dispatchIndex));
      }

   }

   public void visitBeanField(FieldElement beanField) {
      int getDispatchIndex = this.dispatchWriter.addGetField(beanField);
      int setDispatchIndex = this.dispatchWriter.addSetField(beanField);
      this.beanFields.add(new BeanIntrospectionWriter.BeanFieldData(beanField, getDispatchIndex, setDispatchIndex));
   }

   void indexProperty(String annotationName, String property, @Nullable String value) {
      this.indexByAnnotationAndValue.put(new BeanIntrospectionWriter.AnnotationWithValue(annotationName, value), property);
      ((Set)this.indexByAnnotations.computeIfAbsent(annotationName, a -> new LinkedHashSet())).add(property);
   }

   @Override
   public void accept(ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {
      if (!this.executed) {
         this.executed = true;
         this.writeIntrospectionReference(classWriterOutputVisitor);
         this.loadTypeMethods.clear();
         this.writeIntrospectionClass(classWriterOutputVisitor);
      }

   }

   private void buildStaticInit(ClassWriter classWriter) {
      GeneratorAdapter staticInit = this.visitStaticInitializer(classWriter);
      if (this.constructor != null) {
         if (!this.constructor.getAnnotationMetadata().isEmpty()) {
            Type am = Type.getType(AnnotationMetadata.class);
            classWriter.visitField(26, "$FIELD_CONSTRUCTOR_ANNOTATION_METADATA", am.getDescriptor(), null, null);
            this.pushAnnotationMetadata(classWriter, staticInit, this.constructor.getAnnotationMetadata());
            staticInit.putStatic(this.introspectionType, "$FIELD_CONSTRUCTOR_ANNOTATION_METADATA", am);
         }

         if (ArrayUtils.isNotEmpty(this.constructor.getParameters())) {
            Type args = Type.getType(Argument[].class);
            classWriter.visitField(26, "$CONSTRUCTOR_ARGUMENTS", args.getDescriptor(), null, null);
            pushBuildArgumentsForMethod(
               this.introspectionType.getClassName(),
               this.introspectionType,
               classWriter,
               staticInit,
               Arrays.asList(this.constructor.getParameters()),
               this.defaults,
               this.loadTypeMethods
            );
            staticInit.putStatic(this.introspectionType, "$CONSTRUCTOR_ARGUMENTS", args);
         }
      }

      if (!this.beanProperties.isEmpty() || !this.beanFields.isEmpty()) {
         Type beanPropertiesRefs = Type.getType(AbstractInitializableBeanIntrospection.BeanPropertyRef[].class);
         classWriter.visitField(26, "$PROPERTIES_REFERENCES", beanPropertiesRefs.getDescriptor(), null, null);
         int size = this.beanProperties.size() + this.beanFields.size();
         pushNewArray(staticInit, AbstractInitializableBeanIntrospection.BeanPropertyRef.class, size);
         int i = 0;

         for(BeanIntrospectionWriter.BeanPropertyData beanPropertyData : this.beanProperties) {
            pushStoreInArray(staticInit, i++, size, () -> this.pushBeanPropertyReference(classWriter, staticInit, beanPropertyData));
         }

         for(BeanIntrospectionWriter.BeanFieldData beanFieldData : this.beanFields) {
            pushStoreInArray(staticInit, i++, size, () -> this.pushBeanPropertyReference(classWriter, staticInit, beanFieldData));
         }

         staticInit.putStatic(this.introspectionType, "$PROPERTIES_REFERENCES", beanPropertiesRefs);
      }

      if (!this.beanMethods.isEmpty()) {
         Type beanMethodsRefs = Type.getType(AbstractInitializableBeanIntrospection.BeanMethodRef[].class);
         classWriter.visitField(26, "$METHODS_REFERENCES", beanMethodsRefs.getDescriptor(), null, null);
         pushNewArray(staticInit, AbstractInitializableBeanIntrospection.BeanMethodRef.class, this.beanMethods.size());
         int i = 0;

         for(BeanIntrospectionWriter.BeanMethodData beanMethodData : this.beanMethods) {
            pushStoreInArray(staticInit, i++, this.beanMethods.size(), () -> this.pushBeanMethodReference(classWriter, staticInit, beanMethodData));
         }

         staticInit.putStatic(this.introspectionType, "$METHODS_REFERENCES", beanMethodsRefs);
      }

      int indexesIndex = 0;

      for(String annotationName : this.indexByAnnotations.keySet()) {
         int[] indexes = ((Set)this.indexByAnnotations.get(annotationName)).stream().mapToInt(prop -> this.getPropertyIndex(prop)).toArray();
         String newIndexField = "INDEX_" + ++indexesIndex;
         Type type = Type.getType(int[].class);
         classWriter.visitField(26, newIndexField, type.getDescriptor(), null, null);
         pushNewArray(staticInit, Integer.TYPE, indexes.length);
         int i = 0;

         for(int index : indexes) {
            pushStoreInArray(staticInit, Type.INT_TYPE, i++, indexes.length, () -> staticInit.push(index));
         }

         staticInit.putStatic(this.introspectionType, newIndexField, type);
         this.annotationIndexFields.put(annotationName, newIndexField);
      }

      staticInit.returnValue();
      staticInit.visitMaxs(13, 1);
      staticInit.visitEnd();
   }

   private void pushBeanPropertyReference(ClassWriter classWriter, GeneratorAdapter staticInit, BeanIntrospectionWriter.BeanPropertyData beanPropertyData) {
      staticInit.newInstance(Type.getType(AbstractInitializableBeanIntrospection.BeanPropertyRef.class));
      staticInit.dup();
      pushCreateArgument(
         this.beanType.getClassName(),
         this.introspectionType,
         classWriter,
         staticInit,
         beanPropertyData.name,
         beanPropertyData.typedElement,
         beanPropertyData.annotationMetadata,
         beanPropertyData.typeArguments,
         this.defaults,
         this.loadTypeMethods
      );
      staticInit.push(beanPropertyData.getMethodDispatchIndex);
      staticInit.push(beanPropertyData.setMethodDispatchIndex);
      staticInit.push(beanPropertyData.withMethodDispatchIndex);
      staticInit.push(beanPropertyData.isReadOnly);
      staticInit.push(!beanPropertyData.isReadOnly || this.hasAssociatedConstructorArgument(beanPropertyData.name, beanPropertyData.typedElement));
      this.invokeConstructor(
         staticInit,
         AbstractInitializableBeanIntrospection.BeanPropertyRef.class,
         new Class[]{Argument.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE, Boolean.TYPE}
      );
   }

   private void pushBeanPropertyReference(ClassWriter classWriter, GeneratorAdapter staticInit, BeanIntrospectionWriter.BeanFieldData beanFieldData) {
      staticInit.newInstance(Type.getType(AbstractInitializableBeanIntrospection.BeanPropertyRef.class));
      staticInit.dup();
      pushCreateArgument(
         this.beanType.getClassName(),
         this.introspectionType,
         classWriter,
         staticInit,
         beanFieldData.beanField.getName(),
         beanFieldData.beanField.getGenericType(),
         beanFieldData.beanField.getAnnotationMetadata(),
         beanFieldData.beanField.getGenericType().getTypeArguments(),
         this.defaults,
         this.loadTypeMethods
      );
      staticInit.push(beanFieldData.getDispatchIndex);
      staticInit.push(beanFieldData.setDispatchIndex);
      staticInit.push(-1);
      staticInit.push(beanFieldData.beanField.isFinal());
      staticInit.push(!beanFieldData.beanField.isFinal() || this.hasAssociatedConstructorArgument(beanFieldData.beanField.getName(), beanFieldData.beanField));
      this.invokeConstructor(
         staticInit,
         AbstractInitializableBeanIntrospection.BeanPropertyRef.class,
         new Class[]{Argument.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE, Boolean.TYPE}
      );
   }

   private void pushBeanMethodReference(ClassWriter classWriter, GeneratorAdapter staticInit, BeanIntrospectionWriter.BeanMethodData beanMethodData) {
      staticInit.newInstance(Type.getType(AbstractInitializableBeanIntrospection.BeanMethodRef.class));
      staticInit.dup();
      ClassElement genericReturnType = beanMethodData.methodElement.getGenericReturnType();
      this.pushReturnTypeArgument(
         this.introspectionType, classWriter, staticInit, this.classElement.getName(), genericReturnType, this.defaults, this.loadTypeMethods
      );
      staticInit.push(beanMethodData.methodElement.getName());
      this.pushAnnotationMetadata(classWriter, staticInit, beanMethodData.methodElement.getAnnotationMetadata());
      if (beanMethodData.methodElement.getParameters().length == 0) {
         staticInit.push((String)null);
      } else {
         pushBuildArgumentsForMethod(
            this.beanType.getClassName(),
            this.introspectionType,
            classWriter,
            staticInit,
            Arrays.asList(beanMethodData.methodElement.getParameters()),
            new HashMap(),
            this.loadTypeMethods
         );
      }

      staticInit.push(beanMethodData.dispatchIndex);
      this.invokeConstructor(
         staticInit,
         AbstractInitializableBeanIntrospection.BeanMethodRef.class,
         new Class[]{Argument.class, String.class, AnnotationMetadata.class, Argument[].class, Integer.TYPE}
      );
   }

   private boolean hasAssociatedConstructorArgument(String name, TypedElement typedElement) {
      if (this.constructor != null) {
         ParameterElement[] parameters = this.constructor.getParameters();

         for(ParameterElement parameter : parameters) {
            if (name.equals(parameter.getName())) {
               return typedElement.getType().isAssignable(parameter.getGenericType());
            }
         }
      }

      return false;
   }

   private void writeIntrospectionClass(ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {
      Type superType = Type.getType(AbstractInitializableBeanIntrospection.class);
      ClassWriter classWriter = new ClassWriter(3);
      classWriter.visit(52, 4112, this.introspectionType.getInternalName(), null, superType.getInternalName(), null);
      classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
      this.buildStaticInit(classWriter);
      GeneratorAdapter constructorWriter = this.startConstructor(classWriter);
      constructorWriter.loadThis();
      constructorWriter.push(this.beanType);
      if (this.annotationMetadata != null && this.annotationMetadata != AnnotationMetadata.EMPTY_METADATA) {
         constructorWriter.getStatic(this.targetClassType, "$ANNOTATION_METADATA", Type.getType(AnnotationMetadata.class));
      } else {
         constructorWriter.visitInsn(1);
      }

      if (this.constructor != null) {
         if (!this.constructor.getAnnotationMetadata().isEmpty()) {
            constructorWriter.getStatic(this.introspectionType, "$FIELD_CONSTRUCTOR_ANNOTATION_METADATA", Type.getType(AnnotationMetadata.class));
         } else {
            constructorWriter.push((String)null);
         }

         if (ArrayUtils.isNotEmpty(this.constructor.getParameters())) {
            constructorWriter.getStatic(this.introspectionType, "$CONSTRUCTOR_ARGUMENTS", Type.getType(Argument[].class));
         } else {
            constructorWriter.push((String)null);
         }
      } else {
         constructorWriter.push((String)null);
         constructorWriter.push((String)null);
      }

      if (this.beanProperties.isEmpty() && this.beanFields.isEmpty()) {
         constructorWriter.push((String)null);
      } else {
         constructorWriter.getStatic(
            this.introspectionType, "$PROPERTIES_REFERENCES", Type.getType(AbstractInitializableBeanIntrospection.BeanPropertyRef[].class)
         );
      }

      if (this.beanMethods.isEmpty()) {
         constructorWriter.push((String)null);
      } else {
         constructorWriter.getStatic(this.introspectionType, "$METHODS_REFERENCES", Type.getType(AbstractInitializableBeanIntrospection.BeanMethodRef[].class));
      }

      this.invokeConstructor(
         constructorWriter,
         AbstractInitializableBeanIntrospection.class,
         new Class[]{
            Class.class,
            AnnotationMetadata.class,
            AnnotationMetadata.class,
            Argument[].class,
            AbstractInitializableBeanIntrospection.BeanPropertyRef[].class,
            AbstractInitializableBeanIntrospection.BeanMethodRef[].class
         }
      );
      constructorWriter.returnValue();
      constructorWriter.visitMaxs(2, 1);
      constructorWriter.visitEnd();
      this.dispatchWriter.buildDispatchOneMethod(classWriter);
      this.dispatchWriter.buildDispatchMethod(classWriter);
      this.buildPropertyIndexOfMethod(classWriter);
      this.buildFindIndexedProperty(classWriter);
      this.buildGetIndexedProperties(classWriter);
      if (this.defaultConstructor != null) {
         this.writeInstantiateMethod(classWriter, this.defaultConstructor, "instantiate");
      }

      if (this.constructor != null && ArrayUtils.isNotEmpty(this.constructor.getParameters())) {
         this.writeInstantiateMethod(classWriter, this.constructor, "instantiateInternal", Object[].class);
      }

      for(GeneratorAdapter method : this.loadTypeMethods.values()) {
         method.visitMaxs(3, 1);
         method.visitEnd();
      }

      classWriter.visitEnd();
      OutputStream outputStream = classWriterOutputVisitor.visitClass(this.introspectionName, this.getOriginatingElements());
      Throwable var18 = null;

      try {
         outputStream.write(classWriter.toByteArray());
      } catch (Throwable var15) {
         var18 = var15;
         throw var15;
      } finally {
         if (outputStream != null) {
            if (var18 != null) {
               try {
                  outputStream.close();
               } catch (Throwable var14) {
                  var18.addSuppressed(var14);
               }
            } else {
               outputStream.close();
            }
         }

      }

   }

   private void buildPropertyIndexOfMethod(ClassWriter classWriter) {
      final GeneratorAdapter findMethod = new GeneratorAdapter(
         classWriter.visitMethod(17, PROPERTY_INDEX_OF.getName(), PROPERTY_INDEX_OF.getDescriptor(), null, null),
         17,
         PROPERTY_INDEX_OF.getName(),
         PROPERTY_INDEX_OF.getDescriptor()
      );
      (new StringSwitchWriter() {
         @Override
         protected Set<String> getKeys() {
            Set<String> keys = new HashSet();

            for(BeanIntrospectionWriter.BeanPropertyData prop : BeanIntrospectionWriter.this.beanProperties) {
               keys.add(prop.name);
            }

            for(BeanIntrospectionWriter.BeanFieldData field : BeanIntrospectionWriter.this.beanFields) {
               keys.add(field.beanField.getName());
            }

            return keys;
         }

         @Override
         protected void pushStringValue() {
            findMethod.loadArg(0);
         }

         @Override
         protected void onMatch(String value, Label end) {
            findMethod.loadThis();
            findMethod.push(BeanIntrospectionWriter.this.getPropertyIndex(value));
            findMethod.returnValue();
         }
      }).write(findMethod);
      findMethod.push(-1);
      findMethod.returnValue();
      findMethod.visitMaxs(13, 1);
      findMethod.visitEnd();
   }

   private void buildFindIndexedProperty(ClassWriter classWriter) {
      if (!this.indexByAnnotationAndValue.isEmpty()) {
         final GeneratorAdapter writer = new GeneratorAdapter(
            classWriter.visitMethod(20, FIND_INDEXED_PROPERTY_METHOD.getName(), FIND_INDEXED_PROPERTY_METHOD.getDescriptor(), null, null),
            20,
            FIND_INDEXED_PROPERTY_METHOD.getName(),
            FIND_INDEXED_PROPERTY_METHOD.getDescriptor()
         );
         writer.loadThis();
         writer.loadArg(0);
         writer.invokeVirtual(Type.getType(Class.class), new Method("getName", Type.getType(String.class), new Type[0]));
         final int classNameLocal = writer.newLocal(Type.getType(String.class));
         writer.storeLocal(classNameLocal);
         writer.loadLocal(classNameLocal);
         (new StringSwitchWriter() {
               @Override
               protected Set<String> getKeys() {
                  return (Set<String>)BeanIntrospectionWriter.this.indexByAnnotationAndValue
                     .keySet()
                     .stream()
                     .map(s -> s.annotationName)
                     .collect(Collectors.toSet());
               }
   
               @Override
               protected void pushStringValue() {
                  writer.loadLocal(classNameLocal);
               }
   
               @Override
               protected void onMatch(String annotationName, Label end) {
                  if (BeanIntrospectionWriter.this.indexByAnnotationAndValue
                     .keySet()
                     .stream()
                     .anyMatch(s -> s.annotationName.equals(annotationName) && s.value == null)) {
                     Label falseLabel = new Label();
                     writer.loadArg(1);
                     writer.ifNonNull(falseLabel);
                     String propertyName = (String)BeanIntrospectionWriter.this.indexByAnnotationAndValue
                        .get(new BeanIntrospectionWriter.AnnotationWithValue(annotationName, null));
                     int propertyIndex = BeanIntrospectionWriter.this.getPropertyIndex(propertyName);
                     writer.loadThis();
                     writer.push(propertyIndex);
                     writer.invokeVirtual(BeanIntrospectionWriter.this.introspectionType, BeanIntrospectionWriter.FIND_PROPERTY_BY_INDEX_METHOD);
                     writer.returnValue();
                     writer.visitLabel(falseLabel);
                  } else {
                     Label falseLabel = new Label();
                     writer.loadArg(1);
                     writer.ifNonNull(falseLabel);
                     writer.goTo(end);
                     writer.visitLabel(falseLabel);
                  }
   
                  final Set<String> valueMatches = (Set)BeanIntrospectionWriter.this.indexByAnnotationAndValue
                     .keySet()
                     .stream()
                     .filter(s -> s.annotationName.equals(annotationName) && s.value != null)
                     .map(s -> s.value)
                     .collect(Collectors.toSet());
                  if (!valueMatches.isEmpty()) {
                     (new StringSwitchWriter() {
                           @Override
                           protected Set<String> getKeys() {
                              return valueMatches;
                           }
      
                           @Override
                           protected void pushStringValue() {
                              writer.loadArg(1);
                           }
      
                           @Override
                           protected void onMatch(String value, Label end) {
                              String propertyName = (String)BeanIntrospectionWriter.this.indexByAnnotationAndValue
                                 .get(new BeanIntrospectionWriter.AnnotationWithValue(annotationName, value));
                              int propertyIndex = BeanIntrospectionWriter.this.getPropertyIndex(propertyName);
                              writer.loadThis();
                              writer.push(propertyIndex);
                              writer.invokeVirtual(BeanIntrospectionWriter.this.introspectionType, BeanIntrospectionWriter.FIND_PROPERTY_BY_INDEX_METHOD);
                              writer.returnValue();
                           }
                        })
                        .write(writer);
                  }
   
                  writer.goTo(end);
               }
            })
            .write(writer);
         writer.push((String)null);
         writer.returnValue();
         writer.visitMaxs(13, 1);
         writer.visitEnd();
      }
   }

   private void buildGetIndexedProperties(ClassWriter classWriter) {
      if (!this.indexByAnnotations.isEmpty()) {
         final GeneratorAdapter writer = new GeneratorAdapter(
            classWriter.visitMethod(17, GET_INDEXED_PROPERTIES.getName(), GET_INDEXED_PROPERTIES.getDescriptor(), null, null),
            17,
            GET_INDEXED_PROPERTIES.getName(),
            GET_INDEXED_PROPERTIES.getDescriptor()
         );
         writer.loadThis();
         writer.loadArg(0);
         writer.invokeVirtual(Type.getType(Class.class), new Method("getName", Type.getType(String.class), new Type[0]));
         final int classNameLocal = writer.newLocal(Type.getType(String.class));
         writer.storeLocal(classNameLocal);
         writer.loadLocal(classNameLocal);
         (new StringSwitchWriter() {
               @Override
               protected Set<String> getKeys() {
                  return BeanIntrospectionWriter.this.indexByAnnotations.keySet();
               }
   
               @Override
               protected void pushStringValue() {
                  writer.loadLocal(classNameLocal);
               }
   
               @Override
               protected void onMatch(String annotationName, Label end) {
                  writer.loadThis();
                  writer.getStatic(
                     BeanIntrospectionWriter.this.introspectionType,
                     (String)BeanIntrospectionWriter.this.annotationIndexFields.get(annotationName),
                     Type.getType(int[].class)
                  );
                  writer.invokeVirtual(BeanIntrospectionWriter.this.introspectionType, BeanIntrospectionWriter.GET_BP_INDEXED_SUBSET_METHOD);
                  writer.returnValue();
               }
            })
            .write(writer);
         writer.invokeStatic(Type.getType(Collections.class), COLLECTIONS_EMPTY_LIST);
         writer.returnValue();
         writer.visitMaxs(13, 1);
         writer.visitEnd();
      }
   }

   private int getPropertyIndex(String propertyName) {
      BeanIntrospectionWriter.BeanPropertyData beanPropertyData = (BeanIntrospectionWriter.BeanPropertyData)this.beanProperties
         .stream()
         .filter(bp -> bp.name.equals(propertyName))
         .findFirst()
         .orElse(null);
      if (beanPropertyData != null) {
         return this.beanProperties.indexOf(beanPropertyData);
      } else {
         BeanIntrospectionWriter.BeanFieldData beanFieldData = (BeanIntrospectionWriter.BeanFieldData)this.beanFields
            .stream()
            .filter(f -> f.beanField.getName().equals(propertyName))
            .findFirst()
            .orElse(null);
         if (beanFieldData != null) {
            return this.beanProperties.size() + this.beanFields.indexOf(beanFieldData);
         } else {
            throw new IllegalStateException("Property not found: " + propertyName + " " + this.classElement.getName());
         }
      }
   }

   private void writeInstantiateMethod(ClassWriter classWriter, MethodElement constructor, String methodName, Class... args) {
      String desc = getMethodDescriptor(Object.class, Arrays.asList(args));
      GeneratorAdapter instantiateInternal = new GeneratorAdapter(classWriter.visitMethod(1, methodName, desc, null, null), 1, methodName, desc);
      this.invokeBeanConstructor(
         instantiateInternal,
         constructor,
         (writer, con) -> {
            List<ParameterElement> constructorArguments = Arrays.asList(con.getParameters());
            Collection<Type> argumentTypes = (Collection)constructorArguments.stream()
               .map(pe -> JavaModelUtils.getTypeReference(pe.getType()))
               .collect(Collectors.toList());
            int i = 0;
   
            for(Type argumentType : argumentTypes) {
               writer.loadArg(0);
               writer.push(i++);
               writer.arrayLoad(TYPE_OBJECT);
               pushCastToType(writer, argumentType);
            }
   
         }
      );
      instantiateInternal.returnValue();
      instantiateInternal.visitMaxs(3, 1);
      instantiateInternal.visitEnd();
   }

   private void invokeBeanConstructor(GeneratorAdapter writer, MethodElement constructor, BiConsumer<GeneratorAdapter, MethodElement> argumentsPusher) {
      boolean isConstructor = constructor instanceof ConstructorElement;
      boolean isCompanion = constructor != this.defaultConstructor && constructor.getDeclaringType().getSimpleName().endsWith("$Companion");
      List<ParameterElement> constructorArguments = Arrays.asList(constructor.getParameters());
      Collection<Type> argumentTypes = (Collection)constructorArguments.stream()
         .map(pe -> JavaModelUtils.getTypeReference(pe.getType()))
         .collect(Collectors.toList());
      if (isConstructor) {
         writer.newInstance(this.beanType);
         writer.dup();
      } else if (isCompanion) {
         writer.getStatic(this.beanType, "Companion", JavaModelUtils.getTypeReference(constructor.getDeclaringType()));
      }

      argumentsPusher.accept(writer, constructor);
      if (isConstructor) {
         String constructorDescriptor = getConstructorDescriptor(constructorArguments);
         writer.invokeConstructor(this.beanType, new Method("<init>", constructorDescriptor));
      } else if (constructor.isStatic()) {
         String methodDescriptor = getMethodDescriptor(this.beanType, argumentTypes);
         Method method = new Method(constructor.getName(), methodDescriptor);
         if (this.classElement.isInterface()) {
            writer.visitMethodInsn(184, this.beanType.getInternalName(), method.getName(), method.getDescriptor(), true);
         } else {
            writer.invokeStatic(this.beanType, method);
         }
      } else if (isCompanion) {
         writer.invokeVirtual(
            JavaModelUtils.getTypeReference(constructor.getDeclaringType()),
            new Method(constructor.getName(), getMethodDescriptor(this.beanType, argumentTypes))
         );
      }

   }

   private void writeIntrospectionReference(ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {
      Type superType = Type.getType(AbstractBeanIntrospectionReference.class);
      String referenceName = this.targetClassType.getClassName();
      classWriterOutputVisitor.visitServiceDescriptor(BeanIntrospectionReference.class, referenceName, this.getOriginatingElement());
      OutputStream referenceStream = classWriterOutputVisitor.visitClass(referenceName, this.getOriginatingElements());
      Throwable var5 = null;

      try {
         this.startService(this.referenceWriter, BeanIntrospectionReference.class, this.targetClassType.getInternalName(), superType);
         ClassWriter classWriter = this.generateClassBytes(this.referenceWriter);

         for(GeneratorAdapter generatorAdapter : this.loadTypeMethods.values()) {
            generatorAdapter.visitMaxs(1, 1);
            generatorAdapter.visitEnd();
         }

         referenceStream.write(classWriter.toByteArray());
      } catch (Throwable var16) {
         var5 = var16;
         throw var16;
      } finally {
         if (referenceStream != null) {
            if (var5 != null) {
               try {
                  referenceStream.close();
               } catch (Throwable var15) {
                  var5.addSuppressed(var15);
               }
            } else {
               referenceStream.close();
            }
         }

      }

   }

   private ClassWriter generateClassBytes(ClassWriter classWriter) {
      this.writeAnnotationMetadataStaticInitializer(classWriter, new HashMap());
      GeneratorAdapter cv = this.startConstructor(classWriter);
      cv.loadThis();
      this.invokeConstructor(cv, AbstractBeanIntrospectionReference.class, new Class[0]);
      cv.returnValue();
      cv.visitMaxs(2, 1);
      GeneratorAdapter loadMethod = this.startPublicMethodZeroArgs(classWriter, BeanIntrospection.class, "load");
      this.pushNewInstance(loadMethod, this.introspectionType);
      loadMethod.returnValue();
      loadMethod.visitMaxs(2, 1);
      loadMethod.endMethod();
      GeneratorAdapter nameMethod = this.startPublicMethodZeroArgs(classWriter, String.class, "getName");
      nameMethod.push(this.beanType.getClassName());
      nameMethod.returnValue();
      nameMethod.visitMaxs(1, 1);
      nameMethod.endMethod();
      GeneratorAdapter getBeanType = this.startPublicMethodZeroArgs(classWriter, Class.class, "getBeanType");
      getBeanType.push(this.beanType);
      getBeanType.returnValue();
      getBeanType.visitMaxs(2, 1);
      getBeanType.endMethod();
      this.writeGetAnnotationMetadataMethod(classWriter);
      return classWriter;
   }

   private void pushAnnotationMetadata(ClassWriter classWriter, GeneratorAdapter staticInit, AnnotationMetadata annotationMetadata) {
      if (annotationMetadata == AnnotationMetadata.EMPTY_METADATA || annotationMetadata.isEmpty()) {
         staticInit.push((String)null);
      } else if (annotationMetadata instanceof AnnotationMetadataReference) {
         AnnotationMetadataReference reference = (AnnotationMetadataReference)annotationMetadata;
         String className = reference.getClassName();
         staticInit.getStatic(getTypeReferenceForName(className, new String[0]), "$ANNOTATION_METADATA", Type.getType(AnnotationMetadata.class));
      } else if (annotationMetadata instanceof AnnotationMetadataHierarchy) {
         AnnotationMetadataWriter.instantiateNewMetadataHierarchy(
            this.introspectionType, classWriter, staticInit, (AnnotationMetadataHierarchy)annotationMetadata, this.defaults, this.loadTypeMethods
         );
      } else if (annotationMetadata instanceof DefaultAnnotationMetadata) {
         AnnotationMetadataWriter.instantiateNewMetadata(
            this.introspectionType, classWriter, staticInit, (DefaultAnnotationMetadata)annotationMetadata, this.defaults, this.loadTypeMethods
         );
      } else {
         staticInit.push((String)null);
      }

   }

   @NonNull
   private static String computeReferenceName(String className) {
      String packageName = NameUtils.getPackageName(className);
      String shortName = NameUtils.getSimpleName(className);
      return packageName + ".$" + shortName + "$IntrospectionRef";
   }

   @NonNull
   private static String computeIntrospectionName(String className) {
      String packageName = NameUtils.getPackageName(className);
      String shortName = NameUtils.getSimpleName(className);
      return packageName + ".$" + shortName + "$Introspection";
   }

   @NonNull
   private static String computeIntrospectionName(String generatingName, String className) {
      String packageName = NameUtils.getPackageName(generatingName);
      return packageName + ".$" + className.replace('.', '_') + "$Introspection";
   }

   void visitConstructor(MethodElement constructor) {
      this.constructor = constructor;
   }

   void visitDefaultConstructor(MethodElement constructor) {
      this.defaultConstructor = constructor;
   }

   private static final class AnnotationWithValue {
      @NonNull
      final String annotationName;
      @Nullable
      final String value;

      private AnnotationWithValue(@NonNull String annotationName, @Nullable String value) {
         this.annotationName = annotationName;
         this.value = value;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BeanIntrospectionWriter.AnnotationWithValue that = (BeanIntrospectionWriter.AnnotationWithValue)o;
            return this.annotationName.equals(that.annotationName) && Objects.equals(this.value, that.value);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.annotationName, this.value});
      }
   }

   private static final class BeanFieldData {
      @NotNull
      final FieldElement beanField;
      final int getDispatchIndex;
      final int setDispatchIndex;

      private BeanFieldData(FieldElement beanField, int getDispatchIndex, int setDispatchIndex) {
         this.beanField = beanField;
         this.getDispatchIndex = getDispatchIndex;
         this.setDispatchIndex = setDispatchIndex;
      }
   }

   private static final class BeanMethodData {
      @NotNull
      final MethodElement methodElement;
      final int dispatchIndex;

      private BeanMethodData(MethodElement methodElement, int dispatchIndex) {
         this.methodElement = methodElement;
         this.dispatchIndex = dispatchIndex;
      }
   }

   private static final class BeanPropertyData {
      @NonNull
      final TypedElement typedElement;
      @NonNull
      final String name;
      final AnnotationMetadata annotationMetadata;
      @Nullable
      final Map<String, ClassElement> typeArguments;
      final int getMethodDispatchIndex;
      final int setMethodDispatchIndex;
      final int withMethodDispatchIndex;
      final boolean isReadOnly;

      private BeanPropertyData(
         @NonNull TypedElement typedElement,
         @NonNull String name,
         @Nullable AnnotationMetadata annotationMetadata,
         @Nullable Map<String, ClassElement> typeArguments,
         int getMethodDispatchIndex,
         int setMethodDispatchIndex,
         int withMethodDispatchIndex,
         boolean isReadOnly
      ) {
         this.typedElement = typedElement;
         this.name = name;
         this.annotationMetadata = annotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : annotationMetadata;
         this.typeArguments = typeArguments;
         this.getMethodDispatchIndex = getMethodDispatchIndex;
         this.setMethodDispatchIndex = setMethodDispatchIndex;
         this.withMethodDispatchIndex = withMethodDispatchIndex;
         this.isReadOnly = isReadOnly;
      }
   }

   private final class CopyConstructorDispatchTarget implements DispatchWriter.DispatchTarget {
      private final MethodElement constructor;
      private final String parameterName;

      private CopyConstructorDispatchTarget(MethodElement constructor, String name) {
         this.constructor = constructor;
         this.parameterName = name;
      }

      @Override
      public boolean supportsDispatchOne() {
         return true;
      }

      @Override
      public void writeDispatchOne(GeneratorAdapter writer) {
         Set<BeanIntrospectionWriter.BeanPropertyData> constructorProps = new HashSet();
         boolean isMutable = true;
         String nonMutableMessage = null;
         ParameterElement[] parameters = this.constructor.getParameters();
         Object[] constructorArguments = new Object[parameters.length];

         for(int i = 0; i < parameters.length; ++i) {
            ParameterElement parameter = parameters[i];
            String parameterName = parameter.getName();
            if (this.parameterName.equals(parameterName)) {
               constructorArguments[i] = this;
            } else {
               BeanIntrospectionWriter.BeanPropertyData prop = (BeanIntrospectionWriter.BeanPropertyData)BeanIntrospectionWriter.this.beanProperties
                  .stream()
                  .filter(bp -> bp.name.equals(parameterName))
                  .findAny()
                  .orElse(null);
               int getMethodDispatchIndex = prop == null ? -1 : prop.getMethodDispatchIndex;
               if (getMethodDispatchIndex == -1) {
                  isMutable = false;
                  nonMutableMessage = "Cannot create copy of type ["
                     + BeanIntrospectionWriter.this.beanType.getClassName()
                     + "]. Constructor contains argument ["
                     + parameterName
                     + "] that is not a readable property";
                  break;
               }

               DispatchWriter.MethodDispatchTarget methodDispatchTarget = (DispatchWriter.MethodDispatchTarget)BeanIntrospectionWriter.this.dispatchWriter
                  .getDispatchTargets()
                  .get(getMethodDispatchIndex);
               MethodElement readMethod = methodDispatchTarget.getMethodElement();
               if (readMethod.getGenericReturnType().isAssignable(parameter.getGenericType())) {
                  constructorArguments[i] = readMethod;
                  constructorProps.add(prop);
               } else {
                  isMutable = false;
                  nonMutableMessage = "Cannot create copy of type ["
                     + BeanIntrospectionWriter.this.beanType.getClassName()
                     + "]. Property of type ["
                     + readMethod.getGenericReturnType().getName()
                     + "] is not assignable to constructor argument ["
                     + parameterName
                     + "]";
               }
            }
         }

         if (isMutable) {
            writer.loadArg(1);
            BeanIntrospectionWriter.pushCastToType(writer, BeanIntrospectionWriter.this.beanType);
            int prevBeanTypeLocal = writer.newLocal(BeanIntrospectionWriter.this.beanType);
            writer.storeLocal(prevBeanTypeLocal, BeanIntrospectionWriter.this.beanType);
            BeanIntrospectionWriter.this.invokeBeanConstructor(writer, this.constructor, (constructorWriter, constructor) -> {
               for(int i = 0; i < parameters.length; ++i) {
                  ParameterElement parameter = parameters[i];
                  Object constructorArgument = constructorArguments[i];
                  if (constructorArgument == this) {
                     constructorWriter.loadArg(2);
                     BeanIntrospectionWriter.pushCastToType(constructorWriter, parameter);
                     if (!parameter.isPrimitive()) {
                        BeanIntrospectionWriter.pushBoxPrimitiveIfNecessary(parameter, constructorWriter);
                     }
                  } else {
                     MethodElement readMethodx = (MethodElement)constructorArgument;
                     constructorWriter.loadLocal(prevBeanTypeLocal, BeanIntrospectionWriter.this.beanType);
                     this.invokeMethod(constructorWriter, readMethodx);
                  }
               }

            });
            List<BeanIntrospectionWriter.BeanPropertyData> readWriteProps = (List)BeanIntrospectionWriter.this.beanProperties
               .stream()
               .filter(bp -> bp.setMethodDispatchIndex != -1 && bp.getMethodDispatchIndex != -1 && !constructorProps.contains(bp))
               .collect(Collectors.toList());
            if (!readWriteProps.isEmpty()) {
               int beanTypeLocal = writer.newLocal(BeanIntrospectionWriter.this.beanType);
               writer.storeLocal(beanTypeLocal, BeanIntrospectionWriter.this.beanType);

               for(BeanIntrospectionWriter.BeanPropertyData readWriteProp : readWriteProps) {
                  MethodElement writeMethod = ((DispatchWriter.MethodDispatchTarget)BeanIntrospectionWriter.this.dispatchWriter
                        .getDispatchTargets()
                        .get(readWriteProp.setMethodDispatchIndex))
                     .getMethodElement();
                  MethodElement readMethod = ((DispatchWriter.MethodDispatchTarget)BeanIntrospectionWriter.this.dispatchWriter
                        .getDispatchTargets()
                        .get(readWriteProp.getMethodDispatchIndex))
                     .getMethodElement();
                  writer.loadLocal(beanTypeLocal, BeanIntrospectionWriter.this.beanType);
                  writer.loadLocal(prevBeanTypeLocal, BeanIntrospectionWriter.this.beanType);
                  this.invokeMethod(writer, readMethod);
                  ClassElement writeReturnType = this.invokeMethod(writer, writeMethod);
                  if (!writeReturnType.getName().equals("void")) {
                     writer.pop();
                  }
               }

               writer.loadLocal(beanTypeLocal, BeanIntrospectionWriter.this.beanType);
            }
         } else {
            writer.throwException(Type.getType(UnsupportedOperationException.class), nonMutableMessage);
         }

      }

      @NonNull
      private ClassElement invokeMethod(GeneratorAdapter mutateMethod, MethodElement method) {
         ClassElement returnType = method.getReturnType();
         if (BeanIntrospectionWriter.this.classElement.isInterface()) {
            mutateMethod.invokeInterface(
               BeanIntrospectionWriter.this.beanType,
               new Method(method.getName(), BeanIntrospectionWriter.getMethodDescriptor(returnType, Arrays.asList(method.getParameters())))
            );
         } else {
            mutateMethod.invokeVirtual(
               BeanIntrospectionWriter.this.beanType,
               new Method(method.getName(), BeanIntrospectionWriter.getMethodDescriptor(returnType, Arrays.asList(method.getParameters())))
            );
         }

         return returnType;
      }
   }

   private static final class ExceptionDispatchTarget implements DispatchWriter.DispatchTarget {
      private final Class<?> exceptionType;
      private final String message;

      private ExceptionDispatchTarget(Class<?> exceptionType, String message) {
         this.exceptionType = exceptionType;
         this.message = message;
      }

      @Override
      public boolean supportsDispatchOne() {
         return true;
      }

      @Override
      public void writeDispatchOne(GeneratorAdapter writer) {
         writer.throwException(Type.getType(this.exceptionType), this.message);
      }
   }
}
