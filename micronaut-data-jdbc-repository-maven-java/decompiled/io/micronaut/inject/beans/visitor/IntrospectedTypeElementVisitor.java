package io.micronaut.inject.beans.visitor;

import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.annotation.Executable;
import io.micronaut.core.annotation.AccessorsStyle;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ConstructorElement;
import io.micronaut.inject.ast.ElementModifier;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.ast.PropertyElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import io.micronaut.inject.writer.ClassGenerationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Internal
public class IntrospectedTypeElementVisitor implements TypeElementVisitor<Object, Object> {
   public static final int POSITION = -100;
   private static final String JAVAX_VALIDATION_CONSTRAINT = "javax.validation.Constraint";
   private static final AnnotationValue<Introspected.IndexedAnnotation> ANN_CONSTRAINT = AnnotationValue.<Introspected.IndexedAnnotation>builder(
         Introspected.IndexedAnnotation.class
      )
      .member("annotation", new AnnotationClassValue("javax.validation.Constraint"))
      .build();
   private static final String JAVAX_VALIDATION_VALID = "javax.validation.Valid";
   private static final AnnotationValue<Introspected.IndexedAnnotation> ANN_VALID = AnnotationValue.<Introspected.IndexedAnnotation>builder(
         Introspected.IndexedAnnotation.class
      )
      .member("annotation", new AnnotationClassValue("javax.validation.Valid"))
      .build();
   private static final Introspected.AccessKind[] DEFAULT_ACCESS_KIND = new Introspected.AccessKind[]{Introspected.AccessKind.METHOD};
   private static final Introspected.Visibility[] DEFAULT_VISIBILITY = new Introspected.Visibility[]{Introspected.Visibility.DEFAULT};
   private Map<String, BeanIntrospectionWriter> writers = new LinkedHashMap(10);
   private List<IntrospectedTypeElementVisitor.AbstractIntrospection> abstractIntrospections = new ArrayList();
   private IntrospectedTypeElementVisitor.AbstractIntrospection currentAbstractIntrospection;
   private ClassElement currentClassElement;

   @Override
   public int getOrder() {
      return -100;
   }

   @Override
   public void visitClass(ClassElement element, VisitorContext context) {
      this.currentClassElement = null;
      this.currentAbstractIntrospection = null;
      if (!element.isPrivate() && element.hasStereotype(Introspected.class)) {
         AnnotationValue<Introspected> introspected = element.getAnnotation(Introspected.class);
         if (introspected != null && !this.writers.containsKey(element.getName())) {
            this.currentClassElement = element;
            this.processIntrospected(element, context, introspected);
         }
      }

   }

   @Override
   public void visitConstructor(ConstructorElement element, VisitorContext context) {
      ClassElement declaringType = element.getDeclaringType();
      if (element.getDeclaringType().hasStereotype(ConfigurationReader.class)) {
         ParameterElement[] parameters = element.getParameters();
         this.introspectIfValidated(context, declaringType, parameters);
      }

   }

   private boolean isIntrospected(VisitorContext context, ClassElement c) {
      return this.writers.containsKey(c.getName()) || context.getClassElement(c.getPackageName() + ".$" + c.getSimpleName() + "$Introspection").isPresent();
   }

