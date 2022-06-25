package io.micronaut.validation.validator;

import io.micronaut.aop.Intercepted;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.MessageSource;
import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.annotation.AnnotatedElement;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ArgumentValue;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.MethodReference;
import io.micronaut.inject.annotation.AnnotatedElementValidator;
import io.micronaut.inject.validation.BeanDefinitionValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import io.micronaut.validation.validator.constraints.ConstraintValidatorRegistry;
import io.micronaut.validation.validator.extractors.ValueExtractorRegistry;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.validation.ClockProvider;
import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.MethodType;
import javax.validation.metadata.PropertyDescriptor;
import javax.validation.metadata.Scope;
import javax.validation.valueextraction.ValueExtractor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Singleton
@Primary
@Requires(
   property = "micronaut.validator.enabled",
   value = "true",
   defaultValue = "true"
)
public class DefaultValidator implements Validator, ExecutableMethodValidator, ReactiveValidator, AnnotatedElementValidator, BeanDefinitionValidator {
   private static final List<Class<?>> DEFAULT_GROUPS = Collections.singletonList(Default.class);
   private final ConstraintValidatorRegistry constraintValidatorRegistry;
   private final ClockProvider clockProvider;
   private final ValueExtractorRegistry valueExtractorRegistry;
   private final TraversableResolver traversableResolver;
   private final ExecutionHandleLocator executionHandleLocator;
   private final MessageSource messageSource;

