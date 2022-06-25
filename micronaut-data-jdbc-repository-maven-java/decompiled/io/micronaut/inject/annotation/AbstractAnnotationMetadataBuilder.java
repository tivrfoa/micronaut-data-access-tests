package io.micronaut.inject.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Aliases;
import io.micronaut.context.annotation.DefaultScope;
import io.micronaut.context.annotation.NonBinding;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotatedElement;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.InstantiatedMember;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.OptionalValues;
import io.micronaut.inject.visitor.VisitorContext;
import jakarta.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractAnnotationMetadataBuilder<T, A> {
   private static final Map<String, String> DEPRECATED_ANNOTATION_NAMES = Collections.emptyMap();
   private static final Map<String, List<AnnotationMapper<?>>> ANNOTATION_MAPPERS = new HashMap(10);
   private static final Map<String, List<AnnotationTransformer<Annotation>>> ANNOTATION_TRANSFORMERS = new HashMap(5);
   private static final Map<String, List<AnnotationRemapper>> ANNOTATION_REMAPPERS = new HashMap(5);
   private static final Map<AbstractAnnotationMetadataBuilder.MetadataKey, AnnotationMetadata> MUTATED_ANNOTATION_METADATA = new HashMap(100);
   private static final Map<String, Set<String>> NON_BINDING_CACHE = new HashMap(50);
   private static final List<String> DEFAULT_ANNOTATE_EXCLUDES = Arrays.asList(Internal.class.getName(), Experimental.class.getName());
   private static final Map<String, Map<String, Object>> ANNOTATION_DEFAULTS = new HashMap(20);
   private boolean validating = true;
   private final Set<T> erroneousElements = new HashSet();

   protected AbstractAnnotationMetadataBuilder() {
   }

   private AnnotationMetadata metadataForError(RuntimeException e) {
      if ("org.eclipse.jdt.internal.compiler.problem.AbortCompilation".equals(e.getClass().getName())) {
         return AnnotationMetadata.EMPTY_METADATA;
      } else {
         throw e;
      }
   }

   public AnnotationMetadata buildDeclared(T element) {
      DefaultAnnotationMetadata annotationMetadata = new MutableAnnotationMetadata();

      try {
         AnnotationMetadata metadata = this.buildInternal((T)null, element, annotationMetadata, true, true, true);
         return metadata.isEmpty() ? AnnotationMetadata.EMPTY_METADATA : metadata;
      } catch (RuntimeException var4) {
         return this.metadataForError(var4);
      }
   }

   public AnnotationMetadata buildDeclared(T element, List<? extends A> annotations, boolean includeTypeAnnotations) {
      if (CollectionUtils.isEmpty(annotations)) {
         return AnnotationMetadata.EMPTY_METADATA;
      } else {
         DefaultAnnotationMetadata annotationMetadata = new MutableAnnotationMetadata();
         if (includeTypeAnnotations) {
            this.buildInternal(element, element, annotationMetadata, false, true, true);
         }

         try {
            this.includeAnnotations(annotationMetadata, element, false, true, annotations, true);
            return (AnnotationMetadata)(annotationMetadata.isEmpty() ? AnnotationMetadata.EMPTY_METADATA : annotationMetadata);
         } catch (RuntimeException var6) {
            return this.metadataForError(var6);
         }
      }
   }

   public AnnotationMetadata buildOverridden(T element) {
      AnnotationMetadata existing = (AnnotationMetadata)MUTATED_ANNOTATION_METADATA.get(
         new AbstractAnnotationMetadataBuilder.MetadataKey(this.getDeclaringType(element), element)
      );
      if (existing != null) {
         return existing;
      } else {
         DefaultAnnotationMetadata annotationMetadata = new MutableAnnotationMetadata();

         try {
            AnnotationMetadata metadata = this.buildInternal((T)null, element, annotationMetadata, false, false, true);
            return metadata.isEmpty() ? AnnotationMetadata.EMPTY_METADATA : metadata;
         } catch (RuntimeException var5) {
            return this.metadataForError(var5);
         }
      }
   }

   public AnnotationMetadata build(T element) {
      String declaringType = this.getDeclaringType(element);
      return this.build(declaringType, element);
   }

   public AnnotationMetadata build(String declaringType, T element) {
      AnnotationMetadata existing = this.lookupExisting(declaringType, element);
      if (existing != null) {
         return existing;
      } else {
         DefaultAnnotationMetadata annotationMetadata = new MutableAnnotationMetadata();

         try {
            AnnotationMetadata metadata = this.buildInternal((T)null, element, annotationMetadata, true, false, true);
            return metadata.isEmpty() ? AnnotationMetadata.EMPTY_METADATA : metadata;
         } catch (RuntimeException var6) {
            return this.metadataForError(var6);
         }
      }
   }

   protected abstract boolean isMethodOrClassElement(T element);

   @Nullable
   protected abstract String getDeclaringType(@NonNull T element);

   public AnnotationMetadata buildForMethod(T element) {
      String declaringType = this.getDeclaringType(element);
      AnnotationMetadata existing = this.lookupExisting(declaringType, element);
      if (existing != null) {
         return existing;
      } else {
         DefaultAnnotationMetadata annotationMetadata = new MutableAnnotationMetadata();
         return this.buildInternal((T)null, element, annotationMetadata, false, false, true);
      }
   }

   public AnnotationMetadata buildForParent(T parent, T element) {
      return this.buildForParents(parent == null ? Collections.emptyList() : Collections.singletonList(parent), element);
   }

   public AnnotationMetadata buildForParents(List<T> parents, T element) {
      String declaringType = this.getDeclaringType(element);
      return this.buildForParents(declaringType, parents, element);
   }

   public AnnotationMetadata buildForParent(String declaringType, T parent, T element) {
      return this.buildForParents(declaringType, parent == null ? Collections.emptyList() : Collections.singletonList(parent), element);
   }

   public AnnotationMetadata buildForParents(String declaringType, List<T> parents, T element) {
      AnnotationMetadata existing = this.lookupExisting(declaringType, element);
      DefaultAnnotationMetadata annotationMetadata;
      if (existing instanceof DefaultAnnotationMetadata) {
         annotationMetadata = ((DefaultAnnotationMetadata)existing).clone();
         if (parents.isEmpty()) {
            return annotationMetadata;
         }
      } else if (existing instanceof AnnotationMetadataHierarchy) {
         AnnotationMetadata declaredMetadata = ((AnnotationMetadataHierarchy)existing).getDeclaredMetadata();
         if (declaredMetadata instanceof DefaultAnnotationMetadata) {
            annotationMetadata = ((DefaultAnnotationMetadata)declaredMetadata).clone();
         } else {
            annotationMetadata = new MutableAnnotationMetadata();
         }

         if (parents.isEmpty()) {
            return annotationMetadata;
         }
      } else {
         annotationMetadata = new MutableAnnotationMetadata();
      }

      return this.buildInternalMulti(parents, element, annotationMetadata, false, false, true);
   }

   public AnnotationMetadata buildForParent(T parent, T element, boolean inheritTypeAnnotations) {
      String declaringType = this.getDeclaringType(element);
      AnnotationMetadata existing = this.lookupExisting(declaringType, element);
      DefaultAnnotationMetadata annotationMetadata;
      if (existing instanceof DefaultAnnotationMetadata) {
         annotationMetadata = ((DefaultAnnotationMetadata)existing).clone();
      } else if (existing instanceof AnnotationMetadataHierarchy) {
         AnnotationMetadata declaredMetadata = existing.getDeclaredMetadata();
         if (declaredMetadata instanceof DefaultAnnotationMetadata) {
            annotationMetadata = ((DefaultAnnotationMetadata)declaredMetadata).clone();
         } else {
            annotationMetadata = new MutableAnnotationMetadata();
         }
      } else {
         annotationMetadata = new MutableAnnotationMetadata();
      }

      return this.buildInternal(parent, element, annotationMetadata, inheritTypeAnnotations, false, true);
   }

   protected abstract T getTypeForAnnotation(A annotationMirror);

   protected abstract boolean hasAnnotation(T element, Class<? extends Annotation> annotation);

   protected abstract boolean hasAnnotation(T element, String annotation);

   protected abstract boolean hasAnnotations(T element);

   protected abstract String getAnnotationTypeName(A annotationMirror);

   protected abstract String getElementName(T element);

   protected abstract List<? extends A> getAnnotationsForType(T element);

   protected abstract List<T> buildHierarchy(T element, boolean inheritTypeAnnotations, boolean declaredOnly);

   protected abstract void readAnnotationRawValues(
      T originatingElement, String annotationName, T member, String memberName, Object annotationValue, Map<CharSequence, Object> annotationValues
   );

   protected void validateAnnotationValue(T originatingElement, String annotationName, T member, String memberName, Object resolvedValue) {
      if (this.validating) {
         AnnotatedElementValidator elementValidator = this.getElementValidator();
         if (elementValidator != null && !this.erroneousElements.contains(member)) {
            boolean shouldValidate = !annotationName.equals(AliasFor.class.getName())
               && (!(resolvedValue instanceof String) || !resolvedValue.toString().contains("${"));
            if (shouldValidate) {
               shouldValidate = this.isValidationRequired(member);
            }

            if (shouldValidate) {
               final AnnotationMetadata metadata;
               try {
                  this.validating = false;
                  metadata = this.buildDeclared(member);
               } finally {
                  this.validating = true;
               }

               Set<String> errors = elementValidator.validatedAnnotatedElement(new AnnotatedElement() {
                  @NonNull
                  @Override
                  public String getName() {
                     return memberName;
                  }

                  @Override
                  public AnnotationMetadata getAnnotationMetadata() {
                     return metadata;
                  }
               }, resolvedValue);
               if (CollectionUtils.isNotEmpty(errors)) {
                  this.erroneousElements.add(member);

                  for(String error : errors) {
                     error = "@" + NameUtils.getSimpleName(annotationName) + "." + memberName + ": " + error;
                     this.addError(originatingElement, error);
                  }
               }
            }
         }

      }
   }

   protected abstract boolean isValidationRequired(T member);

   @Nullable
   protected AnnotatedElementValidator getElementValidator() {
      return null;
   }

   protected abstract void addError(@NonNull T originatingElement, @NonNull String error);

   protected abstract void addWarning(@NonNull T originatingElement, @NonNull String warning);

   protected abstract Object readAnnotationValue(T originatingElement, T member, String memberName, Object annotationValue);

   protected abstract Map<? extends T, ?> readAnnotationDefaultValues(A annotationMirror);

   protected abstract Map<? extends T, ?> readAnnotationDefaultValues(String annotationName, T annotationType);

   protected abstract Map<? extends T, ?> readAnnotationRawValues(A annotationMirror);

   protected abstract OptionalValues<?> getAnnotationValues(T originatingElement, T member, Class<?> annotationType);

   protected abstract String getAnnotationMemberName(T member);

   @Nullable
   protected abstract String getRepeatableName(A annotationMirror);

   @Nullable
   protected abstract String getRepeatableNameForType(T annotationType);

   protected AnnotationValue readNestedAnnotationValue(T originatingElement, A annotationMirror) {
      Map<? extends T, ?> annotationValues = this.readAnnotationRawValues(annotationMirror);
      String annotationTypeName = this.getAnnotationTypeName(annotationMirror);
      AnnotationValue av;
      if (annotationValues.isEmpty()) {
         av = new AnnotationValue(annotationTypeName);
      } else {
         Map<CharSequence, Object> resolvedValues = new LinkedHashMap();

         for(Entry<? extends T, ?> entry : annotationValues.entrySet()) {
            T member = (T)entry.getKey();
            OptionalValues<?> aliasForValues = this.getAnnotationValues(originatingElement, member, AliasFor.class);
            Object annotationValue = entry.getValue();
            Optional<?> aliasMember = aliasForValues.get("member");
            Optional<?> aliasAnnotation = aliasForValues.get("annotation");
            Optional<?> aliasAnnotationName = aliasForValues.get("annotationName");
            if (aliasMember.isPresent() && !aliasAnnotation.isPresent() && !aliasAnnotationName.isPresent()) {
               String aliasedNamed = aliasMember.get().toString();
               this.readAnnotationRawValues(originatingElement, annotationTypeName, member, aliasedNamed, annotationValue, resolvedValues);
            }

            String memberName = this.getAnnotationMemberName(member);
            this.readAnnotationRawValues(originatingElement, annotationTypeName, member, memberName, annotationValue, resolvedValues);
         }

         av = new AnnotationValue(annotationTypeName, resolvedValues);
      }

      return av;
   }

   protected abstract Optional<T> getAnnotationMirror(String annotationName);

   protected Map<CharSequence, Object> populateAnnotationData(
      T originatingElement,
      @Nullable T parent,
      A annotationMirror,
      DefaultAnnotationMetadata metadata,
      boolean isDeclared,
      RetentionPolicy retentionPolicy,
      boolean allowAliases
   ) {
      return this.populateAnnotationData(
         originatingElement, parent == originatingElement, annotationMirror, metadata, isDeclared, retentionPolicy, allowAliases
      );
   }

   protected Map<CharSequence, Object> populateAnnotationData(
      T originatingElement,
      boolean originatingElementIsSameParent,
      A annotationMirror,
      DefaultAnnotationMetadata metadata,
      boolean isDeclared,
      RetentionPolicy retentionPolicy,
      boolean allowAliases
   ) {
      String annotationName = this.getAnnotationTypeName(annotationMirror);
      if (retentionPolicy == RetentionPolicy.RUNTIME) {
         this.processAnnotationDefaults(originatingElement, metadata, annotationName, () -> this.readAnnotationDefaultValues(annotationMirror));
      }

      List<String> parentAnnotations = new ArrayList();
      parentAnnotations.add(annotationName);
      Map<? extends T, ?> elementValues = this.readAnnotationRawValues(annotationMirror);
      Map<CharSequence, Object> annotationValues;
      if (CollectionUtils.isEmpty(elementValues)) {
         annotationValues = new LinkedHashMap(3);
      } else {
         annotationValues = new LinkedHashMap(5);
         Set<String> nonBindingMembers = new HashSet(2);

         for(Entry<? extends T, ?> entry : elementValues.entrySet()) {
            T member = (T)entry.getKey();
            if (member != null) {
               Object annotationValue = entry.getValue();
               if (this.hasAnnotations(member)) {
                  DefaultAnnotationMetadata memberMetadata = new DefaultAnnotationMetadata();
                  List<? extends A> annotationsForMember = (List)this.getAnnotationsForType(member)
                     .stream()
                     .filter(a -> !this.getAnnotationTypeName((A)a).equals(annotationName))
                     .collect(Collectors.toList());
                  this.includeAnnotations(memberMetadata, member, false, true, annotationsForMember, false);
                  boolean isInstantiatedMember = memberMetadata.hasAnnotation(InstantiatedMember.class);
                  if (memberMetadata.hasAnnotation(NonBinding.class)) {
                     String memberName = this.getElementName(member);
                     nonBindingMembers.add(memberName);
                  }

                  if (isInstantiatedMember) {
                     String memberName = this.getAnnotationMemberName(member);
                     Object rawValue = this.readAnnotationValue(originatingElement, member, memberName, annotationValue);
                     if (rawValue instanceof AnnotationClassValue) {
                        AnnotationClassValue acv = (AnnotationClassValue)rawValue;
                        annotationValues.put(memberName, new AnnotationClassValue(acv.getName(), true));
                     }
                  }
               }

               if (allowAliases) {
                  this.handleAnnotationAlias(
                     originatingElement, metadata, isDeclared, annotationName, parentAnnotations, annotationValues, member, annotationValue
                  );
               }
            }
         }

         if (!nonBindingMembers.isEmpty()) {
            T annotationType = this.getTypeForAnnotation(annotationMirror);
            if (this.hasAnnotation(annotationType, "javax.inject.Qualifier") || this.hasAnnotation(annotationType, Qualifier.class)) {
               metadata.addDeclaredStereotype(
                  Collections.singletonList(this.getAnnotationTypeName(annotationMirror)),
                  "javax.inject.Qualifier",
                  Collections.singletonMap("nonBinding", nonBindingMembers)
               );
            }
         }
      }

      List<AnnotationMapper<?>> mappers = this.getAnnotationMappers(annotationName);
      if (mappers != null) {
         AnnotationValue<?> annotationValue = new AnnotationValue(annotationName, annotationValues);
         VisitorContext visitorContext = this.createVisitorContext();

         for(AnnotationMapper mapper : mappers) {
            List mapped = mapper.map(annotationValue, visitorContext);
            if (mapped != null) {
               for(Object o : mapped) {
                  if (o instanceof AnnotationValue) {
                     AnnotationValue av = (AnnotationValue)o;
                     retentionPolicy = av.getRetentionPolicy();
                     String mappedAnnotationName = av.getAnnotationName();
                     Optional<T> mappedMirror = this.getAnnotationMirror(mappedAnnotationName);
                     String repeatableName = (String)mappedMirror.map(this::getRepeatableNameForType).orElse(null);
                     if (repeatableName != null) {
                        if (isDeclared) {
                           metadata.addDeclaredRepeatable(repeatableName, av, retentionPolicy);
                        } else {
                           metadata.addRepeatable(repeatableName, av, retentionPolicy);
                        }
                     } else {
                        Map<CharSequence, Object> values = av.getValues();
                        if (isDeclared) {
                           metadata.addDeclaredAnnotation(mappedAnnotationName, values, retentionPolicy);
                        } else {
                           metadata.addAnnotation(mappedAnnotationName, values, retentionPolicy);
                        }
                     }

                     mappedMirror.ifPresent(
                        annMirror -> {
                           Map<CharSequence, Object> values = av.getValues();
                           values.forEach(
                              (key, value) -> {
                                 T memberx = this.getAnnotationMember((T)annMirror, key);
                                 if (memberx != null) {
                                    this.handleAnnotationAlias(
                                       originatingElement,
                                       metadata,
                                       isDeclared,
                                       mappedAnnotationName,
                                       Collections.emptyList(),
                                       annotationValues,
                                       (T)memberx,
                                       value
                                    );
                                 }
      
                              }
                           );
                           if (retentionPolicy == RetentionPolicy.RUNTIME) {
                              this.processAnnotationDefaults(
                                 originatingElement, metadata, mappedAnnotationName, () -> this.readAnnotationDefaultValues(mappedAnnotationName, (T)annMirror)
                              );
                           }
   
                           ArrayList<String> parents = new ArrayList();
                           this.processAnnotationStereotype(
                              parents,
                              (T)annMirror,
                              mappedAnnotationName,
                              metadata,
                              isDeclared,
                              this.isInheritedAnnotationType((T)annMirror) || originatingElementIsSameParent
                           );
                        }
                     );
                  }
               }
            }
         }
      }

      return annotationValues;
   }

   private void handleAnnotationAlias(
      T originatingElement,
      DefaultAnnotationMetadata metadata,
      boolean isDeclared,
      String annotationName,
      List<String> parentAnnotations,
      Map<CharSequence, Object> annotationValues,
      T member,
      Object annotationValue
   ) {
      Optional<?> aliases = this.getAnnotationValues(originatingElement, member, Aliases.class).get("value");
      if (aliases.isPresent()) {
         Object value = aliases.get();
         if (value instanceof AnnotationValue[]) {
            AnnotationValue[] values = (AnnotationValue[])value;

            for(AnnotationValue av : values) {
               OptionalValues<Object> aliasForValues = OptionalValues.of(Object.class, av.getValues());
               this.processAnnotationAlias(
                  originatingElement, annotationName, member, metadata, isDeclared, parentAnnotations, annotationValues, annotationValue, aliasForValues
               );
            }
         }

         this.readAnnotationRawValues(originatingElement, annotationName, member, this.getAnnotationMemberName(member), annotationValue, annotationValues);
      } else {
         OptionalValues<?> aliasForValues = this.getAnnotationValues(originatingElement, member, AliasFor.class);
         this.processAnnotationAlias(
            originatingElement, annotationName, member, metadata, isDeclared, parentAnnotations, annotationValues, annotationValue, aliasForValues
         );
         this.readAnnotationRawValues(originatingElement, annotationName, member, this.getAnnotationMemberName(member), annotationValue, annotationValues);
      }

   }

   @Nullable
   protected abstract T getAnnotationMember(T originatingElement, CharSequence member);

   @NonNull
   protected List<AnnotationMapper<? extends Annotation>> getAnnotationMappers(@NonNull String annotationName) {
      return (List<AnnotationMapper<? extends Annotation>>)ANNOTATION_MAPPERS.get(annotationName);
   }

   @NonNull
   protected List<AnnotationTransformer<Annotation>> getAnnotationTransformers(@NonNull String annotationName) {
      return (List<AnnotationTransformer<Annotation>>)ANNOTATION_TRANSFORMERS.get(annotationName);
   }

   protected abstract VisitorContext createVisitorContext();

   private void processAnnotationDefaults(
      T originatingElement, DefaultAnnotationMetadata metadata, String annotationName, Supplier<Map<? extends T, ?>> elementDefaultValues
   ) {
      Map<String, Object> defaults = (Map)ANNOTATION_DEFAULTS.get(annotationName);
      Map<CharSequence, Object> defaultValues;
      if (defaults != null) {
         defaultValues = new LinkedHashMap(defaults);
      } else {
         defaultValues = this.getAnnotationDefaults(originatingElement, annotationName, (Map<? extends T, ?>)elementDefaultValues.get());
         if (defaultValues != null) {
            ANNOTATION_DEFAULTS.put(
               annotationName, defaultValues.entrySet().stream().collect(Collectors.toMap(entry -> ((CharSequence)entry.getKey()).toString(), Entry::getValue))
            );
         } else {
            defaultValues = Collections.emptyMap();
         }
      }

      metadata.addDefaultAnnotationValues(annotationName, defaultValues);
   }

   private Map<CharSequence, Object> getAnnotationDefaults(T originatingElement, String annotationName, Map<? extends T, ?> elementDefaultValues) {
      if (elementDefaultValues != null) {
         Map<CharSequence, Object> defaultValues = new LinkedHashMap();

         for(Entry<? extends T, ?> entry : elementDefaultValues.entrySet()) {
            T member = (T)entry.getKey();
            String memberName = this.getAnnotationMemberName(member);
            if (!defaultValues.containsKey(memberName)) {
               Object annotationValue = entry.getValue();
               this.readAnnotationRawValues(originatingElement, annotationName, member, memberName, annotationValue, defaultValues);
            }
         }

         return defaultValues;
      } else {
         return null;
      }
   }

   private AnnotationMetadata lookupExisting(String declaringType, T element) {
      return (AnnotationMetadata)MUTATED_ANNOTATION_METADATA.get(new AbstractAnnotationMetadataBuilder.MetadataKey(declaringType, element));
   }

   private void processAnnotationAlias(
      T originatingElement,
      String annotationName,
      T member,
      DefaultAnnotationMetadata metadata,
      boolean isDeclared,
      List<String> parentAnnotations,
      Map<CharSequence, Object> annotationValues,
      Object annotationValue,
      OptionalValues<?> aliasForValues
   ) {
      Optional<?> aliasAnnotation = aliasForValues.get("annotation");
      Optional<?> aliasAnnotationName = aliasForValues.get("annotationName");
      Optional<?> aliasMember = aliasForValues.get("member");
      if (!aliasAnnotation.isPresent() && !aliasAnnotationName.isPresent()) {
         if (aliasMember.isPresent()) {
            String aliasedNamed = aliasMember.get().toString();
            Object v = this.readAnnotationValue(originatingElement, member, aliasedNamed, annotationValue);
            if (v != null) {
               annotationValues.put(aliasedNamed, v);
            }

            this.readAnnotationRawValues(originatingElement, annotationName, member, aliasedNamed, annotationValue, annotationValues);
         }
      } else if (aliasMember.isPresent()) {
         String aliasedAnnotation;
         if (aliasAnnotation.isPresent()) {
            aliasedAnnotation = aliasAnnotation.get().toString();
         } else {
            aliasedAnnotation = aliasAnnotationName.get().toString();
         }

         String aliasedMemberName = aliasMember.get().toString();
         Object v = this.readAnnotationValue(originatingElement, member, aliasedMemberName, annotationValue);
         if (v != null) {
            for(AnnotationValue<?> remappedAnnotation : this.remapAnnotation(aliasedAnnotation)) {
               String aliasedAnnotationName = remappedAnnotation.getAnnotationName();
               Optional<T> annotationMirror = this.getAnnotationMirror(aliasedAnnotationName);
               RetentionPolicy retentionPolicy = RetentionPolicy.RUNTIME;
               String repeatableName = null;
               if (annotationMirror.isPresent()) {
                  T annotationTypeMirror = (T)annotationMirror.get();
                  this.processAnnotationDefaults(
                     originatingElement, metadata, aliasedAnnotationName, () -> this.readAnnotationDefaultValues(aliasedAnnotationName, annotationTypeMirror)
                  );
                  retentionPolicy = this.getRetentionPolicy(annotationTypeMirror);
                  repeatableName = this.getRepeatableNameForType(annotationTypeMirror);
               }

               if (isDeclared) {
                  if (StringUtils.isNotEmpty(repeatableName)) {
                     metadata.addDeclaredRepeatableStereotype(
                        parentAnnotations,
                        repeatableName,
                        AnnotationValue.builder(aliasedAnnotationName, retentionPolicy).members(Collections.singletonMap(aliasedMemberName, v)).build()
                     );
                  } else {
                     metadata.addDeclaredStereotype(
                        Collections.emptyList(), aliasedAnnotationName, Collections.singletonMap(aliasedMemberName, v), retentionPolicy
                     );
                  }
               } else if (StringUtils.isNotEmpty(repeatableName)) {
                  metadata.addRepeatableStereotype(
                     parentAnnotations,
                     repeatableName,
                     AnnotationValue.builder(aliasedAnnotationName, retentionPolicy).members(Collections.singletonMap(aliasedMemberName, v)).build()
                  );
               } else {
                  metadata.addStereotype(Collections.emptyList(), aliasedAnnotationName, Collections.singletonMap(aliasedMemberName, v), retentionPolicy);
               }

               if (annotationMirror.isPresent()) {
                  T am = (T)annotationMirror.get();
                  this.processAnnotationStereotype(
                     Collections.singletonList(aliasedAnnotationName), am, aliasedAnnotationName, metadata, isDeclared, this.isInheritedAnnotationType(am)
                  );
               } else {
                  this.processAnnotationStereotype(Collections.singletonList(aliasedAnnotationName), remappedAnnotation, metadata, isDeclared);
               }
            }
         }
      }

   }

   @NonNull
   protected abstract RetentionPolicy getRetentionPolicy(@NonNull T annotation);

   private AnnotationMetadata buildInternal(
      T parent, T element, DefaultAnnotationMetadata annotationMetadata, boolean inheritTypeAnnotations, boolean declaredOnly, boolean allowAliases
   ) {
      return this.buildInternalMulti(
         parent == null ? Collections.emptyList() : Collections.singletonList(parent),
         element,
         annotationMetadata,
         inheritTypeAnnotations,
         declaredOnly,
         allowAliases
      );
   }

   private AnnotationMetadata buildInternalMulti(
      List<T> parents, T element, DefaultAnnotationMetadata annotationMetadata, boolean inheritTypeAnnotations, boolean declaredOnly, boolean allowAliases
   ) {
      List<T> hierarchy = this.buildHierarchy(element, inheritTypeAnnotations, declaredOnly);

      for(T parent : parents) {
         List<T> parentHierarchy = this.buildHierarchy(parent, inheritTypeAnnotations, declaredOnly);
         if (hierarchy.isEmpty() && !parentHierarchy.isEmpty()) {
            hierarchy = parentHierarchy;
         } else {
            hierarchy.addAll(0, parentHierarchy);
         }
      }

      Collections.reverse(hierarchy);

      for(T currentElement : hierarchy) {
         if (currentElement != null) {
            List<? extends A> annotationHierarchy = this.getAnnotationsForType(currentElement);
            if (!annotationHierarchy.isEmpty()) {
               this.includeAnnotations(
                  annotationMetadata, currentElement, parents.contains(currentElement), currentElement == element, annotationHierarchy, allowAliases
               );
            }
         }
      }

      if (!annotationMetadata.hasDeclaredStereotype("javax.inject.Scope") && annotationMetadata.hasDeclaredStereotype(DefaultScope.class)) {
         Optional<String> value = annotationMetadata.stringValue(DefaultScope.class);
         value.ifPresent(name -> annotationMetadata.addDeclaredAnnotation(name, Collections.emptyMap()));
      }

      return annotationMetadata;
   }

   private void includeAnnotations(
      DefaultAnnotationMetadata annotationMetadata,
      T element,
      boolean originatingElementIsSameParent,
      boolean isDeclared,
      List<? extends A> annotationHierarchy,
      boolean allowAliases
   ) {
      ArrayList<? extends A> hierarchyCopy = new ArrayList(annotationHierarchy);
      ListIterator<? extends A> listIterator = hierarchyCopy.listIterator();

      while(listIterator.hasNext()) {
         A annotationMirror = (A)listIterator.next();
         String annotationName = this.getAnnotationTypeName(annotationMirror);
         if (!this.isExcludedAnnotation(element, annotationName)) {
            if (DEPRECATED_ANNOTATION_NAMES.containsKey(annotationName)) {
               this.addWarning(
                  element,
                  "Usages of deprecated annotation "
                     + annotationName
                     + " found. You should use "
                     + (String)DEPRECATED_ANNOTATION_NAMES.get(annotationName)
                     + " instead."
               );
            }

            T annotationType = this.getTypeForAnnotation(annotationMirror);
            RetentionPolicy retentionPolicy = this.getRetentionPolicy(annotationType);
            Map<CharSequence, Object> annotationValues = this.populateAnnotationData(
               element, originatingElementIsSameParent, annotationMirror, annotationMetadata, isDeclared, retentionPolicy, allowAliases
            );
            if (isDeclared) {
               this.applyTransformations(
                  listIterator,
                  annotationMetadata,
                  true,
                  annotationType,
                  annotationValues,
                  Collections.emptyList(),
                  null,
                  annotationMetadata::addDeclaredRepeatable,
                  annotationMetadata::addDeclaredAnnotation
               );
            } else if (!this.isInheritedAnnotation(annotationMirror) && !originatingElementIsSameParent) {
               listIterator.remove();
            } else {
               this.applyTransformations(
                  listIterator,
                  annotationMetadata,
                  false,
                  annotationType,
                  annotationValues,
                  Collections.emptyList(),
                  null,
                  annotationMetadata::addRepeatable,
                  annotationMetadata::addAnnotation
               );
            }
         }
      }

      for(A annotationMirror : hierarchyCopy) {
         String annotationTypeName = this.getAnnotationTypeName(annotationMirror);
         String packageName = NameUtils.getPackageName(annotationTypeName);
         if (!AnnotationUtil.STEREOTYPE_EXCLUDES.contains(packageName)) {
            this.processAnnotationStereotype(element, originatingElementIsSameParent, annotationMirror, annotationMetadata, isDeclared);
         }
      }

   }

   protected boolean isExcludedAnnotation(@NonNull T element, @NonNull String annotationName) {
      return AnnotationUtil.INTERNAL_ANNOTATION_NAMES.contains(annotationName);
   }

   protected abstract boolean isInheritedAnnotation(@NonNull A annotationMirror);

   protected abstract boolean isInheritedAnnotationType(@NonNull T annotationType);

   private void buildStereotypeHierarchy(
      List<String> parents, T element, DefaultAnnotationMetadata metadata, boolean isDeclared, boolean isInherited, boolean allowAliases, List<String> excludes
   ) {
      List<? extends A> annotationMirrors = this.getAnnotationsForType(element);
      LinkedList<AnnotationValueBuilder<?>> interceptorBindings = new LinkedList();
      String lastParent = CollectionUtils.last(parents);
      if (!annotationMirrors.isEmpty()) {
         List<A> topLevel = new ArrayList();
         ListIterator<? extends A> listIterator = annotationMirrors.listIterator();

         while(listIterator.hasNext()) {
            A annotationMirror = (A)listIterator.next();
            String annotationName = this.getAnnotationTypeName(annotationMirror);
            if (!annotationName.equals(this.getElementName(element))
               && !AnnotationUtil.INTERNAL_ANNOTATION_NAMES.contains(annotationName)
               && !excludes.contains(annotationName)
               && (!AnnotationUtil.ADVICE_STEREOTYPES.contains(lastParent) || !"io.micronaut.aop.InterceptorBinding".equals(annotationName))) {
               topLevel.add(annotationMirror);
               T annotationTypeMirror = this.getTypeForAnnotation(annotationMirror);
               RetentionPolicy retentionPolicy = this.getRetentionPolicy(annotationTypeMirror);
               Map<CharSequence, Object> data = this.populateAnnotationData(
                  element, (T)null, annotationMirror, metadata, isDeclared, retentionPolicy, allowAliases
               );
               this.handleAnnotationStereotype(
                  parents, metadata, isDeclared, isInherited, interceptorBindings, lastParent, listIterator, annotationTypeMirror, annotationName, data
               );
            }
         }

         topLevel.removeIf(ax -> !annotationMirrors.contains(ax));

         for(A annotationMirror : topLevel) {
            this.processAnnotationStereotype(parents, annotationMirror, metadata, isDeclared, isInherited);
         }
      }

      if (lastParent != null) {
         AnnotationMetadata modifiedStereotypes = (AnnotationMetadata)MUTATED_ANNOTATION_METADATA.get(
            new AbstractAnnotationMetadataBuilder.MetadataKey(lastParent, element)
         );
         if (modifiedStereotypes != null) {
            Set<String> annotationNames = modifiedStereotypes.getAnnotationNames();
            this.handleModifiedStereotypes(parents, metadata, isDeclared, isInherited, excludes, interceptorBindings, lastParent, modifiedStereotypes);

            for(String annotationName : annotationNames) {
               AnnotationValue<Annotation> a = modifiedStereotypes.getAnnotation(annotationName);
               if (a != null) {
                  String stereotypeName = a.getAnnotationName();
                  if (!AnnotationUtil.INTERNAL_ANNOTATION_NAMES.contains(stereotypeName) && !excludes.contains(stereotypeName)) {
                     T annotationType = (T)this.getAnnotationMirror(stereotypeName).orElse(null);
                     if (annotationType != null) {
                        Map<CharSequence, Object> values = a.getValues();
                        this.handleAnnotationStereotype(
                           parents, metadata, isDeclared, isInherited, interceptorBindings, lastParent, null, annotationType, annotationName, values
                        );
                     } else if (isDeclared) {
                        metadata.addDeclaredStereotype(parents, stereotypeName, a.getValues(), a.getRetentionPolicy());
                     } else {
                        metadata.addStereotype(parents, stereotypeName, a.getValues(), a.getRetentionPolicy());
                     }
                  }
               }
            }
         }
      }

      if (!interceptorBindings.isEmpty()) {
         for(AnnotationValueBuilder<?> interceptorBinding : interceptorBindings) {
            if (isDeclared) {
               metadata.addDeclaredRepeatable("io.micronaut.aop.InterceptorBindingDefinitions", interceptorBinding.build());
            } else {
               metadata.addRepeatable("io.micronaut.aop.InterceptorBindingDefinitions", interceptorBinding.build());
            }
         }
      }

   }

   private void handleModifiedStereotypes(
      List<String> parents,
      DefaultAnnotationMetadata metadata,
      boolean isDeclared,
      boolean isInherited,
      List<String> excludes,
      LinkedList<AnnotationValueBuilder<?>> interceptorBindings,
      String lastParent,
      AnnotationMetadata modifiedStereotypes
   ) {
      for(String stereotypeName : modifiedStereotypes.getStereotypeAnnotationNames()) {
         AnnotationValue<Annotation> a = modifiedStereotypes.getAnnotation(stereotypeName);
         if (a != null && !AnnotationUtil.INTERNAL_ANNOTATION_NAMES.contains(stereotypeName) && !excludes.contains(stereotypeName)) {
            T annotationType = (T)this.getAnnotationMirror(stereotypeName).orElse(null);
            List<String> stereotypeParents = modifiedStereotypes.getAnnotationNamesByStereotype(stereotypeName);
            List<String> resolvedParents = new ArrayList(parents);
            resolvedParents.addAll(stereotypeParents);
            Map<CharSequence, Object> values = a.getValues();
            if (annotationType != null) {
               this.handleAnnotationStereotype(
                  resolvedParents, metadata, isDeclared, isInherited, interceptorBindings, lastParent, null, annotationType, stereotypeName, values
               );
            } else {
               metadata.addStereotype(resolvedParents, stereotypeName, values, RetentionPolicy.RUNTIME);
            }
         }
      }

   }

   private void handleAnnotationStereotype(
      List<String> parents,
      DefaultAnnotationMetadata metadata,
      boolean isDeclared,
      boolean isInherited,
      LinkedList<AnnotationValueBuilder<?>> interceptorBindings,
      String lastParent,
      @Nullable ListIterator<? extends A> listIterator,
      T annotationType,
      String annotationName,
      Map<CharSequence, Object> data
   ) {
      this.addToInterceptorBindingsIfNecessary(interceptorBindings, lastParent, annotationName);
      boolean hasInterceptorBinding = !interceptorBindings.isEmpty();
      if (hasInterceptorBinding && "io.micronaut.aop.InterceptorBinding".equals(annotationName)) {
         this.handleMemberBinding(metadata, lastParent, data);
         ((AnnotationValueBuilder)interceptorBindings.getLast()).members(data);
      } else {
         if (annotationName.equals("javax.annotation.Nonnull")) {
            String when = Objects.toString(data.get("when"));
            if (when.equals("UNKNOWN") || when.equals("MAYBE") || when.equals("NEVER")) {
               return;
            }
         }

         if (hasInterceptorBinding && Type.class.getName().equals(annotationName)) {
            Object o = data.get("value");
            AnnotationClassValue<?> interceptorType = null;
            if (o instanceof AnnotationClassValue) {
               interceptorType = (AnnotationClassValue)o;
            } else if (o instanceof AnnotationClassValue[]) {
               AnnotationClassValue[] values = (AnnotationClassValue[])o;
               if (values.length > 0) {
                  interceptorType = values[0];
               }
            }

            if (interceptorType != null) {
               for(AnnotationValueBuilder<?> interceptorBinding : interceptorBindings) {
                  interceptorBinding.member("interceptorType", interceptorType);
               }
            }
         }

         if (isDeclared) {
            this.applyTransformations(
               listIterator,
               metadata,
               true,
               annotationType,
               data,
               parents,
               interceptorBindings,
               (string, av) -> metadata.addDeclaredRepeatableStereotype(parents, string, av),
               (string, valuesx, rp) -> metadata.addDeclaredStereotype(parents, string, valuesx, rp)
            );
         } else if (isInherited) {
            this.applyTransformations(
               listIterator,
               metadata,
               false,
               annotationType,
               data,
               parents,
               interceptorBindings,
               (string, av) -> metadata.addRepeatableStereotype(parents, string, av),
               (string, valuesx, rp) -> metadata.addStereotype(parents, string, valuesx, rp)
            );
         } else if (listIterator != null) {
            listIterator.remove();
         }

      }
   }

   private void handleMemberBinding(DefaultAnnotationMetadata metadata, String lastParent, Map<CharSequence, Object> data) {
      if (data.containsKey("bindMembers")) {
         Object o = data.remove("bindMembers");
         if (o instanceof Boolean && (Boolean)o) {
            Map<CharSequence, Object> values = metadata.getValues(lastParent);
            if (!values.isEmpty()) {
               Set<String> nonBinding = (Set)NON_BINDING_CACHE.computeIfAbsent(lastParent, annotationName -> {
                  HashSet<String> nonBindingResult = new HashSet(5);
                  Map<String, ? extends T> members = this.getAnnotationMembers(lastParent);
                  if (CollectionUtils.isNotEmpty(members)) {
                     members.forEach((name, ann) -> {
                        if (this.hasSimpleAnnotation((T)ann, NonBinding.class.getSimpleName())) {
                           nonBindingResult.add(name);
                        }

                     });
                  }

                  return (Set)(nonBindingResult.isEmpty() ? Collections.emptySet() : nonBindingResult);
               });
               if (!nonBinding.isEmpty()) {
                  values = new HashMap(values);
                  values.keySet().removeAll(nonBinding);
               }

               AnnotationValueBuilder<Annotation> builder = AnnotationValue.<Annotation>builder(lastParent).members(values);
               data.put("bindMembers", builder.build());
            }
         }
      }

   }

   @NonNull
   protected abstract Map<String, ? extends T> getAnnotationMembers(@NonNull String annotationType);

   protected abstract boolean hasSimpleAnnotation(T element, String simpleName);

   private void addToInterceptorBindingsIfNecessary(LinkedList<AnnotationValueBuilder<?>> interceptorBindings, String lastParent, String annotationName) {
      if (lastParent != null) {
         AnnotationValueBuilder<?> interceptorBinding = null;
         if ("io.micronaut.aop.Around".equals(annotationName) || "io.micronaut.aop.InterceptorBinding".equals(annotationName)) {
            interceptorBinding = AnnotationValue.builder("io.micronaut.aop.InterceptorBinding")
               .member("value", new AnnotationClassValue(lastParent))
               .member("kind", "AROUND");
         } else if ("io.micronaut.aop.Introduction".equals(annotationName)) {
            interceptorBinding = AnnotationValue.builder("io.micronaut.aop.InterceptorBinding")
               .member("value", new AnnotationClassValue(lastParent))
               .member("kind", "INTRODUCTION");
         } else if ("io.micronaut.aop.AroundConstruct".equals(annotationName)) {
            interceptorBinding = AnnotationValue.builder("io.micronaut.aop.InterceptorBinding")
               .member("value", new AnnotationClassValue(lastParent))
               .member("kind", "AROUND_CONSTRUCT");
         }

         if (interceptorBinding != null) {
            interceptorBindings.add(interceptorBinding);
         }
      }

   }

   private void buildStereotypeHierarchy(
      List<String> parents, AnnotationValue<?> annotationValue, DefaultAnnotationMetadata metadata, boolean isDeclared, List<String> excludes
   ) {
      List<AnnotationValue<?>> annotationMirrors = annotationValue.getStereotypes();
      LinkedList<AnnotationValueBuilder<?>> interceptorBindings = new LinkedList();
      String lastParent = CollectionUtils.last(parents);
      if (CollectionUtils.isNotEmpty(annotationMirrors)) {
         List<AnnotationValue<?>> topLevel = new ArrayList();

         for(AnnotationValue<?> annotationMirror : annotationMirrors) {
            String annotationName = annotationMirror.getAnnotationName();
            if (!annotationName.equals(annotationValue.getAnnotationName())
               && !AnnotationUtil.INTERNAL_ANNOTATION_NAMES.contains(annotationName)
               && !excludes.contains(annotationName)
               && (!AnnotationUtil.ADVICE_STEREOTYPES.contains(lastParent) || !"io.micronaut.aop.InterceptorBinding".equals(annotationName))) {
               this.addToInterceptorBindingsIfNecessary(interceptorBindings, lastParent, annotationName);
               RetentionPolicy retentionPolicy = annotationMirror.getRetentionPolicy();
               topLevel.add(annotationMirror);
               Map<CharSequence, Object> data = annotationMirror.getValues();
               boolean hasInterceptorBinding = !interceptorBindings.isEmpty();
               if (hasInterceptorBinding && "io.micronaut.aop.InterceptorBinding".equals(annotationName)) {
                  this.handleMemberBinding(metadata, lastParent, data);
                  ((AnnotationValueBuilder)interceptorBindings.getLast()).members(data);
               } else {
                  if (hasInterceptorBinding && Type.class.getName().equals(annotationName)) {
                     Object o = data.get("value");
                     AnnotationClassValue<?> interceptorType = null;
                     if (o instanceof AnnotationClassValue) {
                        interceptorType = (AnnotationClassValue)o;
                     } else if (o instanceof AnnotationClassValue[]) {
                        AnnotationClassValue[] values = (AnnotationClassValue[])o;
                        if (values.length > 0) {
                           interceptorType = values[0];
                        }
                     }

                     if (interceptorType != null) {
                        for(AnnotationValueBuilder<?> interceptorBinding : interceptorBindings) {
                           interceptorBinding.member("interceptorType", interceptorType);
                        }
                     }
                  }

                  if (isDeclared) {
                     metadata.addDeclaredStereotype(parents, annotationName, data, retentionPolicy);
                  } else {
                     metadata.addStereotype(parents, annotationName, data, retentionPolicy);
                  }
               }
            }
         }

         for(AnnotationValue<?> annotationMirror : topLevel) {
            this.processAnnotationStereotype(parents, annotationMirror, metadata, isDeclared);
         }
      }

      if (!interceptorBindings.isEmpty()) {
         for(AnnotationValueBuilder<?> interceptorBinding : interceptorBindings) {
            if (isDeclared) {
               metadata.addDeclaredRepeatable("io.micronaut.aop.InterceptorBindingDefinitions", interceptorBinding.build());
            } else {
               metadata.addRepeatable("io.micronaut.aop.InterceptorBindingDefinitions", interceptorBinding.build());
            }
         }
      }

   }

   private void processAnnotationStereotype(
      T element, boolean originatingElementIsSameParent, A annotationMirror, DefaultAnnotationMetadata annotationMetadata, boolean isDeclared
   ) {
      T annotationType = this.getTypeForAnnotation(annotationMirror);
      String parentAnnotationName = this.getAnnotationTypeName(annotationMirror);
      if (!parentAnnotationName.endsWith(".Nullable")) {
         this.processAnnotationStereotypes(
            annotationMetadata,
            isDeclared,
            this.isInheritedAnnotation(annotationMirror) || originatingElementIsSameParent,
            annotationType,
            parentAnnotationName,
            Collections.emptyList()
         );
      }

   }

   private void processAnnotationStereotypes(
      DefaultAnnotationMetadata annotationMetadata, boolean isDeclared, boolean isInherited, T annotationType, String annotationName, List<String> excludes
   ) {
      List<String> parentAnnotations = new ArrayList();
      parentAnnotations.add(annotationName);
      this.buildStereotypeHierarchy(parentAnnotations, annotationType, annotationMetadata, isDeclared, isInherited, true, excludes);
   }

   private void processAnnotationStereotypes(
      DefaultAnnotationMetadata annotationMetadata, boolean isDeclared, AnnotationValue<?> annotation, List<String> parents
   ) {
      List<String> parentAnnotations = new ArrayList(parents);
      parentAnnotations.add(annotation.getAnnotationName());
      this.buildStereotypeHierarchy(parentAnnotations, annotation, annotationMetadata, isDeclared, Collections.emptyList());
   }

   private void processAnnotationStereotype(
      List<String> parents, A annotationMirror, DefaultAnnotationMetadata metadata, boolean isDeclared, boolean isInherited
   ) {
      T typeForAnnotation = this.getTypeForAnnotation(annotationMirror);
      String annotationTypeName = this.getAnnotationTypeName(annotationMirror);
      this.processAnnotationStereotype(parents, typeForAnnotation, annotationTypeName, metadata, isDeclared, isInherited);
   }

   private void processAnnotationStereotype(
      List<String> parents, T annotationType, String annotationTypeName, DefaultAnnotationMetadata metadata, boolean isDeclared, boolean isInherited
   ) {
      List<String> stereoTypeParents = new ArrayList(parents);
      stereoTypeParents.add(annotationTypeName);
      this.buildStereotypeHierarchy(stereoTypeParents, annotationType, metadata, isDeclared, isInherited, true, Collections.emptyList());
   }

   private void processAnnotationStereotype(List<String> parents, AnnotationValue<?> annotationType, DefaultAnnotationMetadata metadata, boolean isDeclared) {
      List<String> stereoTypeParents = new ArrayList(parents);
      stereoTypeParents.add(annotationType.getAnnotationName());
      this.buildStereotypeHierarchy(stereoTypeParents, annotationType, metadata, isDeclared, Collections.emptyList());
   }

   private void applyTransformations(
      @Nullable ListIterator<? extends A> hierarchyIterator,
      DefaultAnnotationMetadata annotationMetadata,
      boolean isDeclared,
      @NonNull T annotationType,
      Map<CharSequence, Object> data,
      List<String> parents,
      @Nullable LinkedList<AnnotationValueBuilder<?>> interceptorBindings,
      BiConsumer<String, AnnotationValue> addRepeatableAnnotation,
      AbstractAnnotationMetadataBuilder.TriConsumer<String, Map<CharSequence, Object>, RetentionPolicy> addAnnotation
   ) {
      this.applyTransformationsForAnnotationType(
         hierarchyIterator, annotationMetadata, isDeclared, annotationType, data, parents, interceptorBindings, addRepeatableAnnotation, addAnnotation
      );
   }

   private void applyTransformationsForAnnotationType(
      @Nullable ListIterator<? extends A> hierarchyIterator,
      DefaultAnnotationMetadata annotationMetadata,
      boolean isDeclared,
      @NonNull T annotationType,
      Map<CharSequence, Object> data,
      List<String> parents,
      @Nullable LinkedList<AnnotationValueBuilder<?>> interceptorBindings,
      BiConsumer<String, AnnotationValue> addRepeatableAnnotation,
      AbstractAnnotationMetadataBuilder.TriConsumer<String, Map<CharSequence, Object>, RetentionPolicy> addAnnotation
   ) {
      String annotationName = this.getElementName(annotationType);
      String packageName = NameUtils.getPackageName(annotationName);
      String repeatableName = this.getRepeatableNameForType(annotationType);
      RetentionPolicy retentionPolicy = this.getRetentionPolicy(annotationType);
      List<AnnotationRemapper> annotationRemappers = (List)ANNOTATION_REMAPPERS.get(packageName);
      List<AnnotationTransformer<Annotation>> annotationTransformers = this.getAnnotationTransformers(annotationName);
      boolean remapped = CollectionUtils.isNotEmpty(annotationRemappers);
      boolean transformed = CollectionUtils.isNotEmpty(annotationTransformers);
      if (repeatableName != null) {
         if (!remapped && !transformed) {
            AnnotationValue av = new AnnotationValue(annotationName, data);
            addRepeatableAnnotation.accept(repeatableName, av);
         } else if (remapped) {
            VisitorContext visitorContext = this.createVisitorContext();
            AnnotationValue<?> av = new AnnotationValue(annotationName, data);
            AnnotationValue<?> repeatableAnn = AnnotationValue.builder(repeatableName).values(av).build();
            boolean wasRemapped = false;

            for(AnnotationRemapper annotationRemapper : annotationRemappers) {
               List<AnnotationValue<?>> remappedRepeatable = annotationRemapper.remap(repeatableAnn, visitorContext);
               List<AnnotationValue<?>> remappedValue = annotationRemapper.remap(av, visitorContext);
               if (CollectionUtils.isNotEmpty(remappedRepeatable)) {
                  for(AnnotationValue<?> repeatable : remappedRepeatable) {
                     for(AnnotationValue<?> rmv : remappedValue) {
                        if (rmv == av && remappedValue.size() == 1) {
                           addRepeatableAnnotation.accept(repeatableName, av);
                           break;
                        }

                        wasRemapped = true;
                        addRepeatableAnnotation.accept(repeatable.getAnnotationName(), rmv);
                     }
                  }
               }
            }

            if (wasRemapped && hierarchyIterator != null) {
               hierarchyIterator.remove();
            }
         } else {
            VisitorContext visitorContext = this.createVisitorContext();
            AnnotationValue<Annotation> av = new AnnotationValue<>(annotationName, data);
            AnnotationValue<Annotation> repeatableAnn = AnnotationValue.<Annotation>builder(repeatableName).values(av).build();
            List<AnnotationTransformer<Annotation>> repeatableTransformers = this.getAnnotationTransformers(repeatableName);
            if (hierarchyIterator != null) {
               hierarchyIterator.remove();
            }

            if (CollectionUtils.isNotEmpty(repeatableTransformers)) {
               for(AnnotationTransformer<Annotation> repeatableTransformer : repeatableTransformers) {
                  for(AnnotationValue<?> annotationValue : repeatableTransformer.transform(repeatableAnn, visitorContext)) {
                     for(AnnotationTransformer<Annotation> transformer : annotationTransformers) {
                        for(AnnotationValue<?> value : transformer.transform(av, visitorContext)) {
                           addRepeatableAnnotation.accept(annotationValue.getAnnotationName(), value);
                           if (CollectionUtils.isNotEmpty(value.getStereotypes())) {
                              this.addTransformedStereotypes(annotationMetadata, isDeclared, value, parents);
                           } else {
                              this.addTransformedStereotypes(annotationMetadata, isDeclared, value.getAnnotationName(), parents);
                           }
                        }
                     }
                  }
               }
            } else {
               for(AnnotationTransformer<Annotation> transformer : annotationTransformers) {
                  for(AnnotationValue<?> value : transformer.transform(av, visitorContext)) {
                     addRepeatableAnnotation.accept(repeatableName, value);
                     if (CollectionUtils.isNotEmpty(value.getStereotypes())) {
                        this.addTransformedStereotypes(annotationMetadata, isDeclared, value, parents);
                     } else {
                        this.addTransformedStereotypes(annotationMetadata, isDeclared, value.getAnnotationName(), parents);
                     }
                  }
               }
            }
         }
      } else if (!remapped && !transformed) {
         addAnnotation.accept((T)annotationName, data, retentionPolicy);
      } else if (remapped) {
         AnnotationValue<?> av = new AnnotationValue(annotationName, data);
         VisitorContext visitorContext = this.createVisitorContext();
         boolean wasRemapped = false;

         for(AnnotationRemapper annotationRemapper : annotationRemappers) {
            List<AnnotationValue<?>> remappedValues = annotationRemapper.remap(av, visitorContext);
            if (CollectionUtils.isNotEmpty(remappedValues)) {
               for(AnnotationValue<?> annotationValue : remappedValues) {
                  if (annotationValue == av && remappedValues.size() == 1) {
                     addAnnotation.accept((T)annotationName, data, retentionPolicy);
                     break;
                  }

                  wasRemapped = true;
                  String transformedAnnotationName = this.handleTransformedAnnotationValue(
                     parents, interceptorBindings, addRepeatableAnnotation, addAnnotation, annotationValue, annotationMetadata
                  );
                  if (CollectionUtils.isNotEmpty(annotationValue.getStereotypes())) {
                     this.addTransformedStereotypes(annotationMetadata, isDeclared, annotationValue, parents);
                  } else {
                     this.addTransformedStereotypes(annotationMetadata, isDeclared, transformedAnnotationName, parents);
                  }
               }
            }
         }

         if (wasRemapped && hierarchyIterator != null) {
            hierarchyIterator.remove();
         }
      } else {
         AnnotationValue<Annotation> av = new AnnotationValue<>(annotationName, data);
         VisitorContext visitorContext = this.createVisitorContext();
         if (hierarchyIterator != null) {
            hierarchyIterator.remove();
         }

         for(AnnotationTransformer<Annotation> annotationTransformer : annotationTransformers) {
            for(AnnotationValue<?> transformedValue : annotationTransformer.transform(av, visitorContext)) {
               String transformedAnnotationName = this.handleTransformedAnnotationValue(
                  parents, interceptorBindings, addRepeatableAnnotation, addAnnotation, transformedValue, annotationMetadata
               );
               if (CollectionUtils.isNotEmpty(transformedValue.getStereotypes())) {
                  this.addTransformedStereotypes(annotationMetadata, isDeclared, transformedValue, parents);
               } else {
                  this.addTransformedStereotypes(annotationMetadata, isDeclared, transformedAnnotationName, parents);
               }
            }
         }
      }

   }

   private String handleTransformedAnnotationValue(
      List<String> parents,
      LinkedList<AnnotationValueBuilder<?>> interceptorBindings,
      BiConsumer<String, AnnotationValue> addRepeatableAnnotation,
      AbstractAnnotationMetadataBuilder.TriConsumer<String, Map<CharSequence, Object>, RetentionPolicy> addAnnotation,
      AnnotationValue<?> transformedValue,
      DefaultAnnotationMetadata annotationMetadata
   ) {
      String transformedAnnotationName = transformedValue.getAnnotationName();
      this.addTransformedInterceptorBindingsIfNecessary(parents, interceptorBindings, transformedValue, transformedAnnotationName, annotationMetadata);
      String transformedRepeatableName;
      if (this.isRepeatableCandidate(transformedAnnotationName)) {
         String resolvedName = null;

         try {
            resolvedName = (String)this.getAnnotationMirror(transformedAnnotationName).map(this::getRepeatableNameForType).orElse(null);
         } catch (Exception var11) {
         }

         transformedRepeatableName = resolvedName;
      } else {
         transformedRepeatableName = null;
      }

      if (transformedRepeatableName != null) {
         addRepeatableAnnotation.accept(transformedRepeatableName, transformedValue);
      } else {
         addAnnotation.accept((T)transformedAnnotationName, transformedValue.getValues(), transformedValue.getRetentionPolicy());
      }

      return transformedAnnotationName;
   }

   private void addTransformedInterceptorBindingsIfNecessary(
      List<String> parents,
      LinkedList<AnnotationValueBuilder<?>> interceptorBindings,
      AnnotationValue<?> transformedValue,
      String transformedAnnotationName,
      DefaultAnnotationMetadata annotationMetadata
   ) {
      if (interceptorBindings != null && !parents.isEmpty() && "io.micronaut.aop.InterceptorBinding".equals(transformedAnnotationName)) {
         AnnotationValueBuilder<Annotation> newBuilder = AnnotationValue.<Annotation>builder(transformedAnnotationName, transformedValue.getRetentionPolicy())
            .members(transformedValue.getValues());
         if (!transformedValue.contains("value")) {
            newBuilder.value((String)parents.get(parents.size() - 1));
         }

         if (transformedValue.booleanValue("bindMembers").orElse(false)) {
            String parent = CollectionUtils.last(parents);
            HashMap<CharSequence, Object> data = new HashMap(transformedValue.getValues());
            this.handleMemberBinding(annotationMetadata, parent, data);
            newBuilder.members(data);
         }

         interceptorBindings.add(newBuilder);
      }

   }

   private List<AnnotationValue<?>> remapAnnotation(String annotationName) {
      String packageName = NameUtils.getPackageName(annotationName);
      List<AnnotationRemapper> annotationRemappers = (List)ANNOTATION_REMAPPERS.get(packageName);
      List<AnnotationValue<?>> mappedAnnotations = new ArrayList();
      if (annotationRemappers != null && !annotationRemappers.isEmpty()) {
         VisitorContext visitorContext = this.createVisitorContext();
         AnnotationValue<?> av = new AnnotationValue(annotationName);

         for(AnnotationRemapper annotationRemapper : annotationRemappers) {
            List<AnnotationValue<?>> remappedValues = annotationRemapper.remap(av, visitorContext);
            if (CollectionUtils.isNotEmpty(remappedValues)) {
               for(AnnotationValue<?> annotationValue : remappedValues) {
                  if (annotationValue == av && remappedValues.size() == 1) {
                     break;
                  }

                  mappedAnnotations.add(annotationValue);
               }
            }
         }

         return mappedAnnotations;
      } else {
         mappedAnnotations.add(AnnotationValue.builder(annotationName).build());
         return mappedAnnotations;
      }
   }

   private boolean isRepeatableCandidate(String transformedAnnotationName) {
      return !AnnotationUtil.INTERNAL_ANNOTATION_NAMES.contains(transformedAnnotationName)
         && !"javax.annotation.Nullable".equals(transformedAnnotationName)
         && !"javax.annotation.Nonnull".equals(transformedAnnotationName);
   }

   private void addTransformedStereotypes(
      DefaultAnnotationMetadata annotationMetadata, boolean isDeclared, String transformedAnnotationName, List<String> parents
   ) {
      if (!AnnotationUtil.INTERNAL_ANNOTATION_NAMES.contains(transformedAnnotationName)) {
         String packageName = NameUtils.getPackageName(transformedAnnotationName);
         if (!AnnotationUtil.STEREOTYPE_EXCLUDES.contains(packageName)) {
            this.getAnnotationMirror(transformedAnnotationName)
               .ifPresent(a -> this.processAnnotationStereotypes(annotationMetadata, isDeclared, false, (T)a, transformedAnnotationName, parents));
         }
      }

   }

   private void addTransformedStereotypes(
      DefaultAnnotationMetadata annotationMetadata, boolean isDeclared, AnnotationValue<?> transformedAnnotation, List<String> parents
   ) {
      String transformedAnnotationName = transformedAnnotation.getAnnotationName();
      if (!AnnotationUtil.INTERNAL_ANNOTATION_NAMES.contains(transformedAnnotationName)) {
         String packageName = NameUtils.getPackageName(transformedAnnotationName);
         if (!AnnotationUtil.STEREOTYPE_EXCLUDES.contains(packageName)) {
            this.processAnnotationStereotypes(annotationMetadata, isDeclared, transformedAnnotation, parents);
         }
      }

   }

   @Internal
   public static void addMutatedMetadata(String declaringType, Object element, AnnotationMetadata metadata) {
      if (element != null && metadata != null) {
         MUTATED_ANNOTATION_METADATA.put(new AbstractAnnotationMetadataBuilder.MetadataKey<>(declaringType, element), metadata);
      }

   }

   @Internal
   public static boolean isMetadataMutated(String declaringType, Object element) {
      return element != null ? MUTATED_ANNOTATION_METADATA.containsKey(new AbstractAnnotationMetadataBuilder.MetadataKey<>(declaringType, element)) : false;
   }

   @Internal
   public static void clearMutated() {
      MUTATED_ANNOTATION_METADATA.clear();
   }

   @Internal
   public static void clearCaches() {
      ANNOTATION_DEFAULTS.clear();
   }

   @Internal
   public static void copyToRuntime() {
      ANNOTATION_DEFAULTS.forEach(DefaultAnnotationMetadata::registerAnnotationDefaults);
   }

   @Internal
   public static boolean isAnnotationMapped(@Nullable String annotationName) {
      return annotationName != null
         && (
            ANNOTATION_MAPPERS.containsKey(annotationName)
               || ANNOTATION_TRANSFORMERS.containsKey(annotationName)
               || ANNOTATION_TRANSFORMERS.keySet().stream().anyMatch(annotationName::startsWith)
         );
   }

   @Internal
   public static Set<String> getMappedAnnotationNames() {
      HashSet<String> all = new HashSet(ANNOTATION_MAPPERS.keySet());
      all.addAll(ANNOTATION_TRANSFORMERS.keySet());
      return all;
   }

   @Internal
   public static Set<String> getMappedAnnotationPackages() {
      return ANNOTATION_REMAPPERS.keySet();
   }

   public <A2 extends Annotation> AnnotationMetadata annotate(AnnotationMetadata annotationMetadata, AnnotationValue<A2> annotationValue) {
      String annotationName = annotationValue.getAnnotationName();
      boolean isReference = annotationMetadata instanceof AnnotationMetadataReference;
      boolean isReferenceOrEmpty = annotationMetadata == AnnotationMetadata.EMPTY_METADATA || isReference;
      if (!(annotationMetadata instanceof DefaultAnnotationMetadata) && !isReferenceOrEmpty) {
         if (annotationMetadata instanceof AnnotationMetadataHierarchy) {
            AnnotationMetadataHierarchy hierarchy = (AnnotationMetadataHierarchy)annotationMetadata;
            AnnotationMetadata declaredMetadata = this.annotate(hierarchy.getDeclaredMetadata(), annotationValue);
            return hierarchy.createSibling(declaredMetadata);
         } else {
            return annotationMetadata;
         }
      } else {
         DefaultAnnotationMetadata defaultMetadata = (DefaultAnnotationMetadata)(isReferenceOrEmpty
            ? new MutableAnnotationMetadata()
            : (DefaultAnnotationMetadata)annotationMetadata);
         T annotationMirror = (T)this.getAnnotationMirror(annotationName).orElse(null);
         if (annotationMirror != null) {
            this.applyTransformationsForAnnotationType(
               null,
               defaultMetadata,
               true,
               annotationMirror,
               annotationValue.getValues(),
               Collections.emptyList(),
               new LinkedList(),
               defaultMetadata::addDeclaredRepeatable,
               defaultMetadata::addDeclaredAnnotation
            );
            this.processAnnotationDefaults(
               annotationMirror, defaultMetadata, annotationName, () -> this.readAnnotationDefaultValues(annotationName, annotationMirror)
            );
            this.processAnnotationStereotypes(
               defaultMetadata, true, this.isInheritedAnnotationType(annotationMirror), annotationMirror, annotationName, DEFAULT_ANNOTATE_EXCLUDES
            );
         } else {
            defaultMetadata.addDeclaredAnnotation(annotationName, annotationValue.getValues());
         }

         if (isReference) {
            AnnotationMetadataReference ref = (AnnotationMetadataReference)annotationMetadata;
            return new AnnotationMetadataHierarchy(ref, defaultMetadata);
         } else {
            return defaultMetadata;
         }
      }
   }

   public AnnotationMetadata removeAnnotation(AnnotationMetadata annotationMetadata, String annotationType) {
      boolean isHierarchy = annotationMetadata instanceof AnnotationMetadataHierarchy;
      AnnotationMetadata declaredMetadata = annotationMetadata;
      if (isHierarchy) {
         declaredMetadata = annotationMetadata.getDeclaredMetadata();
      }

      if (declaredMetadata instanceof DefaultAnnotationMetadata) {
         DefaultAnnotationMetadata defaultMetadata = (DefaultAnnotationMetadata)declaredMetadata;
         T annotationMirror = (T)this.getAnnotationMirror(annotationType).orElse(null);
         if (annotationMirror != null) {
            String repeatableName = this.getRepeatableNameForType(annotationMirror);
            if (repeatableName != null) {
               defaultMetadata.removeAnnotation(repeatableName);
            } else {
               defaultMetadata.removeAnnotation(annotationType);
            }
         } else {
            defaultMetadata.removeAnnotation(annotationType);
         }

         return isHierarchy ? ((AnnotationMetadataHierarchy)annotationMetadata).createSibling(declaredMetadata) : declaredMetadata;
      } else {
         return annotationMetadata;
      }
   }

   public AnnotationMetadata removeStereotype(AnnotationMetadata annotationMetadata, String annotationType) {
      boolean isHierarchy = annotationMetadata instanceof AnnotationMetadataHierarchy;
      AnnotationMetadata declaredMetadata = annotationMetadata;
      if (isHierarchy) {
         declaredMetadata = annotationMetadata.getDeclaredMetadata();
      }

      if (declaredMetadata instanceof DefaultAnnotationMetadata) {
         DefaultAnnotationMetadata defaultMetadata = (DefaultAnnotationMetadata)declaredMetadata;
         T annotationMirror = (T)this.getAnnotationMirror(annotationType).orElse(null);
         if (annotationMirror != null) {
            String repeatableName = this.getRepeatableNameForType(annotationMirror);
            if (repeatableName != null) {
               defaultMetadata.removeStereotype(repeatableName);
            } else {
               defaultMetadata.removeStereotype(annotationType);
            }
         } else {
            defaultMetadata.removeStereotype(annotationType);
         }

         return isHierarchy ? ((AnnotationMetadataHierarchy)annotationMetadata).createSibling(declaredMetadata) : declaredMetadata;
      } else {
         return annotationMetadata;
      }
   }

   @NonNull
   public <T1 extends Annotation> AnnotationMetadata removeAnnotationIf(
      @NonNull AnnotationMetadata annotationMetadata, @NonNull Predicate<AnnotationValue<T1>> predicate
   ) {
      boolean isHierarchy = annotationMetadata instanceof AnnotationMetadataHierarchy;
      AnnotationMetadata declaredMetadata = annotationMetadata;
      if (isHierarchy) {
         declaredMetadata = annotationMetadata.getDeclaredMetadata();
      }

      if (declaredMetadata instanceof DefaultAnnotationMetadata) {
         DefaultAnnotationMetadata defaultMetadata = (DefaultAnnotationMetadata)declaredMetadata;
         defaultMetadata.removeAnnotationIf(predicate);
         return isHierarchy ? ((AnnotationMetadataHierarchy)annotationMetadata).createSibling(declaredMetadata) : declaredMetadata;
      } else {
         return annotationMetadata;
      }
   }

   static {
      for(ServiceDefinition<AnnotationMapper> definition : SoftServiceLoader.load(
         AnnotationMapper.class, AbstractAnnotationMetadataBuilder.class.getClassLoader()
      )) {
         if (definition.isPresent()) {
            AnnotationMapper mapper = definition.load();

            try {
               String name = null;
               if (mapper instanceof TypedAnnotationMapper) {
                  name = ((TypedAnnotationMapper)mapper).annotationType().getName();
               } else if (mapper instanceof NamedAnnotationMapper) {
                  name = ((NamedAnnotationMapper)mapper).getName();
               }

               if (StringUtils.isNotEmpty(name)) {
                  ((List)ANNOTATION_MAPPERS.computeIfAbsent(name, s -> new ArrayList(2))).add(mapper);
               }
            } catch (Throwable var9) {
            }
         }
      }

      for(ServiceDefinition<AnnotationTransformer> definition : SoftServiceLoader.load(
         AnnotationTransformer.class, AbstractAnnotationMetadataBuilder.class.getClassLoader()
      )) {
         if (definition.isPresent()) {
            AnnotationTransformer transformer = definition.load();

            try {
               String name = null;
               if (transformer instanceof TypedAnnotationTransformer) {
                  name = ((TypedAnnotationTransformer)transformer).annotationType().getName();
               } else if (transformer instanceof NamedAnnotationTransformer) {
                  name = ((NamedAnnotationTransformer)transformer).getName();
               }

               if (StringUtils.isNotEmpty(name)) {
                  ((List)ANNOTATION_TRANSFORMERS.computeIfAbsent(name, s -> new ArrayList(2))).add(transformer);
               }
            } catch (Throwable var8) {
            }
         }
      }

      for(ServiceDefinition<AnnotationRemapper> definition : SoftServiceLoader.load(
         AnnotationRemapper.class, AbstractAnnotationMetadataBuilder.class.getClassLoader()
      )) {
         if (definition.isPresent()) {
            AnnotationRemapper mapper = definition.load();

            try {
               String name = mapper.getPackageName();
               if (StringUtils.isNotEmpty(name)) {
                  ((List)ANNOTATION_REMAPPERS.computeIfAbsent(name, s -> new ArrayList(2))).add(mapper);
               }
            } catch (Throwable var7) {
            }
         }
      }

   }

   private static class MetadataKey<T> {
      final String declaringName;
      final T element;

      MetadataKey(String declaringName, T element) {
         this.declaringName = declaringName;
         this.element = element;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AbstractAnnotationMetadataBuilder.MetadataKey that = (AbstractAnnotationMetadataBuilder.MetadataKey)o;
            return this.declaringName.equals(that.declaringName) && this.element.equals(that.element);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.declaringName, this.element});
      }
   }

   private interface TriConsumer<T, U, V> {
      void accept(T t, U u, V v);
   }
}
