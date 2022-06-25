package io.micronaut.inject.writer;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.MutableAnnotationMetadata;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ConstructorElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.ast.ElementFactory;
import io.micronaut.inject.ast.ElementModifier;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MemberElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.ast.TypedElement;
import io.micronaut.inject.ast.beans.BeanConstructorElement;
import io.micronaut.inject.ast.beans.BeanElementBuilder;
import io.micronaut.inject.ast.beans.BeanFieldElement;
import io.micronaut.inject.ast.beans.BeanMethodElement;
import io.micronaut.inject.ast.beans.BeanParameterElement;
import io.micronaut.inject.configuration.ConfigurationMetadataBuilder;
import io.micronaut.inject.visitor.VisitorContext;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Internal
public abstract class AbstractBeanDefinitionBuilder implements BeanElementBuilder {
   private static final Map<String, AtomicInteger> BEAN_COUNTER = new HashMap(15);
   private static final Predicate<Set<ElementModifier>> PUBLIC_FILTER = elementModifiers -> elementModifiers.contains(ElementModifier.PUBLIC);
   private static final Predicate<Set<ElementModifier>> NON_PUBLIC_FILTER = elementModifiers -> !elementModifiers.contains(ElementModifier.PUBLIC);
   private static final Comparator<MemberElement> SORTER = (o1, o2) -> {
      ClassElement d1 = o1.getDeclaringType();
      ClassElement d2 = o2.getDeclaringType();
      String o1Type = d1.getName();
      String o2Type = d2.getName();
      if (o1Type.equals(o2Type)) {
         return 0;
      } else {
         return d1.isAssignable(d2) ? 1 : -1;
      }
   };
   protected final ConfigurationMetadataBuilder<?> metadataBuilder;
   protected final VisitorContext visitorContext;
   private final Element originatingElement;
   private final ClassElement originatingType;
   private final ClassElement beanType;
   private final int identifier;
   private final MutableAnnotationMetadata annotationMetadata;
   private final List<BeanMethodElement> executableMethods = new ArrayList(5);
   private final List<BeanMethodElement> interceptedMethods = new ArrayList(5);
   private final List<AbstractBeanDefinitionBuilder> childBeans = new ArrayList(5);
   private final List<BeanMethodElement> injectedMethods = new ArrayList(5);
   private final List<BeanMethodElement> preDestroyMethods = new ArrayList(5);
   private final List<BeanMethodElement> postConstructMethods = new ArrayList(5);
   private final List<BeanFieldElement> injectedFields = new ArrayList(5);
   private BeanConstructorElement constructorElement;
   private Map<String, Map<String, ClassElement>> typeArguments;
   private ClassElement[] exposedTypes;
   private boolean intercepted;

   protected AbstractBeanDefinitionBuilder(
      Element originatingElement, ClassElement beanType, ConfigurationMetadataBuilder<?> metadataBuilder, VisitorContext visitorContext
   ) {
      this.originatingElement = originatingElement;
      if (originatingElement instanceof MethodElement) {
         this.originatingType = ((MethodElement)originatingElement).getDeclaringType();
      } else {
         if (!(originatingElement instanceof ClassElement)) {
            throw new IllegalArgumentException("Invalid originating element: " + originatingElement);
         }

         this.originatingType = (ClassElement)originatingElement;
      }

      this.beanType = beanType;
      this.metadataBuilder = metadataBuilder;
      this.visitorContext = visitorContext;
      this.identifier = ((AtomicInteger)BEAN_COUNTER.computeIfAbsent(beanType.getName(), s -> new AtomicInteger(0))).getAndIncrement();
      AnnotationMetadata annotationMetadata = beanType.getAnnotationMetadata();
      if (annotationMetadata instanceof MutableAnnotationMetadata) {
         this.annotationMetadata = ((MutableAnnotationMetadata)annotationMetadata).clone();
      } else {
         this.annotationMetadata = new MutableAnnotationMetadata();
      }

      this.annotationMetadata.addDeclaredAnnotation(Bean.class.getName(), Collections.emptyMap());
      this.constructorElement = this.initConstructor(beanType);
   }

   @Override
   public BeanElementBuilder intercept(AnnotationValue<?>... annotationValue) {
      for(AnnotationValue<?> value : annotationValue) {
         this.annotate(value);
      }

      this.intercepted = true;
      return this;
   }

   @Internal
   public static void writeBeanDefinitionBuilders(ClassWriterOutputVisitor classWriterOutputVisitor, List<AbstractBeanDefinitionBuilder> beanDefinitionBuilders) throws IOException {
      for(AbstractBeanDefinitionBuilder beanDefinitionBuilder : beanDefinitionBuilders) {
         writeBeanDefinition(classWriterOutputVisitor, beanDefinitionBuilder);

         for(AbstractBeanDefinitionBuilder childBean : beanDefinitionBuilder.getChildBeans()) {
            writeBeanDefinition(classWriterOutputVisitor, childBean);
         }
      }

   }