   @Override
   public void visitMethod(MethodElement element, VisitorContext context) {
      ClassElement declaringType = element.getDeclaringType();
      String methodName = element.getName();
      String[] readPrefixes = (String[])declaringType.getValue(AccessorsStyle.class, "readPrefixes", String[].class).orElse(new String[]{"get"});
      String[] writePrefixes = (String[])declaringType.getValue(AccessorsStyle.class, "writePrefixes", String[].class).orElse(new String[]{"set"});
      if (declaringType.hasStereotype(ConfigurationReader.class)
         && NameUtils.isReaderName(methodName, readPrefixes)
         && !this.writers.containsKey(declaringType.getName())) {
         boolean hasConstraints = element.hasStereotype("javax.validation.Constraint") || element.hasStereotype("javax.validation.Valid");
         if (hasConstraints) {
            this.processIntrospected(declaringType, context, AnnotationValue.<Introspected>builder(Introspected.class).build());
         }
      }

      if (this.currentAbstractIntrospection != null) {
         if (NameUtils.isReaderName(methodName, readPrefixes) && element.getParameters().length == 0) {
            String propertyName = NameUtils.getPropertyNameForGetter(methodName, readPrefixes);
            IntrospectedTypeElementVisitor.AbstractPropertyElement propertyElement = (IntrospectedTypeElementVisitor.AbstractPropertyElement)this.currentAbstractIntrospection
               .properties
               .computeIfAbsent(
                  propertyName,
                  s -> new IntrospectedTypeElementVisitor.AbstractPropertyElement(element.getDeclaringType(), element.getReturnType(), propertyName)
               );
            propertyElement.readMethod = element;
         } else if (NameUtils.isWriterName(methodName, writePrefixes) && element.getParameters().length == 1) {
            String propertyName = NameUtils.getPropertyNameForSetter(methodName, writePrefixes);
            IntrospectedTypeElementVisitor.AbstractPropertyElement propertyElement = (IntrospectedTypeElementVisitor.AbstractPropertyElement)this.currentAbstractIntrospection
               .properties
               .computeIfAbsent(
                  propertyName,
                  s -> new IntrospectedTypeElementVisitor.AbstractPropertyElement(
                        element.getDeclaringType(), element.getParameters()[0].getType(), propertyName
                     )
               );
            propertyElement.writeMethod = element;
         }
      }

   }

   @Override
   public void visitField(FieldElement element, VisitorContext context) {
      ClassElement declaringType = element.getDeclaringType();
      if (declaringType.hasStereotype(ConfigurationReader.class) && !this.writers.containsKey(declaringType.getName())) {
         boolean hasConstraints = element.hasStereotype("javax.validation.Constraint") || element.hasStereotype("javax.validation.Valid");
         if (hasConstraints) {
            this.processIntrospected(declaringType, context, AnnotationValue.<Introspected>builder(Introspected.class).build());
         }
      }

   }

   private void introspectIfValidated(VisitorContext context, ClassElement declaringType, ParameterElement[] parameters) {
      if (!this.writers.containsKey(declaringType.getName())) {
         boolean hasConstraints = Arrays.stream(parameters)
            .anyMatch(e -> e.hasStereotype("javax.validation.Constraint") || e.hasStereotype("javax.validation.Valid"));
         if (hasConstraints) {
            this.processIntrospected(declaringType, context, AnnotationValue.<Introspected>builder(Introspected.class).build());
         }
      }

   }

