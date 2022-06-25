package io.micronaut.inject.writer;

import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;
import io.micronaut.asm.Opcodes;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.asm.commons.Method;
import io.micronaut.asm.commons.TableSwitchGenerator;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.ast.TypedElement;
import io.micronaut.inject.processing.JavaModelUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.validation.constraints.NotNull;

@Internal
public class DispatchWriter extends AbstractClassFileWriter implements Opcodes {
   private static final Method DISPATCH_METHOD = new Method(
      "dispatch", getMethodDescriptor(Object.class, Arrays.asList(Integer.TYPE, Object.class, Object[].class))
   );
   private static final Method DISPATCH_ONE_METHOD = new Method(
      "dispatchOne", getMethodDescriptor(Object.class, Arrays.asList(Integer.TYPE, Object.class, Object.class))
   );
   private static final Method GET_TARGET_METHOD = new Method(
      "getTargetMethodByIndex", getMethodDescriptor(java.lang.reflect.Method.class, Collections.singletonList(Integer.TYPE))
   );
   private static final Method GET_ACCESSIBLE_TARGET_METHOD = new Method(
      "getAccessibleTargetMethodByIndex", getMethodDescriptor(java.lang.reflect.Method.class, Collections.singletonList(Integer.TYPE))
   );
   private static final Method UNKNOWN_DISPATCH_AT_INDEX = new Method(
      "unknownDispatchAtIndexException", getMethodDescriptor(RuntimeException.class, Collections.singletonList(Integer.TYPE))
   );
   private static final String FIELD_INTERCEPTABLE = "$interceptable";
   private static final Type TYPE_REFLECTION_UTILS = Type.getType(ReflectionUtils.class);
   private static final Method METHOD_GET_REQUIRED_METHOD = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(ReflectionUtils.class, "getRequiredMethod", Class.class, String.class, Class[].class)
   );
   private static final Method METHOD_INVOKE_METHOD = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(ReflectionUtils.class, "invokeMethod", Object.class, java.lang.reflect.Method.class, Object[].class)
   );
   private final List<DispatchWriter.DispatchTarget> dispatchTargets = new ArrayList();
   private final Type thisType;
   private boolean hasInterceptedMethod;

   public DispatchWriter(Type thisType) {
      super();
      this.thisType = thisType;
   }

   public int addSetField(FieldElement beanField) {
      return this.addDispatchTarget(new DispatchWriter.FieldSetDispatchTarget(beanField));
   }

   public int addGetField(FieldElement beanField) {
      return this.addDispatchTarget(new DispatchWriter.FieldGetDispatchTarget(beanField));
   }

   public int addMethod(TypedElement declaringType, MethodElement methodElement) {
      return this.addMethod(declaringType, methodElement, false);
   }

   public int addMethod(TypedElement declaringType, MethodElement methodElement, boolean useOneDispatch) {
      return this.addDispatchTarget(new DispatchWriter.MethodDispatchTarget(declaringType, methodElement, useOneDispatch, !useOneDispatch));
   }

   public int addInterceptedMethod(
      TypedElement declaringType, MethodElement methodElement, String interceptedProxyClassName, String interceptedProxyBridgeMethodName
   ) {
      this.hasInterceptedMethod = true;
      return this.addDispatchTarget(
         new DispatchWriter.InterceptableMethodDispatchTarget(
            declaringType, methodElement, interceptedProxyClassName, interceptedProxyBridgeMethodName, this.thisType
         )
      );
   }

   public int addDispatchTarget(DispatchWriter.DispatchTarget dispatchTarget) {
      this.dispatchTargets.add(dispatchTarget);
      return this.dispatchTargets.size() - 1;
   }

   public void buildDispatchMethod(ClassWriter classWriter) {
      int[] cases = this.dispatchTargets
         .stream()
         .filter(DispatchWriter.DispatchTarget::supportsDispatchMulti)
         .mapToInt(this.dispatchTargets::indexOf)
         .toArray();
      if (cases.length != 0) {
         final GeneratorAdapter dispatchMethod = new GeneratorAdapter(
            classWriter.visitMethod(20, DISPATCH_METHOD.getName(), DISPATCH_METHOD.getDescriptor(), null, null),
            20,
            DISPATCH_METHOD.getName(),
            DISPATCH_METHOD.getDescriptor()
         );
         dispatchMethod.loadArg(0);
         dispatchMethod.tableSwitch(cases, new TableSwitchGenerator() {
            @Override
            public void generateCase(int key, Label end) {
               DispatchWriter.DispatchTarget method = (DispatchWriter.DispatchTarget)DispatchWriter.this.dispatchTargets.get(key);
               method.writeDispatchMulti(dispatchMethod, key);
               dispatchMethod.returnValue();
            }

            @Override
            public void generateDefault() {
               dispatchMethod.loadThis();
               dispatchMethod.loadArg(0);
               dispatchMethod.invokeVirtual(DispatchWriter.this.thisType, DispatchWriter.UNKNOWN_DISPATCH_AT_INDEX);
               dispatchMethod.throwException();
            }
         }, true);
         dispatchMethod.visitMaxs(13, 1);
         dispatchMethod.visitEnd();
      }
   }

   public void buildDispatchOneMethod(ClassWriter classWriter) {
      int[] cases = this.dispatchTargets.stream().filter(DispatchWriter.DispatchTarget::supportsDispatchOne).mapToInt(this.dispatchTargets::indexOf).toArray();
      if (cases.length != 0) {
         final GeneratorAdapter dispatchMethod = new GeneratorAdapter(
            classWriter.visitMethod(20, DISPATCH_ONE_METHOD.getName(), DISPATCH_ONE_METHOD.getDescriptor(), null, null),
            20,
            DISPATCH_ONE_METHOD.getName(),
            DISPATCH_ONE_METHOD.getDescriptor()
         );
         dispatchMethod.loadArg(0);
         dispatchMethod.tableSwitch(cases, new TableSwitchGenerator() {
            @Override
            public void generateCase(int key, Label end) {
               DispatchWriter.DispatchTarget method = (DispatchWriter.DispatchTarget)DispatchWriter.this.dispatchTargets.get(key);
               method.writeDispatchOne(dispatchMethod);
               dispatchMethod.returnValue();
            }

            @Override
            public void generateDefault() {
               dispatchMethod.loadThis();
               dispatchMethod.loadArg(0);
               dispatchMethod.invokeVirtual(DispatchWriter.this.thisType, DispatchWriter.UNKNOWN_DISPATCH_AT_INDEX);
               dispatchMethod.throwException();
            }
         }, true);
         dispatchMethod.visitMaxs(13, 1);
         dispatchMethod.visitEnd();
      }
   }

   public void buildGetTargetMethodByIndex(ClassWriter classWriter) {
      final GeneratorAdapter getTargetMethodByIndex = new GeneratorAdapter(
         classWriter.visitMethod(20, GET_TARGET_METHOD.getName(), GET_TARGET_METHOD.getDescriptor(), null, null),
         20,
         GET_TARGET_METHOD.getName(),
         GET_TARGET_METHOD.getDescriptor()
      );
      getTargetMethodByIndex.loadArg(0);
      int[] cases = this.dispatchTargets
         .stream()
         .filter(DispatchWriter.MethodDispatchTarget.class::isInstance)
         .mapToInt(this.dispatchTargets::indexOf)
         .toArray();
      getTargetMethodByIndex.tableSwitch(
         cases,
         new TableSwitchGenerator() {
            @Override
            public void generateCase(int key, Label end) {
               DispatchWriter.MethodDispatchTarget method = (DispatchWriter.MethodDispatchTarget)DispatchWriter.this.dispatchTargets.get(key);
               Type declaringTypeObject = JavaModelUtils.getTypeReference(method.declaringType);
               List<ParameterElement> argumentTypes = Arrays.asList(method.methodElement.getSuspendParameters());
               getTargetMethodByIndex.push(declaringTypeObject);
               getTargetMethodByIndex.push(method.methodElement.getName());
               if (!argumentTypes.isEmpty()) {
                  int len = argumentTypes.size();
                  Iterator<ParameterElement> iter = argumentTypes.iterator();
                  AbstractClassFileWriter.pushNewArray(getTargetMethodByIndex, Class.class, len);
   
                  for(int i = 0; i < len; ++i) {
                     ParameterElement type = (ParameterElement)iter.next();
                     AbstractClassFileWriter.pushStoreInArray(
                        getTargetMethodByIndex, i, len, () -> getTargetMethodByIndex.push(JavaModelUtils.getTypeReference(type))
                     );
                  }
               } else {
                  getTargetMethodByIndex.getStatic(DispatchWriter.TYPE_REFLECTION_UTILS, "EMPTY_CLASS_ARRAY", Type.getType(Class[].class));
               }
   
               getTargetMethodByIndex.invokeStatic(DispatchWriter.TYPE_REFLECTION_UTILS, DispatchWriter.METHOD_GET_REQUIRED_METHOD);
               getTargetMethodByIndex.returnValue();
            }
   
            @Override
            public void generateDefault() {
               getTargetMethodByIndex.loadThis();
               getTargetMethodByIndex.loadArg(0);
               getTargetMethodByIndex.invokeVirtual(DispatchWriter.this.thisType, DispatchWriter.UNKNOWN_DISPATCH_AT_INDEX);
               getTargetMethodByIndex.throwException();
            }
         },
         true
      );
      getTargetMethodByIndex.visitMaxs(13, 1);
      getTargetMethodByIndex.visitEnd();
   }

   @Override
   public void accept(ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {
      throw new IllegalStateException();
   }

   public List<DispatchWriter.DispatchTarget> getDispatchTargets() {
      return this.dispatchTargets;
   }

   public boolean isHasInterceptedMethod() {
      return this.hasInterceptedMethod;
   }

   @Internal
   public interface DispatchTarget {
      default boolean supportsDispatchOne() {
         return false;
      }

      default void writeDispatchOne(GeneratorAdapter writer) {
         throw new IllegalStateException("Not supported");
      }

      default boolean supportsDispatchMulti() {
         return false;
      }

      default void writeDispatchMulti(GeneratorAdapter writer, int methodIndex) {
         throw new IllegalStateException("Not supported");
      }
   }

   @Internal
   public static final class FieldGetDispatchTarget implements DispatchWriter.DispatchTarget {
      @NotNull
      final FieldElement beanField;

      public FieldGetDispatchTarget(FieldElement beanField) {
         this.beanField = beanField;
      }

      @Override
      public boolean supportsDispatchOne() {
         return true;
      }

      @Override
      public boolean supportsDispatchMulti() {
         return false;
      }

      @Override
      public void writeDispatchOne(GeneratorAdapter writer) {
         Type propertyType = JavaModelUtils.getTypeReference(this.beanField.getType());
         Type beanType = JavaModelUtils.getTypeReference(this.beanField.getOwningType());
         writer.loadArg(1);
         AbstractClassFileWriter.pushCastToType(writer, beanType);
         writer.getField(JavaModelUtils.getTypeReference(this.beanField.getOwningType()), this.beanField.getName(), propertyType);
         AbstractClassFileWriter.pushBoxPrimitiveIfNecessary(propertyType, writer);
      }
   }

   @Internal
   public static final class FieldSetDispatchTarget implements DispatchWriter.DispatchTarget {
      @NotNull
      final FieldElement beanField;

      public FieldSetDispatchTarget(FieldElement beanField) {
         this.beanField = beanField;
      }

      @Override
      public boolean supportsDispatchOne() {
         return true;
      }

      @Override
      public boolean supportsDispatchMulti() {
         return false;
      }

      @Override
      public void writeDispatchOne(GeneratorAdapter writer) {
         Type propertyType = JavaModelUtils.getTypeReference(this.beanField.getType());
         Type beanType = JavaModelUtils.getTypeReference(this.beanField.getOwningType());
         writer.loadArg(1);
         AbstractClassFileWriter.pushCastToType(writer, beanType);
         writer.loadArg(2);
         AbstractClassFileWriter.pushCastToType(writer, propertyType);
         writer.putField(beanType, this.beanField.getName(), propertyType);
         writer.push((String)null);
      }
   }

   @Internal
   public static final class InterceptableMethodDispatchTarget extends DispatchWriter.MethodDispatchTarget {
      final String interceptedProxyClassName;
      final String interceptedProxyBridgeMethodName;
      final Type thisType;

      private InterceptableMethodDispatchTarget(
         TypedElement declaringType, MethodElement methodElement, String interceptedProxyClassName, String interceptedProxyBridgeMethodName, Type thisType
      ) {
         super(declaringType, methodElement, false, true);
         this.interceptedProxyClassName = interceptedProxyClassName;
         this.interceptedProxyBridgeMethodName = interceptedProxyBridgeMethodName;
         this.thisType = thisType;
      }

      @Override
      public void writeDispatchMulti(GeneratorAdapter writer, int methodIndex) {
         String methodName = this.methodElement.getName();
         List<ParameterElement> argumentTypes = Arrays.asList(this.methodElement.getSuspendParameters());
         Type declaringTypeObject = JavaModelUtils.getTypeReference(this.declaringType);
         ClassElement returnType = this.methodElement.isSuspend() ? ClassElement.of(Object.class) : this.methodElement.getReturnType();
         boolean isInterface = this.declaringType.getType().isInterface();
         Type returnTypeObject = JavaModelUtils.getTypeReference(returnType);
         writer.loadArg(1);
         writer.dup();
         String methodDescriptor = AbstractClassFileWriter.getMethodDescriptor(returnType, argumentTypes);
         Label invokeTargetBlock = new Label();
         Type interceptedProxyType = AbstractClassFileWriter.getObjectType(this.interceptedProxyClassName);
         writer.loadThis();
         writer.getField(this.thisType, "$interceptable", Type.getType(Boolean.TYPE));
         writer.push(true);
         writer.ifCmp(Type.BOOLEAN_TYPE, 154, invokeTargetBlock);
         writer.loadArg(1);
         writer.instanceOf(interceptedProxyType);
         writer.push(true);
         writer.ifCmp(Type.BOOLEAN_TYPE, 154, invokeTargetBlock);
         AbstractClassFileWriter.pushCastToType(writer, interceptedProxyType);
         Iterator<ParameterElement> iterator = argumentTypes.iterator();

         for(int i = 0; i < argumentTypes.size(); ++i) {
            writer.loadArg(2);
            writer.push(i);
            writer.visitInsn(50);
            AbstractClassFileWriter.pushCastToType(writer, (TypedElement)iterator.next());
         }

         writer.visitMethodInsn(182, interceptedProxyType.getInternalName(), this.interceptedProxyBridgeMethodName, methodDescriptor, false);
         if (returnTypeObject.equals(Type.VOID_TYPE)) {
            writer.visitInsn(1);
         } else {
            AbstractClassFileWriter.pushBoxPrimitiveIfNecessary(returnType, writer);
         }

         writer.returnValue();
         writer.visitLabel(invokeTargetBlock);
         writer.pop();
         AbstractClassFileWriter.pushCastToType(writer, declaringTypeObject);
         boolean hasArgs = !argumentTypes.isEmpty();
         if (hasArgs) {
            int argCount = argumentTypes.size();
            Iterator<ParameterElement> argIterator = argumentTypes.iterator();

            for(int i = 0; i < argCount; ++i) {
               writer.loadArg(2);
               writer.push(i);
               writer.visitInsn(50);
               AbstractClassFileWriter.pushCastToType(writer, (TypedElement)argIterator.next());
            }
         }

         writer.visitMethodInsn(isInterface ? 185 : 182, declaringTypeObject.getInternalName(), methodName, methodDescriptor, isInterface);
         if (returnTypeObject.equals(Type.VOID_TYPE)) {
            writer.visitInsn(1);
         } else {
            AbstractClassFileWriter.pushBoxPrimitiveIfNecessary(returnType, writer);
         }

      }
   }

   @Internal
   public static class MethodDispatchTarget implements DispatchWriter.DispatchTarget {
      final TypedElement declaringType;
      final MethodElement methodElement;
      final boolean oneDispatch;
      final boolean multiDispatch;

      private MethodDispatchTarget(TypedElement declaringType, MethodElement methodElement, boolean oneDispatch, boolean multiDispatch) {
         this.declaringType = declaringType;
         this.methodElement = methodElement;
         this.oneDispatch = oneDispatch;
         this.multiDispatch = multiDispatch;
      }

      public MethodElement getMethodElement() {
         return this.methodElement;
      }

      @Override
      public boolean supportsDispatchOne() {
         return this.oneDispatch;
      }

      @Override
      public boolean supportsDispatchMulti() {
         return this.multiDispatch;
      }

      @Override
      public void writeDispatchMulti(GeneratorAdapter writer, int methodIndex) {
         String methodName = this.methodElement.getName();
         List<ParameterElement> argumentTypes = Arrays.asList(this.methodElement.getSuspendParameters());
         Type declaringTypeObject = JavaModelUtils.getTypeReference(this.declaringType);
         boolean reflectionRequired = this.methodElement.isReflectionRequired();
         ClassElement returnType = this.methodElement.isSuspend() ? ClassElement.of(Object.class) : this.methodElement.getReturnType();
         boolean isInterface = this.declaringType.getType().isInterface();
         Type returnTypeObject = JavaModelUtils.getTypeReference(returnType);
         writer.loadArg(1);
         if (reflectionRequired) {
            writer.loadThis();
            writer.push(methodIndex);
            writer.invokeVirtual(ExecutableMethodsDefinitionWriter.SUPER_TYPE, DispatchWriter.GET_ACCESSIBLE_TARGET_METHOD);
         } else {
            writer.dup();
            AbstractClassFileWriter.pushCastToType(writer, declaringTypeObject);
         }

         boolean hasArgs = !argumentTypes.isEmpty();
         if (hasArgs) {
            if (reflectionRequired) {
               writer.loadArg(2);
            } else {
               int argCount = argumentTypes.size();
               Iterator<ParameterElement> argIterator = argumentTypes.iterator();

               for(int i = 0; i < argCount; ++i) {
                  writer.loadArg(2);
                  writer.push(i);
                  writer.visitInsn(50);
                  AbstractClassFileWriter.pushCastToType(writer, (TypedElement)argIterator.next());
               }
            }
         } else if (reflectionRequired) {
            writer.getStatic(Type.getType(ArrayUtils.class), "EMPTY_OBJECT_ARRAY", Type.getType(Object[].class));
         }

         if (reflectionRequired) {
            writer.invokeStatic(DispatchWriter.TYPE_REFLECTION_UTILS, DispatchWriter.METHOD_INVOKE_METHOD);
         } else {
            String methodDescriptor = AbstractClassFileWriter.getMethodDescriptor(returnType, argumentTypes);
            writer.visitMethodInsn(isInterface ? 185 : 182, declaringTypeObject.getInternalName(), methodName, methodDescriptor, isInterface);
         }

         if (returnTypeObject.equals(Type.VOID_TYPE)) {
            writer.visitInsn(1);
         } else {
            AbstractClassFileWriter.pushBoxPrimitiveIfNecessary(returnType, writer);
         }

      }

      @Override
      public void writeDispatchOne(GeneratorAdapter writer) {
         String methodName = this.methodElement.getName();
         List<ParameterElement> argumentTypes = Arrays.asList(this.methodElement.getSuspendParameters());
         Type declaringTypeObject = JavaModelUtils.getTypeReference(this.declaringType);
         ClassElement returnType = this.methodElement.isSuspend() ? ClassElement.of(Object.class) : this.methodElement.getReturnType();
         boolean isInterface = this.declaringType.getType().isInterface();
         Type returnTypeObject = JavaModelUtils.getTypeReference(returnType);
         writer.loadArg(1);
         AbstractClassFileWriter.pushCastToType(writer, this.declaringType);
         boolean hasArgs = !argumentTypes.isEmpty();
         if (hasArgs) {
            writer.loadArg(2);
            AbstractClassFileWriter.pushCastToType(writer, (TypedElement)argumentTypes.get(0));
         }

         writer.visitMethodInsn(
            isInterface ? 185 : 182,
            declaringTypeObject.getInternalName(),
            methodName,
            AbstractClassFileWriter.getMethodDescriptor(returnType, argumentTypes),
            isInterface
         );
         if (returnTypeObject.equals(Type.VOID_TYPE)) {
            writer.visitInsn(1);
         } else {
            AbstractClassFileWriter.pushBoxPrimitiveIfNecessary(returnType, writer);
         }

      }
   }
}
