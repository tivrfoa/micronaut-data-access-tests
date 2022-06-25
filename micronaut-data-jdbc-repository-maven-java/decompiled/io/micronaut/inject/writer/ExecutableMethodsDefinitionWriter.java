package io.micronaut.inject.writer;

import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;
import io.micronaut.asm.Opcodes;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.asm.commons.Method;
import io.micronaut.asm.commons.TableSwitchGenerator;
import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataReference;
import io.micronaut.inject.annotation.AnnotationMetadataWriter;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.ast.TypedElement;
import io.micronaut.inject.processing.JavaModelUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Internal
public class ExecutableMethodsDefinitionWriter extends AbstractClassFileWriter implements Opcodes {
   public static final String CLASS_SUFFIX = "$Exec";
   public static final Method GET_EXECUTABLE_AT_INDEX_METHOD = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(AbstractExecutableMethodsDefinition.class, "getExecutableMethodByIndex", Integer.TYPE)
   );
   public static final Type SUPER_TYPE = Type.getType(AbstractExecutableMethodsDefinition.class);
   private static final Method SUPER_CONSTRUCTOR = Method.getMethod(
      ReflectionUtils.getRequiredInternalConstructor(AbstractExecutableMethodsDefinition.class, AbstractExecutableMethodsDefinition.MethodReference[].class)
   );
   private static final Method WITH_INTERCEPTED_CONSTRUCTOR = new Method("<init>", getConstructorDescriptor(new Class[]{Boolean.TYPE}));
   private static final Method GET_METHOD = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(AbstractExecutableMethodsDefinition.class, "getMethod", String.class, Class[].class)
   );
   private static final Method AT_INDEX_MATCHED_METHOD = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(AbstractExecutableMethodsDefinition.class, "methodAtIndexMatches", Integer.TYPE, String.class, Class[].class)
   );
   private static final String FIELD_METHODS_REFERENCES = "$METHODS_REFERENCES";
   private static final String FIELD_INTERCEPTABLE = "$interceptable";
   private static final int MIN_METHODS_TO_GENERATE_GET_METHOD = 5;
   private final String className;
   private final String internalName;
   private final Type thisType;
   private final String beanDefinitionReferenceClassName;
   private final Map<String, Integer> defaultsStorage = new HashMap();
   private final Map<String, GeneratorAdapter> loadTypeMethods = new LinkedHashMap();
   private final List<String> addedMethods = new ArrayList();
   private final DispatchWriter methodDispatchWriter;

   public ExecutableMethodsDefinitionWriter(String beanDefinitionClassName, String beanDefinitionReferenceClassName, OriginatingElements originatingElements) {
      super(originatingElements);
      this.className = beanDefinitionClassName + "$Exec";
      this.internalName = getInternalName(this.className);
      this.thisType = Type.getObjectType(this.internalName);
      this.beanDefinitionReferenceClassName = beanDefinitionReferenceClassName;
      this.methodDispatchWriter = new DispatchWriter(this.thisType);
   }

   public String getClassName() {
      return this.className;
   }

   public Type getClassType() {
      return this.thisType;
   }

   private MethodElement getMethodElement(int index) {
      return ((DispatchWriter.MethodDispatchTarget)this.methodDispatchWriter.getDispatchTargets().get(index)).methodElement;
   }

   public boolean isSupportsInterceptedProxy() {
      return this.methodDispatchWriter.isHasInterceptedMethod();
   }

   public boolean isAbstract(int index) {
      MethodElement methodElement = this.getMethodElement(index);
      return this.isInterface(index) && !methodElement.isDefault() || methodElement.isAbstract();
   }

   public boolean isInterface(int index) {
      return this.getMethodElement(index).getDeclaringType().isInterface();
   }

   public boolean isDefault(int index) {
      return this.getMethodElement(index).isDefault();
   }

   public boolean isSuspend(int index) {
      return this.getMethodElement(index).isSuspend();
   }

   public int visitExecutableMethod(
      TypedElement declaringType, MethodElement methodElement, String interceptedProxyClassName, String interceptedProxyBridgeMethodName
   ) {
      String methodKey = methodElement.getName()
         + "("
         + (String)Arrays.stream(methodElement.getSuspendParameters()).map(p -> p.getType().getName()).collect(Collectors.joining(","))
         + ")";
      int index = this.addedMethods.indexOf(methodKey);
      if (index > -1) {
         return index;
      } else {
         this.addedMethods.add(methodKey);
         return interceptedProxyClassName == null
            ? this.methodDispatchWriter.addMethod(declaringType, methodElement)
            : this.methodDispatchWriter.addInterceptedMethod(declaringType, methodElement, interceptedProxyClassName, interceptedProxyBridgeMethodName);
      }
   }

   @Override
   public void accept(ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {
      ClassWriter classWriter = new ClassWriter(3);
      classWriter.visit(52, 4112, this.internalName, null, SUPER_TYPE.getInternalName(), null);
      classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
      Type methodsFieldType = Type.getType(AbstractExecutableMethodsDefinition.MethodReference[].class);
      this.buildStaticInit(classWriter, methodsFieldType);
      this.buildConstructor(classWriter, methodsFieldType);
      this.methodDispatchWriter.buildDispatchMethod(classWriter);
      this.methodDispatchWriter.buildGetTargetMethodByIndex(classWriter);
      if (this.methodDispatchWriter.getDispatchTargets().size() > 5) {
         this.buildGetMethod(classWriter);
      }

      for(GeneratorAdapter method : this.loadTypeMethods.values()) {
         method.visitMaxs(3, 1);
         method.visitEnd();
      }

      classWriter.visitEnd();
      OutputStream outputStream = classWriterOutputVisitor.visitClass(this.className, this.getOriginatingElements());
      Throwable var17 = null;

      try {
         outputStream.write(classWriter.toByteArray());
      } catch (Throwable var14) {
         var17 = var14;
         throw var14;
      } finally {
         if (outputStream != null) {
            if (var17 != null) {
               try {
                  outputStream.close();
               } catch (Throwable var13) {
                  var17.addSuppressed(var13);
               }
            } else {
               outputStream.close();
            }
         }

      }

   }

   private void buildStaticInit(ClassWriter classWriter, Type methodsFieldType) {
      GeneratorAdapter staticInit = this.visitStaticInitializer(classWriter);
      classWriter.visitField(26, "$METHODS_REFERENCES", methodsFieldType.getDescriptor(), null, null);
      pushNewArray(staticInit, AbstractExecutableMethodsDefinition.MethodReference.class, this.methodDispatchWriter.getDispatchTargets().size());
      int i = 0;

      for(DispatchWriter.DispatchTarget dispatchTarget : this.methodDispatchWriter.getDispatchTargets()) {
         DispatchWriter.MethodDispatchTarget method = (DispatchWriter.MethodDispatchTarget)dispatchTarget;
         pushStoreInArray(
            staticInit,
            i++,
            this.methodDispatchWriter.getDispatchTargets().size(),
            () -> this.pushNewMethodReference(classWriter, staticInit, method.declaringType, method.methodElement)
         );
      }

      staticInit.putStatic(this.thisType, "$METHODS_REFERENCES", methodsFieldType);
      staticInit.returnValue();
      staticInit.visitMaxs(13, 1);
      staticInit.visitEnd();
   }

   private void buildConstructor(ClassWriter classWriter, Type methodsFieldType) {
      boolean includeInterceptedField = this.methodDispatchWriter.isHasInterceptedMethod();
      if (includeInterceptedField) {
         classWriter.visitField(18, "$interceptable", Type.getType(Boolean.TYPE).getDescriptor(), null, null);
         GeneratorAdapter defaultConstructorWriter = this.startConstructor(classWriter);
         defaultConstructorWriter.loadThis();
         defaultConstructorWriter.push(false);
         defaultConstructorWriter.invokeConstructor(this.thisType, WITH_INTERCEPTED_CONSTRUCTOR);
         defaultConstructorWriter.returnValue();
         defaultConstructorWriter.visitMaxs(1, 1);
         defaultConstructorWriter.visitEnd();
         GeneratorAdapter withInterceptedConstructor = this.startConstructor(classWriter, new Class[]{Boolean.TYPE});
         withInterceptedConstructor.loadThis();
         withInterceptedConstructor.getStatic(this.thisType, "$METHODS_REFERENCES", methodsFieldType);
         withInterceptedConstructor.invokeConstructor(SUPER_TYPE, SUPER_CONSTRUCTOR);
         withInterceptedConstructor.loadThis();
         withInterceptedConstructor.loadArg(0);
         withInterceptedConstructor.putField(this.thisType, "$interceptable", Type.getType(Boolean.TYPE));
         withInterceptedConstructor.returnValue();
         withInterceptedConstructor.visitMaxs(1, 1);
         withInterceptedConstructor.visitEnd();
      } else {
         GeneratorAdapter constructorWriter = this.startConstructor(classWriter);
         constructorWriter.loadThis();
         constructorWriter.getStatic(this.thisType, "$METHODS_REFERENCES", methodsFieldType);
         constructorWriter.invokeConstructor(SUPER_TYPE, SUPER_CONSTRUCTOR);
         constructorWriter.returnValue();
         constructorWriter.visitMaxs(1, 1);
         constructorWriter.visitEnd();
      }

   }

   private void buildGetMethod(ClassWriter classWriter) {
      final GeneratorAdapter findMethod = new GeneratorAdapter(
         classWriter.visitMethod(18, GET_METHOD.getName(), GET_METHOD.getDescriptor(), null, null), 18, GET_METHOD.getName(), GET_METHOD.getDescriptor()
      );
      findMethod.loadThis();
      findMethod.loadArg(0);
      findMethod.invokeVirtual(Type.getType(Object.class), new Method("hashCode", Type.INT_TYPE, new Type[0]));
      final Map<Integer, List<DispatchWriter.MethodDispatchTarget>> hashToMethods = new TreeMap();

      for(DispatchWriter.DispatchTarget dispatchTarget : this.methodDispatchWriter.getDispatchTargets()) {
         DispatchWriter.MethodDispatchTarget method = (DispatchWriter.MethodDispatchTarget)dispatchTarget;
         int hash = method.methodElement.getName().hashCode();
         ((List)hashToMethods.computeIfAbsent(hash, h -> new ArrayList())).add(method);
      }

      int[] hashCodeArray = hashToMethods.keySet().stream().mapToInt(i -> i).toArray();
      findMethod.tableSwitch(hashCodeArray, new TableSwitchGenerator() {
         @Override
         public void generateCase(int hashCode, Label end) {
            for(DispatchWriter.MethodDispatchTarget method : (List)hashToMethods.get(hashCode)) {
               int index = ExecutableMethodsDefinitionWriter.this.methodDispatchWriter.getDispatchTargets().indexOf(method);
               if (index < 0) {
                  throw new IllegalStateException();
               }

               findMethod.loadThis();
               findMethod.push(index);
               findMethod.loadArg(0);
               findMethod.loadArg(1);
               findMethod.invokeVirtual(ExecutableMethodsDefinitionWriter.SUPER_TYPE, ExecutableMethodsDefinitionWriter.AT_INDEX_MATCHED_METHOD);
               findMethod.push(true);
               Label falseLabel = new Label();
               findMethod.ifCmp(Type.BOOLEAN_TYPE, 154, falseLabel);
               findMethod.loadThis();
               findMethod.push(index);
               findMethod.invokeVirtual(ExecutableMethodsDefinitionWriter.SUPER_TYPE, ExecutableMethodsDefinitionWriter.GET_EXECUTABLE_AT_INDEX_METHOD);
               findMethod.returnValue();
               findMethod.visitLabel(falseLabel);
            }

            findMethod.goTo(end);
         }

         @Override
         public void generateDefault() {
         }
      });
      findMethod.push((String)null);
      findMethod.returnValue();
      findMethod.visitMaxs(13, 1);
      findMethod.visitEnd();
   }

   private void pushNewMethodReference(ClassWriter classWriter, GeneratorAdapter staticInit, TypedElement declaringType, MethodElement methodElement) {
      staticInit.newInstance(Type.getType(AbstractExecutableMethodsDefinition.MethodReference.class));
      staticInit.dup();
      Type typeReference = JavaModelUtils.getTypeReference(declaringType.getType());
      staticInit.push(typeReference);
      AnnotationMetadata annotationMetadata = methodElement.getAnnotationMetadata();
      if (annotationMetadata instanceof AnnotationMetadataHierarchy) {
         annotationMetadata = new AnnotationMetadataHierarchy(
            new AnnotationMetadataReference(this.beanDefinitionReferenceClassName, annotationMetadata), annotationMetadata.getDeclaredMetadata()
         );
      }

      this.pushAnnotationMetadata(classWriter, staticInit, annotationMetadata);
      staticInit.push(methodElement.getName());
      ClassElement genericReturnType = methodElement.getGenericReturnType();
      this.pushReturnTypeArgument(
         this.thisType, classWriter, staticInit, declaringType.getName(), genericReturnType, this.defaultsStorage, this.loadTypeMethods
      );
      ParameterElement[] parameters = methodElement.getSuspendParameters();
      if (parameters.length == 0) {
         staticInit.visitInsn(1);
      } else {
         pushBuildArgumentsForMethod(
            typeReference.getClassName(), this.thisType, classWriter, staticInit, Arrays.asList(parameters), this.defaultsStorage, this.loadTypeMethods
         );
      }

      staticInit.push(methodElement.isAbstract());
      staticInit.push(methodElement.isSuspend());
      this.invokeConstructor(
         staticInit,
         AbstractExecutableMethodsDefinition.MethodReference.class,
         new Class[]{Class.class, AnnotationMetadata.class, String.class, Argument.class, Argument[].class, Boolean.TYPE, Boolean.TYPE}
      );
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
            this.thisType, classWriter, staticInit, (AnnotationMetadataHierarchy)annotationMetadata, this.defaultsStorage, this.loadTypeMethods
         );
      } else if (annotationMetadata instanceof DefaultAnnotationMetadata) {
         AnnotationMetadataWriter.instantiateNewMetadata(
            this.thisType, classWriter, staticInit, (DefaultAnnotationMetadata)annotationMetadata, this.defaultsStorage, this.loadTypeMethods
         );
      } else {
         staticInit.push((String)null);
      }

   }
}