   private void processIntrospected(ClassElement element, VisitorContext context, AnnotationValue<Introspected> introspected) {
      String[] packages = introspected.stringValues("packages");
      AnnotationClassValue[] classes = introspected.get("classes", AnnotationClassValue[].class, new AnnotationClassValue[0]);
      boolean metadata = introspected.booleanValue("annotationMetadata").orElse(true);
      Set<String> includes = CollectionUtils.setOf(introspected.stringValues("includes"));
      Set<String> excludes = CollectionUtils.setOf(introspected.stringValues("excludes"));
      Set<String> excludedAnnotations = CollectionUtils.setOf(introspected.stringValues("excludedAnnotations"));
      Set<String> includedAnnotations = CollectionUtils.setOf(introspected.stringValues("includedAnnotations"));
      Set<AnnotationValue> toIndex = CollectionUtils.setOf(introspected.get("indexed", AnnotationValue[].class, new AnnotationValue[0]));
      Introspected.AccessKind[] accessKinds = introspected.enumValues("accessKind", Introspected.AccessKind.class);
      Introspected.Visibility[] visibilities = introspected.enumValues("visibility", Introspected.Visibility.class);
      if (ArrayUtils.isEmpty(accessKinds)) {
         accessKinds = DEFAULT_ACCESS_KIND;
      }

      if (ArrayUtils.isEmpty(visibilities)) {
         visibilities = DEFAULT_VISIBILITY;
      }

      Introspected.AccessKind[] finalAccessKinds = accessKinds;
      Introspected.Visibility[] finalVisibilities = visibilities;
      Set<AnnotationValue> indexedAnnotations;
      if (CollectionUtils.isEmpty(toIndex)) {
         indexedAnnotations = CollectionUtils.setOf(ANN_CONSTRAINT, ANN_VALID);
      } else {
         toIndex.addAll(CollectionUtils.<AnnotationValue>setOf(ANN_CONSTRAINT, ANN_VALID));
         indexedAnnotations = toIndex;
      }

      if (ArrayUtils.isNotEmpty(classes)) {
         AtomicInteger index = new AtomicInteger(0);

         for(AnnotationClassValue aClass : classes) {
            Optional<ClassElement> classElement = context.getClassElement(aClass.getName());
            classElement.ifPresent(
               ce -> {
                  if (ce.isPublic() && !this.isIntrospected(context, ce)) {
                     AnnotationMetadata typeMetadata = ce.getAnnotationMetadata();
                     AnnotationMetadata resolvedMetadata = (AnnotationMetadata)(typeMetadata == AnnotationMetadata.EMPTY_METADATA
                        ? element.getAnnotationMetadata()
                        : new AnnotationMetadataHierarchy(element.getAnnotationMetadata(), typeMetadata));
                     BeanIntrospectionWriter writerx = new BeanIntrospectionWriter(
                        element.getName(), index.getAndIncrement(), element, ce, metadata ? resolvedMetadata : null
                     );
                     this.processElement(
                        metadata, includes, excludes, excludedAnnotations, indexedAnnotations, ce, writerx, finalVisibilities, finalAccessKinds
                     );
                  }
   
               }
            );
         }
      } else if (ArrayUtils.isNotEmpty(packages)) {
         if (includedAnnotations.isEmpty()) {
            context.fail("When specifying 'packages' you must also specify 'includedAnnotations' to limit scanning", element);
         } else {
            for(String aPackage : packages) {
               ClassElement[] elements = context.getClassElements(aPackage, (String[])includedAnnotations.toArray(new String[0]));
               int j = 0;

               for(ClassElement classElement : elements) {
                  if (!classElement.isAbstract() && classElement.isPublic() && !this.isIntrospected(context, classElement)) {
                     BeanIntrospectionWriter writer = new BeanIntrospectionWriter(
                        element.getName(), j++, element, classElement, metadata ? element.getAnnotationMetadata() : null
                     );
                     this.processElement(
                        metadata, includes, excludes, excludedAnnotations, indexedAnnotations, classElement, writer, finalVisibilities, finalAccessKinds
                     );
                  }
               }
            }
         }
      } else {
         BeanIntrospectionWriter writer = new BeanIntrospectionWriter(element, metadata ? element.getAnnotationMetadata() : null);
         this.processElement(metadata, includes, excludes, excludedAnnotations, indexedAnnotations, element, writer, finalVisibilities, finalAccessKinds);
      }

   }

   @NonNull
   @Override
   public TypeElementVisitor.VisitorKind getVisitorKind() {
      return TypeElementVisitor.VisitorKind.ISOLATING;
   }

   @Override
   public void finish(VisitorContext visitorContext) {
      try {
         for(IntrospectedTypeElementVisitor.AbstractIntrospection abstractIntrospection : this.abstractIntrospections) {
            Collection<? extends PropertyElement> properties = abstractIntrospection.properties.values();
            if (CollectionUtils.isNotEmpty(properties)) {
               this.processBeanProperties(
                  abstractIntrospection.writer,
                  properties,
                  abstractIntrospection.includes,
                  abstractIntrospection.excludes,
                  abstractIntrospection.ignored,
                  abstractIntrospection.indexedAnnotations,
                  abstractIntrospection.metadata
               );
               this.writers.put(abstractIntrospection.writer.getBeanType().getClassName(), abstractIntrospection.writer);
            }
         }

         if (!this.writers.isEmpty()) {
            for(BeanIntrospectionWriter writer : this.writers.values()) {
               try {
                  writer.accept(visitorContext);
               } catch (IOException var8) {
                  throw new ClassGenerationException("I/O error occurred during class generation: " + var8.getMessage(), var8);
               }
            }
         }
      } finally {
         this.abstractIntrospections.clear();
         this.writers.clear();
      }

   }

