package io.micronaut.inject.writer;

import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;
import io.micronaut.asm.MethodVisitor;
import io.micronaut.asm.Opcodes;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.asm.commons.Method;
import io.micronaut.context.AbstractExecutableMethod;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.annotation.AnnotationMetadataReference;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.ast.TypedElement;
import io.micronaut.inject.processing.JavaModelUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Internal
public class ExecutableMethodWriter extends AbstractAnnotationMetadataWriter implements Opcodes {
   public static final Method METHOD_INVOKE_INTERNAL = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(AbstractExecutableMethod.class, "invokeInternal", Object.class, Object[].class)
   );
   protected static final Method METHOD_IS_ABSTRACT = Method.getMethod(ReflectionUtils.getRequiredInternalMethod(ExecutableMethod.class, "isAbstract"));
   protected static final Method METHOD_IS_SUSPEND = Method.getMethod(ReflectionUtils.getRequiredInternalMethod(ExecutableMethod.class, "isSuspend"));
   protected static final Method METHOD_GET_TARGET = Method.getMethod("java.lang.reflect.Method resolveTargetMethod()");
   private static final Type TYPE_REFLECTION_UTILS = Type.getType(ReflectionUtils.class);
   private static final Method METHOD_GET_REQUIRED_METHOD = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(ReflectionUtils.class, "getRequiredMethod", Class.class, String.class, Class[].class)
   );
   private static final String FIELD_INTERCEPTABLE = "$interceptable";
   protected final Type methodType;
   private final ClassWriter classWriter = new ClassWriter(3);
   private final String className;
   private final String internalName;
   private final boolean isInterface;
   private final boolean isAbstract;
   private final boolean isSuspend;
   private final boolean isDefault;
   private final String interceptedProxyClassName;
   private final String interceptedProxyBridgeMethodName;

   public ExecutableMethodWriter(
      String methodClassName,
      boolean isInterface,
      boolean isAbstract,
      boolean isDefault,
      boolean isSuspend,
      OriginatingElements originatingElements,
      AnnotationMetadata annotationMetadata,
      String interceptedProxyClassName,
      String interceptedProxyBridgeMethodName
   ) {
      super(methodClassName, originatingElements, annotationMetadata, true);
      this.className = methodClassName;
      this.internalName = getInternalName(methodClassName);
      this.methodType = getObjectType(methodClassName);
      this.isInterface = isInterface;
      this.isAbstract = isAbstract;
      this.isDefault = isDefault;
      this.isSuspend = isSuspend;
      this.interceptedProxyClassName = interceptedProxyClassName;
      this.interceptedProxyBridgeMethodName = interceptedProxyBridgeMethodName;
   }

   public boolean isSupportsInterceptedProxy() {
      return this.interceptedProxyClassName != null;
   }

   public boolean isAbstract() {
      return this.isAbstract;
   }

   public boolean isInterface() {
      return this.isInterface;
   }

   public boolean isDefault() {
      return this.isDefault;
   }

   public boolean isSuspend() {
      return this.isSuspend;
   }

   public String getClassName() {
      return this.className;
   }

   public String getInternalName() {
      return this.internalName;
   }

   public void visitMethod(TypedElement declaringType, MethodElement methodElement) {
      String methodName = methodElement.getName();
      List<ParameterElement> argumentTypes = Arrays.asList(methodElement.getSuspendParameters());
      Type declaringTypeObject = JavaModelUtils.getTypeReference(declaringType);
      boolean hasArgs = !argumentTypes.isEmpty();
      this.classWriter.visit(52, 4096, this.internalName, null, Type.getInternalName(AbstractExecutableMethod.class), null);
      this.classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
      if (!(this.annotationMetadata instanceof AnnotationMetadataReference)) {
         this.writeAnnotationMetadataStaticInitializer(this.classWriter);
      }

      this.writeGetAnnotationMetadataMethod(this.classWriter);
      MethodVisitor executorMethodConstructor;
      GeneratorAdapter constructorWriter;
      if (this.interceptedProxyBridgeMethodName != null) {
         String descriptor = Type.getDescriptor(Boolean.TYPE);
         this.classWriter.visitField(18, "$interceptable", descriptor, null, null);
         GeneratorAdapter defaultConstructorWriter = new GeneratorAdapter(this.startConstructor(this.classWriter), 1, "<init>", "()V");
         String executorMethodConstructorDescriptor = getConstructorDescriptor(new Class[]{Boolean.TYPE});
         executorMethodConstructor = this.startConstructor(this.classWriter, new Class[]{Boolean.TYPE});
         constructorWriter = new GeneratorAdapter(executorMethodConstructor, 1, "<init>", executorMethodConstructorDescriptor);
         defaultConstructorWriter.loadThis();
         defaultConstructorWriter.push(false);
         defaultConstructorWriter.visitMethodInsn(183, this.internalName, "<init>", executorMethodConstructorDescriptor, false);
         defaultConstructorWriter.visitInsn(177);
         defaultConstructorWriter.visitMaxs(13, 1);
         constructorWriter.loadThis();
         constructorWriter.loadArg(0);
         constructorWriter.putField(Type.getObjectType(this.internalName), "$interceptable", Type.getType(Boolean.TYPE));
      } else {
         executorMethodConstructor = this.startConstructor(this.classWriter);
         constructorWriter = new GeneratorAdapter(executorMethodConstructor, 1, "<init>", "()V");
      }

      constructorWriter.loadThis();
      constructorWriter.loadThis();
      constructorWriter.push(declaringTypeObject);
      constructorWriter.push(methodName);
      ClassElement genericReturnType = methodElement.getGenericReturnType();
      if (genericReturnType.isPrimitive() && !genericReturnType.isArray()) {
         String constantName = genericReturnType.getName().toUpperCase(Locale.ENGLISH);
         Type type = Type.getType(Argument.class);
         constructorWriter.getStatic(type, constantName, type);
      } else {
         pushCreateArgument(
            declaringType.getName(),
            this.methodType,
            this.classWriter,
            constructorWriter,
            genericReturnType.getName(),
            genericReturnType,
            genericReturnType.getAnnotationMetadata(),
            genericReturnType.getTypeArguments(),
            new HashMap(),
            this.loadTypeMethods
         );
      }

      if (hasArgs) {
         pushBuildArgumentsForMethod(
            this.methodType.getClassName(), this.methodType, this.classWriter, constructorWriter, argumentTypes, new HashMap(), this.loadTypeMethods
         );

         for(ParameterElement pe : argumentTypes) {
            DefaultAnnotationMetadata.contributeDefaults(this.annotationMetadata, pe.getAnnotationMetadata());
            DefaultAnnotationMetadata.contributeRepeatable(this.annotationMetadata, pe.getGenericType());
         }

         this.invokeConstructor(
            executorMethodConstructor, AbstractExecutableMethod.class, new Class[]{Class.class, String.class, Argument.class, Argument[].class}
         );
      } else {
         this.invokeConstructor(executorMethodConstructor, AbstractExecutableMethod.class, new Class[]{Class.class, String.class, Argument.class});
      }

      constructorWriter.visitInsn(177);
      constructorWriter.visitMaxs(13, 1);
      GeneratorAdapter isAbstractMethod = new GeneratorAdapter(
         this.classWriter.visitMethod(17, METHOD_IS_ABSTRACT.getName(), METHOD_IS_ABSTRACT.getDescriptor(), null, null),
         1,
         METHOD_IS_ABSTRACT.getName(),
         METHOD_IS_ABSTRACT.getDescriptor()
      );
      isAbstractMethod.push(this.isAbstract());
      isAbstractMethod.returnValue();
      isAbstractMethod.visitMaxs(1, 1);
      isAbstractMethod.endMethod();
      GeneratorAdapter isSuspendMethod = new GeneratorAdapter(
         this.classWriter.visitMethod(17, METHOD_IS_SUSPEND.getName(), METHOD_IS_SUSPEND.getDescriptor(), null, null),
         1,
         METHOD_IS_SUSPEND.getName(),
         METHOD_IS_SUSPEND.getDescriptor()
      );
      isSuspendMethod.push(this.isSuspend());
      isSuspendMethod.returnValue();
      isSuspendMethod.visitMaxs(1, 1);
      isSuspendMethod.endMethod();
      String invokeDescriptor = METHOD_INVOKE_INTERNAL.getDescriptor();
      String invokeInternalName = METHOD_INVOKE_INTERNAL.getName();
      GeneratorAdapter invokeMethod = new GeneratorAdapter(
         this.classWriter.visitMethod(1, invokeInternalName, invokeDescriptor, null, null), 1, invokeInternalName, invokeDescriptor
      );
      ClassElement returnType = methodElement.isSuspend() ? ClassElement.of(Object.class) : methodElement.getReturnType();
      this.buildInvokeMethod(declaringTypeObject, methodName, returnType, argumentTypes, invokeMethod);
      this.buildResolveTargetMethod(methodName, declaringTypeObject, hasArgs, argumentTypes);

      for(GeneratorAdapter method : this.loadTypeMethods.values()) {
         method.visitMaxs(3, 1);
         method.visitEnd();
      }

   }

   @Override
   public void accept(ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {
      OutputStream outputStream = classWriterOutputVisitor.visitClass(this.className, this.getOriginatingElements());
      Throwable var3 = null;

      try {
         outputStream.write(this.classWriter.toByteArray());
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (outputStream != null) {
            if (var3 != null) {
               try {
                  outputStream.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               outputStream.close();
            }
         }

      }

   }

   @NonNull
   @Override
   protected final GeneratorAdapter beginAnnotationMetadataMethod(ClassWriter classWriter) {
      return this.startProtectedMethod(classWriter, "resolveAnnotationMetadata", AnnotationMetadata.class.getName(), new String[0]);
   }

   protected void buildInvokeMethod(
      Type declaringTypeObject, String methodName, ClassElement returnType, Collection<ParameterElement> argumentTypes, GeneratorAdapter invokeMethodVisitor
   ) {
      Type returnTypeObject = JavaModelUtils.getTypeReference(returnType);
      invokeMethodVisitor.visitVarInsn(25, 1);
      invokeMethodVisitor.dup();
      String methodDescriptor = getMethodDescriptor(returnType, argumentTypes);
      if (this.interceptedProxyClassName != null) {
         Label invokeTargetBlock = new Label();
         Type interceptedProxyType = getObjectType(this.interceptedProxyClassName);
         invokeMethodVisitor.loadThis();
         invokeMethodVisitor.getField(Type.getObjectType(this.internalName), "$interceptable", Type.getType(Boolean.TYPE));
         invokeMethodVisitor.push(true);
         invokeMethodVisitor.ifCmp(Type.BOOLEAN_TYPE, 154, invokeTargetBlock);
         invokeMethodVisitor.loadArg(0);
         invokeMethodVisitor.instanceOf(interceptedProxyType);
         invokeMethodVisitor.push(true);
         invokeMethodVisitor.ifCmp(Type.BOOLEAN_TYPE, 154, invokeTargetBlock);
         pushCastToType(invokeMethodVisitor, interceptedProxyType);
         Iterator<ParameterElement> iterator = argumentTypes.iterator();

         for(int i = 0; i < argumentTypes.size(); ++i) {
            invokeMethodVisitor.loadArg(1);
            invokeMethodVisitor.push(i);
            invokeMethodVisitor.visitInsn(50);
            pushCastToType(invokeMethodVisitor, (TypedElement)iterator.next());
         }

         invokeMethodVisitor.visitMethodInsn(182, interceptedProxyType.getInternalName(), this.interceptedProxyBridgeMethodName, methodDescriptor, false);
         if (returnTypeObject.equals(Type.VOID_TYPE)) {
            invokeMethodVisitor.visitInsn(1);
         } else {
            pushBoxPrimitiveIfNecessary(returnType, invokeMethodVisitor);
         }

         invokeMethodVisitor.visitInsn(176);
         invokeMethodVisitor.visitLabel(invokeTargetBlock);
         invokeMethodVisitor.pop();
      }

      pushCastToType(invokeMethodVisitor, declaringTypeObject);
      boolean hasArgs = !argumentTypes.isEmpty();
      if (hasArgs) {
         int argCount = argumentTypes.size();
         Iterator<ParameterElement> argIterator = argumentTypes.iterator();

         for(int i = 0; i < argCount; ++i) {
            invokeMethodVisitor.visitVarInsn(25, 2);
            invokeMethodVisitor.push(i);
            invokeMethodVisitor.visitInsn(50);
            pushCastToType(invokeMethodVisitor, (TypedElement)argIterator.next());
         }
      }

      invokeMethodVisitor.visitMethodInsn(this.isInterface ? 185 : 182, declaringTypeObject.getInternalName(), methodName, methodDescriptor, this.isInterface);
      if (returnTypeObject.equals(Type.VOID_TYPE)) {
         invokeMethodVisitor.visitInsn(1);
      } else {
         pushBoxPrimitiveIfNecessary(returnType, invokeMethodVisitor);
      }

      invokeMethodVisitor.visitInsn(176);
      invokeMethodVisitor.visitMaxs(13, 1);
      invokeMethodVisitor.visitEnd();
   }

   private void buildResolveTargetMethod(String methodName, Type declaringTypeObject, boolean hasArgs, Collection<ParameterElement> argumentTypeClasses) {
      String targetMethodInternalName = METHOD_GET_TARGET.getName();
      String targetMethodDescriptor = METHOD_GET_TARGET.getDescriptor();
      GeneratorAdapter getTargetMethod = new GeneratorAdapter(
         this.classWriter.visitMethod(17, targetMethodInternalName, targetMethodDescriptor, null, null), 17, targetMethodInternalName, targetMethodDescriptor
      );
      getTargetMethod.push(declaringTypeObject);
      getTargetMethod.push(methodName);
      if (hasArgs) {
         int len = argumentTypeClasses.size();
         Iterator<ParameterElement> iter = argumentTypeClasses.iterator();
         pushNewArray(getTargetMethod, Class.class, len);

         for(int i = 0; i < len; ++i) {
            ParameterElement type = (ParameterElement)iter.next();
            pushStoreInArray(getTargetMethod, i, len, () -> getTargetMethod.push(JavaModelUtils.getTypeReference(type)));
         }
      } else {
         getTargetMethod.getStatic(TYPE_REFLECTION_UTILS, "EMPTY_CLASS_ARRAY", Type.getType(Class[].class));
      }

      getTargetMethod.invokeStatic(TYPE_REFLECTION_UTILS, METHOD_GET_REQUIRED_METHOD);
      getTargetMethod.returnValue();
      getTargetMethod.visitMaxs(1, 1);
      getTargetMethod.endMethod();
   }
}