   protected DefaultValidator(@NonNull ValidatorConfiguration configuration) {
      ArgumentUtils.requireNonNull("configuration", configuration);
      this.constraintValidatorRegistry = configuration.getConstraintValidatorRegistry();
      this.clockProvider = configuration.getClockProvider();
      this.valueExtractorRegistry = configuration.getValueExtractorRegistry();
      this.traversableResolver = configuration.getTraversableResolver();
      this.executionHandleLocator = configuration.getExecutionHandleLocator();
      this.messageSource = configuration.getMessageSource();
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validate(@NonNull T object, @Nullable Class<?>... groups) {
      ArgumentUtils.requireNonNull("object", object);
      BeanIntrospection<T> introspection = this.getBeanIntrospection(object);
      return introspection == null ? Collections.emptySet() : this.validate(introspection, object, groups);
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validate(@NonNull BeanIntrospection<T> introspection, @NonNull T object, @Nullable Class<?>... groups) {
      if (introspection == null) {
         throw new ValidationException("Passed object [" + object + "] cannot be introspected. Please annotate with @Introspected");
      } else {
         Collection<? extends BeanProperty<Object, Object>> constrainedProperties = introspection.getIndexedProperties(Constraint.class);
         Collection<BeanProperty<Object, Object>> cascadeProperties = introspection.getIndexedProperties(Valid.class);
         List<Class<? extends Annotation>> pojoConstraints = introspection.getAnnotationTypesByStereotype(Constraint.class);
         if (!CollectionUtils.isNotEmpty(constrainedProperties)
            && !CollectionUtils.isNotEmpty(cascadeProperties)
            && !CollectionUtils.isNotEmpty(pojoConstraints)) {
            return Collections.emptySet();
         } else {
            DefaultValidator.DefaultConstraintValidatorContext context = new DefaultValidator.DefaultConstraintValidatorContext(object, groups);
            Set<ConstraintViolation<T>> overallViolations = new HashSet(5);
            return this.doValidate(introspection, object, object, constrainedProperties, cascadeProperties, context, overallViolations, pojoConstraints);
         }
      }
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validateProperty(@NonNull T object, @NonNull String propertyName, @Nullable Class<?>... groups) {
      ArgumentUtils.requireNonNull("object", object);
      ArgumentUtils.requireNonNull("propertyName", (T)propertyName);
      BeanIntrospection<Object> introspection = this.getBeanIntrospection(object);
      if (introspection == null) {
         throw new ValidationException("Passed object [" + object + "] cannot be introspected. Please annotate with @Introspected");
      } else {
         Optional<BeanProperty<Object, Object>> property = introspection.getProperty(propertyName);
         if (property.isPresent()) {
            BeanProperty<Object, Object> constrainedProperty = (BeanProperty)property.get();
            DefaultValidator.DefaultConstraintValidatorContext context = new DefaultValidator.DefaultConstraintValidatorContext(object, groups);
            Set overallViolations = new HashSet(5);
            Object propertyValue = constrainedProperty.get(object);
            Class<T> rootBeanClass = object.getClass();
            this.validateConstrainedPropertyInternal(
               rootBeanClass, object, object, constrainedProperty, constrainedProperty.getType(), propertyValue, context, overallViolations, null
            );
            return Collections.unmodifiableSet(overallViolations);
         } else {
            return Collections.emptySet();
         }
      }
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validateValue(
      @NonNull Class<T> beanType, @NonNull String propertyName, @Nullable Object value, @Nullable Class<?>... groups
   ) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      ArgumentUtils.requireNonNull("propertyName", (T)propertyName);
      BeanIntrospection<Object> introspection = this.getBeanIntrospection(beanType);
      if (introspection == null) {
         throw new ValidationException("Passed bean type [" + beanType + "] cannot be introspected. Please annotate with @Introspected");
      } else {
         BeanProperty<Object, Object> beanProperty = (BeanProperty)introspection.getProperty(propertyName)
            .orElseThrow(() -> new ValidationException("No property [" + propertyName + "] found on type: " + beanType));
         HashSet overallViolations = new HashSet(5);
         DefaultValidator.DefaultConstraintValidatorContext context = new DefaultValidator.DefaultConstraintValidatorContext(groups);

         try {
            context.addPropertyNode(propertyName, null);
            this.validatePropertyInternal(beanType, (T)null, null, context, overallViolations, beanProperty.getType(), beanProperty, value);
         } finally {
            context.removeLast();
         }

         return Collections.unmodifiableSet(overallViolations);
      }
   }

   @NonNull
   @Override
   public Set<String> validatedAnnotatedElement(@NonNull AnnotatedElement element, @Nullable Object value) {
      ArgumentUtils.requireNonNull("element", element);
      if (!element.getAnnotationMetadata().hasStereotype(Constraint.class)) {
         return Collections.emptySet();
      } else {
         Set<ConstraintViolation<Object>> overallViolations = new HashSet(5);
         DefaultValidator.DefaultConstraintValidatorContext context = new DefaultValidator.DefaultConstraintValidatorContext(new Class[0]);

         try {
            context.addPropertyNode(element.getName(), null);
            this.validatePropertyInternal(null, element, element, context, overallViolations, value != null ? value.getClass() : Object.class, element, value);
         } finally {
            context.removeLast();
         }

         return Collections.unmodifiableSet((Set)overallViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()));
      }
   }

   @NonNull
   @Override
   public <T> T createValid(@NonNull Class<T> beanType, Object... arguments) throws ConstraintViolationException {
      ArgumentUtils.requireNonNull("type", (T)beanType);
      BeanIntrospection<T> introspection = this.getBeanIntrospection(beanType);
      if (introspection == null) {
         throw new ValidationException("Passed bean type [" + beanType + "] cannot be introspected. Please annotate with @Introspected");
      } else {
         Set<ConstraintViolation<T>> constraintViolations = this.validateConstructorParameters(introspection, arguments);
         if (constraintViolations.isEmpty()) {
            T instance = introspection.instantiate(arguments);
            Set<ConstraintViolation<T>> errors = this.validate(introspection, instance);
            if (errors.isEmpty()) {
               return instance;
            } else {
               throw new ConstraintViolationException(errors);
            }
         } else {
            throw new ConstraintViolationException(constraintViolations);
         }
      }
   }

   @Override
   public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
      return (BeanDescriptor)BeanIntrospector.SHARED
         .findIntrospection(clazz)
         .map(IntrospectedBeanDescriptor::new)
         .orElseGet(() -> new DefaultValidator.EmptyDescriptor(clazz));
   }

   @Override
   public <T> T unwrap(Class<T> type) {
      throw new UnsupportedOperationException("Validator unwrapping not supported by this implementation");
   }

   @NonNull
   @Override
   public ExecutableMethodValidator forExecutables() {
      return this;
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validateParameters(
      @NonNull T object, @NonNull ExecutableMethod method, @NonNull Object[] parameterValues, @Nullable Class<?>... groups
   ) {
      ArgumentUtils.requireNonNull("parameterValues", parameterValues);
      ArgumentUtils.requireNonNull("object", object);
      ArgumentUtils.requireNonNull("method", method);
      Argument[] arguments = method.getArguments();
      int argLen = arguments.length;
      if (argLen != parameterValues.length) {
         throw new IllegalArgumentException("The method parameter array must have exactly " + argLen + " elements.");
      } else {
         DefaultValidator.DefaultConstraintValidatorContext context = new DefaultValidator.DefaultConstraintValidatorContext(object, groups);
         Set overallViolations = new HashSet(5);
         Path.Node node = context.addMethodNode(method);

         try {
            Class<T> rootClass = object.getClass();
            this.validateParametersInternal(rootClass, object, parameterValues, arguments, argLen, context, overallViolations, node);
         } finally {
            context.removeLast();
         }

         return Collections.unmodifiableSet(overallViolations);
      }
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validateParameters(
      @NonNull T object, @NonNull ExecutableMethod method, @NonNull Collection<MutableArgumentValue<?>> argumentValues, @Nullable Class<?>... groups
   ) {
      ArgumentUtils.requireNonNull("object", object);
      ArgumentUtils.requireNonNull("method", method);
      ArgumentUtils.requireNonNull("parameterValues", (T)argumentValues);
      Argument[] arguments = method.getArguments();
      int argLen = arguments.length;
      if (argLen != argumentValues.size()) {
         throw new IllegalArgumentException("The method parameter array must have exactly " + argLen + " elements.");
      } else {
         DefaultValidator.DefaultConstraintValidatorContext context = new DefaultValidator.DefaultConstraintValidatorContext(object, groups);
         Set overallViolations = new HashSet(5);
         Path.Node node = context.addMethodNode(method);

         try {
            Class<T> rootClass = object.getClass();
            this.validateParametersInternal(
               rootClass, object, argumentValues.stream().map(ArgumentValue::getValue).toArray(), arguments, argLen, context, overallViolations, node
            );
         } finally {
            context.removeLast();
         }

         return Collections.unmodifiableSet(overallViolations);
      }
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validateParameters(
      @NonNull T object, @NonNull Method method, @NonNull Object[] parameterValues, @Nullable Class<?>... groups
   ) {
      ArgumentUtils.requireNonNull("method", (T)method);
      return (Set<ConstraintViolation<T>>)this.executionHandleLocator
         .findExecutableMethod(method.getDeclaringClass(), method.getName(), method.getParameterTypes())
         .map(executableMethod -> this.validateParameters(object, executableMethod, parameterValues, groups))
         .orElse(Collections.emptySet());
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validateReturnValue(
      @NonNull T object, @NonNull Method method, @Nullable Object returnValue, @Nullable Class<?>... groups
   ) {
      ArgumentUtils.requireNonNull("method", (T)method);
      ArgumentUtils.requireNonNull("object", object);
      return (Set<ConstraintViolation<T>>)this.executionHandleLocator
         .findExecutableMethod(method.getDeclaringClass(), method.getName(), method.getParameterTypes())
         .map(executableMethod -> this.validateReturnValue(object, executableMethod, returnValue, groups))
         .orElse(Collections.emptySet());
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validateReturnValue(
      @NonNull T object, @NonNull ExecutableMethod<?, Object> executableMethod, @Nullable Object returnValue, @Nullable Class<?>... groups
   ) {
      ReturnType<Object> returnType = executableMethod.getReturnType();
      Argument<Object> returnTypeArgument = returnType.asArgument();
      HashSet overallViolations = new HashSet(3);
      Class<T> rootBeanClass = object.getClass();
      DefaultValidator.DefaultConstraintValidatorContext context = new DefaultValidator.DefaultConstraintValidatorContext(object, groups);
      this.validateConstrainedPropertyInternal(
         rootBeanClass, object, object, returnTypeArgument, returnType.getType(), returnValue, context, overallViolations, null
      );
      AnnotationMetadata annotationMetadata = returnTypeArgument.getAnnotationMetadata();
      boolean hasValid = annotationMetadata.isAnnotationPresent(Valid.class);
      if (hasValid) {
         this.validateCascadePropertyInternal(context, rootBeanClass, object, object, returnTypeArgument, returnValue, overallViolations);
      }

      return overallViolations;
   }

   private <T> void validateCascadePropertyInternal(
      DefaultValidator.DefaultConstraintValidatorContext context,
      @NonNull Class<T> rootBeanClass,
      @Nullable T rootBean,
      Object object,
      @NonNull Argument<?> cascadeProperty,
      @Nullable Object propertyValue,
      Set overallViolations
   ) {
      if (propertyValue != null) {
         Optional<? extends ValueExtractor<Object>> opt = this.valueExtractorRegistry.findValueExtractor(cascadeProperty.getType());
         opt.ifPresent(
            valueExtractor -> valueExtractor.extractValues(
                  propertyValue,
                  new ValueExtractor.ValueReceiver() {
                     @Override
                     public void value(String nodeName, Object object1) {
                     }
      
                     @Override
                     public void iterableValue(String nodeName, Object iterableValue) {
                        if (iterableValue == null || !context.validatedObjects.contains(iterableValue)) {
                           DefaultValidator.this.cascadeToIterableValue(
                              context, rootBeanClass, rootBean, object, null, cascadeProperty, iterableValue, overallViolations, null, null, true
                           );
                        }
                     }
      
                     @Override
                     public void indexedValue(String nodeName, int i, Object iterableValue) {
                        if (iterableValue == null || !context.validatedObjects.contains(iterableValue)) {
                           DefaultValidator.this.cascadeToIterableValue(
                              context, rootBeanClass, rootBean, object, null, cascadeProperty, iterableValue, overallViolations, i, null, true
                           );
                        }
                     }
      
                     @Override
                     public void keyedValue(String nodeName, Object key, Object keyedValue) {
                        if (keyedValue == null || !context.validatedObjects.contains(keyedValue)) {
                           DefaultValidator.this.cascadeToIterableValue(
                              context, rootBeanClass, rootBean, object, null, cascadeProperty, keyedValue, overallViolations, null, key, false
                           );
                        }
                     }
                  }
               )
         );
         if (!opt.isPresent() && !context.validatedObjects.contains(propertyValue)) {
            try {
               Path.Node node = context.addReturnValueNode(cascadeProperty.getName());
               boolean canCascade = this.canCascade(rootBeanClass, context, propertyValue, node);
               if (canCascade) {
                  this.cascadeToOne(
                     rootBeanClass, rootBean, object, context, overallViolations, cascadeProperty, cascadeProperty.getType(), propertyValue, null
                  );
               }
            } finally {
               context.removeLast();
            }
         }
      }

   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validateConstructorParameters(
      @NonNull Constructor<? extends T> constructor, @NonNull Object[] parameterValues, @Nullable Class<?>... groups
   ) {
      ArgumentUtils.requireNonNull("constructor", (T)constructor);
      Class<? extends T> declaringClass = constructor.getDeclaringClass();
      BeanIntrospection<? extends T> introspection = BeanIntrospection.getIntrospection(declaringClass);
      return this.validateConstructorParameters(introspection, parameterValues);
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validateConstructorParameters(
      @NonNull BeanIntrospection<? extends T> introspection, @NonNull Object[] parameterValues, @Nullable Class<?>... groups
   ) {
      ArgumentUtils.requireNonNull("introspection", introspection);
      Class<? extends T> beanType = introspection.getBeanType();
      Argument<?>[] constructorArguments = introspection.getConstructorArguments();
      return this.validateConstructorParameters(beanType, constructorArguments, parameterValues, groups);
   }

   @Override
   public <T> Set<ConstraintViolation<T>> validateConstructorParameters(
      Class<? extends T> beanType, Argument<?>[] constructorArguments, @NonNull Object[] parameterValues, @Nullable Class<?>[] groups
   ) {
      parameterValues = parameterValues != null ? parameterValues : ArrayUtils.EMPTY_OBJECT_ARRAY;
      int argLength = constructorArguments.length;
      if (parameterValues.length != argLength) {
         throw new IllegalArgumentException("Expected exactly [" + argLength + "] constructor arguments");
      } else {
         DefaultValidator.DefaultConstraintValidatorContext context = new DefaultValidator.DefaultConstraintValidatorContext(groups);
         Set overallViolations = new HashSet(5);
         Path.Node node = context.addConstructorNode(beanType.getSimpleName(), constructorArguments);

         try {
            this.validateParametersInternal(beanType, (T)null, parameterValues, constructorArguments, argLength, context, overallViolations, node);
         } finally {
            context.removeLast();
         }

         return Collections.unmodifiableSet(overallViolations);
      }
   }

   @NonNull
   @Override
   public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(
      @NonNull Constructor<? extends T> constructor, @NonNull T createdObject, @Nullable Class<?>... groups
   ) {
      return this.validate(createdObject, groups);
   }

   @Nullable
   protected BeanIntrospection<Object> getBeanIntrospection(@NonNull Object object, @NonNull Class<?> definedClass) {
      return object == null
         ? null
         : (BeanIntrospection)BeanIntrospector.SHARED
            .findIntrospection(object.getClass())
            .orElseGet(() -> (BeanIntrospection)BeanIntrospector.SHARED.findIntrospection(definedClass).orElse(null));
   }

   @Nullable
   protected BeanIntrospection<Object> getBeanIntrospection(@NonNull Object object) {
      if (object == null) {
         return null;
      } else {
         return object instanceof Class
            ? (BeanIntrospection)BeanIntrospector.SHARED.findIntrospection((Class)object).orElse(null)
            : (BeanIntrospection)BeanIntrospector.SHARED.findIntrospection(object.getClass()).orElse(null);
      }
   }

   private <T> void validateParametersInternal(
      @NonNull Class<T> rootClass,
      @Nullable T object,
      @NonNull Object[] parameters,
      Argument[] arguments,
      int argLen,
      DefaultValidator.DefaultConstraintValidatorContext context,
      Set overallViolations,
      Path.Node parentNode
   ) {
      for(int i = 0; i < argLen; ++i) {
         final Argument argument = arguments[i];
         Class<?> parameterType = argument.getType();
         AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
         boolean hasValid = annotationMetadata.hasStereotype(Validator.ANN_VALID);
         boolean hasConstraint = annotationMetadata.hasStereotype(Validator.ANN_CONSTRAINT);
         if (hasValid || hasConstraint) {
            final Object parameterValue = parameters[i];
            ValueExtractor<Object> valueExtractor = null;
            boolean hasValue = parameterValue != null;
            boolean isValid = hasValue && hasValid;
            boolean isPublisher = hasValue && Publishers.isConvertibleToPublisher(parameterType);
            if (isPublisher) {
               this.instrumentPublisherArgumentWithValidation(
                  rootClass, object, parameters, context, i, argument, parameterType, annotationMetadata, parameterValue, isValid
               );
            } else {
               boolean isCompletionStage = hasValue && CompletionStage.class.isAssignableFrom(parameterType);
               if (isCompletionStage) {
                  this.instrumentCompletionStageArgumentWithValidation(
                     rootClass, object, parameters, context, i, argument, annotationMetadata, parameterValue, isValid
                  );
               } else {
                  if (hasValue) {
                     valueExtractor = (ValueExtractor)this.valueExtractorRegistry.findUnwrapValueExtractor(parameterType).orElse(null);
                  }

                  int finalIndex = i;
                  if (valueExtractor != null) {
                     valueExtractor.extractValues(
                        parameterValue,
                        (nodeName, unwrappedValue) -> this.validateParameterInternal(
                              rootClass,
                              object,
                              parameters,
                              context,
                              overallViolations,
                              argument.getName(),
                              unwrappedValue == null ? Object.class : unwrappedValue.getClass(),
                              finalIndex,
                              annotationMetadata,
                              unwrappedValue
                           )
                     );
                  } else {
                     this.validateParameterInternal(
                        rootClass,
                        object,
                        parameters,
                        context,
                        overallViolations,
                        argument.getName(),
                        parameterType,
                        finalIndex,
                        annotationMetadata,
                        parameterValue
                     );
                  }

                  if (isValid && !context.validatedObjects.contains(parameterValue)) {
                     valueExtractor = (ValueExtractor)this.valueExtractorRegistry.findValueExtractor(parameterType).orElse(null);
                     if (valueExtractor != null) {
                        valueExtractor.extractValues(
                           parameterValue,
                           new ValueExtractor.ValueReceiver() {
                              @Override
                              public void value(String nodeName, Object object1) {
                              }
   
                              @Override
                              public void iterableValue(String nodeName, Object iterableValue) {
                                 if (iterableValue == null || !context.validatedObjects.contains(iterableValue)) {
                                    DefaultValidator.this.cascadeToIterableValue(
                                       context, rootClass, object, parameterValue, parentNode, argument, iterableValue, overallViolations, null, null, true
                                    );
                                 }
                              }
   
                              @Override
                              public void indexedValue(String nodeName, int i, Object iterableValue) {
                                 if (iterableValue == null || !context.validatedObjects.contains(iterableValue)) {
                                    DefaultValidator.this.cascadeToIterableValue(
                                       context, rootClass, object, parameterValue, parentNode, argument, iterableValue, overallViolations, i, null, true
                                    );
                                 }
                              }
   
                              @Override
                              public void keyedValue(String nodeName, Object key, Object keyedValue) {
                                 if (keyedValue == null || !context.validatedObjects.contains(keyedValue)) {
                                    DefaultValidator.this.cascadeToIterableValue(
                                       context, rootClass, object, parameterValue, parentNode, argument, keyedValue, overallViolations, null, key, false
                                    );
                                 }
                              }
                           }
                        );
                     } else {
                        BeanIntrospection<Object> beanIntrospection = this.getBeanIntrospection(parameterValue, parameterType);
                        if (beanIntrospection != null) {
                           try {
                              context.addParameterNode(argument.getName(), i);
                              this.cascadeToOneIntrospection(context, object, parameterValue, beanIntrospection, overallViolations);
                           } finally {
                              context.removeLast();
                           }
                        } else {
                           context.addParameterNode(argument.getName(), i);
                           overallViolations.add(
                              this.createIntrospectionConstraintViolation(rootClass, object, context, parameterType, parameterValue, parameters)
                           );
                           context.removeLast();
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private <T> void instrumentPublisherArgumentWithValidation(
      @NonNull Class<T> rootClass,
      @Nullable T object,
      @NonNull Object[] argumentValues,
      DefaultValidator.DefaultConstraintValidatorContext context,
      int argumentIndex,
      Argument argument,
      Class<?> parameterType,
      AnnotationMetadata annotationMetadata,
      Object parameterValue,
      boolean isValid
   ) {
      Publisher<Object> publisher = Publishers.convertPublisher(parameterValue, Publisher.class);
      DefaultValidator.PathImpl copied = new DefaultValidator.PathImpl(context.currentPath);
      Flux<Object> finalFlowable = Flux.from(publisher)
         .flatMap(
            o -> {
               DefaultValidator.DefaultConstraintValidatorContext newContext = new DefaultValidator.DefaultConstraintValidatorContext(
                  object, copied, new Class[0]
               );
               Set newViolations = new HashSet();
               BeanIntrospection<Object> beanIntrospection = isValid && o != null && !ClassUtils.isJavaBasicType(o.getClass())
                  ? this.getBeanIntrospection(o)
                  : null;
               if (beanIntrospection != null) {
                  try {
                     context.addParameterNode(argument.getName(), argumentIndex);
                     this.cascadeToOneIntrospection(newContext, object, o, beanIntrospection, newViolations);
                  } finally {
                     context.removeLast();
                  }
               } else {
                  Class t = (Class)argument.getFirstTypeVariable().map(TypeInformation::getType).orElse(null);
                  this.validateParameterInternal(
                     rootClass,
                     object,
                     argumentValues,
                     newContext,
                     newViolations,
                     argument.getName(),
                     t != null ? t : Object.class,
                     argumentIndex,
                     annotationMetadata,
                     o
                  );
               }
      
               return !newViolations.isEmpty() ? Flux.error(new ConstraintViolationException(newViolations)) : Flux.just(o);
            }
         );
      argumentValues[argumentIndex] = Publishers.convertPublisher(finalFlowable, parameterType);
   }

   private <T> void instrumentCompletionStageArgumentWithValidation(
      @NonNull Class<T> rootClass,
      @Nullable T object,
      @NonNull Object[] argumentValues,
      DefaultValidator.DefaultConstraintValidatorContext context,
      int argumentIndex,
      Argument argument,
      AnnotationMetadata annotationMetadata,
      Object parameterValue,
      boolean isValid
   ) {
      CompletionStage<Object> publisher = (CompletionStage)parameterValue;
      DefaultValidator.PathImpl copied = new DefaultValidator.PathImpl(context.currentPath);
      CompletionStage<Object> validatedStage = publisher.thenApply(
         o -> {
            DefaultValidator.DefaultConstraintValidatorContext newContext = new DefaultValidator.DefaultConstraintValidatorContext(object, copied, new Class[0]);
            Set newViolations = new HashSet();
            BeanIntrospection<Object> beanIntrospection = isValid && o != null && !ClassUtils.isJavaBasicType(o.getClass())
               ? this.getBeanIntrospection(o)
               : null;
            if (beanIntrospection != null) {
               try {
                  context.addParameterNode(argument.getName(), argumentIndex);
                  this.cascadeToOneIntrospection(newContext, object, o, beanIntrospection, newViolations);
               } finally {
                  context.removeLast();
               }
            } else {
               Class t = (Class)argument.getFirstTypeVariable().map(TypeInformation::getType).orElse(null);
               this.validateParameterInternal(
                  rootClass,
                  object,
                  argumentValues,
                  newContext,
                  newViolations,
                  argument.getName(),
                  t != null ? t : Object.class,
                  argumentIndex,
                  annotationMetadata,
                  o
               );
            }
   
            if (!newViolations.isEmpty()) {
               throw new ConstraintViolationException(newViolations);
            } else {
               return o;
            }
         }
      );
      argumentValues[argumentIndex] = validatedStage;
   }

   private <T> void validateParameterInternal(
      @NonNull Class<T> rootClass,
      @Nullable T object,
      @NonNull Object[] argumentValues,
      @NonNull DefaultValidator.DefaultConstraintValidatorContext context,
      @NonNull Set overallViolations,
      @NonNull String parameterName,
      @NonNull Class<?> parameterType,
      int parameterIndex,
      @NonNull AnnotationMetadata annotationMetadata,
      @Nullable Object parameterValue
   ) {
      String currentMessageTemplate = (String)context.getMessageTemplate().orElse(null);

      try {
         context.addParameterNode(parameterName, parameterIndex);

         for(Class<? extends Annotation> constraintType : annotationMetadata.getAnnotationTypesByStereotype(Constraint.class)) {
            ConstraintValidator constraintValidator = (ConstraintValidator)this.constraintValidatorRegistry
               .findConstraintValidator(constraintType, parameterType)
               .orElse(null);
            if (constraintValidator != null) {
               AnnotationValue<? extends Annotation> annotationValue = annotationMetadata.getAnnotation(constraintType);
               if (annotationValue != null && !constraintValidator.isValid(parameterValue, annotationValue, context)) {
                  String messageTemplate = this.buildMessageTemplate(context, annotationValue, annotationMetadata);
                  Map<String, Object> variables = this.newConstraintVariables(annotationValue, parameterValue, annotationMetadata);
                  overallViolations.add(
                     new DefaultValidator.DefaultConstraintViolation(
                        object,
                        rootClass,
                        object,
                        parameterValue,
                        this.messageSource.interpolate(messageTemplate, MessageSource.MessageContext.of(variables)),
                        messageTemplate,
                        new DefaultValidator.PathImpl(context.currentPath),
                        new DefaultConstraintDescriptor<>(annotationMetadata, constraintType, annotationValue),
                        argumentValues
                     )
                  );
               }
            }
         }
      } finally {
         context.removeLast();
         context.messageTemplate(currentMessageTemplate);
      }

   }

   private <T> void validatePojoInternal(
      @NonNull Class<T> rootClass,
      @Nullable T object,
      @Nullable Object[] argumentValues,
      @NonNull DefaultValidator.DefaultConstraintValidatorContext context,
      @NonNull Set overallViolations,
      @NonNull Class<?> parameterType,
      @NonNull Object parameterValue,
      Class<? extends Annotation> pojoConstraint,
      AnnotationValue constraintAnnotation
   ) {
      ConstraintValidator constraintValidator = (ConstraintValidator)this.constraintValidatorRegistry
         .findConstraintValidator(pojoConstraint, parameterType)
         .orElse(null);
      if (constraintValidator != null) {
         String currentMessageTemplate = (String)context.getMessageTemplate().orElse(null);
         if (!constraintValidator.isValid(parameterValue, constraintAnnotation, context)) {
            BeanIntrospection<Object> beanIntrospection = this.getBeanIntrospection(parameterValue);
            if (beanIntrospection == null) {
               throw new ValidationException("Passed object [" + parameterValue + "] cannot be introspected. Please annotate with @Introspected");
            }

            AnnotationMetadata beanAnnotationMetadata = beanIntrospection.getAnnotationMetadata();
            AnnotationValue<? extends Annotation> annotationValue = beanAnnotationMetadata.getAnnotation(pojoConstraint);
            String propertyValue = "";
            String messageTemplate = this.buildMessageTemplate(context, annotationValue, beanAnnotationMetadata);
            Map<String, Object> variables = this.newConstraintVariables(annotationValue, "", beanAnnotationMetadata);
            overallViolations.add(
               new DefaultValidator.DefaultConstraintViolation(
                  object,
                  rootClass,
                  object,
                  parameterValue,
                  this.messageSource.interpolate(messageTemplate, MessageSource.MessageContext.of(variables)),
                  messageTemplate,
                  new DefaultValidator.PathImpl(context.currentPath),
                  new DefaultConstraintDescriptor<>(beanAnnotationMetadata, pojoConstraint, annotationValue),
                  argumentValues
               )
            );
         }

         context.messageTemplate(currentMessageTemplate);
      }

   }

   private <T> Set<ConstraintViolation<T>> doValidate(
      BeanIntrospection<T> introspection,
      @NonNull T rootBean,
      @NonNull Object object,
      Collection<? extends BeanProperty<Object, Object>> constrainedProperties,
      Collection<BeanProperty<Object, Object>> cascadeProperties,
      DefaultValidator.DefaultConstraintValidatorContext context,
      Set overallViolations,
      List<Class<? extends Annotation>> pojoConstraints
   ) {
      Class<T> rootBeanClass = rootBean.getClass();

      for(BeanProperty<Object, Object> constrainedProperty : constrainedProperties) {
         Object propertyValue = constrainedProperty.get(object);
         this.validateConstrainedPropertyInternal(
            rootBeanClass, rootBean, object, constrainedProperty, constrainedProperty.getType(), propertyValue, context, overallViolations, null
         );
      }

      for(Class<? extends Annotation> pojoConstraint : pojoConstraints) {
         this.validatePojoInternal(
            rootBeanClass, rootBean, null, context, overallViolations, object.getClass(), object, pojoConstraint, introspection.getAnnotation(pojoConstraint)
         );
      }

      for(BeanProperty<Object, Object> cascadeProperty : cascadeProperties) {
         Object propertyValue = cascadeProperty.get(object);
         if (propertyValue != null) {
            Optional<? extends ValueExtractor<Object>> opt = this.valueExtractorRegistry.findValueExtractor(propertyValue.getClass());
            opt.ifPresent(
               valueExtractor -> valueExtractor.extractValues(
                     propertyValue,
                     new ValueExtractor.ValueReceiver() {
                        @Override
                        public void value(String nodeName, Object object1) {
                        }
      
                        @Override
                        public void iterableValue(String nodeName, Object iterableValue) {
                           if (iterableValue == null || !context.validatedObjects.contains(iterableValue)) {
                              DefaultValidator.this.cascadeToIterableValue(
                                 context, rootBeanClass, rootBean, object, cascadeProperty, iterableValue, overallViolations, null, null, true
                              );
                           }
                        }
      
                        @Override
                        public void indexedValue(String nodeName, int i, Object iterableValue) {
                           if (iterableValue == null || !context.validatedObjects.contains(iterableValue)) {
                              DefaultValidator.this.cascadeToIterableValue(
                                 context, rootBeanClass, rootBean, object, cascadeProperty, iterableValue, overallViolations, i, null, true
                              );
                           }
                        }
      
                        @Override
                        public void keyedValue(String nodeName, Object key, Object keyedValue) {
                           if (keyedValue == null || !context.validatedObjects.contains(keyedValue)) {
                              DefaultValidator.this.cascadeToIterableValue(
                                 context, rootBeanClass, rootBean, object, cascadeProperty, keyedValue, overallViolations, null, key, false
                              );
                           }
                        }
                     }
                  )
            );
            if (!opt.isPresent() && !context.validatedObjects.contains(propertyValue)) {
               Path.Node node = context.addPropertyNode(cascadeProperty.getName(), null);

               try {
                  boolean canCascade = this.canCascade(rootBeanClass, context, propertyValue, node);
                  if (canCascade) {
                     this.cascadeToOne(
                        rootBeanClass, rootBean, object, context, overallViolations, cascadeProperty, cascadeProperty.getType(), propertyValue, null
                     );
                  }
               } finally {
                  context.removeLast();
               }
            }
         }
      }

      return Collections.unmodifiableSet(overallViolations);
   }

   private <T> boolean canCascade(Class<T> rootBeanClass, DefaultValidator.DefaultConstraintValidatorContext context, Object propertyValue, Path.Node node) {
      boolean canCascade = this.traversableResolver.isCascadable(propertyValue, node, rootBeanClass, context.currentPath, ElementType.FIELD);
      boolean isReachable = this.traversableResolver.isReachable(propertyValue, node, rootBeanClass, context.currentPath, ElementType.FIELD);
      return canCascade && isReachable;
   }

   private <T> void cascadeToIterableValue(
      DefaultValidator.DefaultConstraintValidatorContext context,
      @NonNull Class<T> rootClass,
      @Nullable T rootBean,
      Object object,
      BeanProperty<Object, Object> cascadeProperty,
      Object iterableValue,
      Set overallViolations,
      Integer index,
      Object key,
      boolean isIterable
   ) {
      DefaultValidator.DefaultPropertyNode container = new DefaultValidator.DefaultPropertyNode(
         cascadeProperty.getName(), cascadeProperty.getType(), index, key, ElementKind.CONTAINER_ELEMENT, isIterable
      );
      this.cascadeToOne(rootClass, rootBean, object, context, overallViolations, cascadeProperty, cascadeProperty.getType(), iterableValue, container);
   }

   private <T> void cascadeToIterableValue(
      DefaultValidator.DefaultConstraintValidatorContext context,
      @NonNull Class<T> rootClass,
      @Nullable T rootBean,
      @Nullable Object object,
      Path.Node node,
      Argument methodArgument,
      Object iterableValue,
      Set overallViolations,
      Integer index,
      Object key,
      boolean isIterable
   ) {
      if (this.canCascade(rootClass, context, iterableValue, node)) {
         DefaultValidator.DefaultPropertyNode currentContainerNode = new DefaultValidator.DefaultPropertyNode(
            methodArgument.getName(), methodArgument.getClass(), index, key, ElementKind.CONTAINER_ELEMENT, isIterable
         );
         this.cascadeToOne(
            rootClass, rootBean, object, context, overallViolations, methodArgument, methodArgument.getType(), iterableValue, currentContainerNode
         );
      }

   }

   private <T> void cascadeToOne(
      @NonNull Class<T> rootClass,
      @Nullable T rootBean,
      Object object,
      DefaultValidator.DefaultConstraintValidatorContext context,
      Set overallViolations,
      AnnotatedElement cascadeProperty,
      Class propertyType,
      Object propertyValue,
      @Nullable DefaultValidator.DefaultPropertyNode container
   ) {
      Class<?> beanType = Object.class;
      if (propertyValue != null) {
         beanType = propertyValue.getClass();
      } else if (cascadeProperty instanceof BeanProperty) {
         Argument argument = ((BeanProperty)cascadeProperty).asArgument();
         if (Map.class.isAssignableFrom(argument.getType())) {
            Argument[] typeParameters = argument.getTypeParameters();
            if (typeParameters.length == 2) {
               beanType = typeParameters[1].getType();
            }
         } else {
            beanType = (Class)argument.getFirstTypeVariable().map(TypeInformation::getType).orElse(null);
         }
      }

      BeanIntrospection<Object> beanIntrospection = this.getBeanIntrospection(beanType);
      AnnotationMetadata annotationMetadata = cascadeProperty.getAnnotationMetadata();
      if (beanIntrospection == null && !annotationMetadata.hasStereotype(Constraint.class)) {
         overallViolations.add(this.createIntrospectionConstraintViolation(rootClass, rootBean, context, beanType, propertyValue));
      } else {
         if (beanIntrospection != null) {
            if (container != null) {
               context.addPropertyNode(container.getName(), container);
            }

            try {
               this.cascadeToOneIntrospection(context, rootBean, propertyValue, beanIntrospection, overallViolations);
            } finally {
               if (container != null) {
                  context.removeLast();
               }

            }
         } else {
            this.validateConstrainedPropertyInternal(
               rootClass, rootBean, object, cascadeProperty, propertyType, propertyValue, context, overallViolations, container
            );
         }

      }
   }

   private <T> void cascadeToOneIntrospection(
      DefaultValidator.DefaultConstraintValidatorContext context, T rootBean, Object bean, BeanIntrospection<Object> beanIntrospection, Set overallViolations
   ) {
      context.validatedObjects.add(bean);
      Collection<BeanProperty<Object, Object>> cascadeConstraints = beanIntrospection.getIndexedProperties(Constraint.class);
      Collection<BeanProperty<Object, Object>> cascadeNestedProperties = beanIntrospection.getIndexedProperties(Valid.class);
      List<Class<? extends Annotation>> pojoConstraints = beanIntrospection.getAnnotationMetadata().getAnnotationTypesByStereotype(Constraint.class);
      if (CollectionUtils.isNotEmpty(cascadeConstraints) || CollectionUtils.isNotEmpty(cascadeNestedProperties) || CollectionUtils.isNotEmpty(pojoConstraints)) {
         this.doValidate(beanIntrospection, rootBean, bean, cascadeConstraints, cascadeNestedProperties, context, overallViolations, pojoConstraints);
      }

   }

   private <T> void validateConstrainedPropertyInternal(
      @NonNull Class<T> rootBeanClass,
      @Nullable T rootBean,
      @NonNull Object object,
      @NonNull AnnotatedElement constrainedProperty,
      @NonNull Class propertyType,
      @Nullable Object propertyValue,
      DefaultValidator.DefaultConstraintValidatorContext context,
      Set<ConstraintViolation<Object>> overallViolations,
      @Nullable DefaultValidator.DefaultPropertyNode container
   ) {
      context.addPropertyNode(constrainedProperty.getName(), container);
      String currentMessageTemplate = (String)context.getMessageTemplate().orElse(null);
      this.validatePropertyInternal(rootBeanClass, rootBean, object, context, overallViolations, propertyType, constrainedProperty, propertyValue);
      context.removeLast();
      context.messageTemplate(currentMessageTemplate);
   }

   private <T> void validatePropertyInternal(
      @Nullable Class<T> rootBeanClass,
      @Nullable T rootBean,
      @Nullable Object object,
      @NonNull DefaultValidator.DefaultConstraintValidatorContext context,
      @NonNull Set<ConstraintViolation<Object>> overallViolations,
      @NonNull Class propertyType,
      @NonNull AnnotatedElement constrainedProperty,
      @Nullable Object propertyValue
   ) {
      AnnotationMetadata annotationMetadata = constrainedProperty.getAnnotationMetadata();

      for(Class<? extends Annotation> constraintType : annotationMetadata.getAnnotationTypesByStereotype(Constraint.class)) {
         ValueExtractor<Object> valueExtractor = null;
         if (propertyValue != null && !annotationMetadata.hasAnnotation(Valid.class)) {
            valueExtractor = (ValueExtractor)this.valueExtractorRegistry.findUnwrapValueExtractor(propertyValue.getClass()).orElse(null);
         }

         if (valueExtractor != null) {
            valueExtractor.extractValues(
               propertyValue,
               (nodeName, extractedValue) -> this.valueConstraintOnProperty(
                     rootBeanClass, rootBean, object, context, overallViolations, constrainedProperty, propertyType, extractedValue, constraintType
                  )
            );
         } else {
            this.valueConstraintOnProperty(
               rootBeanClass, rootBean, object, context, overallViolations, constrainedProperty, propertyType, propertyValue, constraintType
            );
         }
      }

   }

   private <T> void valueConstraintOnProperty(
      @Nullable Class<T> rootBeanClass,
      @Nullable T rootBean,
      @Nullable Object object,
      DefaultValidator.DefaultConstraintValidatorContext context,
      Set<ConstraintViolation<Object>> overallViolations,
      AnnotatedElement constrainedProperty,
      Class propertyType,
      @Nullable Object propertyValue,
      Class<? extends Annotation> constraintType
   ) {
      AnnotationMetadata annotationMetadata = constrainedProperty.getAnnotationMetadata();
      List<? extends AnnotationValue<? extends Annotation>> annotationValues = annotationMetadata.getAnnotationValuesByType(constraintType);
      Set<AnnotationValue<? extends Annotation>> constraints = new HashSet(3);

      for(Class<?> group : context.groups) {
         for(AnnotationValue<? extends Annotation> annotationValue : annotationValues) {
            Class<?>[] classValues = annotationValue.classValues("groups");
            if (!ArrayUtils.isEmpty(classValues)) {
               List<Class> constraintGroups = Arrays.asList(classValues);
               if (constraintGroups.contains(group)) {
                  constraints.add(annotationValue);
               }
            } else if (context.groups == DEFAULT_GROUPS || group == Default.class) {
               constraints.add(annotationValue);
            }
         }
      }

      Class<Object> targetType = propertyValue != null ? propertyValue.getClass() : propertyType;
      ConstraintValidator<? extends Annotation, Object> validator = (ConstraintValidator)this.constraintValidatorRegistry
         .findConstraintValidator(constraintType, targetType)
         .orElse(null);
      if (validator != null) {
         for(AnnotationValue annotationValue : constraints) {
            if (!validator.isValid(propertyValue, annotationValue, context)) {
               String messageTemplate = this.buildMessageTemplate(context, annotationValue, annotationMetadata);
               Map<String, Object> variables = this.newConstraintVariables(annotationValue, propertyValue, annotationMetadata);
               overallViolations.add(
                  new DefaultValidator.DefaultConstraintViolation(
                     rootBean,
                     rootBeanClass,
                     object,
                     propertyValue,
                     this.messageSource.interpolate(messageTemplate, MessageSource.MessageContext.of(variables)),
                     messageTemplate,
                     new DefaultValidator.PathImpl(context.currentPath),
                     new DefaultConstraintDescriptor(annotationMetadata, constraintType, annotationValue),
                     new Object[0]
                  )
               );
            }
         }
      }

   }

   private Map<String, Object> newConstraintVariables(AnnotationValue annotationValue, @Nullable Object propertyValue, AnnotationMetadata annotationMetadata) {
      Map<?, ?> values = annotationValue.getValues();
      int initSize = (int)Math.ceil((double)values.size() / 0.75);
      Map<String, Object> variables = new LinkedHashMap(initSize);

      for(Entry<?, ?> entry : values.entrySet()) {
         variables.put(entry.getKey().toString(), entry.getValue());
      }

      variables.put("validatedValue", propertyValue);
      Map<String, Object> defaultValues = annotationMetadata.getDefaultValues(annotationValue.getAnnotationName());

      for(Entry<String, Object> entry : defaultValues.entrySet()) {
         String n = (String)entry.getKey();
         if (!variables.containsKey(n)) {
            Object v = entry.getValue();
            if (v != null) {
               variables.put(n, v);
            }
         }
      }

      return variables;
   }

   private String buildMessageTemplate(
      final DefaultValidator.DefaultConstraintValidatorContext context, final AnnotationValue<?> annotationValue, final AnnotationMetadata annotationMetadata
   ) {
      return (String)context.getMessageTemplate()
         .orElseGet(
            () -> (String)annotationValue.stringValue("message")
                  .orElseGet(
                     () -> (String)annotationMetadata.getDefaultValue(annotationValue.getAnnotationName(), "message", String.class)
                           .orElse("{" + annotationValue.getAnnotationName() + ".message}")
                  )
         );
   }

   @NonNull
   @Override
   public <T> Publisher<T> validatePublisher(@NonNull Publisher<T> publisher, Class<?>... groups) {
      ArgumentUtils.requireNonNull("publisher", publisher);
      Publisher<T> reactiveSequence = Publishers.convertPublisher(publisher, Publisher.class);
      return Flux.from(reactiveSequence).flatMap(object -> {
         Set<ConstraintViolation<Object>> constraintViolations = this.validate(object, groups);
         return !constraintViolations.isEmpty() ? Flux.error(new ConstraintViolationException(constraintViolations)) : Flux.just(object);
      });
   }

   @NonNull
   @Override
   public <T> CompletionStage<T> validateCompletionStage(@NonNull CompletionStage<T> completionStage, Class<?>... groups) {
      ArgumentUtils.requireNonNull("completionStage", (T)completionStage);
      return completionStage.thenApply(t -> {
         Set<ConstraintViolation<Object>> constraintViolations = this.validate(t, groups);
         if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
         } else {
            return t;
         }
      });
   }

   @Override
   public <T> void validateBeanArgument(
      @NonNull BeanResolutionContext resolutionContext, @NonNull InjectionPoint injectionPoint, @NonNull Argument<T> argument, int index, @Nullable T value
   ) throws BeanInstantiationException {
      AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
      boolean hasValid = annotationMetadata.hasStereotype(Valid.class);
      boolean hasConstraint = annotationMetadata.hasStereotype(Constraint.class);
      Class<T> parameterType = argument.getType();
      final Class rootClass = injectionPoint.getDeclaringBean().getBeanType();
      final DefaultValidator.DefaultConstraintValidatorContext context = new DefaultValidator.DefaultConstraintValidatorContext(value, new Class[0]);
      final Set overallViolations = new HashSet(5);
      if (hasConstraint) {
         final Path.Node parentNode = context.addConstructorNode(rootClass.getName(), injectionPoint.getDeclaringBean().getConstructor().getArguments());
         ValueExtractor<Object> valueExtractor = (ValueExtractor)this.valueExtractorRegistry.findValueExtractor(parameterType).orElse(null);
         if (valueExtractor != null) {
            valueExtractor.extractValues(
               value,
               new ValueExtractor.ValueReceiver() {
                  @Override
                  public void value(String nodeName, Object object1) {
                  }
   
                  @Override
                  public void iterableValue(String nodeName, Object iterableValue) {
                     if (iterableValue == null || !context.validatedObjects.contains(iterableValue)) {
                        DefaultValidator.this.cascadeToIterableValue(
                           context, rootClass, (T)null, value, parentNode, argument, iterableValue, overallViolations, null, null, true
                        );
                     }
                  }
   
                  @Override
                  public void indexedValue(String nodeName, int i, Object iterableValue) {
                     if (iterableValue == null || !context.validatedObjects.contains(iterableValue)) {
                        DefaultValidator.this.cascadeToIterableValue(
                           context, rootClass, (T)null, value, parentNode, argument, iterableValue, overallViolations, i, null, true
                        );
                     }
                  }
   
                  @Override
                  public void keyedValue(String nodeName, Object key, Object keyedValue) {
                     if (keyedValue == null || !context.validatedObjects.contains(keyedValue)) {
                        DefaultValidator.this.cascadeToIterableValue(
                           context, rootClass, (T)null, value, parentNode, argument, keyedValue, overallViolations, null, key, false
                        );
                     }
                  }
               }
            );
         } else {
            this.validateParameterInternal(
               rootClass,
               (T)null,
               ArrayUtils.EMPTY_OBJECT_ARRAY,
               context,
               overallViolations,
               argument.getName(),
               parameterType,
               index,
               annotationMetadata,
               value
            );
         }

         context.removeLast();
      } else if (hasValid && value != null) {
         BeanIntrospection<Object> beanIntrospection = this.getBeanIntrospection(value, parameterType);
         if (beanIntrospection != null) {
            try {
               context.addParameterNode(argument.getName(), index);
               this.cascadeToOneIntrospection(context, (T)null, value, beanIntrospection, overallViolations);
            } finally {
               context.removeLast();
            }
         }
      }

      this.failOnError(resolutionContext, overallViolations, rootClass);
   }

   @Override
   public <T> void validateBean(@NonNull BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> definition, @NonNull T bean) throws BeanInstantiationException {
      BeanIntrospection<T> introspection = this.getBeanIntrospection(bean);
      if (introspection != null) {
         Set<ConstraintViolation<T>> errors = this.validate(introspection, bean);
         Class<?> beanType = bean.getClass();
         this.failOnError(resolutionContext, errors, beanType);
      } else if (bean instanceof Intercepted && definition.hasStereotype(ConfigurationReader.class)) {
         Collection<ExecutableMethod<T, ?>> executableMethods = definition.getExecutableMethods();
         if (CollectionUtils.isNotEmpty(executableMethods)) {
            Set<ConstraintViolation<Object>> errors = new HashSet();
            DefaultValidator.DefaultConstraintValidatorContext context = new DefaultValidator.DefaultConstraintValidatorContext(bean, new Class[0]);
            Class<T> beanType = definition.getBeanType();
            Class<?>[] interfaces = beanType.getInterfaces();
            if (ArrayUtils.isNotEmpty(interfaces)) {
               context.addConstructorNode(interfaces[0].getSimpleName());
            } else {
               context.addConstructorNode(beanType.getSimpleName());
            }

            for(ExecutableMethod executableMethod : executableMethods) {
               if (executableMethod.hasAnnotation(Property.class)) {
                  boolean hasConstraint = executableMethod.hasStereotype(Constraint.class);
                  boolean isValid = executableMethod.hasStereotype(Valid.class);
                  if (hasConstraint || isValid) {
                     Object value = executableMethod.invoke(bean, new Object[0]);
                     this.validateConstrainedPropertyInternal(
                        beanType, bean, bean, executableMethod, executableMethod.getReturnType().getType(), value, context, errors, null
                     );
                  }
               }
            }

            this.failOnError(resolutionContext, errors, beanType);
         }
      }

   }

   private <T> void failOnError(@NonNull BeanResolutionContext resolutionContext, Set<ConstraintViolation<T>> errors, Class<?> beanType) {
      if (!errors.isEmpty()) {
         StringBuilder builder = new StringBuilder()
            .append("Validation failed for bean definition [")
            .append(beanType.getName())
            .append("]\nList of constraint violations:[\n");

         for(ConstraintViolation<?> violation : errors) {
            builder.append('\t').append(violation.getPropertyPath()).append(" - ").append(violation.getMessage()).append('\n');
         }

         builder.append(']');
         throw new BeanInstantiationException(resolutionContext, builder.toString());
      }
   }

   @NonNull
   private <T> DefaultValidator.DefaultConstraintViolation<T> createIntrospectionConstraintViolation(
      @NonNull Class<T> rootClass,
      T object,
      DefaultValidator.DefaultConstraintValidatorContext context,
      Class<?> parameterType,
      Object parameterValue,
      Object... parameters
   ) {
      String messageTemplate = (String)context.getMessageTemplate().orElseGet(() -> "{" + Introspected.class.getName() + ".message}");
      return new DefaultValidator.DefaultConstraintViolation<>(
         object,
         rootClass,
         object,
         parameterValue,
         this.messageSource.interpolate(messageTemplate, MessageSource.MessageContext.of(Collections.singletonMap("type", parameterType.getName()))),
         messageTemplate,
         new DefaultValidator.PathImpl(context.currentPath),
         null,
         parameters
      );
   }

   private final class DefaultConstraintValidatorContext implements ConstraintValidatorContext {
      final Set<Object> validatedObjects = new HashSet(20);
      final DefaultValidator.PathImpl currentPath;
      final List<Class<?>> groups;
      String messageTemplate = null;

      private <T> DefaultConstraintValidatorContext(T object, Class<?>... groups) {
         this(object, DefaultValidator.this.new PathImpl(), groups);
      }

      private <T> DefaultConstraintValidatorContext(T object, DefaultValidator.PathImpl path, Class<?>... groups) {
         if (object != null) {
            this.validatedObjects.add(object);
         }

         if (ArrayUtils.isNotEmpty(groups)) {
            this.sanityCheckGroups(groups);
            List<Class<?>> groupList = new ArrayList();

            for(Class<?> group : groups) {
               this.addInheritedGroups(group, groupList);
            }

            this.groups = Collections.unmodifiableList(groupList);
         } else {
            this.groups = DefaultValidator.DEFAULT_GROUPS;
         }

         this.currentPath = path != null ? path : DefaultValidator.this.new PathImpl();
      }

      private DefaultConstraintValidatorContext(Class<?>... groups) {
         this(null, groups);
      }

      private void sanityCheckGroups(Class<?>[] groups) {
         ArgumentUtils.requireNonNull("groups", groups);

         for(Class<?> clazz : groups) {
            if (clazz == null) {
               throw new IllegalArgumentException("Validation groups must be non-null");
            }

            if (!clazz.isInterface()) {
               throw new IllegalArgumentException("Validation groups must be interfaces. " + clazz.getName() + " is not.");
            }
         }

      }

      private void addInheritedGroups(Class<?> group, List<Class<?>> groups) {
         if (!groups.contains(group)) {
            groups.add(group);
         }

         for(Class<?> inheritedGroup : group.getInterfaces()) {
            this.addInheritedGroups(inheritedGroup, groups);
         }

      }

      @NonNull
      @Override
      public ClockProvider getClockProvider() {
         return DefaultValidator.this.clockProvider;
      }

      @Nullable
      @Override
      public Object getRootBean() {
         return this.validatedObjects.isEmpty() ? null : this.validatedObjects.iterator().next();
      }

      @Override
      public void messageTemplate(@Nullable final String messageTemplate) {
         this.messageTemplate = messageTemplate;
      }

      Optional<String> getMessageTemplate() {
         return Optional.ofNullable(this.messageTemplate);
      }

      Path.Node addPropertyNode(String name, @Nullable DefaultValidator.DefaultPropertyNode container) {
         DefaultValidator.DefaultPropertyNode node;
         if (container != null) {
            node = DefaultValidator.this.new DefaultPropertyNode(name, container);
         } else {
            node = DefaultValidator.this.new DefaultPropertyNode(name, null, null, null, ElementKind.PROPERTY, false);
         }

         this.currentPath.nodes.add(node);
         return node;
      }

      Path.Node addReturnValueNode(String name) {
         DefaultValidator.DefaultReturnValueNode returnValueNode = DefaultValidator.this.new DefaultReturnValueNode(name);
         this.currentPath.nodes.add(returnValueNode);
         return returnValueNode;
      }

      void removeLast() {
         this.currentPath.nodes.removeLast();
      }

      Path.Node addMethodNode(MethodReference<?, ?> reference) {
         DefaultValidator.DefaultMethodNode methodNode = DefaultValidator.this.new DefaultMethodNode(reference);
         this.currentPath.nodes.add(methodNode);
         return methodNode;
      }

      void addParameterNode(String name, int index) {
         DefaultValidator.DefaultParameterNode node = DefaultValidator.this.new DefaultParameterNode(name, index);
         this.currentPath.nodes.add(node);
      }

      Path.Node addConstructorNode(String simpleName, Argument<?>... constructorArguments) {
         DefaultValidator.DefaultConstructorNode node = DefaultValidator.this.new DefaultConstructorNode(new MethodReference<Object, Object>() {
            @Override
            public Argument[] getArguments() {
               return constructorArguments;
            }

            @Override
            public Method getTargetMethod() {
               return null;
            }

            @Override
            public ReturnType<Object> getReturnType() {
               return null;
            }

            @Override
            public Class getDeclaringType() {
               return null;
            }

            @Override
            public String getMethodName() {
               return simpleName;
            }
         });
         this.currentPath.nodes.add(node);
         return node;
      }
   }

   private final class DefaultConstraintViolation<T> implements ConstraintViolation<T> {
      private final T rootBean;
      private final Object invalidValue;
      private final String message;
      private final String messageTemplate;
      private final Path path;
      private final Class<T> rootBeanClass;
      private final Object leafBean;
      private final ConstraintDescriptor<?> constraintDescriptor;
      private final Object[] executableParams;

      private DefaultConstraintViolation(
         @Nullable T rootBean,
         @Nullable Class<T> rootBeanClass,
         Object leafBean,
         Object invalidValue,
         String message,
         String messageTemplate,
         Path path,
         ConstraintDescriptor<?> constraintDescriptor,
         Object... executableParams
      ) {
         this.rootBean = rootBean;
         this.rootBeanClass = rootBeanClass;
         this.invalidValue = invalidValue;
         this.message = message;
         this.messageTemplate = messageTemplate;
         this.path = path;
         this.leafBean = leafBean;
         this.constraintDescriptor = constraintDescriptor;
         this.executableParams = executableParams;
      }

      @Override
      public String getMessage() {
         return this.message;
      }

      @Override
      public String getMessageTemplate() {
         return this.messageTemplate;
      }

      @Override
      public T getRootBean() {
         return this.rootBean;
      }

      @Override
      public Class<T> getRootBeanClass() {
         return this.rootBeanClass;
      }

      @Override
      public Object getLeafBean() {
         return this.leafBean;
      }

      @Override
      public Object[] getExecutableParameters() {
         return this.executableParams != null ? this.executableParams : ArrayUtils.EMPTY_OBJECT_ARRAY;
      }

      @Override
      public Object getExecutableReturnValue() {
         return null;
      }

      @Override
      public Path getPropertyPath() {
         return this.path;
      }

      @Override
      public Object getInvalidValue() {
         return this.invalidValue;
      }

      @Override
      public ConstraintDescriptor<?> getConstraintDescriptor() {
         return this.constraintDescriptor;
      }

      @Override
      public <U> U unwrap(Class<U> type) {
         throw new UnsupportedOperationException("Unwrapping is unsupported by this implementation");
      }

      public String toString() {
         return "DefaultConstraintViolation{rootBean=" + this.rootBeanClass + ", invalidValue=" + this.invalidValue + ", path=" + this.path + '}';
      }
   }

   private final class DefaultConstructorNode extends DefaultValidator.DefaultMethodNode implements Path.ConstructorNode {
      public DefaultConstructorNode(MethodReference<Object, Object> methodReference) {
         super(methodReference);
      }

      @Override
      public ElementKind getKind() {
         return ElementKind.CONSTRUCTOR;
      }
   }

   private class DefaultMethodNode implements Path.MethodNode {
      private final MethodReference<?, ?> methodReference;

      public DefaultMethodNode(MethodReference<?, ?> methodReference) {
         this.methodReference = methodReference;
      }

      @Override
      public List<Class<?>> getParameterTypes() {
         return Arrays.asList(this.methodReference.getArgumentTypes());
      }

      @Override
      public String getName() {
         return this.methodReference.getMethodName();
      }

      @Override
      public boolean isInIterable() {
         return false;
      }

      @Override
      public Integer getIndex() {
         return null;
      }

      @Override
      public Object getKey() {
         return null;
      }

      @Override
      public ElementKind getKind() {
         return ElementKind.METHOD;
      }

      @Override
      public String toString() {
         return this.getName();
      }

      @Override
      public <T extends Path.Node> T as(Class<T> nodeType) {
         throw new UnsupportedOperationException("Unwrapping is unsupported by this implementation");
      }
   }

   private final class DefaultParameterNode extends DefaultValidator.DefaultPropertyNode implements Path.ParameterNode {
      private final int parameterIndex;

      DefaultParameterNode(@NonNull String name, int parameterIndex) {
         super(name, null, null, null, ElementKind.PARAMETER, false);
         this.parameterIndex = parameterIndex;
      }

      @Override
      public ElementKind getKind() {
         return ElementKind.PARAMETER;
      }

      @Override
      public <T extends Path.Node> T as(Class<T> nodeType) {
         throw new UnsupportedOperationException("Unwrapping is unsupported by this implementation");
      }

      @Override
      public int getParameterIndex() {
         return this.parameterIndex;
      }
   }

   private class DefaultPropertyNode implements Path.PropertyNode {
      private final Class<?> containerClass;
      private final String name;
      private final Integer index;
      private final Object key;
      private final ElementKind kind;
      private final boolean isIterable;

      DefaultPropertyNode(
         @NonNull String name, @Nullable Class<?> containerClass, @Nullable Integer index, @Nullable Object key, @NonNull ElementKind kind, boolean isIterable
      ) {
         this.containerClass = containerClass;
         this.name = name;
         this.index = index;
         this.key = key;
         this.kind = kind;
         this.isIterable = isIterable || index != null;
      }

      DefaultPropertyNode(@NonNull String name, @NonNull DefaultValidator.DefaultPropertyNode parent) {
         this(name, parent.containerClass, parent.getIndex(), parent.getKey(), ElementKind.CONTAINER_ELEMENT, parent.isInIterable());
      }

      @Override
      public Class<?> getContainerClass() {
         return this.containerClass;
      }

      @Override
      public Integer getTypeArgumentIndex() {
         return null;
      }

      @Override
      public String getName() {
         return this.name;
      }

      @Override
      public boolean isInIterable() {
         return this.isIterable;
      }

      @Override
      public Integer getIndex() {
         return this.index;
      }

      @Override
      public Object getKey() {
         return this.key;
      }

      @Override
      public ElementKind getKind() {
         return this.kind;
      }

      @Override
      public String toString() {
         return this.getName();
      }

      @Override
      public <T extends Path.Node> T as(Class<T> nodeType) {
         throw new UnsupportedOperationException("Unwrapping is unsupported by this implementation");
      }
   }

   private class DefaultReturnValueNode implements Path.ReturnValueNode {
      private final String name;
      private final Integer index;
      private final Object key;
      private final ElementKind kind;
      private final boolean isInIterable;

      public DefaultReturnValueNode(String name, Integer index, Object key, ElementKind kind, boolean isInIterable) {
         this.name = name;
         this.index = index;
         this.key = key;
         this.kind = kind;
         this.isInIterable = isInIterable;
      }

      public DefaultReturnValueNode(String name) {
         this(name, null, null, ElementKind.RETURN_VALUE, false);
      }

      @Override
      public String getName() {
         return this.name;
      }

      @Override
      public Integer getIndex() {
         return this.index;
      }

      @Override
      public Object getKey() {
         return this.key;
      }

      @Override
      public ElementKind getKind() {
         return this.kind;
      }

      @Override
      public boolean isInIterable() {
         return this.isInIterable;
      }

      @Override
      public <T extends Path.Node> T as(Class<T> nodeType) {
         throw new UnsupportedOperationException("Unwrapping is unsupported by this implementation");
      }
   }

   private final class EmptyDescriptor implements BeanDescriptor, ElementDescriptor.ConstraintFinder {
      private final Class<?> elementClass;

      EmptyDescriptor(Class<?> elementClass) {
         this.elementClass = elementClass;
      }

      @Override
      public boolean isBeanConstrained() {
         return false;
      }

      @Override
      public PropertyDescriptor getConstraintsForProperty(String propertyName) {
         return null;
      }

      @Override
      public Set<PropertyDescriptor> getConstrainedProperties() {
         return Collections.emptySet();
      }

      @Override
      public MethodDescriptor getConstraintsForMethod(String methodName, Class<?>... parameterTypes) {
         return null;
      }

      @Override
      public Set<MethodDescriptor> getConstrainedMethods(MethodType methodType, MethodType... methodTypes) {
         return Collections.emptySet();
      }

      @Override
      public ConstructorDescriptor getConstraintsForConstructor(Class<?>... parameterTypes) {
         return null;
      }

      @Override
      public Set<ConstructorDescriptor> getConstrainedConstructors() {
         return Collections.emptySet();
      }

      @Override
      public boolean hasConstraints() {
         return false;
      }

      @Override
      public Class<?> getElementClass() {
         return this.elementClass;
      }

      @Override
      public ElementDescriptor.ConstraintFinder unorderedAndMatchingGroups(Class<?>... groups) {
         return this;
      }

      @Override
      public ElementDescriptor.ConstraintFinder lookingAt(Scope scope) {
         return this;
      }

      @Override
      public ElementDescriptor.ConstraintFinder declaredOn(ElementType... types) {
         return this;
      }

      @Override
      public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
         return Collections.emptySet();
      }

      @Override
      public ElementDescriptor.ConstraintFinder findConstraints() {
         return this;
      }
   }

   private final class PathImpl implements Path {
      final Deque<Path.Node> nodes;

      private PathImpl(DefaultValidator.PathImpl nodes) {
         this.nodes = new LinkedList(nodes.nodes);
      }

      private PathImpl() {
         this.nodes = new LinkedList();
      }

      public Iterator<Path.Node> iterator() {
         return this.nodes.iterator();
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder();
         Iterator<Path.Node> i = this.nodes.iterator();

         while(i.hasNext()) {
            Path.Node node = (Path.Node)i.next();
            builder.append(node.getName());
            if (node.getKind() == ElementKind.CONTAINER_ELEMENT) {
               Integer index = node.getIndex();
               if (index != null) {
                  builder.append('[').append(index).append(']');
               } else {
                  Object key = node.getKey();
                  if (key != null) {
                     builder.append('[').append(key).append(']');
                  } else {
                     builder.append("[]");
                  }
               }
            }

            if (i.hasNext()) {
               builder.append('.');
            }
         }

         return builder.toString();
      }
   }
}