   private void processElement(
      boolean metadata,
      Set<String> includes,
      Set<String> excludes,
      Set<String> excludedAnnotations,
      Set<AnnotationValue> indexedAnnotations,
      ClassElement ce,
      BeanIntrospectionWriter writer,
      Introspected.Visibility[] visibilities,
      Introspected.AccessKind... accessKinds
   ) {
      Optional<MethodElement> constructorElement = ce.getPrimaryConstructor();
      if (ce.isAbstract() && !constructorElement.isPresent() && ce.hasStereotype(Introspected.class)) {
         this.currentAbstractIntrospection = new IntrospectedTypeElementVisitor.AbstractIntrospection(
            writer, includes, excludes, excludedAnnotations, indexedAnnotations, metadata
         );
         this.abstractIntrospections.add(this.currentAbstractIntrospection);
      } else {
         List<Introspected.AccessKind> accessKindSet = Arrays.asList(accessKinds);
         Set<Introspected.Visibility> visibilitySet = CollectionUtils.setOf(visibilities);
         List<PropertyElement> beanProperties = accessKindSet.contains(Introspected.AccessKind.METHOD) ? ce.getBeanProperties() : Collections.emptyList();
         List<FieldElement> beanFields;
         if (accessKindSet.contains(Introspected.AccessKind.FIELD)) {
            Predicate<String> nameFilter = null;
            if (accessKindSet.iterator().next() == Introspected.AccessKind.METHOD) {
               nameFilter = name -> {
                  for(PropertyElement beanProperty : beanProperties) {
                     if (name.equals(beanProperty.getName())) {
                        return false;
                     }
                  }

                  return true;
               };
            }

            ElementQuery<FieldElement> query;
            if (visibilitySet.contains(Introspected.Visibility.DEFAULT)) {
               query = ElementQuery.<FieldElement>of(FieldElement.class)
                  .onlyAccessible()
                  .modifiers(modifiers -> !modifiers.contains(ElementModifier.STATIC) && !modifiers.contains(ElementModifier.PROTECTED));
            } else {
               query = ElementQuery.<FieldElement>of(FieldElement.class)
                  .modifiers(
                     modifiers -> !modifiers.contains(ElementModifier.STATIC)
                           && visibilitySet.stream().anyMatch(v -> modifiers.contains(ElementModifier.valueOf(v.name())))
                  );
            }

            if (nameFilter != null) {
               query = query.named(nameFilter);
            }

            beanFields = ce.getEnclosedElements(query);
         } else {
            beanFields = Collections.emptyList();
         }

         if (!beanFields.isEmpty() && !beanProperties.isEmpty()) {
            beanProperties = (List)beanProperties.stream()
               .filter(pe -> beanFields.stream().noneMatch(fieldElement -> fieldElement.getName().equals(pe.getName())))
               .collect(Collectors.toList());
         }

         MethodElement constructor = (MethodElement)constructorElement.orElse(null);
         this.process(
            constructor,
            (MethodElement)ce.getDefaultConstructor().orElse(null),
            writer,
            beanProperties,
            includes,
            excludes,
            excludedAnnotations,
            indexedAnnotations,
            metadata
         );

         for(FieldElement beanField : beanFields) {
            writer.visitBeanField(beanField);
         }

         ElementQuery<MethodElement> query = ElementQuery.<MethodElement>of(MethodElement.class)
            .onlyAccessible()
            .modifiers(modifiers -> !modifiers.contains(ElementModifier.STATIC))
            .annotated(am -> am.hasStereotype(Executable.class));

         for(MethodElement executableMethod : ce.getEnclosedElements(query)) {
            writer.visitBeanMethod(executableMethod);
         }
      }

   }