   private static void writeBeanDefinition(ClassWriterOutputVisitor classWriterOutputVisitor, AbstractBeanDefinitionBuilder beanDefinitionBuilder) throws IOException {
      ClassOutputWriter beanDefinitionWriter = beanDefinitionBuilder.build();
      if (beanDefinitionWriter != null) {
         beanDefinitionWriter.accept(classWriterOutputVisitor);
      }

   }

   private AbstractBeanDefinitionBuilder.InternalBeanConstructorElement initConstructor(ClassElement beanType) {
      return (AbstractBeanDefinitionBuilder.InternalBeanConstructorElement)beanType.getPrimaryConstructor()
         .map(m -> new AbstractBeanDefinitionBuilder.InternalBeanConstructorElement(m, !m.isPublic(), this.initBeanParameters(m.getParameters())))
         .orElse(null);
   }

   protected boolean isIntercepted() {
      return this.intercepted || !this.interceptedMethods.isEmpty();
   }

   @Override
   public BeanElementBuilder inject() {
      this.processInjectedMethods();
      this.processInjectedFields();
      return this;
   }

   public List<AbstractBeanDefinitionBuilder> getChildBeans() {
      return this.childBeans;
   }

   private void processInjectedFields() {
      ElementQuery<FieldElement> baseQuery = ElementQuery.ALL_FIELDS.onlyInstance().onlyInjected();
      Set<FieldElement> accessibleFields = new HashSet();
      this.beanType.getEnclosedElements(baseQuery.modifiers(PUBLIC_FILTER)).forEach(fieldElement -> {
         accessibleFields.add(fieldElement);
         new AbstractBeanDefinitionBuilder.InternalBeanElementField(fieldElement, false).inject();
      });
      this.beanType.getEnclosedElements(baseQuery.modifiers(NON_PUBLIC_FILTER)).forEach(fieldElement -> {
         if (!accessibleFields.contains(fieldElement)) {
            new AbstractBeanDefinitionBuilder.InternalBeanElementField(fieldElement, true).inject();
         }

      });
   }

   private void processInjectedMethods() {
      ElementQuery<MethodElement> baseQuery = ElementQuery.ALL_METHODS.onlyInstance().onlyConcrete().onlyInjected();
      Set<MethodElement> accessibleMethods = new HashSet();
      this.beanType.getEnclosedElements(baseQuery.modifiers(PUBLIC_FILTER)).forEach(methodElement -> {
         accessibleMethods.add(methodElement);
         this.handleMethod(methodElement, false);
      });
      this.beanType.getEnclosedElements(baseQuery.modifiers(NON_PUBLIC_FILTER)).forEach(methodElement -> {
         if (!accessibleMethods.contains(methodElement)) {
            this.handleMethod(methodElement, true);
         }

      });
   }

   private void handleMethod(MethodElement methodElement, boolean requiresReflection) {
      boolean lifecycleMethod = false;
      if (methodElement.getAnnotationMetadata().hasDeclaredAnnotation("javax.annotation.PreDestroy")) {
         new AbstractBeanDefinitionBuilder.InternalBeanElementMethod(methodElement, requiresReflection).preDestroy();
         lifecycleMethod = true;
      }

      if (methodElement.getAnnotationMetadata().hasDeclaredAnnotation("javax.annotation.PostConstruct")) {
         new AbstractBeanDefinitionBuilder.InternalBeanElementMethod(methodElement, requiresReflection).postConstruct();
         lifecycleMethod = true;
      }

      if (!lifecycleMethod) {
         new AbstractBeanDefinitionBuilder.InternalBeanElementMethod(methodElement, requiresReflection).inject();
      }

   }

   @NonNull
   @Override
   public Element getOriginatingElement() {
      return this.originatingElement;
   }

   @NonNull
   @Override
   public ClassElement getBeanType() {
      return this.beanType;
   }

