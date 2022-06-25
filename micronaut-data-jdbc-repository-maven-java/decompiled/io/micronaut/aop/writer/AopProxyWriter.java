package io.micronaut.aop.writer;

import io.micronaut.aop.HotSwappableInterceptedProxy;
import io.micronaut.aop.Intercepted;
import io.micronaut.aop.InterceptedProxy;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.InterceptorKind;
import io.micronaut.aop.Introduced;
import io.micronaut.aop.chain.InterceptorChain;
import io.micronaut.aop.chain.MethodInterceptorChain;
import io.micronaut.aop.internal.intercepted.InterceptedMethodUtil;
import io.micronaut.asm.ClassVisitor;
import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;
import io.micronaut.asm.MethodVisitor;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.asm.commons.Method;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.core.value.OptionalValues;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.ProxyBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataReference;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.ast.TypedElement;
import io.micronaut.inject.configuration.ConfigurationMetadata;
import io.micronaut.inject.configuration.ConfigurationMetadataBuilder;
import io.micronaut.inject.processing.JavaModelUtils;
import io.micronaut.inject.visitor.VisitorContext;
import io.micronaut.inject.writer.AbstractClassFileWriter;
import io.micronaut.inject.writer.BeanDefinitionWriter;
import io.micronaut.inject.writer.ClassWriterOutputVisitor;
import io.micronaut.inject.writer.ExecutableMethodsDefinitionWriter;
import io.micronaut.inject.writer.OriginatingElements;
import io.micronaut.inject.writer.ProxyingBeanDefinitionVisitor;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AopProxyWriter extends AbstractClassFileWriter implements ProxyingBeanDefinitionVisitor, Toggleable {
   public static final int MAX_LOCALS = 3;
   public static final Method METHOD_GET_PROXY_TARGET = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(
         ExecutionHandleLocator.class, "getProxyTargetMethod", Argument.class, Qualifier.class, String.class, Class[].class
      )
   );
   public static final Method METHOD_GET_PROXY_TARGET_BEAN_WITH_CONTEXT = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(DefaultBeanContext.class, "getProxyTargetBean", BeanResolutionContext.class, Argument.class, Qualifier.class)
   );
   public static final Method METHOD_GET_PROXY_TARGET_BEAN = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(BeanLocator.class, "getProxyTargetBean", Argument.class, Qualifier.class)
   );
   public static final Method METHOD_HAS_CACHED_INTERCEPTED_METHOD = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(InterceptedProxy.class, "hasCachedInterceptedTarget")
   );
   public static final Type FIELD_TYPE_INTERCEPTORS = Type.getType(Interceptor[][].class);
   public static final Type TYPE_INTERCEPTOR_CHAIN = Type.getType(InterceptorChain.class);
   public static final Type TYPE_METHOD_INTERCEPTOR_CHAIN = Type.getType(MethodInterceptorChain.class);
   public static final String FIELD_TARGET = "$target";
   public static final String FIELD_BEAN_RESOLUTION_CONTEXT = "$beanResolutionContext";
   public static final String FIELD_READ_WRITE_LOCK = "$target_rwl";
   public static final Type TYPE_READ_WRITE_LOCK = Type.getType(ReentrantReadWriteLock.class);
   public static final String FIELD_READ_LOCK = "$target_rl";
   public static final String FIELD_WRITE_LOCK = "$target_wl";
   public static final Type TYPE_LOCK = Type.getType(Lock.class);
   public static final Type TYPE_BEAN_LOCATOR = Type.getType(BeanLocator.class);
   public static final Type TYPE_DEFAULT_BEAN_CONTEXT = Type.getType(DefaultBeanContext.class);
   private static final Method METHOD_PROXY_TARGET_TYPE = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(ProxyBeanDefinition.class, "getTargetDefinitionType")
   );
   private static final Method METHOD_PROXY_TARGET_CLASS = Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(ProxyBeanDefinition.class, "getTargetType")
   );
   private static final java.lang.reflect.Method RESOLVE_INTRODUCTION_INTERCEPTORS_METHOD = ReflectionUtils.getRequiredInternalMethod(
      InterceptorChain.class, "resolveIntroductionInterceptors", BeanContext.class, ExecutableMethod.class, List.class
   );
   private static final java.lang.reflect.Method RESOLVE_AROUND_INTERCEPTORS_METHOD = ReflectionUtils.getRequiredInternalMethod(
      InterceptorChain.class, "resolveAroundInterceptors", BeanContext.class, ExecutableMethod.class, List.class
   );
   private static final Constructor CONSTRUCTOR_METHOD_INTERCEPTOR_CHAIN = (Constructor)ReflectionUtils.findConstructor(
         MethodInterceptorChain.class, Interceptor[].class, Object.class, ExecutableMethod.class, Object[].class
      )
      .orElseThrow(() -> new IllegalStateException("new MethodInterceptorChain(..) constructor not found. Incompatible version of Micronaut?"));
   private static final Constructor CONSTRUCTOR_METHOD_INTERCEPTOR_CHAIN_NO_PARAMS = (Constructor)ReflectionUtils.findConstructor(
         MethodInterceptorChain.class, Interceptor[].class, Object.class, ExecutableMethod.class
      )
      .orElseThrow(() -> new IllegalStateException("new MethodInterceptorChain(..) constructor not found. Incompatible version of Micronaut?"));
   private static final String FIELD_INTERCEPTORS = "$interceptors";
   private static final String FIELD_BEAN_LOCATOR = "$beanLocator";
   private static final String FIELD_BEAN_QUALIFIER = "$beanQualifier";
   private static final String FIELD_PROXY_METHODS = "$proxyMethods";
   private static final Type FIELD_TYPE_PROXY_METHODS = Type.getType(ExecutableMethod[].class);
   private static final Type EXECUTABLE_METHOD_TYPE = Type.getType(ExecutableMethod.class);
   private static final Type INTERCEPTOR_ARRAY_TYPE = Type.getType(Interceptor[].class);
   private final String packageName;
   private final String targetClassShortName;
   private final ClassWriter classWriter;
   private final String targetClassFullName;
   private final String proxyFullName;
   private final BeanDefinitionWriter proxyBeanDefinitionWriter;
   private final String proxyInternalName;
   private final Set<AnnotationValue<?>> interceptorBinding;
   private final Set<ClassElement> interfaceTypes;
   private final Type proxyType;
   private final boolean hotswap;
   private final boolean lazy;
   private final boolean cacheLazyTarget;
   private final boolean isInterface;
   private final BeanDefinitionWriter parentWriter;
   private final boolean isIntroduction;
   private final boolean implementInterface;
   private boolean isProxyTarget;
   private MethodVisitor constructorWriter;
   private final List<AopProxyWriter.MethodRef> proxiedMethods = new ArrayList();
   private final Set<AopProxyWriter.MethodRef> proxiedMethodsRefSet = new HashSet();
   private final List<AopProxyWriter.MethodRef> proxyTargetMethods = new ArrayList();
   private int proxyMethodCount = 0;
   private GeneratorAdapter constructorGenerator;
   private int interceptorArgumentIndex;
   private int beanResolutionContextArgumentIndex = -1;
   private int beanContextArgumentIndex = -1;
   private int qualifierIndex;
   private final List<Runnable> deferredInjectionPoints = new ArrayList();
   private boolean constructorRequiresReflection;
   private MethodElement declaredConstructor;
   private MethodElement newConstructor;
   private ParameterElement interceptorParameter;
   private ParameterElement qualifierParameter;
   private VisitorContext visitorContext;

   public AopProxyWriter(
      BeanDefinitionWriter parent,
      OptionalValues<Boolean> settings,
      ConfigurationMetadataBuilder<?> metadataBuilder,
      VisitorContext visitorContext,
      AnnotationValue<?>... interceptorBinding
   ) {
      super(parent.getOriginatingElements());
      this.isIntroduction = false;
      this.implementInterface = true;
      this.parentWriter = parent;
      this.isProxyTarget = settings.get(Interceptor.PROXY_TARGET).orElse(false) || parent.isInterface();
      this.hotswap = this.isProxyTarget && settings.get(Interceptor.HOTSWAP).orElse(false);
      this.lazy = this.isProxyTarget && settings.get(Interceptor.LAZY).orElse(false);
      this.cacheLazyTarget = this.lazy && settings.get(Interceptor.CACHEABLE_LAZY_TARGET).orElse(false);
      this.isInterface = parent.isInterface();
      this.packageName = parent.getPackageName();
      this.targetClassShortName = parent.getBeanSimpleName();
      this.targetClassFullName = this.packageName + '.' + this.targetClassShortName;
      this.classWriter = new ClassWriter(3);
      this.proxyFullName = parent.getBeanDefinitionName() + "$Intercepted";
      this.proxyInternalName = getInternalName(this.proxyFullName);
      this.proxyType = getTypeReferenceForName(this.proxyFullName, new String[0]);
      this.interceptorBinding = this.toInterceptorBindingMap(interceptorBinding);
      this.interfaceTypes = Collections.emptySet();
      ClassElement aopElement = ClassElement.of(this.proxyFullName, this.isInterface, parent.getAnnotationMetadata());
      this.proxyBeanDefinitionWriter = new BeanDefinitionWriter(aopElement, parent, metadataBuilder, visitorContext);
      this.startClass(this.classWriter, getInternalName(this.proxyFullName), getTypeReferenceForName(this.targetClassFullName, new String[0]));
      this.proxyBeanDefinitionWriter.setInterceptedType(this.targetClassFullName);
   }

   public AopProxyWriter(
      String packageName,
      String className,
      boolean isInterface,
      Element originatingElement,
      AnnotationMetadata annotationMetadata,
      ClassElement[] interfaceTypes,
      VisitorContext visitorContext,
      ConfigurationMetadataBuilder<?> metadataBuilder,
      ConfigurationMetadata configurationMetadata,
      AnnotationValue<?>... interceptorBinding
   ) {
      this(
         packageName,
         className,
         isInterface,
         true,
         originatingElement,
         annotationMetadata,
         interfaceTypes,
         visitorContext,
         metadataBuilder,
         configurationMetadata,
         interceptorBinding
      );
   }

   public AopProxyWriter(
      String packageName,
      String className,
      boolean isInterface,
      boolean implementInterface,
      Element originatingElement,
      AnnotationMetadata annotationMetadata,
      ClassElement[] interfaceTypes,
      VisitorContext visitorContext,
      ConfigurationMetadataBuilder<?> metadataBuilder,
      ConfigurationMetadata configurationMetadata,
      AnnotationValue<?>... interceptorBinding
   ) {
      super(OriginatingElements.of(originatingElement));
      this.isIntroduction = true;
      this.implementInterface = implementInterface;
      if (!implementInterface && ArrayUtils.isEmpty(interfaceTypes)) {
         throw new IllegalArgumentException(
            "if argument implementInterface is false at least one interface should be provided to the 'interfaceTypes' argument"
         );
      } else {
         this.packageName = packageName;
         this.isInterface = isInterface;
         this.hotswap = false;
         this.lazy = false;
         this.cacheLazyTarget = false;
         this.targetClassShortName = className;
         this.targetClassFullName = packageName + '.' + this.targetClassShortName;
         this.parentWriter = null;
         this.proxyFullName = this.targetClassFullName + "$Intercepted";
         this.proxyInternalName = getInternalName(this.proxyFullName);
         this.proxyType = getTypeReferenceForName(this.proxyFullName, new String[0]);
         this.interceptorBinding = this.toInterceptorBindingMap(interceptorBinding);
         this.interfaceTypes = (Set<ClassElement>)(interfaceTypes != null ? new LinkedHashSet(Arrays.asList(interfaceTypes)) : Collections.emptySet());
         this.classWriter = new ClassWriter(3);
         if (configurationMetadata != null) {
            String existingPrefix = (String)annotationMetadata.stringValue(ConfigurationReader.class, "prefix").orElse("");
            String computedPrefix = StringUtils.isNotEmpty(existingPrefix)
               ? existingPrefix + "." + configurationMetadata.getName()
               : configurationMetadata.getName();
            annotationMetadata = DefaultAnnotationMetadata.mutateMember(annotationMetadata, ConfigurationReader.class.getName(), "prefix", computedPrefix);
         }

         ClassElement aopElement = ClassElement.of(this.proxyFullName, isInterface, annotationMetadata);
         this.proxyBeanDefinitionWriter = new BeanDefinitionWriter(aopElement, this, metadataBuilder, visitorContext);
         if (isInterface) {
            if (implementInterface) {
               this.proxyBeanDefinitionWriter.setInterceptedType(this.targetClassFullName);
            }
         } else {
            this.proxyBeanDefinitionWriter.setInterceptedType(this.targetClassFullName);
         }

         this.startClass(this.classWriter, this.proxyInternalName, getTypeReferenceForName(this.targetClassFullName, new String[0]));
      }
   }

   @Override
   public boolean isEnabled() {
      return this.proxyBeanDefinitionWriter.isEnabled();
   }

   public boolean isProxyTarget() {
      return this.isProxyTarget;
   }

   @Override
   protected void startClass(ClassVisitor classWriter, String className, Type superType) {
      String[] interfaces = this.getImplementedInterfaceInternalNames();
      classWriter.visit(52, 4096, className, null, !this.isInterface ? superType.getInternalName() : null, interfaces);
      classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
      classWriter.visitField(18, "$interceptors", FIELD_TYPE_INTERCEPTORS.getDescriptor(), null, null);
      classWriter.visitField(18, "$proxyMethods", FIELD_TYPE_PROXY_METHODS.getDescriptor(), null, null);
   }

   private String[] getImplementedInterfaceInternalNames() {
      return (String[])this.interfaceTypes.stream().map(o -> JavaModelUtils.getTypeReference(o).getInternalName()).toArray(x$0 -> new String[x$0]);
   }

   @Deprecated
   @Override
   public Element getOriginatingElement() {
      return this.proxyBeanDefinitionWriter.getOriginatingElement();
   }

   @Override
   public void visitBeanFactoryMethod(ClassElement factoryClass, MethodElement factoryMethod) {
      this.proxyBeanDefinitionWriter.visitBeanFactoryMethod(factoryClass, factoryMethod);
   }

   @Override
   public void visitBeanFactoryMethod(ClassElement factoryClass, MethodElement factoryMethod, ParameterElement[] parameters) {
      this.proxyBeanDefinitionWriter.visitBeanFactoryMethod(factoryClass, factoryMethod, parameters);
   }

   @Override
   public void visitBeanFactoryField(ClassElement factoryClass, FieldElement factoryField) {
      this.proxyBeanDefinitionWriter.visitBeanFactoryField(factoryClass, factoryField);
   }

   @Override
   public boolean isSingleton() {
      return this.proxyBeanDefinitionWriter.isSingleton();
   }

   @Override
   public boolean isInterface() {
      return this.isInterface;
   }

   @Override
   public void visitBeanDefinitionInterface(Class<? extends BeanDefinition> interfaceType) {
      this.proxyBeanDefinitionWriter.visitBeanDefinitionInterface(interfaceType);
   }

   @Override
   public String getBeanTypeName() {
      return this.proxyBeanDefinitionWriter.getBeanTypeName();
   }

   @Override
   public Type getProvidedType() {
      return this.proxyBeanDefinitionWriter.getProvidedType();
   }

   @Override
   public void setValidated(boolean validated) {
      this.proxyBeanDefinitionWriter.setValidated(validated);
   }

   @Override
   public void setInterceptedType(String typeName) {
      this.proxyBeanDefinitionWriter.setInterceptedType(typeName);
   }

   @Override
   public Optional<Type> getInterceptedType() {
      return this.proxyBeanDefinitionWriter.getInterceptedType();
   }

   @Override
   public boolean isValidated() {
      return this.proxyBeanDefinitionWriter.isValidated();
   }

   @Override
   public String getBeanDefinitionName() {
      return this.proxyBeanDefinitionWriter.getBeanDefinitionName();
   }

   @Override
   public void visitBeanDefinitionConstructor(MethodElement constructor, boolean requiresReflection, VisitorContext visitorContext) {
      this.constructorRequiresReflection = requiresReflection;
      this.declaredConstructor = constructor;
      this.visitorContext = visitorContext;
      AnnotationValue<?>[] interceptorTypes = InterceptedMethodUtil.resolveInterceptorBinding(
         constructor.getAnnotationMetadata(), InterceptorKind.AROUND_CONSTRUCT
      );
      this.visitInterceptorBinding(interceptorTypes);
   }

   @Override
   public void visitDefaultConstructor(AnnotationMetadata annotationMetadata, VisitorContext visitorContext) {
      this.constructorRequiresReflection = false;
      ClassElement classElement = ClassElement.of(this.proxyType.getClassName());
      this.declaredConstructor = MethodElement.of(classElement, annotationMetadata, classElement, classElement, "<init>");
   }

   private void initConstructor(MethodElement constructor) {
      ClassElement interceptorList = ClassElement.of(
         List.class,
         AnnotationMetadata.EMPTY_METADATA,
         Collections.singletonMap(
            "E", ClassElement.of(BeanRegistration.class, AnnotationMetadata.EMPTY_METADATA, Collections.singletonMap("T", ClassElement.of(Interceptor.class)))
         )
      );
      this.interceptorParameter = ParameterElement.of(interceptorList, "$interceptors");
      this.qualifierParameter = ParameterElement.of(Qualifier.class, "$qualifier");
      ClassElement proxyClass = ClassElement.of(this.proxyType.getClassName());
      ParameterElement[] constructorParameters = constructor.getParameters();
      List<ParameterElement> newConstructorParameters = new ArrayList(constructorParameters.length + 4);
      newConstructorParameters.addAll(Arrays.asList(constructorParameters));
      newConstructorParameters.add(ParameterElement.of(BeanResolutionContext.class, "$beanResolutionContext"));
      newConstructorParameters.add(ParameterElement.of(BeanContext.class, "$beanContext"));
      newConstructorParameters.add(this.qualifierParameter);
      newConstructorParameters.add(this.interceptorParameter);
      this.newConstructor = MethodElement.of(
         proxyClass,
         constructor.getAnnotationMetadata(),
         proxyClass,
         proxyClass,
         "<init>",
         (ParameterElement[])newConstructorParameters.toArray(new ParameterElement[0])
      );
      this.beanResolutionContextArgumentIndex = constructorParameters.length;
      this.beanContextArgumentIndex = constructorParameters.length + 1;
      this.qualifierIndex = constructorParameters.length + 2;
      this.interceptorArgumentIndex = constructorParameters.length + 3;
   }

   @NonNull
   @Override
   public String getBeanDefinitionReferenceClassName() {
      return this.proxyBeanDefinitionWriter.getBeanDefinitionReferenceClassName();
   }

   public void visitIntroductionMethod(TypedElement declaringBean, MethodElement methodElement) {
      this.visitAroundMethod(declaringBean, methodElement);
   }

   public void visitAroundMethod(TypedElement beanType, MethodElement methodElement) {
      ClassElement returnType = methodElement.isSuspend() ? ClassElement.of(Object.class) : methodElement.getReturnType();
      Type returnTypeObject = JavaModelUtils.getTypeReference(returnType);
      boolean isPrimitive = returnType.isPrimitive();
      boolean isVoidReturn = isPrimitive && returnTypeObject.equals(Type.VOID_TYPE);
      Optional<MethodElement> overridden = methodElement.getOwningType()
         .getEnclosedElement(
            ElementQuery.ALL_METHODS
               .onlyInstance()
               .named((Predicate<String>)(name -> name.equals(methodElement.getName())))
               .filter(el -> el.overrides(methodElement))
         );
      if (overridden.isPresent()) {
         MethodElement overriddenBy = (MethodElement)overridden.get();
         String methodElementKey = methodElement.getName()
            + (String)Arrays.stream(methodElement.getSuspendParameters()).map(p -> p.getType().getName()).collect(Collectors.joining(","));
         String overriddenByKey = overriddenBy.getName()
            + (String)Arrays.stream(methodElement.getSuspendParameters()).map(p -> p.getGenericType().getName()).collect(Collectors.joining(","));
         if (!methodElementKey.equals(overriddenByKey)) {
            this.buildMethodDelegate(methodElement, overriddenBy, isVoidReturn);
            return;
         }
      }

      String methodName = methodElement.getName();
      List<ParameterElement> argumentTypeList = Arrays.asList(methodElement.getSuspendParameters());
      int argumentCount = argumentTypeList.size();
      Type declaringTypeReference = JavaModelUtils.getTypeReference(beanType);
      AopProxyWriter.MethodRef methodKey = new AopProxyWriter.MethodRef(methodName, argumentTypeList, returnTypeObject);
      if (!this.proxiedMethodsRefSet.contains(methodKey)) {
         String interceptedProxyClassName = null;
         String interceptedProxyBridgeMethodName = null;
         if (!this.isProxyTarget && (!methodElement.isAbstract() || methodElement.isDefault())) {
            interceptedProxyClassName = this.proxyFullName;
            interceptedProxyBridgeMethodName = "$$access$$" + methodName;
            String bridgeDesc = getMethodDescriptor(returnType, argumentTypeList);
            MethodVisitor bridgeWriter = this.classWriter.visitMethod(4096, interceptedProxyBridgeMethodName, bridgeDesc, null, null);
            GeneratorAdapter bridgeGenerator = new GeneratorAdapter(bridgeWriter, 4096, interceptedProxyBridgeMethodName, bridgeDesc);
            bridgeGenerator.loadThis();

            for(int i = 0; i < argumentTypeList.size(); ++i) {
               bridgeGenerator.loadArg(i);
            }

            String desc = getMethodDescriptor(returnType, argumentTypeList);
            bridgeWriter.visitMethodInsn(183, declaringTypeReference.getInternalName(), methodName, desc, this.isInterface && methodElement.isDefault());
            pushReturnValue(bridgeWriter, returnType);
            bridgeWriter.visitMaxs(13, 1);
            bridgeWriter.visitEnd();
         }

         BeanDefinitionWriter beanDefinitionWriter = this.parentWriter == null ? this.proxyBeanDefinitionWriter : this.parentWriter;
         int methodIndex = beanDefinitionWriter.visitExecutableMethod(beanType, methodElement, interceptedProxyClassName, interceptedProxyBridgeMethodName);
         int index = this.proxyMethodCount++;
         methodKey.methodIndex = methodIndex;
         this.proxiedMethods.add(methodKey);
         this.proxiedMethodsRefSet.add(methodKey);
         this.proxyTargetMethods.add(methodKey);
         this.buildMethodOverride(returnType, methodName, index, argumentTypeList, argumentCount, isVoidReturn);
      }

   }

   private void buildMethodOverride(
      TypedElement returnType, String methodName, int index, List<ParameterElement> argumentTypeList, int argumentCount, boolean isVoidReturn
   ) {
      String desc = getMethodDescriptor(returnType, argumentTypeList);
      MethodVisitor overridden = this.classWriter.visitMethod(1, methodName, desc, null, null);
      GeneratorAdapter overriddenMethodGenerator = new GeneratorAdapter(overridden, 1, methodName, desc);
      overriddenMethodGenerator.loadThis();
      overriddenMethodGenerator.getField(this.proxyType, "$proxyMethods", FIELD_TYPE_PROXY_METHODS);
      overriddenMethodGenerator.push(index);
      overriddenMethodGenerator.visitInsn(50);
      int methodProxyVar = overriddenMethodGenerator.newLocal(EXECUTABLE_METHOD_TYPE);
      overriddenMethodGenerator.storeLocal(methodProxyVar);
      overriddenMethodGenerator.loadThis();
      overriddenMethodGenerator.getField(this.proxyType, "$interceptors", FIELD_TYPE_INTERCEPTORS);
      overriddenMethodGenerator.push(index);
      overriddenMethodGenerator.visitInsn(50);
      int interceptorsLocalVar = overriddenMethodGenerator.newLocal(INTERCEPTOR_ARRAY_TYPE);
      overriddenMethodGenerator.storeLocal(interceptorsLocalVar);
      overriddenMethodGenerator.newInstance(TYPE_METHOD_INTERCEPTOR_CHAIN);
      overriddenMethodGenerator.dup();
      overriddenMethodGenerator.loadLocal(interceptorsLocalVar);
      overriddenMethodGenerator.loadThis();
      if (this.isProxyTarget) {
         if (!this.hotswap && !this.lazy) {
            overriddenMethodGenerator.getField(this.proxyType, "$target", getTypeReferenceForName(this.targetClassFullName, new String[0]));
         } else {
            overriddenMethodGenerator.invokeInterface(Type.getType(InterceptedProxy.class), Method.getMethod("java.lang.Object interceptedTarget()"));
         }
      }

      overriddenMethodGenerator.loadLocal(methodProxyVar);
      if (argumentCount > 0) {
         overriddenMethodGenerator.push(argumentCount);
         overriddenMethodGenerator.newArray(Type.getType(Object.class));

         for(int i = 0; i < argumentCount; ++i) {
            overriddenMethodGenerator.dup();
            ParameterElement argType = (ParameterElement)argumentTypeList.get(i);
            overriddenMethodGenerator.push(i);
            overriddenMethodGenerator.loadArg(i);
            pushBoxPrimitiveIfNecessary(argType, overriddenMethodGenerator);
            overriddenMethodGenerator.visitInsn(83);
         }

         overriddenMethodGenerator.invokeConstructor(TYPE_METHOD_INTERCEPTOR_CHAIN, Method.getMethod(CONSTRUCTOR_METHOD_INTERCEPTOR_CHAIN));
      } else {
         overriddenMethodGenerator.invokeConstructor(TYPE_METHOD_INTERCEPTOR_CHAIN, Method.getMethod(CONSTRUCTOR_METHOD_INTERCEPTOR_CHAIN_NO_PARAMS));
      }

      int chainVar = overriddenMethodGenerator.newLocal(TYPE_METHOD_INTERCEPTOR_CHAIN);
      overriddenMethodGenerator.storeLocal(chainVar);
      overriddenMethodGenerator.loadLocal(chainVar);
      overriddenMethodGenerator.visitMethodInsn(
         182, TYPE_INTERCEPTOR_CHAIN.getInternalName(), "proceed", getMethodDescriptor(Object.class.getName(), new String[0]), false
      );
      if (isVoidReturn) {
         this.returnVoid(overriddenMethodGenerator);
      } else {
         pushCastToType(overriddenMethodGenerator, returnType);
         pushReturnValue(overriddenMethodGenerator, returnType);
      }

      overriddenMethodGenerator.visitMaxs(13, chainVar);
      overriddenMethodGenerator.visitEnd();
   }

   private void buildMethodDelegate(MethodElement methodElement, MethodElement overriddenBy, boolean isVoidReturn) {
      String desc = getMethodDescriptor(methodElement.getReturnType().getType(), Arrays.asList(methodElement.getSuspendParameters()));
      MethodVisitor overridden = this.classWriter.visitMethod(1, methodElement.getName(), desc, null, null);
      GeneratorAdapter overriddenMethodGenerator = new GeneratorAdapter(overridden, 1, methodElement.getName(), desc);
      overriddenMethodGenerator.loadThis();
      int i = 0;

      for(ParameterElement param : methodElement.getSuspendParameters()) {
         overriddenMethodGenerator.loadArg(i++);
         pushCastToType(overriddenMethodGenerator, param.getGenericType());
      }

      overriddenMethodGenerator.visitMethodInsn(
         183,
         this.proxyType.getInternalName(),
         overriddenBy.getName(),
         getMethodDescriptor(overriddenBy.getReturnType().getType(), Arrays.asList(overriddenBy.getSuspendParameters())),
         this.isInterface && overriddenBy.isDefault()
      );
      if (isVoidReturn) {
         overriddenMethodGenerator.returnValue();
      } else {
         ClassElement returnType = overriddenBy.getReturnType();
         pushCastToType(overriddenMethodGenerator, returnType);
         pushReturnValue(overriddenMethodGenerator, overriddenBy.getReturnType());
      }

      overriddenMethodGenerator.visitMaxs(13, 1);
      overriddenMethodGenerator.visitEnd();
   }

   @Override
   public void visitBeanDefinitionEnd() {
      if (this.declaredConstructor == null) {
         throw new IllegalStateException("The method visitBeanDefinitionConstructor(..) should be called at least once");
      } else {
         this.initConstructor(this.declaredConstructor);
         if (this.parentWriter != null && !this.isProxyTarget) {
            this.processAlreadyVisitedMethods(this.parentWriter);
         }

         this.interceptorParameter.annotate("io.micronaut.inject.qualifiers.InterceptorBindingQualifier", builder -> {
            AnnotationValue<?>[] interceptorBinding = (AnnotationValue[])this.interceptorBinding.toArray(new AnnotationValue[0]);
            builder.values(interceptorBinding);
         });
         this.qualifierParameter.annotate("javax.annotation.Nullable");
         String constructorDescriptor = getConstructorDescriptor(Arrays.asList(this.newConstructor.getParameters()));
         ClassWriter proxyClassWriter = this.classWriter;
         this.constructorWriter = proxyClassWriter.visitMethod(1, "<init>", constructorDescriptor, null, null);
         this.constructorGenerator = new GeneratorAdapter(this.constructorWriter, 1, "<init>", constructorDescriptor);
         GeneratorAdapter proxyConstructorGenerator = this.constructorGenerator;
         proxyConstructorGenerator.loadThis();
         if (this.isInterface) {
            proxyConstructorGenerator.invokeConstructor(TYPE_OBJECT, METHOD_DEFAULT_CONSTRUCTOR);
         } else {
            ParameterElement[] existingArguments = this.declaredConstructor.getParameters();

            for(int i = 0; i < existingArguments.length; ++i) {
               proxyConstructorGenerator.loadArg(i);
            }

            String superConstructorDescriptor = getConstructorDescriptor(Arrays.asList(existingArguments));
            proxyConstructorGenerator.invokeConstructor(
               getTypeReferenceForName(this.targetClassFullName, new String[0]), new Method("<init>", superConstructorDescriptor)
            );
         }

         this.proxyBeanDefinitionWriter.visitBeanDefinitionConstructor(this.newConstructor, this.constructorRequiresReflection, this.visitorContext);
         GeneratorAdapter targetDefinitionGenerator = null;
         GeneratorAdapter targetTypeGenerator = null;
         if (this.parentWriter != null) {
            this.proxyBeanDefinitionWriter.visitBeanDefinitionInterface(ProxyBeanDefinition.class);
            ClassVisitor pcw = this.proxyBeanDefinitionWriter.getClassWriter();
            targetDefinitionGenerator = new GeneratorAdapter(
               pcw.visitMethod(1, METHOD_PROXY_TARGET_TYPE.getName(), METHOD_PROXY_TARGET_TYPE.getDescriptor(), null, null),
               1,
               METHOD_PROXY_TARGET_TYPE.getName(),
               METHOD_PROXY_TARGET_TYPE.getDescriptor()
            );
            targetDefinitionGenerator.loadThis();
            targetDefinitionGenerator.push(getTypeReferenceForName(this.parentWriter.getBeanDefinitionName(), new String[0]));
            targetDefinitionGenerator.returnValue();
            targetTypeGenerator = new GeneratorAdapter(
               pcw.visitMethod(1, METHOD_PROXY_TARGET_CLASS.getName(), METHOD_PROXY_TARGET_CLASS.getDescriptor(), null, null),
               1,
               METHOD_PROXY_TARGET_CLASS.getName(),
               METHOD_PROXY_TARGET_CLASS.getDescriptor()
            );
            targetTypeGenerator.loadThis();
            targetTypeGenerator.push(getTypeReferenceForName(this.parentWriter.getBeanTypeName(), new String[0]));
            targetTypeGenerator.returnValue();
         }

         Class<?> interceptedInterface = this.isIntroduction ? Introduced.class : Intercepted.class;
         Type targetType = getTypeReferenceForName(this.targetClassFullName, new String[0]);
         if (this.isProxyTarget) {
            proxyClassWriter.visitField(18, "$beanLocator", TYPE_BEAN_LOCATOR.getDescriptor(), null, null);
            proxyClassWriter.visitField(2, "$beanQualifier", Type.getType(Qualifier.class).getDescriptor(), null, null);
            this.writeWithQualifierMethod(proxyClassWriter);
            if (!this.lazy || this.cacheLazyTarget) {
               int modifiers = this.hotswap ? 2 : 18;
               proxyClassWriter.visitField(modifiers, "$target", targetType.getDescriptor(), null, null);
            }

            if (this.lazy) {
               interceptedInterface = InterceptedProxy.class;
               proxyClassWriter.visitField(2, "$beanResolutionContext", Type.getType(BeanResolutionContext.class).getDescriptor(), null, null);
            } else {
               interceptedInterface = this.hotswap ? HotSwappableInterceptedProxy.class : InterceptedProxy.class;
               if (this.hotswap) {
                  proxyClassWriter.visitField(18, "$target_rwl", TYPE_READ_WRITE_LOCK.getDescriptor(), null, null);
                  proxyConstructorGenerator.loadThis();
                  this.pushNewInstance(proxyConstructorGenerator, TYPE_READ_WRITE_LOCK);
                  proxyConstructorGenerator.putField(this.proxyType, "$target_rwl", TYPE_READ_WRITE_LOCK);
                  proxyClassWriter.visitField(18, "$target_rl", TYPE_LOCK.getDescriptor(), null, null);
                  proxyConstructorGenerator.loadThis();
                  proxyConstructorGenerator.loadThis();
                  proxyConstructorGenerator.getField(this.proxyType, "$target_rwl", TYPE_READ_WRITE_LOCK);
                  proxyConstructorGenerator.invokeInterface(Type.getType(ReadWriteLock.class), Method.getMethod(Lock.class.getName() + " readLock()"));
                  proxyConstructorGenerator.putField(this.proxyType, "$target_rl", TYPE_LOCK);
                  proxyClassWriter.visitField(18, "$target_wl", Type.getDescriptor(Lock.class), null, null);
                  proxyConstructorGenerator.loadThis();
                  proxyConstructorGenerator.loadThis();
                  proxyConstructorGenerator.getField(this.proxyType, "$target_rwl", TYPE_READ_WRITE_LOCK);
                  proxyConstructorGenerator.invokeInterface(Type.getType(ReadWriteLock.class), Method.getMethod(Lock.class.getName() + " writeLock()"));
                  proxyConstructorGenerator.putField(this.proxyType, "$target_wl", TYPE_LOCK);
               }
            }

            proxyConstructorGenerator.loadThis();
            proxyConstructorGenerator.loadArg(this.beanContextArgumentIndex);
            proxyConstructorGenerator.putField(this.proxyType, "$beanLocator", TYPE_BEAN_LOCATOR);
            proxyConstructorGenerator.loadThis();
            proxyConstructorGenerator.loadArg(this.qualifierIndex);
            proxyConstructorGenerator.putField(this.proxyType, "$beanQualifier", Type.getType(Qualifier.class));
            if (!this.lazy) {
               proxyConstructorGenerator.loadThis();
               this.pushResolveProxyTargetBean(proxyConstructorGenerator, targetType);
               proxyConstructorGenerator.putField(this.proxyType, "$target", targetType);
            } else {
               proxyConstructorGenerator.loadThis();
               proxyConstructorGenerator.loadArg(this.beanResolutionContextArgumentIndex);
               proxyConstructorGenerator.invokeInterface(
                  Type.getType(BeanResolutionContext.class), Method.getMethod(ReflectionUtils.getRequiredMethod(BeanResolutionContext.class, "copy"))
               );
               proxyConstructorGenerator.putField(this.proxyType, "$beanResolutionContext", Type.getType(BeanResolutionContext.class));
            }

            this.writeInterceptedTargetMethod(proxyClassWriter, targetType);
            if (!this.lazy || this.cacheLazyTarget) {
               this.writeHasCachedInterceptedTargetMethod(proxyClassWriter, targetType);
            }

            if (this.hotswap && !this.lazy) {
               this.writeSwapMethod(proxyClassWriter, targetType);
            }
         }

         String[] interfaces = this.getImplementedInterfaceInternalNames();
         if (this.isInterface && this.implementInterface) {
            String[] adviceInterfaces = new String[]{getInternalName(this.targetClassFullName), Type.getInternalName(interceptedInterface)};
            interfaces = ArrayUtils.concat((String[])interfaces, (String[])adviceInterfaces);
         } else {
            String[] adviceInterfaces = new String[]{Type.getInternalName(interceptedInterface)};
            interfaces = ArrayUtils.concat((String[])interfaces, (String[])adviceInterfaces);
         }

         proxyClassWriter.visit(
            52,
            4096,
            this.proxyInternalName,
            null,
            this.isInterface ? TYPE_OBJECT.getInternalName() : getTypeReferenceForName(this.targetClassFullName, new String[0]).getInternalName(),
            interfaces
         );
         proxyConstructorGenerator.loadThis();
         proxyConstructorGenerator.push(this.proxyMethodCount);
         proxyConstructorGenerator.newArray(EXECUTABLE_METHOD_TYPE);
         proxyConstructorGenerator.putField(this.proxyType, "$proxyMethods", FIELD_TYPE_PROXY_METHODS);
         proxyConstructorGenerator.loadThis();
         proxyConstructorGenerator.push(this.proxyMethodCount);
         proxyConstructorGenerator.newArray(INTERCEPTOR_ARRAY_TYPE);
         proxyConstructorGenerator.putField(this.proxyType, "$interceptors", FIELD_TYPE_INTERCEPTORS);
         if (this.isProxyTarget) {
            if (this.proxiedMethods.size() == this.proxyMethodCount) {
               Iterator<AopProxyWriter.MethodRef> iterator = this.proxyTargetMethods.iterator();

               for(int i = 0; i < this.proxyMethodCount; ++i) {
                  AopProxyWriter.MethodRef methodRef = (AopProxyWriter.MethodRef)iterator.next();
                  proxyConstructorGenerator.loadThis();
                  proxyConstructorGenerator.getField(this.proxyType, "$proxyMethods", FIELD_TYPE_PROXY_METHODS);
                  proxyConstructorGenerator.push(i);
                  proxyConstructorGenerator.loadArg(this.beanContextArgumentIndex);
                  this.buildProxyLookupArgument(proxyConstructorGenerator, targetType);
                  proxyConstructorGenerator.loadArg(this.qualifierIndex);
                  pushMethodNameAndTypesArguments(proxyConstructorGenerator, methodRef.name, methodRef.argumentTypes);
                  proxyConstructorGenerator.invokeInterface(Type.getType(ExecutionHandleLocator.class), METHOD_GET_PROXY_TARGET);
                  proxyConstructorGenerator.visitInsn(83);
                  this.pushResolveInterceptorsCall(proxyConstructorGenerator, i, this.isIntroduction);
               }
            }
         } else if (!this.proxiedMethods.isEmpty()) {
            BeanDefinitionWriter beanDefinitionWriter = this.parentWriter == null ? this.proxyBeanDefinitionWriter : this.parentWriter;
            ExecutableMethodsDefinitionWriter executableMethodsDefinitionWriter = beanDefinitionWriter.getExecutableMethodsWriter();
            Type executableMethodsDefinitionType = executableMethodsDefinitionWriter.getClassType();
            proxyConstructorGenerator.newInstance(executableMethodsDefinitionType);
            proxyConstructorGenerator.dup();
            if (executableMethodsDefinitionWriter.isSupportsInterceptedProxy()) {
               proxyConstructorGenerator.push(true);
               proxyConstructorGenerator.invokeConstructor(
                  executableMethodsDefinitionType, new Method("<init>", getConstructorDescriptor(new Class[]{Boolean.TYPE}))
               );
            } else {
               proxyConstructorGenerator.invokeConstructor(executableMethodsDefinitionType, new Method("<init>", "()V"));
            }

            int executableMethodsDefinitionIndex = proxyConstructorGenerator.newLocal(executableMethodsDefinitionType);
            proxyConstructorGenerator.storeLocal(executableMethodsDefinitionIndex, executableMethodsDefinitionType);

            for(int i = 0; i < this.proxyMethodCount; ++i) {
               AopProxyWriter.MethodRef methodRef = (AopProxyWriter.MethodRef)this.proxiedMethods.get(i);
               int methodIndex = methodRef.methodIndex;
               boolean introduction = this.isIntroduction
                  && (
                     executableMethodsDefinitionWriter.isAbstract(methodIndex)
                        || executableMethodsDefinitionWriter.isInterface(methodIndex) && !executableMethodsDefinitionWriter.isDefault(methodIndex)
                  );
               proxyConstructorGenerator.loadThis();
               proxyConstructorGenerator.getField(this.proxyType, "$proxyMethods", FIELD_TYPE_PROXY_METHODS);
               proxyConstructorGenerator.push(i);
               proxyConstructorGenerator.loadLocal(executableMethodsDefinitionIndex);
               proxyConstructorGenerator.push(methodIndex);
               proxyConstructorGenerator.invokeVirtual(executableMethodsDefinitionType, ExecutableMethodsDefinitionWriter.GET_EXECUTABLE_AT_INDEX_METHOD);
               proxyConstructorGenerator.visitInsn(83);
               this.pushResolveInterceptorsCall(proxyConstructorGenerator, i, introduction);
            }
         }

         for(Runnable fieldInjectionPoint : this.deferredInjectionPoints) {
            fieldInjectionPoint.run();
         }

         this.constructorWriter.visitInsn(177);
         this.constructorWriter.visitMaxs(13, 1);
         this.constructorWriter.visitEnd();
         this.proxyBeanDefinitionWriter.visitBeanDefinitionEnd();
         if (targetDefinitionGenerator != null) {
            targetDefinitionGenerator.visitMaxs(1, 1);
            targetDefinitionGenerator.visitEnd();
         }

         if (targetTypeGenerator != null) {
            targetTypeGenerator.visitMaxs(1, 1);
            targetTypeGenerator.visitEnd();
         }

         proxyClassWriter.visitEnd();
      }
   }

   private void pushResolveLazyProxyTargetBean(GeneratorAdapter generatorAdapter, Type targetType) {
      generatorAdapter.loadThis();
      generatorAdapter.getField(this.proxyType, "$beanLocator", TYPE_BEAN_LOCATOR);
      pushCastToType(generatorAdapter, TYPE_DEFAULT_BEAN_CONTEXT);
      generatorAdapter.loadThis();
      generatorAdapter.getField(this.proxyType, "$beanResolutionContext", Type.getType(BeanResolutionContext.class));
      this.buildProxyLookupArgument(generatorAdapter, targetType);
      generatorAdapter.loadThis();
      generatorAdapter.getField(this.proxyType, "$beanQualifier", Type.getType(Qualifier.class));
      generatorAdapter.invokeVirtual(TYPE_DEFAULT_BEAN_CONTEXT, METHOD_GET_PROXY_TARGET_BEAN_WITH_CONTEXT);
      pushCastToType(generatorAdapter, getTypeReferenceForName(this.targetClassFullName, new String[0]));
   }

   private void pushResolveProxyTargetBean(GeneratorAdapter generatorAdapter, Type targetType) {
      generatorAdapter.loadThis();
      generatorAdapter.loadArg(this.beanContextArgumentIndex);
      pushCastToType(generatorAdapter, TYPE_DEFAULT_BEAN_CONTEXT);
      generatorAdapter.loadArg(this.beanResolutionContextArgumentIndex);
      this.buildProxyLookupArgument(generatorAdapter, targetType);
      generatorAdapter.loadThis();
      generatorAdapter.getField(this.proxyType, "$beanQualifier", Type.getType(Qualifier.class));
      generatorAdapter.invokeVirtual(TYPE_DEFAULT_BEAN_CONTEXT, METHOD_GET_PROXY_TARGET_BEAN_WITH_CONTEXT);
      pushCastToType(generatorAdapter, getTypeReferenceForName(this.targetClassFullName, new String[0]));
   }

   private void buildProxyLookupArgument(GeneratorAdapter proxyConstructorGenerator, Type targetType) {
      buildArgumentWithGenerics(
         proxyConstructorGenerator,
         targetType,
         new AnnotationMetadataReference(this.getBeanDefinitionReferenceClassName(), this.getAnnotationMetadata()),
         this.parentWriter != null ? this.parentWriter.getTypeArguments() : this.proxyBeanDefinitionWriter.getTypeArguments()
      );
   }

   @Override
   public void writeTo(File compilationDir) throws IOException {
      this.accept(this.newClassWriterOutputVisitor(compilationDir));
   }

   @NonNull
   @Override
   public ClassElement[] getTypeArguments() {
      return this.proxyBeanDefinitionWriter.getTypeArguments();
   }

   @Override
   public void accept(ClassWriterOutputVisitor visitor) throws IOException {
      this.proxyBeanDefinitionWriter.accept(visitor);
      OutputStream out = visitor.visitClass(this.proxyFullName, this.getOriginatingElements());
      Throwable var3 = null;

      try {
         out.write(this.classWriter.toByteArray());
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (out != null) {
            if (var3 != null) {
               try {
                  out.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               out.close();
            }
         }

      }

   }

   @Override
   public void visitSuperBeanDefinition(String name) {
      this.proxyBeanDefinitionWriter.visitSuperBeanDefinition(name);
   }

   @Override
   public void visitSuperBeanDefinitionFactory(String beanName) {
      this.proxyBeanDefinitionWriter.visitSuperBeanDefinitionFactory(beanName);
   }

   @Override
   public void visitSetterValue(TypedElement declaringType, MethodElement methodElement, boolean requiresReflection, boolean isOptional) {
      this.deferredInjectionPoints
         .add((Runnable)() -> this.proxyBeanDefinitionWriter.visitSetterValue(declaringType, methodElement, requiresReflection, isOptional));
   }

   @Override
   public void visitPostConstructMethod(TypedElement declaringType, MethodElement methodElement, boolean requiresReflection, VisitorContext visitorContext) {
      this.deferredInjectionPoints
         .add((Runnable)() -> this.proxyBeanDefinitionWriter.visitPostConstructMethod(declaringType, methodElement, requiresReflection, visitorContext));
   }

   @Override
   public void visitPreDestroyMethod(TypedElement declaringType, MethodElement methodElement, boolean requiresReflection, VisitorContext visitorContext) {
      this.deferredInjectionPoints
         .add((Runnable)() -> this.proxyBeanDefinitionWriter.visitPreDestroyMethod(declaringType, methodElement, requiresReflection, visitorContext));
   }

   @Override
   public void visitMethodInjectionPoint(TypedElement beanType, MethodElement methodElement, boolean requiresReflection, VisitorContext visitorContext) {
      this.deferredInjectionPoints
         .add((Runnable)() -> this.proxyBeanDefinitionWriter.visitMethodInjectionPoint(beanType, methodElement, requiresReflection, visitorContext));
   }

   @Override
   public int visitExecutableMethod(TypedElement declaringBean, MethodElement methodElement, VisitorContext visitorContext) {
      this.deferredInjectionPoints.add((Runnable)() -> this.proxyBeanDefinitionWriter.visitExecutableMethod(declaringBean, methodElement, visitorContext));
      return -1;
   }

   @Override
   public void visitFieldInjectionPoint(TypedElement declaringType, FieldElement fieldType, boolean requiresReflection) {
      this.deferredInjectionPoints.add((Runnable)() -> this.proxyBeanDefinitionWriter.visitFieldInjectionPoint(declaringType, fieldType, requiresReflection));
   }

   @Override
   public void visitAnnotationMemberPropertyInjectionPoint(
      TypedElement annotationMemberBeanType, String annotationMemberProperty, String requiredValue, String notEqualsValue
   ) {
      this.deferredInjectionPoints
         .add(
            (Runnable)() -> this.proxyBeanDefinitionWriter
                  .visitAnnotationMemberPropertyInjectionPoint(annotationMemberBeanType, annotationMemberProperty, requiredValue, notEqualsValue)
         );
   }

   @Override
   public void visitFieldValue(TypedElement declaringType, FieldElement fieldType, boolean requiresReflection, boolean isOptional) {
      this.deferredInjectionPoints
         .add((Runnable)() -> this.proxyBeanDefinitionWriter.visitFieldValue(declaringType, fieldType, requiresReflection, isOptional));
   }

   @Override
   public String getPackageName() {
      return this.proxyBeanDefinitionWriter.getPackageName();
   }

   @Override
   public String getBeanSimpleName() {
      return this.proxyBeanDefinitionWriter.getBeanSimpleName();
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.proxyBeanDefinitionWriter.getAnnotationMetadata();
   }

   @Override
   public void visitConfigBuilderField(
      ClassElement type, String field, AnnotationMetadata annotationMetadata, ConfigurationMetadataBuilder metadataBuilder, boolean isInterface
   ) {
      this.proxyBeanDefinitionWriter.visitConfigBuilderField(type, field, annotationMetadata, metadataBuilder, isInterface);
   }

   @Override
   public void visitConfigBuilderMethod(
      ClassElement type, String methodName, AnnotationMetadata annotationMetadata, ConfigurationMetadataBuilder metadataBuilder, boolean isInterface
   ) {
      this.proxyBeanDefinitionWriter.visitConfigBuilderMethod(type, methodName, annotationMetadata, metadataBuilder, isInterface);
   }

   @Override
   public void visitConfigBuilderMethod(
      String prefix, ClassElement returnType, String methodName, ClassElement paramType, Map<String, ClassElement> generics, String propertyPath
   ) {
      this.proxyBeanDefinitionWriter.visitConfigBuilderMethod(prefix, returnType, methodName, paramType, generics, propertyPath);
   }

   @Override
   public void visitConfigBuilderDurationMethod(String prefix, ClassElement returnType, String methodName, String propertyPath) {
      this.proxyBeanDefinitionWriter.visitConfigBuilderDurationMethod(prefix, returnType, methodName, propertyPath);
   }

   @Override
   public void visitConfigBuilderEnd() {
      this.proxyBeanDefinitionWriter.visitConfigBuilderEnd();
   }

   @Override
   public void setRequiresMethodProcessing(boolean shouldPreProcess) {
      this.proxyBeanDefinitionWriter.setRequiresMethodProcessing(shouldPreProcess);
   }

   @Override
   public void visitTypeArguments(Map<String, Map<String, ClassElement>> typeArguments) {
      this.proxyBeanDefinitionWriter.visitTypeArguments(typeArguments);
   }

   @Override
   public boolean requiresMethodProcessing() {
      return this.proxyBeanDefinitionWriter.requiresMethodProcessing();
   }

   @Override
   public String getProxiedTypeName() {
      return this.targetClassFullName;
   }

   @Override
   public String getProxiedBeanDefinitionName() {
      return this.parentWriter != null ? this.parentWriter.getBeanDefinitionName() : null;
   }

   public void visitInterceptorBinding(AnnotationValue<?>... interceptorBinding) {
      if (interceptorBinding != null) {
         for(AnnotationValue<?> annotationValue : interceptorBinding) {
            annotationValue.stringValue().ifPresent(annName -> this.interceptorBinding.add(annotationValue));
         }
      }

   }

   private Set<AnnotationValue<?>> toInterceptorBindingMap(AnnotationValue<?>[] interceptorBinding) {
      return new LinkedHashSet(Arrays.asList(interceptorBinding));
   }

   private void readUnlock(GeneratorAdapter interceptedTargetVisitor) {
      this.invokeMethodOnLock(interceptedTargetVisitor, "$target_rl", Method.getMethod("void unlock()"));
   }

   private void readLock(GeneratorAdapter interceptedTargetVisitor) {
      this.invokeMethodOnLock(interceptedTargetVisitor, "$target_rl", Method.getMethod("void lock()"));
   }

   private void writeUnlock(GeneratorAdapter interceptedTargetVisitor) {
      this.invokeMethodOnLock(interceptedTargetVisitor, "$target_wl", Method.getMethod("void unlock()"));
   }

   private void writeLock(GeneratorAdapter interceptedTargetVisitor) {
      this.invokeMethodOnLock(interceptedTargetVisitor, "$target_wl", Method.getMethod("void lock()"));
   }

   private void invokeMethodOnLock(GeneratorAdapter interceptedTargetVisitor, String field, Method method) {
      interceptedTargetVisitor.loadThis();
      interceptedTargetVisitor.getField(this.proxyType, field, TYPE_LOCK);
      interceptedTargetVisitor.invokeInterface(TYPE_LOCK, method);
   }

   private void writeWithQualifierMethod(ClassWriter proxyClassWriter) {
      GeneratorAdapter withQualifierMethod = this.startPublicMethod(
         proxyClassWriter, "$withBeanQualifier", Void.TYPE.getName(), new String[]{Qualifier.class.getName()}
      );
      withQualifierMethod.loadThis();
      withQualifierMethod.loadArg(0);
      withQualifierMethod.putField(this.proxyType, "$beanQualifier", Type.getType(Qualifier.class));
      withQualifierMethod.visitInsn(177);
      withQualifierMethod.visitEnd();
      withQualifierMethod.visitMaxs(1, 1);
   }

   private void writeSwapMethod(ClassWriter proxyClassWriter, Type targetType) {
      GeneratorAdapter swapGenerator = this.startPublicMethod(proxyClassWriter, "swap", targetType.getClassName(), new String[]{targetType.getClassName()});
      Label l0 = new Label();
      Label l1 = new Label();
      Label l2 = new Label();
      swapGenerator.visitTryCatchBlock(l0, l1, l2, null);
      this.writeLock(swapGenerator);
      swapGenerator.visitLabel(l0);
      swapGenerator.loadThis();
      swapGenerator.getField(this.proxyType, "$target", targetType);
      int localRef = swapGenerator.newLocal(targetType);
      swapGenerator.storeLocal(localRef);
      swapGenerator.loadThis();
      swapGenerator.visitVarInsn(25, 1);
      swapGenerator.putField(this.proxyType, "$target", targetType);
      swapGenerator.visitLabel(l1);
      this.writeUnlock(swapGenerator);
      swapGenerator.loadLocal(localRef);
      swapGenerator.returnValue();
      swapGenerator.visitLabel(l2);
      int var = swapGenerator.newLocal(targetType);
      swapGenerator.storeLocal(var);
      this.writeUnlock(swapGenerator);
      swapGenerator.loadLocal(var);
      swapGenerator.throwException();
      swapGenerator.visitMaxs(2, 3);
      swapGenerator.visitEnd();
   }

   private void writeInterceptedTargetMethod(ClassWriter proxyClassWriter, Type targetType) {
      GeneratorAdapter interceptedTargetVisitor = this.startPublicMethod(proxyClassWriter, "interceptedTarget", Object.class.getName(), new String[0]);
      if (this.lazy) {
         if (this.cacheLazyTarget) {
            int targetLocal = interceptedTargetVisitor.newLocal(targetType);
            interceptedTargetVisitor.loadThis();
            interceptedTargetVisitor.getField(this.proxyType, "$target", targetType);
            interceptedTargetVisitor.storeLocal(targetLocal, targetType);
            interceptedTargetVisitor.loadLocal(targetLocal, targetType);
            Label returnLabel = new Label();
            interceptedTargetVisitor.ifNonNull(returnLabel);
            Label synchronizationEnd = new Label();
            interceptedTargetVisitor.loadThis();
            interceptedTargetVisitor.monitorEnter();
            Label tryLabel = new Label();
            Label catchLabel = new Label();
            interceptedTargetVisitor.visitTryCatchBlock(tryLabel, returnLabel, catchLabel, null);
            interceptedTargetVisitor.visitLabel(tryLabel);
            interceptedTargetVisitor.loadThis();
            interceptedTargetVisitor.getField(this.proxyType, "$target", targetType);
            interceptedTargetVisitor.storeLocal(targetLocal, targetType);
            interceptedTargetVisitor.loadLocal(targetLocal, targetType);
            interceptedTargetVisitor.ifNonNull(synchronizationEnd);
            interceptedTargetVisitor.loadThis();
            this.pushResolveLazyProxyTargetBean(interceptedTargetVisitor, targetType);
            interceptedTargetVisitor.putField(this.proxyType, "$target", targetType);
            interceptedTargetVisitor.loadThis();
            interceptedTargetVisitor.push((String)null);
            interceptedTargetVisitor.putField(this.proxyType, "$beanResolutionContext", Type.getType(BeanResolutionContext.class));
            interceptedTargetVisitor.goTo(synchronizationEnd);
            interceptedTargetVisitor.visitLabel(catchLabel);
            interceptedTargetVisitor.loadThis();
            interceptedTargetVisitor.monitorExit();
            interceptedTargetVisitor.throwException();
            interceptedTargetVisitor.visitLabel(synchronizationEnd);
            interceptedTargetVisitor.loadThis();
            interceptedTargetVisitor.monitorExit();
            interceptedTargetVisitor.goTo(returnLabel);
            interceptedTargetVisitor.visitLabel(returnLabel);
            interceptedTargetVisitor.loadThis();
            interceptedTargetVisitor.getField(this.proxyType, "$target", targetType);
            interceptedTargetVisitor.returnValue();
         } else {
            this.pushResolveLazyProxyTargetBean(interceptedTargetVisitor, targetType);
            interceptedTargetVisitor.returnValue();
         }
      } else {
         int localRef = -1;
         Label l1 = null;
         Label l2 = null;
         if (this.hotswap) {
            Label l0 = new Label();
            l1 = new Label();
            l2 = new Label();
            interceptedTargetVisitor.visitTryCatchBlock(l0, l1, l2, null);
            this.readLock(interceptedTargetVisitor);
            interceptedTargetVisitor.visitLabel(l0);
         }

         interceptedTargetVisitor.loadThis();
         interceptedTargetVisitor.getField(this.proxyType, "$target", targetType);
         if (this.hotswap) {
            localRef = interceptedTargetVisitor.newLocal(targetType);
            interceptedTargetVisitor.storeLocal(localRef);
            interceptedTargetVisitor.visitLabel(l1);
            this.readUnlock(interceptedTargetVisitor);
            interceptedTargetVisitor.loadLocal(localRef);
         }

         interceptedTargetVisitor.returnValue();
         if (localRef > -1) {
            interceptedTargetVisitor.visitLabel(l2);
            int var = interceptedTargetVisitor.newLocal(targetType);
            interceptedTargetVisitor.storeLocal(var);
            this.readUnlock(interceptedTargetVisitor);
            interceptedTargetVisitor.loadLocal(var);
            interceptedTargetVisitor.throwException();
         }
      }

      interceptedTargetVisitor.visitMaxs(1, 2);
      interceptedTargetVisitor.visitEnd();
   }

   private void writeHasCachedInterceptedTargetMethod(ClassWriter proxyClassWriter, Type targetType) {
      GeneratorAdapter methodVisitor = this.startPublicMethod(proxyClassWriter, METHOD_HAS_CACHED_INTERCEPTED_METHOD);
      methodVisitor.loadThis();
      methodVisitor.getField(this.proxyType, "$target", targetType);
      Label notNull = new Label();
      methodVisitor.ifNonNull(notNull);
      methodVisitor.push(false);
      methodVisitor.returnValue();
      methodVisitor.visitLabel(notNull);
      methodVisitor.push(true);
      methodVisitor.returnValue();
      methodVisitor.visitMaxs(1, 2);
      methodVisitor.visitEnd();
   }

   private void pushResolveInterceptorsCall(GeneratorAdapter proxyConstructorGenerator, int i, boolean isIntroduction) {
      proxyConstructorGenerator.loadThis();
      proxyConstructorGenerator.getField(this.proxyType, "$interceptors", FIELD_TYPE_INTERCEPTORS);
      proxyConstructorGenerator.push(i);
      proxyConstructorGenerator.loadArg(this.beanContextArgumentIndex);
      proxyConstructorGenerator.loadThis();
      proxyConstructorGenerator.getField(this.proxyType, "$proxyMethods", FIELD_TYPE_PROXY_METHODS);
      proxyConstructorGenerator.push(i);
      proxyConstructorGenerator.visitInsn(50);
      proxyConstructorGenerator.loadArg(this.interceptorArgumentIndex);
      if (isIntroduction) {
         proxyConstructorGenerator.invokeStatic(TYPE_INTERCEPTOR_CHAIN, Method.getMethod(RESOLVE_INTRODUCTION_INTERCEPTORS_METHOD));
      } else {
         proxyConstructorGenerator.invokeStatic(TYPE_INTERCEPTOR_CHAIN, Method.getMethod(RESOLVE_AROUND_INTERCEPTORS_METHOD));
      }

      proxyConstructorGenerator.visitInsn(83);
   }

   private void processAlreadyVisitedMethods(BeanDefinitionWriter parent) {
      for(BeanDefinitionWriter.MethodVisitData methodVisit : parent.getPostConstructMethodVisits()) {
         this.visitPostConstructMethod(methodVisit.getBeanType(), methodVisit.getMethodElement(), methodVisit.isRequiresReflection(), this.visitorContext);
      }

   }

   private static final class MethodRef {
      protected final String name;
      protected final List<ClassElement> argumentTypes;
      protected final Type returnType;
      int methodIndex;
      private final List<String> rawTypes;

      public MethodRef(String name, List<ParameterElement> argumentTypes, Type returnType) {
         this.name = name;
         this.argumentTypes = (List)argumentTypes.stream().map(ParameterElement::getType).collect(Collectors.toList());
         this.rawTypes = (List)this.argumentTypes.stream().map(Element::getName).collect(Collectors.toList());
         this.returnType = returnType;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AopProxyWriter.MethodRef methodRef = (AopProxyWriter.MethodRef)o;
            return Objects.equals(this.name, methodRef.name)
               && Objects.equals(this.rawTypes, methodRef.rawTypes)
               && Objects.equals(this.returnType, methodRef.returnType);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.name, this.rawTypes, this.returnType});
      }
   }
}