   private void process(
      @Nullable MethodElement constructorElement,
      @Nullable MethodElement defaultConstructor,
      BeanIntrospectionWriter writer,
      List<PropertyElement> beanProperties,
      Set<String> includes,
      Set<String> excludes,
      Set<String> ignored,
      Set<AnnotationValue> indexedAnnotations,
      boolean metadata,
      Introspected.AccessKind... accessKind
   ) {
      if (constructorElement != null) {
         ParameterElement[] parameters = constructorElement.getParameters();
         if (ArrayUtils.isNotEmpty(parameters)) {
            writer.visitConstructor(constructorElement);
         }
      }

      if (defaultConstructor != null) {
         writer.visitDefaultConstructor(defaultConstructor);
      }

      this.processBeanProperties(writer, beanProperties, includes, excludes, ignored, indexedAnnotations, metadata);
      this.writers.put(writer.getBeanType().getClassName(), writer);
   }

   private void processBeanProperties(
      BeanIntrospectionWriter writer,
      Collection<? extends PropertyElement> beanProperties,
      Set<String> includes,
      Set<String> excludes,
      Set<String> ignored,
      Set<AnnotationValue> indexedAnnotations,
      boolean metadata
   ) {
      for(PropertyElement beanProperty : beanProperties) {
         ClassElement type = beanProperty.getType();
         ClassElement genericType = beanProperty.getGenericType();
         String name = beanProperty.getName();
         if ((includes.isEmpty() || includes.contains(name))
            && (excludes.isEmpty() || !excludes.contains(name))
            && (ignored.isEmpty() || !ignored.stream().anyMatch(beanProperty::hasAnnotation))) {
            writer.visitProperty(
               type,
               genericType,
               name,
               (MethodElement)beanProperty.getReadMethod().orElse(null),
               (MethodElement)beanProperty.getWriteMethod().orElse(null),
               beanProperty.isReadOnly(),
               metadata ? beanProperty.getAnnotationMetadata() : null,
               genericType.getTypeArguments()
            );

            for(AnnotationValue<?> indexedAnnotation : indexedAnnotations) {
               indexedAnnotation.get("annotation", String.class)
                  .ifPresent(
                     annotationName -> {
                        if (beanProperty.hasStereotype(annotationName)) {
                           writer.indexProperty(
                              annotationName,
                              name,
                              (String)indexedAnnotation.get("member", String.class)
                                 .flatMap(m -> beanProperty.getValue(annotationName, m, String.class))
                                 .orElse(null)
                           );
                        }
      
                     }
                  );
            }
         }
      }

   }

   private class AbstractIntrospection {
      final BeanIntrospectionWriter writer;
      final Set<String> includes;
      final Set<String> excludes;
      final Set<String> ignored;
      final Set<AnnotationValue> indexedAnnotations;
      final boolean metadata;
      final Map<String, IntrospectedTypeElementVisitor.AbstractPropertyElement> properties = new LinkedHashMap();

      public AbstractIntrospection(
         BeanIntrospectionWriter writer,
         Set<String> includes,
         Set<String> excludes,
         Set<String> ignored,
         Set<AnnotationValue> indexedAnnotations,
         boolean metadata
      ) {
         this.writer = writer;
         this.includes = includes;
         this.excludes = excludes;
         this.ignored = ignored;
         this.indexedAnnotations = indexedAnnotations;
         this.metadata = metadata;
      }
   }

   private static class AbstractPropertyElement implements PropertyElement {
      private final ClassElement declaringType;
      private final ClassElement type;
      private final String name;
      private MethodElement writeMethod;
      private MethodElement readMethod;

      AbstractPropertyElement(ClassElement declaringType, ClassElement type, String name) {
         this.declaringType = declaringType;
         this.type = type;
         this.name = name;
      }

      @Override
      public Optional<MethodElement> getWriteMethod() {
         return Optional.ofNullable(this.writeMethod);
      }

      @Override
      public Optional<MethodElement> getReadMethod() {
         return Optional.ofNullable(this.readMethod);
      }

      @NonNull
      @Override
      public ClassElement getType() {
         return this.type;
      }

      @Override
      public ClassElement getDeclaringType() {
         return this.declaringType;
      }

      @NonNull
      @Override
      public String getName() {
         return this.name;
      }

      @Override
      public boolean isProtected() {
         return false;
      }

      @Override
      public boolean isPublic() {
         return true;
      }

      @NonNull
      @Override
      public Object getNativeType() {
         return this;
      }
   }
}