   protected final BeanParameterElement[] initBeanParameters(@NonNull ParameterElement[] constructorParameters) {
      return ArrayUtils.isNotEmpty(constructorParameters)
         ? (BeanParameterElement[])Arrays.stream(constructorParameters)
            .map(x$0 -> new AbstractBeanDefinitionBuilder.InternalBeanParameter(x$0))
            .toArray(x$0 -> new BeanParameterElement[x$0])
         : new BeanParameterElement[0];
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @NonNull
   @Override
   public BeanElementBuilder createWith(@NonNull MethodElement element) {
      if (element != null) {
         this.constructorElement = new AbstractBeanDefinitionBuilder.InternalBeanConstructorElement(
            element, !element.isPublic(), this.initBeanParameters(element.getParameters())
         );
      }

      return this;
   }

   @NonNull
   @Override
   public BeanElementBuilder typed(ClassElement... types) {
      if (ArrayUtils.isNotEmpty(types)) {
         this.exposedTypes = types;
      }

      return this;
   }

   @NonNull
   @Override
   public BeanElementBuilder typeArguments(@NonNull ClassElement... types) {
      Map<String, ClassElement> typeArguments = this.beanType.getTypeArguments();
      Map<String, ClassElement> resolvedTypes = this.resolveTypeArguments(typeArguments, types);
      if (resolvedTypes != null) {
         if (this.typeArguments == null) {
            this.typeArguments = new LinkedHashMap();
         }

         this.typeArguments.put(this.beanType.getName(), typeArguments);
      }

      return this;
   }

   @NonNull
   @Override
   public BeanElementBuilder typeArgumentsForType(ClassElement type, @NonNull ClassElement... types) {
      if (type != null) {
         Map<String, ClassElement> typeArguments = type.getTypeArguments();
         Map<String, ClassElement> resolvedTypes = this.resolveTypeArguments(typeArguments, types);
         if (resolvedTypes != null) {
            if (this.typeArguments == null) {
               this.typeArguments = new LinkedHashMap();
            }

            this.typeArguments.put(type.getName(), resolvedTypes);
         }
      }

      return this;
   }

   @Nullable
   private Map<String, ClassElement> resolveTypeArguments(Map<String, ClassElement> typeArguments, ClassElement... types) {
      Map<String, ClassElement> resolvedTypes = null;
      if (typeArguments.size() == types.length) {
         resolvedTypes = new LinkedHashMap(typeArguments.size());
         Iterator<String> i = typeArguments.keySet().iterator();

         for(ClassElement type : types) {
            String variable = (String)i.next();
            resolvedTypes.put(variable, type);
         }
      }

      return resolvedTypes;
   }

   @Override
   public BeanElementBuilder withConstructor(Consumer<BeanConstructorElement> constructorElement) {
      if (constructorElement != null && this.constructorElement != null) {
         constructorElement.accept(this.constructorElement);
      }

      return this;
   }

   @NonNull
   @Override
   public BeanElementBuilder withMethods(@NonNull ElementQuery<MethodElement> methods, @NonNull Consumer<BeanMethodElement> beanMethods) {
      if (methods != null && beanMethods != null) {
         ElementQuery<MethodElement> baseQuery = methods.onlyInstance();
         this.beanType
            .getEnclosedElements(baseQuery.modifiers(m -> m.contains(ElementModifier.PUBLIC)))
            .forEach(methodElement -> beanMethods.accept(new AbstractBeanDefinitionBuilder.InternalBeanElementMethod(methodElement, false)));
         this.beanType
            .getEnclosedElements(baseQuery.modifiers(m -> !m.contains(ElementModifier.PUBLIC)))
            .forEach(methodElement -> beanMethods.accept(new AbstractBeanDefinitionBuilder.InternalBeanElementMethod(methodElement, true)));
      }

      return this;
   }

   @NonNull
   @Override
   public BeanElementBuilder withFields(@NonNull ElementQuery<FieldElement> fields, @NonNull Consumer<BeanFieldElement> beanFields) {
      if (fields != null && beanFields != null) {
         this.beanType
            .getEnclosedElements(fields.onlyInstance().onlyAccessible(this.originatingType))
            .forEach(fieldElement -> beanFields.accept(new AbstractBeanDefinitionBuilder.InternalBeanElementField(fieldElement, false)));
      }

      return this;
   }

   @NonNull
   @Override
   public BeanElementBuilder withParameters(Consumer<BeanParameterElement[]> parameters) {
      if (parameters != null && this.constructorElement != null) {
         parameters.accept(this.getParameters());
      }

      return this;
   }

   @NonNull
   protected BeanParameterElement[] getParameters() {
      return this.constructorElement.getParameters();
   }

   @NonNull
   @Override
   public String getName() {
      return this.beanType.getName();
   }

   @Override
   public boolean isProtected() {
      return this.beanType.isProtected();
   }

   @Override
   public boolean isPublic() {
      return this.beanType.isPublic();
   }

   @NonNull
   @Override
   public Object getNativeType() {
      return this.beanType;
   }

   @NonNull
   @Override
   public <T extends Annotation> BeanElementBuilder annotate(@NonNull String annotationType, @NonNull Consumer<AnnotationValueBuilder<T>> consumer) {
      this.annotate(this.annotationMetadata, annotationType, consumer);
      return this;
   }

   @Override
   public <T extends Annotation> Element annotate(AnnotationValue<T> annotationValue) {
      this.annotate(this.annotationMetadata, annotationValue);
      return this;
   }

   @Override
   public BeanElementBuilder removeAnnotation(@NonNull String annotationType) {
      this.removeAnnotation(this.annotationMetadata, annotationType);
      return this;
   }

   @Override
   public <T extends Annotation> BeanElementBuilder removeAnnotationIf(@NonNull Predicate<AnnotationValue<T>> predicate) {
      this.removeAnnotationIf(this.annotationMetadata, predicate);
      return this;
   }

   @Override
   public BeanElementBuilder removeStereotype(@NonNull String annotationType) {
      this.removeStereotype(this.annotationMetadata, annotationType);
      return this;
   }

   private BeanElementBuilder addChildBean(@NonNull MethodElement producerMethod, Consumer<BeanElementBuilder> childBeanBuilder) {
      AbstractBeanDefinitionBuilder childBuilder = this.createChildBean(producerMethod);
      this.childBeans.add(childBuilder);
      if (childBeanBuilder != null) {
         childBeanBuilder.accept(childBuilder);
      }

      return this;
   }

   private BeanElementBuilder addChildBean(@NonNull FieldElement producerMethod, Consumer<BeanElementBuilder> childBeanBuilder) {
      AbstractBeanDefinitionBuilder childBuilder = this.createChildBean(producerMethod);
      this.childBeans.add(childBuilder);
      if (childBeanBuilder != null) {
         childBeanBuilder.accept(childBuilder);
      }

      return this;
   }

   @Override
   public <E extends MemberElement> BeanElementBuilder produceBeans(ElementQuery<E> methodsOrFields, Consumer<BeanElementBuilder> childBeanBuilder) {
      methodsOrFields = methodsOrFields.onlyConcrete().onlyInstance().modifiers(modifiers -> modifiers.contains(ElementModifier.PUBLIC));

      for(E enclosedElement : this.beanType.getEnclosedElements(methodsOrFields)) {
         if (enclosedElement instanceof FieldElement) {
            FieldElement fe = (FieldElement)enclosedElement;
            ClassElement type = fe.getGenericField().getType();
            if (type.isPublic() && !type.isPrimitive()) {
               return this.addChildBean(fe, childBeanBuilder);
            }
         }

         if (enclosedElement instanceof MethodElement && !(enclosedElement instanceof ConstructorElement)) {
            MethodElement me = (MethodElement)enclosedElement;
            ClassElement type = me.getGenericReturnType().getType();
            if (type.isPublic() && !type.isPrimitive()) {
               return this.addChildBean(me, childBeanBuilder);
            }
         }
      }

      return this;
   }

   @NonNull
   protected abstract AbstractBeanDefinitionBuilder createChildBean(FieldElement producerField);

   protected void visitInterceptedMethods(BiConsumer<TypedElement, MethodElement> consumer) {
      if (consumer != null) {
         ClassElement beanClass = this.getBeanType();
         if (CollectionUtils.isNotEmpty(this.interceptedMethods)) {
            for(BeanMethodElement interceptedMethod : this.interceptedMethods) {
               this.handleMethod(beanClass, interceptedMethod, consumer);
            }
         }

         if (this.intercepted) {
            beanClass.getEnclosedElements(
                  ElementQuery.ALL_METHODS.onlyInstance().modifiers(mods -> !mods.contains(ElementModifier.FINAL) && mods.contains(ElementModifier.PUBLIC))
               )
               .forEach(method -> {
                  AbstractBeanDefinitionBuilder.InternalBeanElementMethod ibem = new AbstractBeanDefinitionBuilder.InternalBeanElementMethod(method, true);
                  if (!this.interceptedMethods.contains(ibem)) {
                     this.handleMethod(beanClass, ibem, consumer);
                  }
   
               });
         }
      }

   }

   private void handleMethod(ClassElement beanClass, MethodElement method, BiConsumer<TypedElement, MethodElement> consumer) {
      ElementFactory elementFactory = this.visitorContext.getElementFactory();
      AnnotationMetadataHierarchy fusedMetadata = new AnnotationMetadataHierarchy(this.getAnnotationMetadata(), method.getAnnotationMetadata());
      MethodElement finalMethod = elementFactory.newMethodElement(beanClass, method.getNativeType(), fusedMetadata);
      consumer.accept(beanClass, finalMethod);
   }

   @NonNull
   protected abstract AbstractBeanDefinitionBuilder createChildBean(MethodElement producerMethod);

   @Nullable
   public BeanClassWriter build() {
      final BeanClassWriter beanWriter = this.buildBeanClassWriter();
      if (beanWriter == null) {
         return null;
      } else {
         final BeanDefinitionVisitor parentVisitor = beanWriter.getBeanDefinitionVisitor();
         final AnnotationMetadata thisAnnotationMetadata = this.getAnnotationMetadata();
         return this.isIntercepted() && parentVisitor instanceof BeanDefinitionWriter ? new BeanClassWriter() {
            @Override
            public BeanDefinitionVisitor getBeanDefinitionVisitor() {
               return parentVisitor;
            }

            @Override
            public void accept(ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {
               BeanDefinitionWriter beanDefinitionWriter = (BeanDefinitionWriter)parentVisitor;
               BeanDefinitionVisitor aopProxyWriter = AbstractBeanDefinitionBuilder.this.createAopWriter(beanDefinitionWriter, thisAnnotationMetadata);
               if (!AbstractBeanDefinitionBuilder.this.configureBeanVisitor(aopProxyWriter)) {
                  AbstractBeanDefinitionBuilder.this.configureInjectionPoints(aopProxyWriter);
                  AbstractBeanDefinitionBuilder.this.visitInterceptedMethods(AbstractBeanDefinitionBuilder.this.createAroundMethodVisitor(aopProxyWriter));
                  AbstractBeanDefinitionBuilder.this.finalizeAndWriteBean(classWriterOutputVisitor, aopProxyWriter);
                  beanWriter.accept(classWriterOutputVisitor);
               }
            }
         } : beanWriter;
      }
   }

   @NonNull
   protected abstract BiConsumer<TypedElement, MethodElement> createAroundMethodVisitor(BeanDefinitionVisitor aopProxyWriter);

   @NonNull
   protected abstract BeanDefinitionVisitor createAopWriter(BeanDefinitionWriter beanDefinitionWriter, AnnotationMetadata annotationMetadata);

   @NonNull
   private BeanClassWriter buildBeanClassWriter() {
      final BeanDefinitionVisitor beanDefinitionWriter = this.createBeanDefinitionWriter();
      return new BeanClassWriter() {
         @Override
         public BeanDefinitionVisitor getBeanDefinitionVisitor() {
            return beanDefinitionWriter;
         }

         @Override
         public void accept(ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {
            if (!AbstractBeanDefinitionBuilder.this.configureBeanVisitor(beanDefinitionWriter)) {
               AbstractBeanDefinitionBuilder.this.configureInjectionPoints(beanDefinitionWriter);

               for(BeanMethodElement postConstructMethod : AbstractBeanDefinitionBuilder.this.postConstructMethods) {
                  if (postConstructMethod.getDeclaringType().equals(AbstractBeanDefinitionBuilder.this.beanType)) {
                     beanDefinitionWriter.visitPostConstructMethod(
                        AbstractBeanDefinitionBuilder.this.beanType,
                        postConstructMethod,
                        postConstructMethod.isReflectionRequired(),
                        AbstractBeanDefinitionBuilder.this.visitorContext
                     );
                  }
               }

               for(BeanMethodElement preDestroyMethod : AbstractBeanDefinitionBuilder.this.preDestroyMethods) {
                  if (preDestroyMethod.getDeclaringType().equals(AbstractBeanDefinitionBuilder.this.beanType)) {
                     beanDefinitionWriter.visitPreDestroyMethod(
                        AbstractBeanDefinitionBuilder.this.beanType,
                        preDestroyMethod,
                        preDestroyMethod.isReflectionRequired(),
                        AbstractBeanDefinitionBuilder.this.visitorContext
                     );
                  }
               }

               AbstractBeanDefinitionBuilder.this.finalizeAndWriteBean(classWriterOutputVisitor, beanDefinitionWriter);
            }
         }
      };
   }

   private void configureInjectionPoints(BeanDefinitionVisitor beanDefinitionWriter) {
      Map<ClassElement, List<MemberElement>> sortedInjections = new LinkedHashMap();
      List<MemberElement> allInjected = new ArrayList();
      allInjected.addAll(this.injectedFields);
      allInjected.addAll(this.injectedMethods);
      allInjected.sort(SORTER);

      for(MemberElement memberElement : allInjected) {
         List<MemberElement> list = (List)sortedInjections.computeIfAbsent(memberElement.getDeclaringType(), classElement -> new ArrayList());
         list.add(memberElement);
      }

      for(List<MemberElement> members : sortedInjections.values()) {
         members.sort((o1, o2) -> {
            if (o1 instanceof FieldElement && o2 instanceof MethodElement) {
               return 1;
            } else {
               return o1 instanceof MethodElement && o1 instanceof FieldElement ? -1 : 0;
            }
         });
      }

      for(List<MemberElement> list : sortedInjections.values()) {
         for(MemberElement memberElement : list) {
            if (memberElement instanceof FieldElement) {
               AbstractBeanDefinitionBuilder.InternalBeanElementField ibf = (AbstractBeanDefinitionBuilder.InternalBeanElementField)memberElement;
               ibf.with(element -> this.visitField(beanDefinitionWriter, element, element));
            } else {
               AbstractBeanDefinitionBuilder.InternalBeanElementMethod ibm = (AbstractBeanDefinitionBuilder.InternalBeanElementMethod)memberElement;
               ibm.with(element -> beanDefinitionWriter.visitMethodInjectionPoint(ibm.getDeclaringType(), ibm, ibm.isReflectionRequired(), this.visitorContext));
            }
         }
      }

      for(BeanMethodElement executableMethod : this.executableMethods) {
         beanDefinitionWriter.visitExecutableMethod(this.beanType, executableMethod, this.visitorContext);
         if (executableMethod.getAnnotationMetadata().isTrue(Executable.class, "processOnStartup")) {
            beanDefinitionWriter.setRequiresMethodProcessing(true);
         }
      }

   }

   protected void finalizeAndWriteBean(ClassWriterOutputVisitor classWriterOutputVisitor, BeanDefinitionVisitor beanDefinitionWriter) throws IOException {
      beanDefinitionWriter.visitBeanDefinitionEnd();
      BeanDefinitionReferenceWriter beanDefinitionReferenceWriter = new BeanDefinitionReferenceWriter(beanDefinitionWriter);
      beanDefinitionReferenceWriter.setRequiresMethodProcessing(beanDefinitionWriter.requiresMethodProcessing());
      beanDefinitionReferenceWriter.accept(classWriterOutputVisitor);
      beanDefinitionWriter.accept(classWriterOutputVisitor);
   }

   protected boolean configureBeanVisitor(BeanDefinitionVisitor beanDefinitionWriter) {
      if (this.exposedTypes != null) {
         AnnotationClassValue<?>[] annotationClassValues = (AnnotationClassValue[])Arrays.stream(this.exposedTypes)
            .map(ce -> new AnnotationClassValue(ce.getName()))
            .toArray(x$0 -> new AnnotationClassValue[x$0]);
         this.annotate(Bean.class, builder -> builder.member("typed", annotationClassValues));
      }

      if (this.typeArguments != null) {
         beanDefinitionWriter.visitTypeArguments(this.typeArguments);
      }

      if (this.constructorElement == null) {
         this.constructorElement = this.initConstructor(this.beanType);
      }

      if (this.constructorElement == null) {
         this.visitorContext
            .fail(
               "Cannot create associated bean with no accessible primary constructor. Consider supply the constructor with createWith(..)",
               this.originatingElement
            );
         return true;
      } else {
         beanDefinitionWriter.visitBeanDefinitionConstructor(this.constructorElement, !this.constructorElement.isPublic(), this.visitorContext);
         return false;
      }
   }

   protected BeanDefinitionVisitor createBeanDefinitionWriter() {
      return new BeanDefinitionWriter(this, OriginatingElements.of(this.originatingElement), this.metadataBuilder, this.visitorContext, this.identifier);
   }

   private void visitField(
      BeanDefinitionVisitor beanDefinitionWriter, BeanFieldElement injectedField, AbstractBeanDefinitionBuilder.InternalBeanElementField ibf
   ) {
      if (!injectedField.hasAnnotation(Value.class) && !injectedField.hasAnnotation(Property.class)) {
         beanDefinitionWriter.visitFieldInjectionPoint(injectedField.getDeclaringType(), ibf, ibf.isReflectionRequired());
      } else {
         beanDefinitionWriter.visitFieldValue(injectedField.getDeclaringType(), injectedField, ibf.isReflectionRequired(), ibf.isDeclaredNullable());
      }

   }

   protected abstract <T extends Annotation> void annotate(
      AnnotationMetadata annotationMetadata, String annotationType, Consumer<AnnotationValueBuilder<T>> consumer
   );

   protected abstract <T extends Annotation> void annotate(@NonNull AnnotationMetadata annotationMetadata, @NonNull AnnotationValue<T> annotationValue);

   protected abstract void removeStereotype(AnnotationMetadata annotationMetadata, String annotationType);

   protected abstract <T extends Annotation> void removeAnnotationIf(AnnotationMetadata annotationMetadata, Predicate<AnnotationValue<T>> predicate);

   protected abstract void removeAnnotation(AnnotationMetadata annotationMetadata, String annotationType);

   private final class InternalBeanConstructorElement
      extends AbstractBeanDefinitionBuilder.InternalBeanElement<MethodElement>
      implements BeanConstructorElement {
      private final MethodElement methodElement;
      private final boolean requiresReflection;
      private BeanParameterElement[] beanParameters;

      private InternalBeanConstructorElement(MethodElement methodElement, boolean requiresReflection, BeanParameterElement[] beanParameters) {
         super(methodElement);
         this.methodElement = methodElement;
         this.requiresReflection = requiresReflection;
         this.beanParameters = beanParameters;
      }

      public boolean isRequiresReflection() {
         return this.requiresReflection;
      }

      @Override
      public boolean isPackagePrivate() {
         return this.methodElement.isPackagePrivate();
      }

      @Override
      public boolean isAbstract() {
         return this.methodElement.isAbstract();
      }

      @Override
      public boolean isStatic() {
         return this.methodElement.isStatic();
      }

      @Override
      public boolean isPrivate() {
         return this.methodElement.isPrivate();
      }

      @Override
      public boolean isFinal() {
         return this.methodElement.isFinal();
      }

      @Override
      public boolean isSuspend() {
         return this.methodElement.isSuspend();
      }

      @Override
      public boolean isDefault() {
         return this.methodElement.isDefault();
      }

      @Override
      public boolean isProtected() {
         return this.methodElement.isProtected();
      }

      @Override
      public boolean isPublic() {
         return this.methodElement.isPublic();
      }

      @NonNull
      @Override
      public BeanParameterElement[] getParameters() {
         return this.beanParameters;
      }

      @NonNull
      @Override
      public ClassElement getReturnType() {
         return this.methodElement.getReturnType();
      }

      @NonNull
      @Override
      public ClassElement getGenericReturnType() {
         return this.methodElement.getGenericReturnType();
      }

      @NonNull
      @Override
      public MethodElement withNewParameters(@NonNull ParameterElement... newParameters) {
         this.beanParameters = AbstractBeanDefinitionBuilder.this.initBeanParameters(ArrayUtils.concat(this.beanParameters, newParameters));
         return this;
      }

      @Override
      public ClassElement getDeclaringType() {
         return this.methodElement.getDeclaringType();
      }

      @Override
      public ClassElement getOwningType() {
         return AbstractBeanDefinitionBuilder.this.beanType;
      }
   }

   private abstract class InternalBeanElement<E extends Element> implements Element {
      private final E element;
      private final MutableAnnotationMetadata elementMetadata;
      private AnnotationMetadata currentMetadata;

      private InternalBeanElement(E element) {
         this.element = element;
         AnnotationMetadata annotationMetadata = element.getAnnotationMetadata();
         if (annotationMetadata instanceof MutableAnnotationMetadata) {
            this.elementMetadata = ((MutableAnnotationMetadata)annotationMetadata).clone();
         } else {
            this.elementMetadata = new MutableAnnotationMetadata();
         }

      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AbstractBeanDefinitionBuilder.InternalBeanElement<?> that = (AbstractBeanDefinitionBuilder.InternalBeanElement)o;
            return this.element.equals(that.element);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.element});
      }

      @NonNull
      @Override
      public AnnotationMetadata getAnnotationMetadata() {
         return (AnnotationMetadata)(this.currentMetadata != null ? this.currentMetadata : this.elementMetadata);
      }

      @NonNull
      @Override
      public String getName() {
         return this.element.getName();
      }

      @Override
      public boolean isProtected() {
         return this.element.isProtected();
      }

      @Override
      public boolean isPublic() {
         return this.element.isPublic();
      }

      @NonNull
      @Override
      public Object getNativeType() {
         return this.element.getNativeType();
      }

      @NonNull
      @Override
      public <T extends Annotation> Element annotate(@NonNull String annotationType, @NonNull Consumer<AnnotationValueBuilder<T>> consumer) {
         AbstractBeanDefinitionBuilder.this.annotate(this.elementMetadata, annotationType, consumer);
         return this;
      }

      @Override
      public <T extends Annotation> Element annotate(AnnotationValue<T> annotationValue) {
         AbstractBeanDefinitionBuilder.this.annotate(this.elementMetadata, annotationValue);
         return this;
      }

      @Override
      public Element removeAnnotation(@NonNull String annotationType) {
         AbstractBeanDefinitionBuilder.this.removeAnnotation(this.elementMetadata, annotationType);
         return this;
      }

      @Override
      public <T extends Annotation> Element removeAnnotationIf(@NonNull Predicate<AnnotationValue<T>> predicate) {
         AbstractBeanDefinitionBuilder.this.removeAnnotationIf(this.elementMetadata, predicate);
         return this;
      }

      @Override
      public Element removeStereotype(@NonNull String annotationType) {
         AbstractBeanDefinitionBuilder.this.removeStereotype(this.elementMetadata, annotationType);
         return this;
      }

      public <T extends AbstractBeanDefinitionBuilder.InternalBeanElement<E>> void with(Consumer<T> consumer) {
         try {
            this.currentMetadata = (AnnotationMetadata)(this.elementMetadata.isEmpty() ? AnnotationMetadata.EMPTY_METADATA : this.elementMetadata);
            consumer.accept(this);
         } finally {
            this.currentMetadata = null;
         }

      }
   }

   private final class InternalBeanElementField extends AbstractBeanDefinitionBuilder.InternalBeanElement<FieldElement> implements BeanFieldElement {
      private final FieldElement fieldElement;
      private final boolean requiresReflection;
      private ClassElement genericType;

      private InternalBeanElementField(FieldElement element, boolean requiresReflection) {
         super(element);
         this.fieldElement = element;
         this.requiresReflection = requiresReflection;
      }

      public boolean isRequiresReflection() {
         return this.requiresReflection;
      }

      @Override
      public BeanFieldElement inject() {
         if (!AbstractBeanDefinitionBuilder.this.injectedFields.contains(this)) {
            AbstractBeanDefinitionBuilder.this.injectedFields.add(this);
         }

         return BeanFieldElement.super.inject();
      }

      @Override
      public BeanFieldElement injectValue(String expression) {
         if (!AbstractBeanDefinitionBuilder.this.injectedFields.contains(this)) {
            AbstractBeanDefinitionBuilder.this.injectedFields.add(this);
         }

         return BeanFieldElement.super.injectValue(expression);
      }

      @Override
      public ClassElement getDeclaringType() {
         return this.fieldElement.getDeclaringType();
      }

      @Override
      public ClassElement getOwningType() {
         return AbstractBeanDefinitionBuilder.this.beanType;
      }

      @NonNull
      @Override
      public ClassElement getType() {
         return this.fieldElement.getType();
      }

      @Override
      public ClassElement getGenericField() {
         return this.genericType != null ? this.genericType : this.fieldElement.getGenericField();
      }

      @NonNull
      public BeanFieldElement typeArguments(@NonNull ClassElement... types) {
         ClassElement genericType = this.fieldElement.getGenericField();
         Map<String, ClassElement> typeArguments = genericType.getTypeArguments();
         Map<String, ClassElement> resolved = AbstractBeanDefinitionBuilder.this.resolveTypeArguments(typeArguments, types);
         if (resolved != null) {
            String typeName = genericType.getName();
            this.genericType = ClassElement.of(typeName, genericType.isInterface(), this.getAnnotationMetadata(), resolved);
         }

         return this;
      }
   }

   private final class InternalBeanElementMethod extends AbstractBeanDefinitionBuilder.InternalBeanElement<MethodElement> implements BeanMethodElement {
      private final MethodElement methodElement;
      private final boolean requiresReflection;
      private BeanParameterElement[] beanParameters;

      private InternalBeanElementMethod(MethodElement methodElement, boolean requiresReflection) {
         this(methodElement, requiresReflection, AbstractBeanDefinitionBuilder.this.initBeanParameters(methodElement.getParameters()));
      }

      private InternalBeanElementMethod(MethodElement methodElement, boolean requiresReflection, BeanParameterElement[] beanParameters) {
         super(methodElement);
         this.methodElement = methodElement;
         this.requiresReflection = requiresReflection;
         this.beanParameters = beanParameters;
      }

      @Override
      public boolean isReflectionRequired() {
         return this.requiresReflection;
      }

      @Override
      public boolean isPackagePrivate() {
         return this.methodElement.isPackagePrivate();
      }

      @Override
      public boolean isAbstract() {
         return this.methodElement.isAbstract();
      }

      @Override
      public boolean isStatic() {
         return this.methodElement.isStatic();
      }

      @Override
      public boolean isPrivate() {
         return this.methodElement.isPrivate();
      }

      @Override
      public boolean isFinal() {
         return this.methodElement.isFinal();
      }

      @Override
      public boolean isSuspend() {
         return this.methodElement.isSuspend();
      }

      @Override
      public boolean isDefault() {
         return this.methodElement.isDefault();
      }

      @Override
      public boolean isProtected() {
         return this.methodElement.isProtected();
      }

      @Override
      public boolean isPublic() {
         return this.methodElement.isPublic();
      }

      @NonNull
      @Override
      public BeanMethodElement executable() {
         if (!AbstractBeanDefinitionBuilder.this.executableMethods.contains(this)) {
            AbstractBeanDefinitionBuilder.this.executableMethods.add(this);
         }

         return BeanMethodElement.super.executable();
      }

      @Override
      public BeanMethodElement intercept(AnnotationValue<?>... annotationValue) {
         if (!AbstractBeanDefinitionBuilder.this.interceptedMethods.contains(this)) {
            AbstractBeanDefinitionBuilder.this.interceptedMethods.add(this);
         }

         return BeanMethodElement.super.intercept(annotationValue);
      }

      @Override
      public BeanMethodElement executable(boolean processOnStartup) {
         if (!AbstractBeanDefinitionBuilder.this.executableMethods.contains(this)) {
            AbstractBeanDefinitionBuilder.this.executableMethods.add(this);
         }

         return BeanMethodElement.super.executable(processOnStartup);
      }

      @NonNull
      @Override
      public BeanMethodElement inject() {
         if (!AbstractBeanDefinitionBuilder.this.injectedMethods.contains(this)) {
            AbstractBeanDefinitionBuilder.this.injectedMethods.add(this);
         }

         return BeanMethodElement.super.inject();
      }

      @NonNull
      @Override
      public BeanMethodElement preDestroy() {
         if (!AbstractBeanDefinitionBuilder.this.preDestroyMethods.contains(this)) {
            AbstractBeanDefinitionBuilder.this.preDestroyMethods.add(this);
         }

         return BeanMethodElement.super.preDestroy();
      }

      @NonNull
      @Override
      public BeanMethodElement postConstruct() {
         if (!AbstractBeanDefinitionBuilder.this.postConstructMethods.contains(this)) {
            AbstractBeanDefinitionBuilder.this.postConstructMethods.add(this);
         }

         return BeanMethodElement.super.postConstruct();
      }

      @NonNull
      @Override
      public BeanParameterElement[] getParameters() {
         return this.beanParameters;
      }

      @NonNull
      @Override
      public ClassElement getReturnType() {
         return this.methodElement.getReturnType();
      }

      @NonNull
      @Override
      public ClassElement getGenericReturnType() {
         return this.methodElement.getGenericReturnType();
      }

      @NonNull
      @Override
      public MethodElement withNewParameters(@NonNull ParameterElement... newParameters) {
         this.beanParameters = AbstractBeanDefinitionBuilder.this.initBeanParameters(ArrayUtils.concat(this.beanParameters, newParameters));
         return this;
      }

      @Override
      public ClassElement getDeclaringType() {
         return this.methodElement.getDeclaringType();
      }

      @Override
      public ClassElement getOwningType() {
         return AbstractBeanDefinitionBuilder.this.beanType;
      }
   }

   private final class InternalBeanParameter extends AbstractBeanDefinitionBuilder.InternalBeanElement<ParameterElement> implements BeanParameterElement {
      private final ParameterElement parameterElement;
      private ClassElement genericType;

      private InternalBeanParameter(ParameterElement element) {
         super(element);
         this.parameterElement = element;
      }

      @NonNull
      @Override
      public ClassElement getGenericType() {
         return this.genericType != null ? this.genericType : this.parameterElement.getGenericType();
      }

      @NonNull
      @Override
      public ClassElement getType() {
         return this.parameterElement.getType();
      }

      @NonNull
      public BeanParameterElement typeArguments(@NonNull ClassElement... types) {
         ClassElement genericType = this.parameterElement.getGenericType();
         Map<String, ClassElement> typeArguments = genericType.getTypeArguments();
         Map<String, ClassElement> resolved = AbstractBeanDefinitionBuilder.this.resolveTypeArguments(typeArguments, types);
         if (resolved != null) {
            ElementFactory elementFactory = AbstractBeanDefinitionBuilder.this.visitorContext.getElementFactory();
            this.genericType = elementFactory.newClassElement(genericType.getNativeType(), this.getAnnotationMetadata(), resolved);
         }

         return this;
      }
   }
}
