package io.micronaut.inject.writer;

import io.micronaut.asm.ClassVisitor;
import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;
import io.micronaut.asm.MethodVisitor;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.asm.signature.SignatureVisitor;
import io.micronaut.context.AbstractConstructorInjectionPoint;
import io.micronaut.context.AbstractExecutableMethod;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Any;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.annotation.DefaultScope;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.InjectScope;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.PropertySource;
import io.micronaut.context.annotation.Provided;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.AccessorsStyle;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanConstructor;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.DefaultArgument;
import io.micronaut.core.type.TypeVariableResolver;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.inject.AdvisedBeanType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ConstructorInjectionPoint;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.ExecutableMethodsDefinition;
import io.micronaut.inject.InitializingBeanDefinition;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.ProxyBeanDefinition;
import io.micronaut.inject.ValidatedBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataWriter;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.annotation.MutableAnnotationMetadata;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ConstructorElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MemberElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.ast.PrimitiveElement;
import io.micronaut.inject.ast.PropertyElement;
import io.micronaut.inject.ast.TypedElement;
import io.micronaut.inject.ast.beans.BeanElement;
import io.micronaut.inject.ast.beans.BeanElementBuilder;
import io.micronaut.inject.configuration.ConfigurationMetadataBuilder;
import io.micronaut.inject.configuration.PropertyMetadata;
import io.micronaut.inject.processing.JavaModelUtils;
import io.micronaut.inject.qualifiers.AnyQualifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.inject.visitor.BeanElementVisitor;
import io.micronaut.inject.visitor.BeanElementVisitorContext;
import io.micronaut.inject.visitor.VisitorContext;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
public class BeanDefinitionWriter extends AbstractClassFileWriter implements BeanDefinitionVisitor, BeanElement, Toggleable {
   public static final String OMIT_CONFPROP_INJECTION_POINTS = "micronaut.processing.omit.confprop.injectpoints";
   public static final String CLASS_SUFFIX = "$Definition";
   private static final String ANN_CONSTRAINT = "javax.validation.Constraint";
   private static final Constructor<AbstractConstructorInjectionPoint> CONSTRUCTOR_ABSTRACT_CONSTRUCTOR_IP = (Constructor<AbstractConstructorInjectionPoint>)ReflectionUtils.findConstructor(
         AbstractConstructorInjectionPoint.class, BeanDefinition.class
      )
      .orElseThrow(() -> new ClassGenerationException("Invalid version of Micronaut present on the class path"));
   private static final Method POST_CONSTRUCT_METHOD = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class, "postConstruct", BeanResolutionContext.class, BeanContext.class, Object.class
   );
   private static final Method INJECT_BEAN_METHOD = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class, "injectBean", BeanResolutionContext.class, BeanContext.class, Object.class
   );
   private static final Method PRE_DESTROY_METHOD = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class, "preDestroy", BeanResolutionContext.class, BeanContext.class, Object.class
   );
   private static final Method GET_BEAN_FOR_CONSTRUCTOR_ARGUMENT = getBeanLookupMethod("getBeanForConstructorArgument", false);
   private static final Method GET_BEAN_REGISTRATIONS_FOR_CONSTRUCTOR_ARGUMENT = getBeanLookupMethod("getBeanRegistrationsForConstructorArgument", true);
   private static final Method GET_BEAN_REGISTRATION_FOR_CONSTRUCTOR_ARGUMENT = getBeanLookupMethod("getBeanRegistrationForConstructorArgument", true);
   private static final Method GET_BEANS_OF_TYPE_FOR_CONSTRUCTOR_ARGUMENT = getBeanLookupMethod("getBeansOfTypeForConstructorArgument", true);
   private static final Method GET_STREAM_OF_TYPE_FOR_CONSTRUCTOR_ARGUMENT = getBeanLookupMethod("getStreamOfTypeForConstructorArgument", true);
   private static final Method FIND_BEAN_FOR_CONSTRUCTOR_ARGUMENT = getBeanLookupMethod("findBeanForConstructorArgument", true);
   private static final Method GET_BEAN_FOR_FIELD = getBeanLookupMethod("getBeanForField", false);
   private static final Method GET_BEAN_FOR_ANNOTATION = getBeanLookupMethod("getBeanForAnnotation", false);
   private static final Method GET_BEAN_REGISTRATIONS_FOR_FIELD = getBeanLookupMethod("getBeanRegistrationsForField", true);
   private static final Method GET_BEAN_REGISTRATION_FOR_FIELD = getBeanLookupMethod("getBeanRegistrationForField", true);
   private static final Method GET_BEANS_OF_TYPE_FOR_FIELD = getBeanLookupMethod("getBeansOfTypeForField", true);
   private static final Method GET_VALUE_FOR_FIELD = getBeanLookupMethod("getValueForField", false);
   private static final Method GET_STREAM_OF_TYPE_FOR_FIELD = getBeanLookupMethod("getStreamOfTypeForField", true);
   private static final Method FIND_BEAN_FOR_FIELD = getBeanLookupMethod("findBeanForField", true);
   private static final Method GET_VALUE_FOR_PATH = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class, "getValueForPath", BeanResolutionContext.class, BeanContext.class, Argument.class, String.class
   );
   private static final Method CONTAINS_PROPERTIES_METHOD = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class, "containsProperties", BeanResolutionContext.class, BeanContext.class
   );
   private static final Method GET_BEAN_FOR_METHOD_ARGUMENT = getBeanLookupMethodForArgument("getBeanForMethodArgument", false);
   private static final Method GET_BEAN_REGISTRATIONS_FOR_METHOD_ARGUMENT = getBeanLookupMethodForArgument("getBeanRegistrationsForMethodArgument", true);
   private static final Method GET_BEAN_REGISTRATION_FOR_METHOD_ARGUMENT = getBeanLookupMethodForArgument("getBeanRegistrationForMethodArgument", true);
   private static final Method GET_BEANS_OF_TYPE_FOR_METHOD_ARGUMENT = getBeanLookupMethodForArgument("getBeansOfTypeForMethodArgument", true);
   private static final Method GET_STREAM_OF_TYPE_FOR_METHOD_ARGUMENT = getBeanLookupMethodForArgument("getStreamOfTypeForMethodArgument", true);
   private static final Method FIND_BEAN_FOR_METHOD_ARGUMENT = getBeanLookupMethodForArgument("findBeanForMethodArgument", true);
   private static final Method CHECK_INJECTED_BEAN_PROPERTY_VALUE = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class, "checkInjectedBeanPropertyValue", String.class, Object.class, String.class, String.class
   );
   private static final Method GET_PROPERTY_VALUE_FOR_METHOD_ARGUMENT = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class,
      "getPropertyValueForMethodArgument",
      BeanResolutionContext.class,
      BeanContext.class,
      Integer.TYPE,
      Integer.TYPE,
      String.class,
      String.class
   );
   private static final Method GET_PROPERTY_PLACEHOLDER_VALUE_FOR_METHOD_ARGUMENT = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class,
      "getPropertyPlaceholderValueForMethodArgument",
      BeanResolutionContext.class,
      BeanContext.class,
      Integer.TYPE,
      Integer.TYPE,
      String.class
   );
   private static final Method GET_BEAN_FOR_SETTER = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class,
      "getBeanForSetter",
      BeanResolutionContext.class,
      BeanContext.class,
      String.class,
      Argument.class,
      Qualifier.class
   );
   private static final Method GET_BEANS_OF_TYPE_FOR_SETTER = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class,
      "getBeansOfTypeForSetter",
      BeanResolutionContext.class,
      BeanContext.class,
      String.class,
      Argument.class,
      Argument.class,
      Qualifier.class
   );
   private static final Method GET_PROPERTY_VALUE_FOR_SETTER = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class,
      "getPropertyValueForSetter",
      BeanResolutionContext.class,
      BeanContext.class,
      String.class,
      Argument.class,
      String.class,
      String.class
   );
   private static final Method GET_PROPERTY_PLACEHOLDER_VALUE_FOR_SETTER = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class,
      "getPropertyPlaceholderValueForSetter",
      BeanResolutionContext.class,
      BeanContext.class,
      String.class,
      Argument.class,
      String.class
   );
   private static final Method GET_PROPERTY_VALUE_FOR_CONSTRUCTOR_ARGUMENT = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class,
      "getPropertyValueForConstructorArgument",
      BeanResolutionContext.class,
      BeanContext.class,
      Integer.TYPE,
      String.class,
      String.class
   );
   private static final Method GET_PROPERTY_PLACEHOLDER_VALUE_FOR_CONSTRUCTOR_ARGUMENT = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class,
      "getPropertyPlaceholderValueForConstructorArgument",
      BeanResolutionContext.class,
      BeanContext.class,
      Integer.TYPE,
      String.class
   );
   private static final Method GET_PROPERTY_VALUE_FOR_FIELD = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class,
      "getPropertyValueForField",
      BeanResolutionContext.class,
      BeanContext.class,
      Argument.class,
      String.class,
      String.class
   );
   private static final Method GET_PROPERTY_PLACEHOLDER_VALUE_FOR_FIELD = ReflectionUtils.getRequiredInternalMethod(
      AbstractInitializableBeanDefinition.class,
      "getPropertyPlaceholderValueForField",
      BeanResolutionContext.class,
      BeanContext.class,
      Argument.class,
      String.class
   );
   private static final io.micronaut.asm.commons.Method CONTAINS_PROPERTIES_VALUE_METHOD = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(
         AbstractInitializableBeanDefinition.class, "containsPropertiesValue", BeanResolutionContext.class, BeanContext.class, String.class
      )
   );
   private static final io.micronaut.asm.commons.Method CONTAINS_PROPERTY_VALUE_METHOD = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(
         AbstractInitializableBeanDefinition.class, "containsPropertyValue", BeanResolutionContext.class, BeanContext.class, String.class
      )
   );
   private static final Type TYPE_ABSTRACT_BEAN_DEFINITION = Type.getType(AbstractInitializableBeanDefinition.class);
   private static final io.micronaut.asm.commons.Method METHOD_OPTIONAL_EMPTY = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(Optional.class, "empty")
   );
   private static final Type TYPE_OPTIONAL = Type.getType(Optional.class);
   private static final io.micronaut.asm.commons.Method METHOD_OPTIONAL_OF = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(Optional.class, "of", Object.class)
   );
   private static final io.micronaut.asm.commons.Method METHOD_INVOKE_CONSTRUCTOR = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(ConstructorInjectionPoint.class, "invoke", Object[].class)
   );
   private static final String METHOD_DESCRIPTOR_CONSTRUCTOR_INSTANTIATE = getMethodDescriptor(
      Object.class,
      Arrays.asList(BeanResolutionContext.class, BeanContext.class, List.class, BeanDefinition.class, BeanConstructor.class, Integer.TYPE, Object[].class)
   );
   private static final String METHOD_DESCRIPTOR_INTERCEPTED_LIFECYCLE = getMethodDescriptor(
      Object.class, Arrays.asList(BeanResolutionContext.class, BeanContext.class, BeanDefinition.class, ExecutableMethod.class, Object.class)
   );
   private static final Method METHOD_GET_BEAN = ReflectionUtils.getRequiredInternalMethod(
      DefaultBeanContext.class, "getBean", BeanResolutionContext.class, Class.class, Qualifier.class
   );
   private static final io.micronaut.asm.commons.Method COLLECTION_TO_ARRAY = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredInternalMethod(Collection.class, "toArray", Object[].class)
   );
   private static final Type TYPE_RESOLUTION_CONTEXT = Type.getType(BeanResolutionContext.class);
   private static final Type TYPE_BEAN_CONTEXT = Type.getType(BeanContext.class);
   private static final Type TYPE_BEAN_DEFINITION = Type.getType(BeanDefinition.class);
   private static final String METHOD_DESCRIPTOR_INITIALIZE = Type.getMethodDescriptor(
      Type.getType(Object.class), Type.getType(BeanResolutionContext.class), Type.getType(BeanContext.class), Type.getType(Object.class)
   );
   private static final io.micronaut.asm.commons.Method PROTECTED_ABSTRACT_BEAN_DEFINITION_CONSTRUCTOR = new io.micronaut.asm.commons.Method(
      "<init>", getConstructorDescriptor(new Class[]{Class.class, AbstractInitializableBeanDefinition.MethodOrFieldReference.class})
   );
   private static final io.micronaut.asm.commons.Method SET_FIELD_WITH_REFLECTION_METHOD = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(
         AbstractInitializableBeanDefinition.class,
         "setFieldWithReflection",
         BeanResolutionContext.class,
         BeanContext.class,
         Integer.TYPE,
         Object.class,
         Object.class
      )
   );
   private static final io.micronaut.asm.commons.Method INVOKE_WITH_REFLECTION_METHOD = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(
         AbstractInitializableBeanDefinition.class,
         "invokeMethodWithReflection",
         BeanResolutionContext.class,
         BeanContext.class,
         Integer.TYPE,
         Object.class,
         Object[].class
      )
   );
   private static final io.micronaut.asm.commons.Method BEAN_DEFINITION_CLASS_CONSTRUCTOR = new io.micronaut.asm.commons.Method(
      "<init>",
      getConstructorDescriptor(
         new Class[]{
            Class.class,
            AbstractInitializableBeanDefinition.MethodOrFieldReference.class,
            AnnotationMetadata.class,
            AbstractInitializableBeanDefinition.MethodReference[].class,
            AbstractInitializableBeanDefinition.FieldReference[].class,
            AbstractInitializableBeanDefinition.AnnotationReference[].class,
            ExecutableMethodsDefinition.class,
            Map.class,
            Optional.class,
            Boolean.TYPE,
            Boolean.TYPE,
            Boolean.TYPE,
            Boolean.TYPE,
            Boolean.TYPE,
            Boolean.TYPE,
            Boolean.TYPE,
            Boolean.TYPE
         }
      )
   );
   private static final String FIELD_CONSTRUCTOR = "$CONSTRUCTOR";
   private static final String FIELD_INJECTION_METHODS = "$INJECTION_METHODS";
   private static final String FIELD_INJECTION_FIELDS = "$INJECTION_FIELDS";
   private static final String FIELD_ANNOTATION_INJECTIONS = "$ANNOTATION_INJECTIONS";
   private static final String FIELD_TYPE_ARGUMENTS = "$TYPE_ARGUMENTS";
   private static final String FIELD_INNER_CLASSES = "$INNER_CONFIGURATION_CLASSES";
   private static final String FIELD_EXPOSED_TYPES = "$EXPOSED_TYPES";
   private static final io.micronaut.asm.commons.Method METHOD_REFERENCE_CONSTRUCTOR = new io.micronaut.asm.commons.Method(
      "<init>", getConstructorDescriptor(new Class[]{Class.class, String.class, Argument[].class, AnnotationMetadata.class, Boolean.TYPE})
   );
   private static final io.micronaut.asm.commons.Method METHOD_REFERENCE_CONSTRUCTOR_POST_PRE = new io.micronaut.asm.commons.Method(
      "<init>",
      getConstructorDescriptor(new Class[]{Class.class, String.class, Argument[].class, AnnotationMetadata.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE})
   );
   private static final io.micronaut.asm.commons.Method FIELD_REFERENCE_CONSTRUCTOR = new io.micronaut.asm.commons.Method(
      "<init>", getConstructorDescriptor(new Class[]{Class.class, Argument.class, Boolean.TYPE})
   );
   private static final io.micronaut.asm.commons.Method ANNOTATION_REFERENCE_CONSTRUCTOR = new io.micronaut.asm.commons.Method(
      "<init>", getConstructorDescriptor(new Class[]{Argument.class})
   );
   private static final io.micronaut.asm.commons.Method METHOD_QUALIFIER_FOR_ARGUMENT = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(Qualifiers.class, "forArgument", Argument.class)
   );
   private static final io.micronaut.asm.commons.Method METHOD_QUALIFIER_BY_NAME = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(Qualifiers.class, "byName", String.class)
   );
   private static final io.micronaut.asm.commons.Method METHOD_QUALIFIER_BY_ANNOTATION = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(Qualifiers.class, "byAnnotationSimple", AnnotationMetadata.class, String.class)
   );
   private static final io.micronaut.asm.commons.Method METHOD_QUALIFIER_BY_REPEATABLE_ANNOTATION = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(Qualifiers.class, "byRepeatableAnnotation", AnnotationMetadata.class, String.class)
   );
   private static final io.micronaut.asm.commons.Method METHOD_QUALIFIER_BY_QUALIFIERS = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(Qualifiers.class, "byQualifiers", Qualifier[].class)
   );
   private static final io.micronaut.asm.commons.Method METHOD_QUALIFIER_BY_INTERCEPTOR_BINDING = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(Qualifiers.class, "byInterceptorBinding", AnnotationMetadata.class)
   );
   private static final io.micronaut.asm.commons.Method METHOD_QUALIFIER_BY_TYPE = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(Qualifiers.class, "byType", Class[].class)
   );
   private static final io.micronaut.asm.commons.Method METHOD_BEAN_RESOLUTION_CONTEXT_MARK_FACTORY = io.micronaut.asm.commons.Method.getMethod(
      ReflectionUtils.getRequiredMethod(BeanResolutionContext.class, "markDependentAsFactory")
   );
   private static final Type TYPE_QUALIFIERS = Type.getType(Qualifiers.class);
   private static final Type TYPE_QUALIFIER = Type.getType(Qualifier.class);
   private static final String MESSAGE_ONLY_SINGLE_CALL_PERMITTED = "Only a single call to visitBeanFactoryMethod(..) is permitted";
   private final ClassWriter classWriter;
   private final String beanFullClassName;
   private final String beanDefinitionName;
   private final String beanDefinitionInternalName;
   private final Type beanType;
   private final Set<Class> interfaceTypes;
   private final Map<String, Integer> defaultsStorage = new HashMap();
   private final Map<String, GeneratorAdapter> loadTypeMethods = new LinkedHashMap();
   private final Map<String, ClassWriter> innerClasses = new LinkedHashMap(2);
   private final String packageName;
   private final String beanSimpleClassName;
   private final Type beanDefinitionType;
   private final boolean isInterface;
   private final boolean isAbstract;
   private final boolean isConfigurationProperties;
   private final ConfigurationMetadataBuilder<?> metadataBuilder;
   private final Element beanProducingElement;
   private final ClassElement beanTypeElement;
   private final VisitorContext visitorContext;
   private final boolean isPrimitiveBean;
   private final List<String> beanTypeInnerClasses;
   private GeneratorAdapter buildMethodVisitor;
   private GeneratorAdapter injectMethodVisitor;
   private GeneratorAdapter checkIfShouldLoadMethodVisitor;
   private Label injectEnd = null;
   private GeneratorAdapter preDestroyMethodVisitor;
   private GeneratorAdapter postConstructMethodVisitor;
   private boolean postConstructAdded;
   private GeneratorAdapter interceptedDisposeMethod;
   private int currentFieldIndex = 0;
   private int currentMethodIndex = 0;
   private int buildInstanceLocalVarIndex = -1;
   private int injectInstanceLocalVarIndex = -1;
   private int postConstructInstanceLocalVarIndex = -1;
   private int preDestroyInstanceLocalVarIndex = -1;
   private boolean beanFinalized = false;
   private Type superType = TYPE_ABSTRACT_BEAN_DEFINITION;
   private boolean isParametrized = false;
   private boolean superBeanDefinition = false;
   private boolean isSuperFactory = false;
   private final AnnotationMetadata annotationMetadata;
   private ConfigBuilderState currentConfigBuilderState;
   private boolean preprocessMethods = false;
   private Map<String, Map<String, ClassElement>> typeArguments;
   private String interceptedType;
   private int innerClassIndex;
   private final List<BeanDefinitionWriter.FieldVisitData> fieldInjectionPoints = new ArrayList(2);
   private final List<BeanDefinitionWriter.MethodVisitData> methodInjectionPoints = new ArrayList(2);
   private final List<BeanDefinitionWriter.MethodVisitData> postConstructMethodVisits = new ArrayList(2);
   private final List<BeanDefinitionWriter.MethodVisitData> preDestroyMethodVisits = new ArrayList(2);
   private final List<BeanDefinitionWriter.MethodVisitData> allMethodVisits = new ArrayList(2);
   private final Map<Type, List<BeanDefinitionWriter.AnnotationVisitData>> annotationInjectionPoints = new LinkedHashMap(2);
   private final Map<String, Boolean> isLifeCycleCache = new HashMap(2);
   private ExecutableMethodsDefinitionWriter executableMethodsDefinitionWriter;
   private Object constructor;
   private boolean constructorRequiresReflection;
   private boolean disabled = false;
   private final boolean keepConfPropInjectPoints;

   public BeanDefinitionWriter(ClassElement classElement, ConfigurationMetadataBuilder<?> metadataBuilder, VisitorContext visitorContext) {
      this(classElement, OriginatingElements.of(classElement), metadataBuilder, visitorContext, null);
   }

   public BeanDefinitionWriter(
      ClassElement classElement, OriginatingElements originatingElements, ConfigurationMetadataBuilder<?> metadataBuilder, VisitorContext visitorContext
   ) {
      this(classElement, originatingElements, metadataBuilder, visitorContext, null);
   }

   public BeanDefinitionWriter(
      Element beanProducingElement,
      OriginatingElements originatingElements,
      ConfigurationMetadataBuilder<?> metadataBuilder,
      VisitorContext visitorContext,
      @Nullable Integer uniqueIdentifier
   ) {
      super(originatingElements);
      this.metadataBuilder = metadataBuilder;
      this.classWriter = new ClassWriter(3);
      this.beanProducingElement = beanProducingElement;
      if (beanProducingElement instanceof ClassElement) {
         ClassElement classElement = (ClassElement)beanProducingElement;
         this.autoApplyNamedToBeanProducingElement(classElement);
         if (classElement.isPrimitive()) {
            throw new IllegalArgumentException("Primitive beans can only be created from factories");
         }

         this.beanTypeElement = classElement;
         this.packageName = classElement.getPackageName();
         this.isInterface = classElement.isInterface();
         this.isAbstract = classElement.isAbstract();
         this.beanFullClassName = classElement.getName();
         this.beanSimpleClassName = classElement.getSimpleName();
         this.beanDefinitionName = getBeanDefinitionName(this.packageName, this.beanSimpleClassName);
      } else if (beanProducingElement instanceof MethodElement) {
         this.autoApplyNamedToBeanProducingElement(beanProducingElement);
         MethodElement factoryMethodElement = (MethodElement)beanProducingElement;
         ClassElement producedElement = factoryMethodElement.getGenericReturnType();
         this.beanTypeElement = producedElement;
         this.packageName = producedElement.getPackageName();
         this.isInterface = producedElement.isInterface();
         this.isAbstract = false;
         this.beanFullClassName = producedElement.getName();
         this.beanSimpleClassName = producedElement.getSimpleName();
         String upperCaseMethodName = NameUtils.capitalize(factoryMethodElement.getName());
         if (uniqueIdentifier == null) {
            throw new IllegalArgumentException("Factory methods require passing a unique identifier");
         }

         ClassElement declaringType = factoryMethodElement.getOwningType();
         this.beanDefinitionName = declaringType.getPackageName()
            + "."
            + prefixClassName(declaringType.getSimpleName())
            + "$"
            + upperCaseMethodName
            + uniqueIdentifier
            + "$Definition";
      } else if (beanProducingElement instanceof FieldElement) {
         this.autoApplyNamedToBeanProducingElement(beanProducingElement);
         FieldElement factoryMethodElement = (FieldElement)beanProducingElement;
         ClassElement producedElement = factoryMethodElement.getGenericField();
         this.beanTypeElement = producedElement;
         this.packageName = producedElement.getPackageName();
         this.isInterface = producedElement.isInterface();
         this.isAbstract = false;
         this.beanFullClassName = producedElement.getName();
         this.beanSimpleClassName = producedElement.getSimpleName();
         String fieldName = NameUtils.capitalize(factoryMethodElement.getName());
         if (uniqueIdentifier == null) {
            throw new IllegalArgumentException("Factory fields require passing a unique identifier");
         }

         ClassElement declaringType = factoryMethodElement.getOwningType();
         this.beanDefinitionName = declaringType.getPackageName()
            + "."
            + prefixClassName(declaringType.getSimpleName())
            + "$"
            + fieldName
            + uniqueIdentifier
            + "$Definition";
      } else {
         if (!(beanProducingElement instanceof BeanElementBuilder)) {
            throw new IllegalArgumentException("Unsupported element type: " + beanProducingElement.getClass().getName());
         }

         BeanElementBuilder beanElementBuilder = (BeanElementBuilder)beanProducingElement;
         this.beanTypeElement = beanElementBuilder.getBeanType();
         this.packageName = this.beanTypeElement.getPackageName();
         this.isInterface = this.beanTypeElement.isInterface();
         this.isAbstract = this.beanTypeElement.isAbstract();
         this.beanFullClassName = this.beanTypeElement.getName();
         this.beanSimpleClassName = this.beanTypeElement.getSimpleName();
         if (uniqueIdentifier == null) {
            throw new IllegalArgumentException("Beans produced by addAssociatedBean(..) require passing a unique identifier");
         }

         Element originatingElement = beanElementBuilder.getOriginatingElement();
         if (originatingElement instanceof ClassElement) {
            ClassElement originatingClass = (ClassElement)originatingElement;
            this.beanDefinitionName = this.getAssociatedBeanName(uniqueIdentifier, originatingClass);
         } else {
            if (!(originatingElement instanceof MethodElement)) {
               throw new IllegalArgumentException("Unsupported originating element");
            }

            ClassElement originatingClass = ((MethodElement)originatingElement).getDeclaringType();
            this.beanDefinitionName = this.getAssociatedBeanName(uniqueIdentifier, originatingClass);
         }
      }

      this.isPrimitiveBean = this.beanTypeElement.isPrimitive() && !this.beanTypeElement.isArray();
      this.annotationMetadata = beanProducingElement.getAnnotationMetadata();
      this.beanDefinitionType = getTypeReferenceForName(this.beanDefinitionName, new String[0]);
      this.beanType = getTypeReference(this.beanTypeElement);
      this.beanDefinitionInternalName = getInternalName(this.beanDefinitionName);
      this.interfaceTypes = new TreeSet(Comparator.comparing(Class::getName));
      this.interfaceTypes.add(BeanFactory.class);
      this.isConfigurationProperties = this.isConfigurationProperties(this.annotationMetadata);
      this.validateExposedTypes(this.annotationMetadata, visitorContext);
      this.visitorContext = visitorContext;
      this.beanTypeInnerClasses = (List)this.beanTypeElement
         .getEnclosedElements(ElementQuery.of(ClassElement.class))
         .stream()
         .filter(this::isConfigurationProperties)
         .map(Element::getName)
         .collect(Collectors.toList());
      String prop = (String)visitorContext.getOptions().get("micronaut.processing.omit.confprop.injectpoints");
      this.keepConfPropInjectPoints = prop == null || !prop.equals("true");
   }

   @Override
   public boolean isEnabled() {
      return !this.disabled;
   }

   @Nullable
   public ExecutableMethodsDefinitionWriter getExecutableMethodsWriter() {
      return this.executableMethodsDefinitionWriter;
   }

   @NonNull
   private String getAssociatedBeanName(@NonNull Integer uniqueIdentifier, ClassElement originatingClass) {
      return originatingClass.getPackageName()
         + "."
         + prefixClassName(originatingClass.getSimpleName())
         + prefixClassName(this.beanSimpleClassName)
         + uniqueIdentifier
         + "$Definition";
   }

   private void autoApplyNamedToBeanProducingElement(Element beanProducingElement) {
      AnnotationMetadata annotationMetadata = beanProducingElement.getAnnotationMetadata();
      if (!annotationMetadata.hasAnnotation(EachProperty.class) && !annotationMetadata.hasAnnotation(EachBean.class)) {
         this.autoApplyNamedIfPresent(beanProducingElement, annotationMetadata);
      }

   }

   private void validateExposedTypes(AnnotationMetadata annotationMetadata, VisitorContext visitorContext) {
      if (annotationMetadata instanceof AnnotationMetadataHierarchy) {
         annotationMetadata = annotationMetadata.getDeclaredMetadata();
      }

      String[] types = annotationMetadata.stringValues(Bean.class, "typed");
      if (ArrayUtils.isNotEmpty(types) && !this.beanTypeElement.isProxy()) {
         for(String name : types) {
            ClassElement exposedType = (ClassElement)visitorContext.getClassElement(name).orElse(null);
            if (exposedType == null) {
               visitorContext.fail("Bean defines an exposed type [" + name + "] that is not on the classpath", this.beanProducingElement);
            } else if (!this.beanTypeElement.isAssignable(exposedType)) {
               visitorContext.fail("Bean defines an exposed type [" + name + "] that is not implemented by the bean type", this.beanProducingElement);
            }
         }
      }

   }

   @NonNull
   private static String getBeanDefinitionName(String packageName, String className) {
      return packageName + "." + prefixClassName(className) + "$Definition";
   }

   private static String prefixClassName(String className) {
      return className.startsWith("$") ? className : "$" + className;
   }

   @NonNull
   @Override
   public ClassElement[] getTypeArguments() {
      if (this.hasTypeArguments()) {
         Map<String, ClassElement> args = (Map)this.typeArguments.get(this.getBeanTypeName());
         if (CollectionUtils.isNotEmpty(args)) {
            return (ClassElement[])args.values().toArray(ClassElement.ZERO_CLASS_ELEMENTS);
         }
      }

      return BeanDefinitionVisitor.super.getTypeArguments();
   }

   @NonNull
   @Override
   public String getBeanDefinitionReferenceClassName() {
      return this.beanDefinitionName + "$Reference";
   }

   public final List<BeanDefinitionWriter.MethodVisitData> getPostConstructMethodVisits() {
      return Collections.unmodifiableList(this.postConstructMethodVisits);
   }

   public ClassVisitor getClassWriter() {
      return this.classWriter;
   }

   @Override
   public boolean isInterface() {
      return this.isInterface;
   }

   @Override
   public boolean isSingleton() {
      return this.annotationMetadata.hasDeclaredStereotype("javax.inject.Singleton");
   }

   @Override
   public void visitBeanDefinitionInterface(Class<? extends BeanDefinition> interfaceType) {
      this.interfaceTypes.add(interfaceType);
   }

   @Override
   public void visitSuperBeanDefinition(String name) {
      this.superBeanDefinition = true;
      this.superType = getTypeReferenceForName(name, new String[0]);
   }

   @Override
   public void visitSuperBeanDefinitionFactory(String beanName) {
      this.visitSuperBeanDefinition(beanName);
      this.superBeanDefinition = false;
      this.isSuperFactory = true;
   }

   @Override
   public String getBeanTypeName() {
      return this.beanFullClassName;
   }

   @Override
   public Type getProvidedType() {
      return this.beanType;
   }

   @Override
   public void setValidated(boolean validated) {
      if (validated) {
         this.interfaceTypes.add(ValidatedBeanDefinition.class);
      } else {
         this.interfaceTypes.remove(ValidatedBeanDefinition.class);
      }

   }

   @Override
   public void setInterceptedType(String typeName) {
      if (typeName != null) {
         this.interfaceTypes.add(AdvisedBeanType.class);
      }

      this.interceptedType = typeName;
   }

   @Override
   public Optional<Type> getInterceptedType() {
      return Optional.ofNullable(this.interceptedType).map(x$0 -> getTypeReferenceForName(x$0, new String[0]));
   }

   @Override
   public boolean isValidated() {
      return this.interfaceTypes.contains(ValidatedBeanDefinition.class);
   }

   @Override
   public String getBeanDefinitionName() {
      return this.beanDefinitionName;
   }

   @Override
   public void visitBeanFactoryMethod(ClassElement factoryClass, MethodElement factoryMethod) {
      if (this.constructor != null) {
         throw new IllegalStateException("Only a single call to visitBeanFactoryMethod(..) is permitted");
      } else {
         this.constructor = factoryMethod;
         this.visitBuildFactoryMethodDefinition(factoryClass, factoryMethod, factoryMethod.getParameters());
         this.visitInjectMethodDefinition();
      }
   }

   @Override
   public void visitBeanFactoryMethod(ClassElement factoryClass, MethodElement factoryMethod, ParameterElement[] parameters) {
      if (this.constructor != null) {
         throw new IllegalStateException("Only a single call to visitBeanFactoryMethod(..) is permitted");
      } else {
         this.constructor = factoryMethod;
         this.visitBuildFactoryMethodDefinition(factoryClass, factoryMethod, parameters);
         this.visitInjectMethodDefinition();
      }
   }

   @Override
   public void visitBeanFactoryField(ClassElement factoryClass, FieldElement factoryField) {
      if (this.constructor != null) {
         throw new IllegalStateException("Only a single call to visitBeanFactoryMethod(..) is permitted");
      } else {
         this.constructor = factoryField;
         this.autoApplyNamedIfPresent(factoryField, factoryField.getAnnotationMetadata());
         this.visitBuildFactoryMethodDefinition(factoryClass, factoryField);
         this.visitInjectMethodDefinition();
      }
   }

   @Override
   public void visitBeanDefinitionConstructor(MethodElement constructor, boolean requiresReflection, VisitorContext visitorContext) {
      if (this.constructor == null) {
         this.applyConfigurationInjectionIfNecessary(constructor);
         this.constructor = constructor;
         this.constructorRequiresReflection = requiresReflection;
         this.visitBuildMethodDefinition(constructor, requiresReflection);
         this.visitInjectMethodDefinition();
      }

   }

   private void applyConfigurationInjectionIfNecessary(MethodElement constructor) {
      boolean isRecordConfig = this.isRecordConfig(constructor);
      if (isRecordConfig || constructor.hasAnnotation(ConfigurationInject.class)) {
         List<String> injectionTypes = Arrays.asList(
            Property.class.getName(), Value.class.getName(), Parameter.class.getName(), "javax.inject.Qualifier", "javax.inject.Inject"
         );
         if (isRecordConfig) {
            List<PropertyElement> beanProperties = constructor.getDeclaringType().getBeanProperties();
            ParameterElement[] parameters = constructor.getParameters();
            if (beanProperties.size() == parameters.length) {
               for(int i = 0; i < parameters.length; ++i) {
                  ParameterElement parameter = parameters[i];
                  PropertyElement bp = (PropertyElement)beanProperties.get(i);
                  AnnotationMetadata beanPropertyMetadata = bp.getAnnotationMetadata();
                  AnnotationMetadata annotationMetadata = parameter.getAnnotationMetadata();
                  if (injectionTypes.stream().noneMatch(beanPropertyMetadata::hasStereotype)) {
                     this.processConfigurationConstructorParameter(parameter, annotationMetadata);
                  }

                  if (annotationMetadata.hasStereotype("javax.validation.Constraint")) {
                     this.setValidated(true);
                  }
               }
            } else {
               this.processConfigurationInjectionConstructor(constructor, injectionTypes);
            }
         } else {
            this.processConfigurationInjectionConstructor(constructor, injectionTypes);
         }
      }

   }

   private void processConfigurationInjectionConstructor(MethodElement constructor, List<String> injectionTypes) {
      ParameterElement[] parameters = constructor.getParameters();

      for(ParameterElement parameter : parameters) {
         AnnotationMetadata annotationMetadata = parameter.getAnnotationMetadata();
         if (injectionTypes.stream().noneMatch(annotationMetadata::hasStereotype)) {
            this.processConfigurationConstructorParameter(parameter, annotationMetadata);
         }

         if (annotationMetadata.hasStereotype("javax.validation.Constraint")) {
            this.setValidated(true);
         }
      }

   }

   private void processConfigurationConstructorParameter(ParameterElement parameter, AnnotationMetadata annotationMetadata) {
      ClassElement parameterType = parameter.getGenericType();
      if (!parameterType.hasStereotype("javax.inject.Scope")) {
         PropertyMetadata pm = this.metadataBuilder
            .visitProperty(
               parameterType.getName(),
               parameter.getName(),
               (String)parameter.getDocumentation().orElse(null),
               (String)annotationMetadata.stringValue(Bindable.class, "defaultValue").orElse(null)
            );
         parameter.annotate(Property.class, builder -> builder.member("name", pm.getPath()));
      }

   }

   private boolean isRecordConfig(MethodElement constructor) {
      ClassElement declaringType = constructor.getDeclaringType();
      return declaringType.isRecord() && declaringType.hasStereotype(ConfigurationReader.class);
   }

   @Override
   public void visitDefaultConstructor(AnnotationMetadata annotationMetadata, VisitorContext visitorContext) {
      if (this.constructor == null) {
         ClassElement bean = ClassElement.of(this.beanType.getClassName());
         MethodElement defaultConstructor = MethodElement.of(bean, annotationMetadata, bean, bean, "<init>");
         this.constructor = defaultConstructor;
         this.visitBuildMethodDefinition(defaultConstructor, false);
         this.visitInjectMethodDefinition();
      }

   }

   @Override
   public void visitBeanDefinitionEnd() {
      if (this.classWriter == null) {
         throw new IllegalStateException("At least one called to visitBeanDefinitionConstructor(..) is required");
      } else {
         this.processAllBeanElementVisitors();
         if (this.constructor instanceof MethodElement) {
            MethodElement methodElement = (MethodElement)this.constructor;
            boolean isParametrized = Arrays.stream(methodElement.getParameters())
               .map(AnnotationMetadataProvider::getAnnotationMetadata)
               .anyMatch(this::isAnnotatedWithParameter);
            if (isParametrized) {
               this.interfaceTypes.add(ParametrizedBeanFactory.class);
            }
         }

         String[] interfaceInternalNames = new String[this.interfaceTypes.size()];
         Iterator<Class> j = this.interfaceTypes.iterator();

         for(int i = 0; i < interfaceInternalNames.length; ++i) {
            interfaceInternalNames[i] = Type.getInternalName((Class<?>)j.next());
         }

         String beanDefSignature = this.generateBeanDefSig(this.beanType);
         this.classWriter
            .visit(
               52,
               4096,
               this.beanDefinitionInternalName,
               beanDefSignature,
               this.isSuperFactory ? TYPE_ABSTRACT_BEAN_DEFINITION.getInternalName() : this.superType.getInternalName(),
               interfaceInternalNames
            );
         this.classWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
         if (this.buildMethodVisitor == null) {
            throw new IllegalStateException("At least one call to visitBeanDefinitionConstructor() is required");
         } else {
            final GeneratorAdapter staticInit = this.visitStaticInitializer(this.classWriter);
            this.classWriter
               .visitField(26, "$CONSTRUCTOR", Type.getType(AbstractInitializableBeanDefinition.MethodOrFieldReference.class).getDescriptor(), null, null);
            int methodsLength = this.allMethodVisits.size();
            if (!this.superBeanDefinition && methodsLength > 0) {
               Type methodsFieldType = Type.getType(AbstractInitializableBeanDefinition.MethodReference[].class);
               this.classWriter.visitField(26, "$INJECTION_METHODS", methodsFieldType.getDescriptor(), null, null);
               pushNewArray(staticInit, AbstractInitializableBeanDefinition.MethodReference.class, methodsLength);
               int i = 0;

               for(BeanDefinitionWriter.MethodVisitData methodVisitData : this.allMethodVisits) {
                  pushStoreInArray(
                     staticInit,
                     i++,
                     methodsLength,
                     () -> this.pushNewMethodReference(
                           staticInit,
                           JavaModelUtils.getTypeReference(methodVisitData.beanType),
                           methodVisitData.methodElement,
                           methodVisitData.requiresReflection,
                           methodVisitData.isPostConstruct(),
                           methodVisitData.isPreDestroy()
                        )
                  );
               }

               staticInit.putStatic(this.beanDefinitionType, "$INJECTION_METHODS", methodsFieldType);
            }

            if (!this.fieldInjectionPoints.isEmpty()) {
               Type fieldsFieldType = Type.getType(AbstractInitializableBeanDefinition.FieldReference[].class);
               this.classWriter.visitField(26, "$INJECTION_FIELDS", fieldsFieldType.getDescriptor(), null, null);
               int length = this.fieldInjectionPoints.size();
               pushNewArray(staticInit, AbstractInitializableBeanDefinition.FieldReference.class, length);

               for(int i = 0; i < length; ++i) {
                  BeanDefinitionWriter.FieldVisitData fieldVisitData = (BeanDefinitionWriter.FieldVisitData)this.fieldInjectionPoints.get(i);
                  pushStoreInArray(
                     staticInit,
                     i,
                     length,
                     () -> this.pushNewFieldReference(
                           staticInit, JavaModelUtils.getTypeReference(fieldVisitData.beanType), fieldVisitData.fieldElement, fieldVisitData.requiresReflection
                        )
                  );
               }

               staticInit.putStatic(this.beanDefinitionType, "$INJECTION_FIELDS", fieldsFieldType);
            }

            if (!this.annotationInjectionPoints.isEmpty()) {
               Type annotationInjectionsFieldType = Type.getType(AbstractInitializableBeanDefinition.AnnotationReference[].class);
               this.classWriter.visitField(26, "$ANNOTATION_INJECTIONS", annotationInjectionsFieldType.getDescriptor(), null, null);
               List<Type> injectedTypes = new ArrayList(this.annotationInjectionPoints.keySet());
               int length = injectedTypes.size();
               pushNewArray(staticInit, AbstractInitializableBeanDefinition.AnnotationReference.class, length);

               for(int i = 0; i < length; ++i) {
                  Type annotationVisitData = (Type)injectedTypes.get(i);
                  pushStoreInArray(staticInit, i, length, () -> this.pushNewAnnotationReference(staticInit, annotationVisitData));
               }

               staticInit.putStatic(this.beanDefinitionType, "$ANNOTATION_INJECTIONS", annotationInjectionsFieldType);
            }

            if (!this.superBeanDefinition && this.hasTypeArguments()) {
               Type typeArgumentsFieldType = Type.getType(Map.class);
               this.classWriter.visitField(26, "$TYPE_ARGUMENTS", typeArgumentsFieldType.getDescriptor(), null, null);
               pushStringMapOf(
                  staticInit,
                  this.typeArguments,
                  true,
                  null,
                  new Consumer<Map<String, ClassElement>>() {
                     public void accept(Map<String, ClassElement> stringClassElementMap) {
                        AbstractClassFileWriter.pushTypeArgumentElements(
                           BeanDefinitionWriter.this.beanDefinitionType,
                           BeanDefinitionWriter.this.classWriter,
                           staticInit,
                           BeanDefinitionWriter.this.beanDefinitionName,
                           stringClassElementMap,
                           BeanDefinitionWriter.this.defaultsStorage,
                           BeanDefinitionWriter.this.loadTypeMethods
                        );
                     }
                  }
               );
               staticInit.putStatic(this.beanDefinitionType, "$TYPE_ARGUMENTS", typeArgumentsFieldType);
            }

            this.visitBeanDefinitionConstructorInternal(staticInit, this.constructor, this.constructorRequiresReflection);
            this.addInnerConfigurationMethod(staticInit);
            this.addGetExposedTypes(staticInit);
            staticInit.returnValue();
            staticInit.visitMaxs(13, this.defaultsStorage.size() + 3);
            staticInit.visitEnd();
            if (this.buildMethodVisitor != null) {
               if (this.isPrimitiveBean) {
                  pushBoxPrimitiveIfNecessary(this.beanType, this.buildMethodVisitor);
               }

               this.buildMethodVisitor.returnValue();
               this.buildMethodVisitor.visitMaxs(13, 10);
            }

            if (this.injectMethodVisitor != null) {
               if (this.injectEnd != null) {
                  this.injectMethodVisitor.visitLabel(this.injectEnd);
               }

               this.invokeSuperInjectMethod(this.injectMethodVisitor, INJECT_BEAN_METHOD);
               if (this.isPrimitiveBean) {
                  pushBoxPrimitiveIfNecessary(this.beanType, this.injectMethodVisitor);
               }

               this.injectMethodVisitor.returnValue();
               this.injectMethodVisitor.visitMaxs(13, 10);
            }

            if (this.postConstructMethodVisitor != null) {
               this.postConstructMethodVisitor.loadLocal(this.postConstructInstanceLocalVarIndex);
               this.postConstructMethodVisitor.returnValue();
               this.postConstructMethodVisitor.visitMaxs(13, 10);
            }

            if (this.preDestroyMethodVisitor != null) {
               this.preDestroyMethodVisitor.loadLocal(this.preDestroyInstanceLocalVarIndex);
               this.preDestroyMethodVisitor.returnValue();
               this.preDestroyMethodVisitor.visitMaxs(13, 10);
            }

            if (this.interceptedDisposeMethod != null) {
               this.interceptedDisposeMethod.visitMaxs(1, 1);
               this.interceptedDisposeMethod.visitEnd();
            }

            if (this.checkIfShouldLoadMethodVisitor != null) {
               this.buildCheckIfShouldLoadMethod(this.checkIfShouldLoadMethodVisitor, this.annotationInjectionPoints);
               this.checkIfShouldLoadMethodVisitor.visitMaxs(13, 10);
            }

            this.getInterceptedType().ifPresent(t -> this.implementInterceptedTypeMethod(t, this.classWriter));

            for(GeneratorAdapter method : this.loadTypeMethods.values()) {
               method.visitMaxs(3, 1);
               method.visitEnd();
            }

            this.classWriter.visitEnd();
            this.beanFinalized = true;
         }
      }
   }

   private void buildCheckIfShouldLoadMethod(GeneratorAdapter adapter, Map<Type, List<BeanDefinitionWriter.AnnotationVisitData>> beanPropertyVisitData) {
      List<Type> injectedTypes = new ArrayList(beanPropertyVisitData.keySet());

      for(int currentTypeIndex = 0; currentTypeIndex < injectedTypes.size(); ++currentTypeIndex) {
         Type injectedType = (Type)injectedTypes.get(currentTypeIndex);
         List<BeanDefinitionWriter.AnnotationVisitData> annotationVisitData = (List)beanPropertyVisitData.get(injectedType);
         boolean multiplePropertiesFromType = annotationVisitData.size() > 1;
         Integer injectedBeanIndex = null;

         for(int i = 0; i < annotationVisitData.size(); ++i) {
            int currentPropertyIndex = i;
            boolean isLastProperty = currentTypeIndex == injectedTypes.size() - 1 && currentPropertyIndex == annotationVisitData.size() - 1;
            BeanDefinitionWriter.AnnotationVisitData visitData = (BeanDefinitionWriter.AnnotationVisitData)annotationVisitData.get(currentPropertyIndex);
            MethodElement propertyGetter = visitData.memberPropertyGetter;
            adapter.loadThis();
            adapter.push(visitData.memberPropertyName);
            if (injectedBeanIndex != null) {
               adapter.loadLocal(injectedBeanIndex);
            } else {
               adapter.loadThis();
               adapter.loadArg(0);
               adapter.loadArg(1);
               adapter.push(currentTypeIndex);
               this.pushQualifier(adapter, visitData.memberBeanType, () -> this.resolveAnnotationArgument(adapter, currentPropertyIndex));
               this.pushInvokeMethodOnSuperClass(adapter, GET_BEAN_FOR_ANNOTATION);
               pushCastToType(adapter, visitData.memberBeanType);
               if (multiplePropertiesFromType) {
                  injectedBeanIndex = adapter.newLocal(injectedType);
                  adapter.storeLocal(injectedBeanIndex);
                  adapter.loadLocal(injectedBeanIndex);
               }
            }

            io.micronaut.asm.commons.Method propertyGetterMethod = io.micronaut.asm.commons.Method.getMethod(propertyGetter.getDescription(false));
            if (visitData.memberBeanType.getType().isInterface()) {
               adapter.invokeInterface(injectedType, propertyGetterMethod);
            } else {
               adapter.invokeVirtual(injectedType, propertyGetterMethod);
            }

            pushBoxPrimitiveIfNecessary(propertyGetterMethod.getReturnType(), adapter);
            adapter.push(visitData.requiredValue);
            adapter.push(visitData.notEqualsValue);
            this.pushInvokeMethodOnSuperClass(adapter, CHECK_INJECTED_BEAN_PROPERTY_VALUE);
            if (isLastProperty) {
               adapter.returnValue();
            }
         }
      }

   }

   private void processAllBeanElementVisitors() {
      for(BeanElementVisitor<?> visitor : BeanElementVisitor.VISITORS) {
         if (visitor.isEnabled() && visitor.supports(this)) {
            try {
               this.disabled = visitor.visitBeanElement(this, this.visitorContext) == null;
               if (this.disabled) {
                  break;
               }
            } catch (Exception var4) {
               this.visitorContext
                  .fail("Error occurred visiting BeanElementVisitor of type [" + visitor.getClass().getName() + "]: " + var4.getMessage(), this);
               break;
            }
         }
      }

   }

   private void addInnerConfigurationMethod(GeneratorAdapter staticInit) {
      if (this.isConfigurationProperties && this.beanTypeInnerClasses.size() > 0) {
         this.classWriter.visitField(26, "$INNER_CONFIGURATION_CLASSES", Type.getType(Set.class).getDescriptor(), null, null);
         this.pushStoreClassesAsSet(staticInit, (String[])this.beanTypeInnerClasses.toArray(new String[0]));
         staticInit.putStatic(this.beanDefinitionType, "$INNER_CONFIGURATION_CLASSES", Type.getType(Set.class));
         GeneratorAdapter isInnerConfigurationMethod = this.startProtectedMethod(
            this.classWriter, "isInnerConfiguration", Boolean.TYPE.getName(), new String[]{Class.class.getName()}
         );
         isInnerConfigurationMethod.getStatic(this.beanDefinitionType, "$INNER_CONFIGURATION_CLASSES", Type.getType(Set.class));
         isInnerConfigurationMethod.loadArg(0);
         isInnerConfigurationMethod.invokeInterface(
            Type.getType(Collection.class),
            io.micronaut.asm.commons.Method.getMethod(ReflectionUtils.getRequiredMethod(Collection.class, "contains", Object.class))
         );
         isInnerConfigurationMethod.returnValue();
         isInnerConfigurationMethod.visitMaxs(1, 1);
         isInnerConfigurationMethod.visitEnd();
      }

   }

   private void addGetExposedTypes(GeneratorAdapter staticInit) {
      if (this.annotationMetadata.hasDeclaredAnnotation(Bean.class.getName())) {
         String[] exposedTypes = this.annotationMetadata.stringValues(Bean.class.getName(), "typed");
         if (exposedTypes.length > 0) {
            this.classWriter.visitField(26, "$EXPOSED_TYPES", Type.getType(Set.class).getDescriptor(), null, null);
            this.pushStoreClassesAsSet(staticInit, exposedTypes);
            staticInit.putStatic(this.beanDefinitionType, "$EXPOSED_TYPES", Type.getType(Set.class));
            GeneratorAdapter getExposedTypesMethod = this.startPublicMethod(this.classWriter, "getExposedTypes", Set.class.getName(), new String[0]);
            getExposedTypesMethod.getStatic(this.beanDefinitionType, "$EXPOSED_TYPES", Type.getType(Set.class));
            getExposedTypesMethod.returnValue();
            getExposedTypesMethod.visitMaxs(1, 1);
            getExposedTypesMethod.visitEnd();
         }
      }

   }

   private void pushStoreClassesAsSet(GeneratorAdapter writer, String[] classes) {
      if (classes.length > 1) {
         writer.newInstance(Type.getType(HashSet.class));
         writer.dup();
         this.pushArrayOfClasses(writer, classes);
         writer.invokeStatic(
            Type.getType(Arrays.class), io.micronaut.asm.commons.Method.getMethod(ReflectionUtils.getRequiredMethod(Arrays.class, "asList", Object[].class))
         );
         writer.invokeConstructor(
            Type.getType(HashSet.class),
            io.micronaut.asm.commons.Method.getMethod((Constructor<?>)ReflectionUtils.findConstructor(HashSet.class, Collection.class).get())
         );
      } else {
         this.pushClass(writer, classes[0]);
         writer.invokeStatic(
            Type.getType(Collections.class),
            io.micronaut.asm.commons.Method.getMethod(ReflectionUtils.getRequiredMethod(Collections.class, "singleton", Object.class))
         );
      }

   }

   private boolean hasTypeArguments() {
      return this.typeArguments != null
         && !this.typeArguments.isEmpty()
         && this.typeArguments.entrySet().stream().anyMatch(e -> !((Map)e.getValue()).isEmpty());
   }

   private boolean isSingleton(String scope) {
      if (this.beanProducingElement instanceof FieldElement && this.beanProducingElement.isFinal()) {
         return true;
      } else if (scope == null) {
         AnnotationMetadata annotationMetadata;
         if (this.beanProducingElement instanceof ClassElement) {
            annotationMetadata = this.getAnnotationMetadata();
         } else {
            annotationMetadata = this.beanProducingElement.getDeclaredMetadata();
         }

         return annotationMetadata.stringValue(DefaultScope.class)
            .map(t -> t.equals(Singleton.class.getName()) || t.equals("javax.inject.Singleton"))
            .orElse(false);
      } else {
         return scope.equals(Singleton.class.getName()) || scope.equals("javax.inject.Singleton");
      }
   }

   private void lookupReferenceAnnotationMetadata(GeneratorAdapter annotationMetadataMethod) {
      annotationMetadataMethod.loadThis();
      annotationMetadataMethod.getStatic(
         getTypeReferenceForName(this.getBeanDefinitionReferenceClassName(), new String[0]), "$ANNOTATION_METADATA", Type.getType(AnnotationMetadata.class)
      );
      annotationMetadataMethod.returnValue();
      annotationMetadataMethod.visitMaxs(1, 1);
      annotationMetadataMethod.visitEnd();
   }

   public byte[] toByteArray() {
      if (!this.beanFinalized) {
         throw new IllegalStateException("Bean definition not finalized. Call visitBeanDefinitionEnd() first.");
      } else {
         return this.classWriter.toByteArray();
      }
   }

   @Override
   public void accept(ClassWriterOutputVisitor visitor) throws IOException {
      if (!this.disabled) {
         OutputStream out = visitor.visitClass(this.getBeanDefinitionName(), this.getOriginatingElements());
         Throwable var3 = null;

         try {
            if (!this.innerClasses.isEmpty()) {
               for(Entry<String, ClassWriter> entry : this.innerClasses.entrySet()) {
                  OutputStream constructorOut = visitor.visitClass((String)entry.getKey(), this.getOriginatingElements());
                  Throwable var7 = null;

                  try {
                     constructorOut.write(((ClassWriter)entry.getValue()).toByteArray());
                  } catch (Throwable var32) {
                     var7 = var32;
                     throw var32;
                  } finally {
                     if (constructorOut != null) {
                        if (var7 != null) {
                           try {
                              constructorOut.close();
                           } catch (Throwable var31) {
                              var7.addSuppressed(var31);
                           }
                        } else {
                           constructorOut.close();
                        }
                     }

                  }
               }
            }

            try {
               if (this.executableMethodsDefinitionWriter != null) {
                  this.executableMethodsDefinitionWriter.accept(visitor);
               }
            } catch (RuntimeException var34) {
               Throwable cause = var34.getCause();
               if (cause instanceof IOException) {
                  throw (IOException)cause;
               }

               throw var34;
            }

            out.write(this.toByteArray());
         } catch (Throwable var35) {
            var3 = var35;
            throw var35;
         } finally {
            if (out != null) {
               if (var3 != null) {
                  try {
                     out.close();
                  } catch (Throwable var30) {
                     var3.addSuppressed(var30);
                  }
               } else {
                  out.close();
               }
            }

         }

      }
   }

   @Override
   public void visitSetterValue(TypedElement declaringType, MethodElement methodElement, boolean requiresReflection, boolean isOptional) {
      if (!requiresReflection) {
         ParameterElement parameter = methodElement.getParameters()[0];
         AnnotationMetadataHierarchy annotationMetadata = new AnnotationMetadataHierarchy(
            parameter.getAnnotationMetadata(), methodElement.getAnnotationMetadata()
         );
         Label falseCondition = isOptional
            ? this.pushPropertyContainsCheck(this.injectMethodVisitor, parameter.getType(), parameter.getName(), annotationMetadata)
            : null;
         AnnotationMetadata currentAnnotationMetadata = methodElement.getAnnotationMetadata();
         ClassElement genericType = parameter.getGenericType();
         if (this.isConfigurationProperties && this.isValueType(currentAnnotationMetadata)) {
            this.injectMethodVisitor.loadLocal(this.injectInstanceLocalVarIndex, this.beanType);
            Optional<String> property = currentAnnotationMetadata.stringValue(Property.class, "name");
            Optional<String> valueValue = parameter.stringValue(Value.class);
            if (this.isInnerType(genericType)) {
               boolean isArray = genericType.isArray();
               boolean isCollection = genericType.isAssignable(Collection.class);
               if (!isCollection && !isArray) {
                  this.pushInvokeGetBeanForSetter(this.injectMethodVisitor, methodElement.getName(), parameter);
               } else {
                  ClassElement typeArgument = genericType.isArray() ? genericType.fromArray() : (ClassElement)genericType.getFirstTypeArgument().orElse(null);
                  if (typeArgument != null && !typeArgument.isPrimitive()) {
                     this.pushInvokeGetBeansOfTypeForSetter(this.injectMethodVisitor, methodElement.getName(), parameter);
                  } else {
                     this.pushInvokeGetBeanForSetter(this.injectMethodVisitor, methodElement.getName(), parameter);
                  }
               }
            } else if (property.isPresent()) {
               this.pushInvokeGetPropertyValueForSetter(this.injectMethodVisitor, methodElement.getName(), parameter, (String)property.get());
            } else {
               if (!valueValue.isPresent()) {
                  throw new IllegalStateException();
               }

               this.pushInvokeGetPropertyPlaceholderValueForSetter(this.injectMethodVisitor, methodElement.getName(), parameter, (String)valueValue.get());
            }

            Type declaringTypeRef = JavaModelUtils.getTypeReference(declaringType);
            String methodDescriptor = getMethodDescriptor(methodElement.getReturnType(), Arrays.asList(methodElement.getParameters()));
            this.injectMethodVisitor
               .visitMethodInsn(this.isInterface ? 185 : 182, declaringTypeRef.getInternalName(), methodElement.getName(), methodDescriptor, this.isInterface);
            if (methodElement.getReturnType() != PrimitiveElement.VOID) {
               this.injectMethodVisitor.pop();
            }

            if (this.keepConfPropInjectPoints) {
               BeanDefinitionWriter.MethodVisitData methodVisitData = new BeanDefinitionWriter.MethodVisitData(declaringType, methodElement, false);
               this.methodInjectionPoints.add(methodVisitData);
               this.allMethodVisits.add(methodVisitData);
               ++this.currentMethodIndex;
            }
         } else {
            BeanDefinitionWriter.MethodVisitData methodVisitData = new BeanDefinitionWriter.MethodVisitData(declaringType, methodElement, false);
            this.visitMethodInjectionPointInternal(methodVisitData, this.injectMethodVisitor, this.injectInstanceLocalVarIndex);
            this.methodInjectionPoints.add(methodVisitData);
            this.allMethodVisits.add(methodVisitData);
            ++this.currentMethodIndex;
         }

         if (falseCondition != null) {
            this.injectMethodVisitor.visitLabel(falseCondition);
         }
      } else {
         BeanDefinitionWriter.MethodVisitData methodVisitData = new BeanDefinitionWriter.MethodVisitData(declaringType, methodElement, false);
         this.methodInjectionPoints.add(methodVisitData);
         this.allMethodVisits.add(methodVisitData);
         ++this.currentMethodIndex;
      }

   }

   @Override
   public void visitPostConstructMethod(TypedElement declaringType, MethodElement methodElement, boolean requiresReflection, VisitorContext visitorContext) {
      this.visitPostConstructMethodDefinition(false);
      if (!this.superBeanDefinition || this.isInterceptedLifeCycleByType(this.annotationMetadata, "POST_CONSTRUCT")) {
         BeanDefinitionWriter.MethodVisitData methodVisitData = new BeanDefinitionWriter.MethodVisitData(
            declaringType, methodElement, requiresReflection, true, false
         );
         this.postConstructMethodVisits.add(methodVisitData);
         this.allMethodVisits.add(methodVisitData);
         this.visitMethodInjectionPointInternal(methodVisitData, this.postConstructMethodVisitor, this.postConstructInstanceLocalVarIndex);
         ++this.currentMethodIndex;
      }

   }

   @Override
   public void visitPreDestroyMethod(TypedElement declaringType, MethodElement methodElement, boolean requiresReflection, VisitorContext visitorContext) {
      if (!this.superBeanDefinition || this.isInterceptedLifeCycleByType(this.annotationMetadata, "PRE_DESTROY")) {
         this.visitPreDestroyMethodDefinition(false);
         BeanDefinitionWriter.MethodVisitData methodVisitData = new BeanDefinitionWriter.MethodVisitData(
            declaringType, methodElement, requiresReflection, false, true
         );
         this.preDestroyMethodVisits.add(methodVisitData);
         this.allMethodVisits.add(methodVisitData);
         this.visitMethodInjectionPointInternal(methodVisitData, this.preDestroyMethodVisitor, this.preDestroyInstanceLocalVarIndex);
         ++this.currentMethodIndex;
      }

   }

   @Override
   public void visitMethodInjectionPoint(TypedElement declaringType, MethodElement methodElement, boolean requiresReflection, VisitorContext visitorContext) {
      this.applyConfigurationInjectionIfNecessary(methodElement);
      BeanDefinitionWriter.MethodVisitData methodVisitData = new BeanDefinitionWriter.MethodVisitData(declaringType, methodElement, requiresReflection);
      this.methodInjectionPoints.add(methodVisitData);
      this.allMethodVisits.add(methodVisitData);
      this.visitMethodInjectionPointInternal(methodVisitData, this.injectMethodVisitor, this.injectInstanceLocalVarIndex);
      ++this.currentMethodIndex;
   }

   @Override
   public int visitExecutableMethod(TypedElement declaringBean, MethodElement methodElement, VisitorContext visitorContext) {
      return this.visitExecutableMethod(declaringBean, methodElement, null, null);
   }

   public int visitExecutableMethod(
      TypedElement declaringType, MethodElement methodElement, String interceptedProxyClassName, String interceptedProxyBridgeMethodName
   ) {
      AnnotationMetadata annotationMetadata = methodElement.getAnnotationMetadata();
      DefaultAnnotationMetadata.contributeDefaults(this.annotationMetadata, annotationMetadata);

      for(ParameterElement parameterElement : methodElement.getSuspendParameters()) {
         DefaultAnnotationMetadata.contributeDefaults(this.annotationMetadata, parameterElement.getAnnotationMetadata());
         DefaultAnnotationMetadata.contributeRepeatable(this.annotationMetadata, parameterElement.getGenericType());
      }

      if (this.executableMethodsDefinitionWriter == null) {
         this.executableMethodsDefinitionWriter = new ExecutableMethodsDefinitionWriter(
            this.beanDefinitionName, this.getBeanDefinitionReferenceClassName(), this.originatingElements
         );
      }

      return this.executableMethodsDefinitionWriter
         .visitExecutableMethod(declaringType, methodElement, interceptedProxyClassName, interceptedProxyBridgeMethodName);
   }

   public String toString() {
      return "BeanDefinitionWriter{beanFullClassName='" + this.beanFullClassName + '\'' + '}';
   }

   @Override
   public String getPackageName() {
      return this.packageName;
   }

   @Override
   public String getBeanSimpleName() {
      return this.beanSimpleClassName;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Override
   public void visitConfigBuilderField(
      ClassElement type, String field, AnnotationMetadata annotationMetadata, ConfigurationMetadataBuilder metadataBuilder, boolean isInterface
   ) {
      String factoryMethod = (String)annotationMetadata.getValue(ConfigurationBuilder.class, "factoryMethod", String.class).orElse(null);
      if (StringUtils.isNotEmpty(factoryMethod)) {
         Type builderType = JavaModelUtils.getTypeReference(type);
         this.injectMethodVisitor.loadLocal(this.injectInstanceLocalVarIndex, this.beanType);
         this.injectMethodVisitor.invokeStatic(builderType, io.micronaut.asm.commons.Method.getMethod(builderType.getClassName() + " " + factoryMethod + "()"));
         this.injectMethodVisitor.putField(this.beanType, field, builderType);
      }

      this.currentConfigBuilderState = new ConfigBuilderState(type, field, false, annotationMetadata, metadataBuilder, isInterface);
   }

   @Override
   public void visitConfigBuilderMethod(
      ClassElement type, String methodName, AnnotationMetadata annotationMetadata, ConfigurationMetadataBuilder metadataBuilder, boolean isInterface
   ) {
      String factoryMethod = (String)annotationMetadata.getValue(ConfigurationBuilder.class, "factoryMethod", String.class).orElse(null);
      if (StringUtils.isNotEmpty(factoryMethod)) {
         Type builderType = JavaModelUtils.getTypeReference(type);
         this.injectMethodVisitor.loadLocal(this.injectInstanceLocalVarIndex, this.beanType);
         this.injectMethodVisitor.invokeStatic(builderType, io.micronaut.asm.commons.Method.getMethod(builderType.getClassName() + " " + factoryMethod + "()"));
         String propertyName = NameUtils.getPropertyNameForGetter(methodName);
         String setterName = NameUtils.setterNameFor(propertyName);
         this.injectMethodVisitor
            .invokeVirtual(this.beanType, io.micronaut.asm.commons.Method.getMethod("void " + setterName + "(" + builderType.getClassName() + ")"));
      }

      this.currentConfigBuilderState = new ConfigBuilderState(type, methodName, true, annotationMetadata, metadataBuilder, isInterface);
   }

   @Override
   public void visitConfigBuilderDurationMethod(String prefix, ClassElement returnType, String methodName, String path) {
      this.visitConfigBuilderMethodInternal(prefix, returnType, methodName, ClassElement.of(Duration.class), Collections.emptyMap(), true, path);
   }

   @Override
   public void visitConfigBuilderMethod(
      String prefix, ClassElement returnType, String methodName, ClassElement paramType, Map<String, ClassElement> generics, String path
   ) {
      this.visitConfigBuilderMethodInternal(prefix, returnType, methodName, paramType, generics, false, path);
   }

   @Override
   public void visitConfigBuilderEnd() {
      this.currentConfigBuilderState = null;
   }

   @Override
   public void setRequiresMethodProcessing(boolean shouldPreProcess) {
      this.preprocessMethods = shouldPreProcess;
   }

   @Override
   public void visitTypeArguments(Map<String, Map<String, ClassElement>> typeArguments) {
      this.typeArguments = typeArguments;
   }

   @Override
   public boolean requiresMethodProcessing() {
      return this.preprocessMethods;
   }

   @Override
   public void visitFieldInjectionPoint(TypedElement declaringType, FieldElement fieldElement, boolean requiresReflection) {
      this.visitFieldInjectionPointInternal(declaringType, fieldElement, requiresReflection);
   }

   private void visitFieldInjectionPointInternal(TypedElement declaringType, FieldElement fieldElement, boolean requiresReflection) {
      boolean requiresGenericType = false;
      ClassElement genericType = fieldElement.getGenericType();
      boolean isArray = genericType.isArray();
      boolean isCollection = genericType.isAssignable(Collection.class);
      Method methodToInvoke;
      if (isCollection || isArray) {
         requiresGenericType = true;
         ClassElement typeArgument = genericType.isArray() ? genericType.fromArray() : (ClassElement)genericType.getFirstTypeArgument().orElse(null);
         if (typeArgument == null || typeArgument.isPrimitive()) {
            requiresGenericType = false;
            methodToInvoke = GET_BEAN_FOR_FIELD;
         } else if (typeArgument.isAssignable(BeanRegistration.class)) {
            methodToInvoke = GET_BEAN_REGISTRATIONS_FOR_FIELD;
         } else {
            methodToInvoke = GET_BEANS_OF_TYPE_FOR_FIELD;
         }
      } else if (genericType.isAssignable(Stream.class)) {
         requiresGenericType = true;
         methodToInvoke = GET_STREAM_OF_TYPE_FOR_FIELD;
      } else if (genericType.isAssignable(Optional.class)) {
         requiresGenericType = true;
         methodToInvoke = FIND_BEAN_FOR_FIELD;
      } else if (genericType.isAssignable(BeanRegistration.class)) {
         requiresGenericType = true;
         methodToInvoke = GET_BEAN_REGISTRATION_FOR_FIELD;
      } else {
         methodToInvoke = GET_BEAN_FOR_FIELD;
      }

      this.visitFieldInjectionPointInternal(declaringType, fieldElement, requiresReflection, methodToInvoke, isArray, requiresGenericType);
   }

   private boolean isInnerType(ClassElement genericType) {
      String type;
      if (genericType.isAssignable(Collection.class)) {
         type = (String)genericType.getFirstTypeArgument().map(Element::getName).orElse("");
      } else if (genericType.isArray()) {
         type = genericType.fromArray().getName();
      } else {
         type = genericType.getName();
      }

      return this.beanTypeInnerClasses.contains(type);
   }

   @Override
   public void visitAnnotationMemberPropertyInjectionPoint(
      TypedElement annotationMemberBeanType, String annotationMemberProperty, @Nullable String requiredValue, @Nullable String notEqualsValue
   ) {
      ClassElement annotationMemberClassElement = annotationMemberBeanType.getType();
      MethodElement memberPropertyGetter = (MethodElement)annotationMemberClassElement.getBeanProperties()
         .stream()
         .filter(property -> property.getSimpleName().equals(annotationMemberProperty))
         .findFirst()
         .flatMap(PropertyElement::getReadMethod)
         .orElse(null);
      if (memberPropertyGetter == null) {
         String[] readPrefixes = (String[])annotationMemberBeanType.getAnnotationMetadata()
            .getValue(AccessorsStyle.class, "readPrefixes", String[].class)
            .orElse(new String[]{"get"});
         memberPropertyGetter = (MethodElement)annotationMemberClassElement.getEnclosedElement(
               ElementQuery.ALL_METHODS
                  .onlyAccessible(this.beanTypeElement)
                  .onlyInstance()
                  .named((Predicate<String>)(name -> annotationMemberProperty.equals(NameUtils.getPropertyNameForGetter(name, readPrefixes))))
                  .filter(e -> !e.hasParameters())
            )
            .orElse(null);
      }

      if (memberPropertyGetter == null) {
         this.visitorContext
            .fail(
               "Bean property [" + annotationMemberProperty + "] is not available on bean [" + annotationMemberBeanType.getName() + "]",
               annotationMemberBeanType
            );
      } else {
         Type injectedType = JavaModelUtils.getTypeReference(annotationMemberClassElement);
         ((List)this.annotationInjectionPoints.computeIfAbsent(injectedType, type -> new ArrayList(2)))
            .add(
               new BeanDefinitionWriter.AnnotationVisitData(
                  annotationMemberBeanType, annotationMemberProperty, memberPropertyGetter, requiredValue, notEqualsValue
               )
            );
      }

   }

   @Override
   public void visitFieldValue(TypedElement declaringType, FieldElement fieldElement, boolean requiresReflection, boolean isOptional) {
      Label falseCondition = isOptional
         ? this.pushPropertyContainsCheck(this.injectMethodVisitor, fieldElement.getType(), fieldElement.getName(), fieldElement.getAnnotationMetadata())
         : null;
      if (this.isInnerType(fieldElement.getGenericType())) {
         this.visitFieldInjectionPointInternal(declaringType, fieldElement, requiresReflection);
      } else if (this.isConfigurationProperties && !requiresReflection) {
         this.injectMethodVisitor.loadLocal(this.injectInstanceLocalVarIndex, this.beanType);
         Optional<String> property = fieldElement.stringValue(Property.class, "name");
         if (property.isPresent()) {
            this.pushInvokeGetPropertyValueForField(this.injectMethodVisitor, fieldElement, (String)property.get());
         } else {
            Optional<String> valueValue = fieldElement.stringValue(Value.class);
            if (valueValue.isPresent()) {
               this.pushInvokeGetPropertyPlaceholderValueForField(this.injectMethodVisitor, fieldElement, (String)valueValue.get());
            }
         }

         this.putField(this.injectMethodVisitor, fieldElement, requiresReflection, declaringType);
         if (this.keepConfPropInjectPoints) {
            this.fieldInjectionPoints.add(new BeanDefinitionWriter.FieldVisitData(declaringType, fieldElement, requiresReflection));
            ++this.currentFieldIndex;
         }
      } else {
         this.visitFieldInjectionPointInternal(declaringType, fieldElement, requiresReflection, GET_VALUE_FOR_FIELD, isOptional, false);
      }

      if (falseCondition != null) {
         this.injectMethodVisitor.visitLabel(falseCondition);
      }

   }

   private void pushInvokeGetPropertyValueForField(GeneratorAdapter injectMethodVisitor, FieldElement fieldElement, String value) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      MutableAnnotationMetadata mutableAnnotationMetadata = ((MutableAnnotationMetadata)fieldElement.getAnnotationMetadata()).clone();
      this.removeAnnotations(mutableAnnotationMetadata, PropertySource.class.getName(), Property.class.getName());
      if (this.keepConfPropInjectPoints) {
         this.resolveFieldArgument(injectMethodVisitor, this.currentFieldIndex);
      } else {
         pushCreateArgument(
            this.beanFullClassName,
            this.beanDefinitionType,
            this.classWriter,
            injectMethodVisitor,
            fieldElement.getName(),
            fieldElement.getGenericType(),
            mutableAnnotationMetadata,
            fieldElement.getGenericType().getTypeArguments(),
            new HashMap(),
            this.loadTypeMethods
         );
      }

      injectMethodVisitor.push(value);
      injectMethodVisitor.push(this.getCliPrefix(fieldElement.getName()));
      this.pushInvokeMethodOnSuperClass(injectMethodVisitor, GET_PROPERTY_VALUE_FOR_FIELD);
      pushCastToType(injectMethodVisitor, fieldElement.getType());
   }

   private void pushInvokeGetPropertyPlaceholderValueForField(GeneratorAdapter injectMethodVisitor, FieldElement fieldElement, String value) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      MutableAnnotationMetadata mutableAnnotationMetadata = ((MutableAnnotationMetadata)fieldElement.getAnnotationMetadata()).clone();
      this.removeAnnotations(mutableAnnotationMetadata, PropertySource.class.getName(), Property.class.getName());
      if (this.keepConfPropInjectPoints) {
         this.resolveFieldArgument(injectMethodVisitor, this.currentFieldIndex);
      } else {
         pushCreateArgument(
            this.beanFullClassName,
            this.beanDefinitionType,
            this.classWriter,
            injectMethodVisitor,
            fieldElement.getName(),
            fieldElement.getGenericType(),
            mutableAnnotationMetadata,
            fieldElement.getGenericType().getTypeArguments(),
            new HashMap(),
            this.loadTypeMethods
         );
      }

      injectMethodVisitor.push(value);
      this.pushInvokeMethodOnSuperClass(injectMethodVisitor, GET_PROPERTY_PLACEHOLDER_VALUE_FOR_FIELD);
      pushCastToType(injectMethodVisitor, fieldElement.getType());
   }

   private void visitConfigBuilderMethodInternal(
      String prefix,
      ClassElement returnType,
      String methodName,
      ClassElement paramType,
      Map<String, ClassElement> generics,
      boolean isDurationWithTimeUnit,
      String propertyPath
   ) {
      if (this.currentConfigBuilderState != null) {
         Type builderType = this.currentConfigBuilderState.getType();
         String builderName = this.currentConfigBuilderState.getName();
         boolean isResolveBuilderViaMethodCall = this.currentConfigBuilderState.isMethod();
         GeneratorAdapter injectMethodVisitor = this.injectMethodVisitor;
         String propertyName = NameUtils.hyphenate(NameUtils.decapitalize(methodName.substring(prefix.length())), true);
         boolean zeroArgs = paramType == null;
         int optionalLocalIndex = this.pushGetValueForPathCall(injectMethodVisitor, paramType, propertyName, propertyPath, zeroArgs, generics);
         Label ifEnd = new Label();
         injectMethodVisitor.invokeVirtual(
            Type.getType(Optional.class), io.micronaut.asm.commons.Method.getMethod(ReflectionUtils.getRequiredMethod(Optional.class, "isPresent"))
         );
         injectMethodVisitor.push(false);
         injectMethodVisitor.ifCmp(Type.BOOLEAN_TYPE, 153, ifEnd);
         if (zeroArgs) {
            this.pushOptionalGet(injectMethodVisitor, optionalLocalIndex);
            pushCastToType(injectMethodVisitor, Boolean.TYPE);
            injectMethodVisitor.push(false);
            injectMethodVisitor.ifCmp(Type.BOOLEAN_TYPE, 153, ifEnd);
         }

         injectMethodVisitor.visitLabel(new Label());
         String methodDescriptor;
         if (zeroArgs) {
            methodDescriptor = getMethodDescriptor(returnType, Collections.emptyList());
         } else if (isDurationWithTimeUnit) {
            methodDescriptor = getMethodDescriptor(returnType, Arrays.asList(ClassElement.of(Long.TYPE), ClassElement.of(TimeUnit.class)));
         } else {
            methodDescriptor = getMethodDescriptor(returnType, Collections.singleton(paramType));
         }

         Label tryStart = new Label();
         Label tryEnd = new Label();
         Label exceptionHandler = new Label();
         injectMethodVisitor.visitTryCatchBlock(tryStart, tryEnd, exceptionHandler, Type.getInternalName(NoSuchMethodError.class));
         injectMethodVisitor.visitLabel(tryStart);
         injectMethodVisitor.loadLocal(this.injectInstanceLocalVarIndex);
         if (isResolveBuilderViaMethodCall) {
            String desc = builderType.getClassName() + " " + builderName + "()";
            injectMethodVisitor.invokeVirtual(this.beanType, io.micronaut.asm.commons.Method.getMethod(desc));
         } else {
            injectMethodVisitor.getField(this.beanType, builderName, builderType);
         }

         if (!zeroArgs) {
            this.pushOptionalGet(injectMethodVisitor, optionalLocalIndex);
            pushCastToType(injectMethodVisitor, paramType);
         }

         boolean anInterface = this.currentConfigBuilderState.isInterface();
         if (isDurationWithTimeUnit) {
            injectMethodVisitor.invokeVirtual(
               Type.getType(Duration.class), io.micronaut.asm.commons.Method.getMethod(ReflectionUtils.getRequiredMethod(Duration.class, "toMillis"))
            );
            Type tu = Type.getType(TimeUnit.class);
            injectMethodVisitor.getStatic(tu, "MILLISECONDS", tu);
         }

         if (anInterface) {
            injectMethodVisitor.invokeInterface(builderType, new io.micronaut.asm.commons.Method(methodName, methodDescriptor));
         } else {
            injectMethodVisitor.invokeVirtual(builderType, new io.micronaut.asm.commons.Method(methodName, methodDescriptor));
         }

         if (returnType != PrimitiveElement.VOID) {
            injectMethodVisitor.pop();
         }

         injectMethodVisitor.visitJumpInsn(167, tryEnd);
         injectMethodVisitor.visitLabel(exceptionHandler);
         injectMethodVisitor.pop();
         injectMethodVisitor.visitLabel(tryEnd);
         injectMethodVisitor.visitLabel(ifEnd);
      }

   }

   private void pushOptionalGet(GeneratorAdapter injectMethodVisitor, int optionalLocalIndex) {
      injectMethodVisitor.loadLocal(optionalLocalIndex);
      injectMethodVisitor.invokeVirtual(
         Type.getType(Optional.class), io.micronaut.asm.commons.Method.getMethod(ReflectionUtils.getRequiredMethod(Optional.class, "get"))
      );
   }

   private int pushGetValueForPathCall(
      GeneratorAdapter injectMethodVisitor,
      ClassElement propertyType,
      String propertyName,
      String propertyPath,
      boolean zeroArgs,
      Map<String, ClassElement> generics
   ) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      if (zeroArgs) {
         buildArgument(injectMethodVisitor, propertyName, Type.getType(Boolean.class));
      } else {
         buildArgumentWithGenerics(
            this.beanDefinitionType,
            this.classWriter,
            injectMethodVisitor,
            propertyName,
            JavaModelUtils.getTypeReference(propertyType),
            propertyType,
            generics,
            new HashSet(),
            new HashMap(),
            this.loadTypeMethods
         );
      }

      injectMethodVisitor.push(propertyPath);
      injectMethodVisitor.invokeVirtual(this.beanDefinitionType, io.micronaut.asm.commons.Method.getMethod(GET_VALUE_FOR_PATH));
      int optionalInstanceIndex = injectMethodVisitor.newLocal(Type.getType(Optional.class));
      injectMethodVisitor.storeLocal(optionalInstanceIndex);
      injectMethodVisitor.loadLocal(optionalInstanceIndex);
      return optionalInstanceIndex;
   }

   private void visitFieldInjectionPointInternal(
      TypedElement declaringType, FieldElement fieldElement, boolean requiresReflection, Method methodToInvoke, boolean isArray, boolean requiresGenericType
   ) {
      AnnotationMetadata annotationMetadata = fieldElement.getAnnotationMetadata();
      this.autoApplyNamedIfPresent(fieldElement, annotationMetadata);
      DefaultAnnotationMetadata.contributeDefaults(this.annotationMetadata, annotationMetadata);
      DefaultAnnotationMetadata.contributeRepeatable(this.annotationMetadata, fieldElement.getGenericField());
      Type declaringTypeRef = JavaModelUtils.getTypeReference(declaringType);
      GeneratorAdapter injectMethodVisitor = this.injectMethodVisitor;
      injectMethodVisitor.loadLocal(this.injectInstanceLocalVarIndex, this.beanType);
      if (fieldElement.getGenericField().isAssignable(BeanContext.class)) {
         injectMethodVisitor.loadArg(1);
      } else {
         injectMethodVisitor.loadThis();
         injectMethodVisitor.loadArg(0);
         injectMethodVisitor.loadArg(1);
         injectMethodVisitor.push(this.currentFieldIndex);
         if (requiresGenericType) {
            this.resolveFieldArgumentGenericType(injectMethodVisitor, fieldElement.getGenericType(), this.currentFieldIndex);
         }

         this.pushQualifier(injectMethodVisitor, fieldElement, () -> this.resolveFieldArgument(injectMethodVisitor, this.currentFieldIndex));
         this.pushInvokeMethodOnSuperClass(injectMethodVisitor, methodToInvoke);
         if (isArray && requiresGenericType) {
            this.convertToArray(fieldElement.getType().fromArray(), injectMethodVisitor);
         }

         pushCastToType(injectMethodVisitor, fieldElement.getType());
      }

      this.putField(injectMethodVisitor, fieldElement, requiresReflection, declaringType);
      ++this.currentFieldIndex;
      this.fieldInjectionPoints.add(new BeanDefinitionWriter.FieldVisitData(declaringType, fieldElement, requiresReflection));
   }

   private void putField(GeneratorAdapter injectMethodVisitor, FieldElement fieldElement, boolean requiresReflection, TypedElement declaringType) {
      Type declaringTypeRef = JavaModelUtils.getTypeReference(declaringType);
      Type fieldType = JavaModelUtils.getTypeReference(fieldElement.getType());
      if (!requiresReflection) {
         injectMethodVisitor.putField(declaringTypeRef, fieldElement.getName(), fieldType);
      } else {
         pushBoxPrimitiveIfNecessary(fieldType, injectMethodVisitor);
         int storedIndex = injectMethodVisitor.newLocal(Type.getType(Object.class));
         injectMethodVisitor.storeLocal(storedIndex);
         injectMethodVisitor.loadThis();
         injectMethodVisitor.loadArg(0);
         injectMethodVisitor.loadArg(1);
         injectMethodVisitor.push(this.currentFieldIndex);
         injectMethodVisitor.loadLocal(this.injectInstanceLocalVarIndex);
         injectMethodVisitor.loadLocal(storedIndex);
         injectMethodVisitor.invokeVirtual(this.superType, SET_FIELD_WITH_REFLECTION_METHOD);
         injectMethodVisitor.pop();
      }

   }

   private Label pushPropertyContainsCheck(
      GeneratorAdapter injectMethodVisitor, ClassElement propertyType, String propertyName, AnnotationMetadata annotationMetadata
   ) {
      Optional<String> propertyValue = annotationMetadata.stringValue(Property.class, "name");
      Label trueCondition = new Label();
      Label falseCondition = new Label();
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      injectMethodVisitor.push((String)propertyValue.get());
      if (this.isMultiValueProperty(propertyType)) {
         injectMethodVisitor.invokeVirtual(this.beanDefinitionType, CONTAINS_PROPERTIES_VALUE_METHOD);
      } else {
         injectMethodVisitor.invokeVirtual(this.beanDefinitionType, CONTAINS_PROPERTY_VALUE_METHOD);
      }

      injectMethodVisitor.push(false);
      String cliProperty = this.getCliPrefix(propertyName);
      if (cliProperty != null) {
         injectMethodVisitor.ifCmp(Type.BOOLEAN_TYPE, 154, trueCondition);
         injectMethodVisitor.loadThis();
         injectMethodVisitor.loadArg(0);
         injectMethodVisitor.loadArg(1);
         injectMethodVisitor.push(cliProperty);
         injectMethodVisitor.invokeVirtual(this.beanDefinitionType, CONTAINS_PROPERTY_VALUE_METHOD);
         injectMethodVisitor.push(false);
      }

      injectMethodVisitor.ifCmp(Type.BOOLEAN_TYPE, 153, falseCondition);
      injectMethodVisitor.visitLabel(trueCondition);
      return falseCondition;
   }

   private String getCliPrefix(String propertyName) {
      return this.isConfigurationProperties && this.annotationMetadata.isPresent(ConfigurationProperties.class, "cliPrefix")
         ? (String)this.annotationMetadata.stringValue(ConfigurationProperties.class, "cliPrefix").map(val -> val + propertyName).orElse(null)
         : null;
   }

   private boolean isMultiValueProperty(ClassElement type) {
      return type.isAssignable(Map.class) || type.isAssignable(Collection.class) || this.isConfigurationProperties(type);
   }

   private void pushQualifier(GeneratorAdapter generatorAdapter, Element element, Runnable resolveArgument) {
      List<String> qualifierNames = element.getAnnotationNamesByStereotype("javax.inject.Qualifier");
      if (!qualifierNames.isEmpty()) {
         if (qualifierNames.size() == 1) {
            String annotationName = (String)qualifierNames.iterator().next();
            this.pushQualifierForAnnotation(generatorAdapter, element, annotationName, resolveArgument);
         } else {
            int len = qualifierNames.size();
            pushNewArray(generatorAdapter, TYPE_QUALIFIER, len);

            for(int i = 0; i < len; ++i) {
               String annotationName = (String)qualifierNames.get(i);
               pushStoreInArray(generatorAdapter, i, len, () -> this.pushQualifierForAnnotation(generatorAdapter, element, annotationName, resolveArgument));
            }

            generatorAdapter.invokeStatic(TYPE_QUALIFIERS, METHOD_QUALIFIER_BY_QUALIFIERS);
         }
      } else if (element.hasAnnotation("io.micronaut.inject.qualifiers.InterceptorBindingQualifier")) {
         resolveArgument.run();
         this.retrieveAnnotationMetadataFromProvider(generatorAdapter);
         generatorAdapter.invokeStatic(TYPE_QUALIFIERS, METHOD_QUALIFIER_BY_INTERCEPTOR_BINDING);
      } else {
         String[] byType = element.hasDeclaredAnnotation(io.micronaut.context.annotation.Type.NAME)
            ? element.stringValues(io.micronaut.context.annotation.Type.NAME)
            : null;
         if (byType != null && byType.length > 0) {
            this.pushArrayOfClasses(generatorAdapter, byType);
            generatorAdapter.invokeStatic(TYPE_QUALIFIERS, METHOD_QUALIFIER_BY_TYPE);
         } else {
            generatorAdapter.push((String)null);
         }
      }

   }

   private void retrieveAnnotationMetadataFromProvider(GeneratorAdapter generatorAdapter) {
      generatorAdapter.invokeInterface(
         Type.getType(AnnotationMetadataProvider.class),
         io.micronaut.asm.commons.Method.getMethod(ReflectionUtils.getRequiredMethod(AnnotationMetadataProvider.class, "getAnnotationMetadata"))
      );
   }

   private void pushQualifierForAnnotation(GeneratorAdapter generatorAdapter, Element element, String annotationName, Runnable resolveArgument) {
      if (annotationName.equals(Primary.NAME)) {
         generatorAdapter.visitInsn(1);
      } else if (annotationName.equals("javax.inject.Named")) {
         String n = (String)element.stringValue("javax.inject.Named").orElse(element.getName());
         if (!n.contains("$")) {
            generatorAdapter.push(n);
            generatorAdapter.invokeStatic(TYPE_QUALIFIERS, METHOD_QUALIFIER_BY_NAME);
         } else {
            this.doResolveArgument(generatorAdapter, resolveArgument);
         }
      } else if (annotationName.equals(Any.NAME)) {
         Type t = Type.getType(AnyQualifier.class);
         generatorAdapter.getStatic(t, "INSTANCE", t);
      } else {
         String repeatableName = (String)this.visitorContext.getClassElement(annotationName).flatMap(ce -> ce.stringValue(Repeatable.class)).orElse(null);
         resolveArgument.run();
         this.retrieveAnnotationMetadataFromProvider(generatorAdapter);
         if (repeatableName != null) {
            generatorAdapter.push(repeatableName);
            generatorAdapter.invokeStatic(TYPE_QUALIFIERS, METHOD_QUALIFIER_BY_REPEATABLE_ANNOTATION);
         } else {
            generatorAdapter.push(annotationName);
            generatorAdapter.invokeStatic(TYPE_QUALIFIERS, METHOD_QUALIFIER_BY_ANNOTATION);
         }
      }

   }

   private void doResolveArgument(GeneratorAdapter generatorAdapter, Runnable resolveArgument) {
      resolveArgument.run();
      generatorAdapter.invokeStatic(TYPE_QUALIFIERS, METHOD_QUALIFIER_FOR_ARGUMENT);
   }

   private void pushArrayOfClasses(GeneratorAdapter writer, String[] byType) {
      int len = byType.length;
      pushNewArray(writer, Class.class, len);

      for(int i = 0; i < len; ++i) {
         String type = byType[i];
         pushStoreInArray(writer, i, len, () -> this.pushClass(writer, type));
      }

   }

   private void pushClass(GeneratorAdapter writer, String className) {
      writer.push(Type.getObjectType(className.replace('.', '/')));
   }

   private void convertToArray(ClassElement arrayType, GeneratorAdapter injectMethodVisitor) {
      injectMethodVisitor.push(0);
      injectMethodVisitor.newArray(JavaModelUtils.getTypeReference(arrayType));
      injectMethodVisitor.invokeInterface(Type.getType(Collection.class), COLLECTION_TO_ARRAY);
   }

   private void autoApplyNamedIfPresent(Element element, AnnotationMetadata annotationMetadata) {
      if (annotationMetadata.hasAnnotation("javax.inject.Named") || annotationMetadata.hasStereotype("javax.inject.Named")) {
         this.autoApplyNamed(element);
      }

   }

   private void autoApplyNamed(Element element) {
      if (!element.stringValue("javax.inject.Named").isPresent()) {
         element.annotate("javax.inject.Named", builder -> {
            String name;
            if (element instanceof ClassElement) {
               name = NameUtils.decapitalize(element.getSimpleName());
            } else if (element instanceof MethodElement) {
               String n = element.getName();
               if (NameUtils.isGetterName(n)) {
                  name = NameUtils.getPropertyNameForGetter(n);
               } else {
                  name = n;
               }
            } else {
               name = element.getName();
            }

            builder.value(name);
         });
      }

   }

   private void visitMethodInjectionPointInternal(
      BeanDefinitionWriter.MethodVisitData methodVisitData, GeneratorAdapter injectMethodVisitor, int injectInstanceIndex
   ) {
      MethodElement methodElement = methodVisitData.getMethodElement();
      AnnotationMetadata annotationMetadata = methodElement.getAnnotationMetadata();
      List<ParameterElement> argumentTypes = Arrays.asList(methodElement.getParameters());
      this.applyDefaultNamedToParameters(argumentTypes);
      TypedElement declaringType = methodVisitData.beanType;
      String methodName = methodElement.getName();
      boolean requiresReflection = methodVisitData.requiresReflection;
      ClassElement returnType = methodElement.getReturnType();
      DefaultAnnotationMetadata.contributeDefaults(this.annotationMetadata, annotationMetadata);
      DefaultAnnotationMetadata.contributeRepeatable(this.annotationMetadata, returnType);
      boolean hasArguments = methodElement.hasParameters();
      int argCount = hasArguments ? argumentTypes.size() : 0;
      Type declaringTypeRef = JavaModelUtils.getTypeReference(declaringType);
      boolean hasInjectScope = false;

      for(ParameterElement value : argumentTypes) {
         DefaultAnnotationMetadata.contributeDefaults(this.annotationMetadata, value.getAnnotationMetadata());
         DefaultAnnotationMetadata.contributeRepeatable(this.annotationMetadata, value.getGenericType());
         if (value.hasDeclaredAnnotation(InjectScope.class)) {
            hasInjectScope = true;
         }
      }

      if (!requiresReflection) {
         injectMethodVisitor.loadLocal(injectInstanceIndex, this.beanType);
         String methodDescriptor;
         if (hasArguments) {
            methodDescriptor = getMethodDescriptor(returnType, argumentTypes);
            Iterator<ParameterElement> argIterator = argumentTypes.iterator();

            for(int i = 0; i < argCount; ++i) {
               ParameterElement entry = (ParameterElement)argIterator.next();
               this.pushMethodParameterValue(injectMethodVisitor, i, entry);
            }
         } else {
            methodDescriptor = getMethodDescriptor(returnType, Collections.emptyList());
         }

         injectMethodVisitor.visitMethodInsn(this.isInterface ? 185 : 182, declaringTypeRef.getInternalName(), methodName, methodDescriptor, this.isInterface);
         if (this.isConfigurationProperties && returnType != PrimitiveElement.VOID) {
            injectMethodVisitor.pop();
         }
      } else {
         injectMethodVisitor.loadThis();
         injectMethodVisitor.loadArg(0);
         injectMethodVisitor.loadArg(1);
         injectMethodVisitor.push(this.currentMethodIndex);
         injectMethodVisitor.loadLocal(this.injectInstanceLocalVarIndex, this.beanType);
         if (hasArguments) {
            pushNewArray(injectMethodVisitor, Object.class, argumentTypes.size());
            Iterator<ParameterElement> argIterator = argumentTypes.iterator();

            for(int i = 0; i < argCount; ++i) {
               pushStoreInArray(injectMethodVisitor, i, argumentTypes.size(), () -> {
                  ParameterElement entryx = (ParameterElement)argIterator.next();
                  this.pushMethodParameterValue(injectMethodVisitor, i, entryx);
                  pushBoxPrimitiveIfNecessary(entryx.getType(), injectMethodVisitor);
               });
            }
         } else {
            pushNewArray(injectMethodVisitor, Object.class, 0);
         }

         injectMethodVisitor.invokeVirtual(this.superType, INVOKE_WITH_REFLECTION_METHOD);
      }

      this.destroyInjectScopeBeansIfNecessary(injectMethodVisitor, hasInjectScope);
   }

   private void destroyInjectScopeBeansIfNecessary(GeneratorAdapter injectMethodVisitor, boolean hasInjectScope) {
      if (hasInjectScope) {
         injectMethodVisitor.loadArg(0);
         injectMethodVisitor.invokeInterface(
            Type.getType(BeanResolutionContext.class),
            io.micronaut.asm.commons.Method.getMethod(ReflectionUtils.getRequiredInternalMethod(BeanResolutionContext.class, "destroyInjectScopedBeans"))
         );
      }

   }

   private void pushMethodParameterValue(GeneratorAdapter injectMethodVisitor, int i, ParameterElement entry) {
      AnnotationMetadata argMetadata = entry.getAnnotationMetadata();
      if (entry.getGenericType().isAssignable(BeanResolutionContext.class)) {
         injectMethodVisitor.loadArg(0);
      } else if (entry.getGenericType().isAssignable(BeanContext.class)) {
         injectMethodVisitor.loadArg(1);
      } else {
         boolean requiresGenericType = false;
         ClassElement genericType = entry.getGenericType();
         boolean isCollection = genericType.isAssignable(Collection.class);
         boolean isArray = genericType.isArray();
         if (this.isValueType(argMetadata) && !this.isInnerType(entry.getGenericType())) {
            Optional<String> property = argMetadata.stringValue(Property.class, "name");
            if (property.isPresent()) {
               this.pushInvokeGetPropertyValueForMethod(injectMethodVisitor, i, entry, (String)property.get());
            } else {
               Optional<String> valueValue = entry.getAnnotationMetadata().stringValue(Value.class);
               if (valueValue.isPresent()) {
                  this.pushInvokeGetPropertyPlaceholderValueForMethod(injectMethodVisitor, i, entry, (String)valueValue.get());
               }
            }

            return;
         }

         Method methodToInvoke;
         if (isCollection || isArray) {
            requiresGenericType = true;
            ClassElement typeArgument = genericType.isArray() ? genericType.fromArray() : (ClassElement)genericType.getFirstTypeArgument().orElse(null);
            if (typeArgument == null || typeArgument.isPrimitive()) {
               methodToInvoke = GET_BEAN_FOR_METHOD_ARGUMENT;
               requiresGenericType = false;
            } else if (typeArgument.isAssignable(BeanRegistration.class)) {
               methodToInvoke = GET_BEAN_REGISTRATIONS_FOR_METHOD_ARGUMENT;
            } else {
               methodToInvoke = GET_BEANS_OF_TYPE_FOR_METHOD_ARGUMENT;
            }
         } else if (genericType.isAssignable(Stream.class)) {
            requiresGenericType = true;
            methodToInvoke = GET_STREAM_OF_TYPE_FOR_METHOD_ARGUMENT;
         } else if (genericType.isAssignable(Optional.class)) {
            requiresGenericType = true;
            methodToInvoke = FIND_BEAN_FOR_METHOD_ARGUMENT;
         } else if (genericType.isAssignable(BeanRegistration.class)) {
            requiresGenericType = true;
            methodToInvoke = GET_BEAN_REGISTRATION_FOR_METHOD_ARGUMENT;
         } else {
            methodToInvoke = GET_BEAN_FOR_METHOD_ARGUMENT;
         }

         injectMethodVisitor.loadThis();
         injectMethodVisitor.loadArg(0);
         injectMethodVisitor.loadArg(1);
         injectMethodVisitor.push(this.currentMethodIndex);
         injectMethodVisitor.push(i);
         if (requiresGenericType) {
            this.resolveMethodArgumentGenericType(injectMethodVisitor, genericType, this.currentMethodIndex, i);
         }

         this.pushQualifier(injectMethodVisitor, entry, () -> this.resolveMethodArgument(injectMethodVisitor, this.currentMethodIndex, i));
         this.pushInvokeMethodOnSuperClass(injectMethodVisitor, methodToInvoke);
         if (isArray && requiresGenericType) {
            this.convertToArray(genericType.fromArray(), injectMethodVisitor);
         }

         pushCastToType(injectMethodVisitor, entry);
      }

   }

   private void pushInvokeGetPropertyValueForMethod(GeneratorAdapter injectMethodVisitor, int i, ParameterElement entry, String value) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      injectMethodVisitor.push(this.currentMethodIndex);
      injectMethodVisitor.push(i);
      injectMethodVisitor.push(value);
      injectMethodVisitor.push(this.getCliPrefix(entry.getName()));
      this.pushInvokeMethodOnSuperClass(injectMethodVisitor, GET_PROPERTY_VALUE_FOR_METHOD_ARGUMENT);
      pushCastToType(injectMethodVisitor, entry);
   }

   private void pushInvokeGetPropertyPlaceholderValueForMethod(GeneratorAdapter injectMethodVisitor, int i, ParameterElement entry, String value) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      injectMethodVisitor.push(this.currentMethodIndex);
      injectMethodVisitor.push(i);
      injectMethodVisitor.push(value);
      this.pushInvokeMethodOnSuperClass(injectMethodVisitor, GET_PROPERTY_PLACEHOLDER_VALUE_FOR_METHOD_ARGUMENT);
      pushCastToType(injectMethodVisitor, entry);
   }

   private void pushInvokeGetPropertyValueForSetter(GeneratorAdapter injectMethodVisitor, String setterName, ParameterElement entry, String value) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      injectMethodVisitor.push(setterName);
      AnnotationMetadata annotationMetadata;
      if (entry.getAnnotationMetadata() instanceof MutableAnnotationMetadata) {
         annotationMetadata = ((MutableAnnotationMetadata)entry.getAnnotationMetadata()).clone();
         this.removeAnnotations(annotationMetadata, PropertySource.class.getName(), Property.class.getName());
      } else {
         annotationMetadata = entry.getAnnotationMetadata();
      }

      if (this.keepConfPropInjectPoints) {
         this.resolveMethodArgument(injectMethodVisitor, this.currentMethodIndex, 0);
      } else {
         pushCreateArgument(
            this.beanFullClassName,
            this.beanDefinitionType,
            this.classWriter,
            injectMethodVisitor,
            entry.getName(),
            entry.getGenericType(),
            annotationMetadata,
            entry.getGenericType().getTypeArguments(),
            new HashMap(),
            this.loadTypeMethods
         );
      }

      injectMethodVisitor.push(value);
      injectMethodVisitor.push(this.getCliPrefix(entry.getName()));
      this.pushInvokeMethodOnSuperClass(injectMethodVisitor, GET_PROPERTY_VALUE_FOR_SETTER);
      pushCastToType(injectMethodVisitor, entry);
   }

   private void pushInvokeGetBeanForSetter(GeneratorAdapter injectMethodVisitor, String setterName, ParameterElement entry) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      injectMethodVisitor.push(setterName);
      AnnotationMetadata annotationMetadata;
      if (entry.getAnnotationMetadata() instanceof MutableAnnotationMetadata) {
         annotationMetadata = ((MutableAnnotationMetadata)entry.getAnnotationMetadata()).clone();
         this.removeAnnotations(annotationMetadata, PropertySource.class.getName(), Property.class.getName());
      } else {
         annotationMetadata = entry.getAnnotationMetadata();
      }

      if (this.keepConfPropInjectPoints) {
         this.resolveMethodArgument(injectMethodVisitor, this.currentMethodIndex, 0);
      } else {
         pushCreateArgument(
            this.beanFullClassName,
            this.beanDefinitionType,
            this.classWriter,
            injectMethodVisitor,
            entry.getName(),
            entry.getGenericType(),
            annotationMetadata,
            entry.getGenericType().getTypeArguments(),
            new HashMap(),
            this.loadTypeMethods
         );
      }

      this.pushQualifier(injectMethodVisitor, entry.getGenericType(), injectMethodVisitor::dup);
      this.pushInvokeMethodOnSuperClass(injectMethodVisitor, GET_BEAN_FOR_SETTER);
      pushCastToType(injectMethodVisitor, entry);
   }

   private void pushInvokeGetBeansOfTypeForSetter(GeneratorAdapter injectMethodVisitor, String setterName, ParameterElement entry) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      injectMethodVisitor.push(setterName);
      AnnotationMetadata annotationMetadata;
      if (entry.getAnnotationMetadata() instanceof MutableAnnotationMetadata) {
         annotationMetadata = ((MutableAnnotationMetadata)entry.getAnnotationMetadata()).clone();
         this.removeAnnotations(annotationMetadata, PropertySource.class.getName(), Property.class.getName());
      } else {
         annotationMetadata = entry.getAnnotationMetadata();
      }

      ClassElement genericType = entry.getGenericType();
      if (this.keepConfPropInjectPoints) {
         this.resolveMethodArgument(injectMethodVisitor, this.currentMethodIndex, 0);
      } else {
         pushCreateArgument(
            this.beanFullClassName,
            this.beanDefinitionType,
            this.classWriter,
            injectMethodVisitor,
            entry.getName(),
            genericType,
            annotationMetadata,
            genericType.getTypeArguments(),
            new HashMap(),
            this.loadTypeMethods
         );
      }

      int thisArgument = injectMethodVisitor.newLocal(Type.getType(Argument.class));
      injectMethodVisitor.storeLocal(thisArgument);
      injectMethodVisitor.loadLocal(thisArgument);
      if (!this.resolveArgumentGenericType(injectMethodVisitor, genericType)) {
         injectMethodVisitor.loadLocal(thisArgument);
         this.resolveFirstTypeArgument(injectMethodVisitor);
         this.resolveInnerTypeArgumentIfNeeded(injectMethodVisitor, genericType);
      } else {
         injectMethodVisitor.push((String)null);
      }

      this.pushQualifier(injectMethodVisitor, genericType, () -> injectMethodVisitor.loadLocal(thisArgument));
      this.pushInvokeMethodOnSuperClass(injectMethodVisitor, GET_BEANS_OF_TYPE_FOR_SETTER);
      pushCastToType(injectMethodVisitor, entry);
   }

   private void pushInvokeGetPropertyPlaceholderValueForSetter(GeneratorAdapter injectMethodVisitor, String setterName, ParameterElement entry, String value) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      injectMethodVisitor.push(setterName);
      AnnotationMetadata annotationMetadata;
      if (entry.getAnnotationMetadata() instanceof MutableAnnotationMetadata) {
         annotationMetadata = ((MutableAnnotationMetadata)entry.getAnnotationMetadata()).clone();
         this.removeAnnotations(annotationMetadata, PropertySource.class.getName(), Property.class.getName());
      } else {
         annotationMetadata = entry.getAnnotationMetadata();
      }

      if (this.keepConfPropInjectPoints) {
         this.resolveMethodArgument(injectMethodVisitor, this.currentMethodIndex, 0);
      } else {
         pushCreateArgument(
            this.beanFullClassName,
            this.beanDefinitionType,
            this.classWriter,
            injectMethodVisitor,
            entry.getName(),
            entry.getGenericType(),
            annotationMetadata,
            entry.getGenericType().getTypeArguments(),
            new HashMap(),
            this.loadTypeMethods
         );
      }

      injectMethodVisitor.push(value);
      injectMethodVisitor.push(this.getCliPrefix(entry.getName()));
      this.pushInvokeMethodOnSuperClass(injectMethodVisitor, GET_PROPERTY_PLACEHOLDER_VALUE_FOR_SETTER);
      pushCastToType(injectMethodVisitor, entry);
   }

   private void removeAnnotations(AnnotationMetadata annotationMetadata, String... annotationNames) {
      if (annotationMetadata instanceof MutableAnnotationMetadata) {
         MutableAnnotationMetadata mutableAnnotationMetadata = (MutableAnnotationMetadata)annotationMetadata;

         for(String annotation : annotationNames) {
            mutableAnnotationMetadata.removeAnnotation(annotation);
         }
      }

   }

   private void applyDefaultNamedToParameters(List<ParameterElement> argumentTypes) {
      for(ParameterElement parameterElement : argumentTypes) {
         AnnotationMetadata annotationMetadata = parameterElement.getAnnotationMetadata();
         DefaultAnnotationMetadata.contributeDefaults(this.annotationMetadata, annotationMetadata);
         DefaultAnnotationMetadata.contributeRepeatable(this.annotationMetadata, parameterElement.getGenericType());
         this.autoApplyNamedIfPresent(parameterElement, annotationMetadata);
      }

   }

   private void pushInvokeMethodOnSuperClass(MethodVisitor constructorVisitor, Method methodToInvoke) {
      constructorVisitor.visitMethodInsn(
         183,
         this.isSuperFactory ? TYPE_ABSTRACT_BEAN_DEFINITION.getInternalName() : this.superType.getInternalName(),
         methodToInvoke.getName(),
         Type.getMethodDescriptor(methodToInvoke),
         false
      );
   }

   private void visitCheckIfShouldLoadMethodDefinition() {
      String desc = getMethodDescriptor("void", new String[]{BeanResolutionContext.class.getName(), BeanContext.class.getName()});
      this.checkIfShouldLoadMethodVisitor = new GeneratorAdapter(
         this.classWriter.visitMethod(4, "checkIfShouldLoad", desc, null, null), 4, "checkIfShouldLoad", desc
      );
   }

   private void visitInjectMethodDefinition() {
      if (!this.isPrimitiveBean && !this.superBeanDefinition && this.injectMethodVisitor == null) {
         String desc = getMethodDescriptor(
            Object.class.getName(), new String[]{BeanResolutionContext.class.getName(), BeanContext.class.getName(), Object.class.getName()}
         );
         this.injectMethodVisitor = new GeneratorAdapter(this.classWriter.visitMethod(4, "injectBean", desc, null, null), 4, "injectBean", desc);
         GeneratorAdapter injectMethodVisitor = this.injectMethodVisitor;
         if (this.isConfigurationProperties) {
            injectMethodVisitor.loadThis();
            injectMethodVisitor.loadArg(0);
            injectMethodVisitor.loadArg(1);
            injectMethodVisitor.invokeVirtual(this.beanDefinitionType, io.micronaut.asm.commons.Method.getMethod(CONTAINS_PROPERTIES_METHOD));
            injectMethodVisitor.push(false);
            this.injectEnd = new Label();
            injectMethodVisitor.ifCmp(Type.BOOLEAN_TYPE, 153, this.injectEnd);
            injectMethodVisitor.visitLabel(new Label());
         }

         injectMethodVisitor.loadArg(2);
         pushCastToType(injectMethodVisitor, this.beanType);
         this.injectInstanceLocalVarIndex = injectMethodVisitor.newLocal(this.beanType);
         injectMethodVisitor.storeLocal(this.injectInstanceLocalVarIndex);
      }

   }

   private void visitPostConstructMethodDefinition(boolean intercepted) {
      if (!this.postConstructAdded) {
         String lifeCycleMethodName = "initialize";
         if (!this.superBeanDefinition || intercepted) {
            this.interfaceTypes.add(InitializingBeanDefinition.class);
            GeneratorAdapter postConstructMethodVisitor = this.newLifeCycleMethod("initialize");
            this.postConstructMethodVisitor = postConstructMethodVisitor;
            postConstructMethodVisitor.loadArg(2);
            pushCastToType(postConstructMethodVisitor, this.beanType);
            this.postConstructInstanceLocalVarIndex = postConstructMethodVisitor.newLocal(this.beanType);
            postConstructMethodVisitor.storeLocal(this.postConstructInstanceLocalVarIndex);
            this.invokeSuperInjectMethod(postConstructMethodVisitor, POST_CONSTRUCT_METHOD);
         }

         if (intercepted) {
            this.writeInterceptedLifecycleMethod("initialize", "initialize", this.buildMethodVisitor, this.buildInstanceLocalVarIndex);
         } else {
            this.pushBeanDefinitionMethodInvocation(this.buildMethodVisitor, "initialize");
         }

         pushCastToType(this.buildMethodVisitor, this.beanType);
         this.buildMethodVisitor.loadLocal(this.buildInstanceLocalVarIndex);
         this.postConstructAdded = true;
      }

   }

   private void writeInterceptedLifecycleMethod(
      String lifeCycleMethodName, String dispatchMethodName, GeneratorAdapter targetMethodVisitor, int instanceLocalIndex
   ) {
      BeanDefinitionWriter.InnerClassDef postConstructInnerMethod = this.newInnerClass(AbstractExecutableMethod.class);
      ClassWriter postConstructInnerWriter = postConstructInnerMethod.innerClassWriter;
      Type postConstructInnerClassType = postConstructInnerMethod.innerClassType;
      String fieldBeanDef = "$beanDef";
      String fieldResContext = "$resolutionContext";
      String fieldBeanContext = "$beanContext";
      String fieldBean = "$bean";
      this.newFinalField(postConstructInnerWriter, this.beanDefinitionType, "$beanDef");
      this.newFinalField(postConstructInnerWriter, TYPE_RESOLUTION_CONTEXT, "$resolutionContext");
      this.newFinalField(postConstructInnerWriter, TYPE_BEAN_CONTEXT, "$beanContext");
      this.newFinalField(postConstructInnerWriter, this.beanType, "$bean");
      String constructorDescriptor = getConstructorDescriptor(new Type[]{this.beanDefinitionType, TYPE_RESOLUTION_CONTEXT, TYPE_BEAN_CONTEXT, this.beanType});
      GeneratorAdapter protectedConstructor = new GeneratorAdapter(
         postConstructInnerWriter.visitMethod(4, "<init>", constructorDescriptor, null, null), 4, "<init>", constructorDescriptor
      );
      protectedConstructor.loadThis();
      protectedConstructor.loadArg(0);
      protectedConstructor.putField(postConstructInnerClassType, "$beanDef", this.beanDefinitionType);
      protectedConstructor.loadThis();
      protectedConstructor.loadArg(1);
      protectedConstructor.putField(postConstructInnerClassType, "$resolutionContext", TYPE_RESOLUTION_CONTEXT);
      protectedConstructor.loadThis();
      protectedConstructor.loadArg(2);
      protectedConstructor.putField(postConstructInnerClassType, "$beanContext", TYPE_BEAN_CONTEXT);
      protectedConstructor.loadThis();
      protectedConstructor.loadArg(3);
      protectedConstructor.putField(postConstructInnerClassType, "$bean", this.beanType);
      protectedConstructor.loadThis();
      protectedConstructor.push(this.beanType);
      protectedConstructor.push(lifeCycleMethodName);
      this.invokeConstructor(protectedConstructor, AbstractExecutableMethod.class, new Class[]{Class.class, String.class});
      protectedConstructor.returnValue();
      protectedConstructor.visitMaxs(1, 1);
      protectedConstructor.visitEnd();
      GeneratorAdapter getAnnotationMetadata = this.startPublicFinalMethodZeroArgs(postConstructInnerWriter, AnnotationMetadata.class, "getAnnotationMetadata");
      this.lookupReferenceAnnotationMetadata(getAnnotationMetadata);
      GeneratorAdapter invokeMethod = this.startPublicMethod(postConstructInnerWriter, ExecutableMethodWriter.METHOD_INVOKE_INTERNAL);
      invokeMethod.loadThis();
      invokeMethod.getField(postConstructInnerClassType, "$beanDef", this.beanDefinitionType);
      invokeMethod.loadThis();
      invokeMethod.getField(postConstructInnerClassType, "$resolutionContext", TYPE_RESOLUTION_CONTEXT);
      invokeMethod.loadThis();
      invokeMethod.getField(postConstructInnerClassType, "$beanContext", TYPE_BEAN_CONTEXT);
      invokeMethod.loadThis();
      invokeMethod.getField(postConstructInnerClassType, "$bean", this.beanType);
      invokeMethod.visitMethodInsn(182, this.beanDefinitionInternalName, lifeCycleMethodName, METHOD_DESCRIPTOR_INITIALIZE, false);
      invokeMethod.returnValue();
      invokeMethod.visitMaxs(1, 1);
      invokeMethod.visitEnd();
      targetMethodVisitor.visitTypeInsn(187, postConstructInnerMethod.constructorInternalName);
      targetMethodVisitor.visitInsn(89);
      targetMethodVisitor.loadThis();
      targetMethodVisitor.loadArg(0);
      targetMethodVisitor.loadArg(1);
      targetMethodVisitor.loadLocal(instanceLocalIndex);
      pushCastToType(targetMethodVisitor, this.beanType);
      targetMethodVisitor.visitMethodInsn(183, postConstructInnerMethod.constructorInternalName, "<init>", constructorDescriptor, false);
      int executableInstanceIndex = targetMethodVisitor.newLocal(Type.getType(ExecutableMethod.class));
      targetMethodVisitor.storeLocal(executableInstanceIndex);
      targetMethodVisitor.loadArg(0);
      targetMethodVisitor.loadArg(1);
      targetMethodVisitor.loadThis();
      targetMethodVisitor.loadLocal(executableInstanceIndex);
      targetMethodVisitor.loadLocal(instanceLocalIndex);
      pushCastToType(targetMethodVisitor, this.beanType);
      targetMethodVisitor.visitMethodInsn(
         184, "io/micronaut/aop/chain/MethodInterceptorChain", dispatchMethodName, METHOD_DESCRIPTOR_INTERCEPTED_LIFECYCLE, false
      );
      targetMethodVisitor.loadLocal(instanceLocalIndex);
   }

   private void visitPreDestroyMethodDefinition(boolean intercepted) {
      if (this.preDestroyMethodVisitor == null) {
         this.interfaceTypes.add(DisposableBeanDefinition.class);
         GeneratorAdapter preDestroyMethodVisitor;
         if (intercepted) {
            preDestroyMethodVisitor = this.newLifeCycleMethod("doDispose");
            GeneratorAdapter disposeMethod = this.newLifeCycleMethod("dispose");
            disposeMethod.loadArg(2);
            int instanceLocalIndex = disposeMethod.newLocal(this.beanType);
            disposeMethod.storeLocal(instanceLocalIndex);
            this.writeInterceptedLifecycleMethod("doDispose", "dispose", disposeMethod, instanceLocalIndex);
            disposeMethod.returnValue();
            this.interceptedDisposeMethod = disposeMethod;
         } else {
            preDestroyMethodVisitor = this.newLifeCycleMethod("dispose");
         }

         this.preDestroyMethodVisitor = preDestroyMethodVisitor;
         preDestroyMethodVisitor.loadArg(2);
         pushCastToType(preDestroyMethodVisitor, this.beanType);
         this.preDestroyInstanceLocalVarIndex = preDestroyMethodVisitor.newLocal(this.beanType);
         preDestroyMethodVisitor.storeLocal(this.preDestroyInstanceLocalVarIndex);
         this.invokeSuperInjectMethod(preDestroyMethodVisitor, PRE_DESTROY_METHOD);
      }

   }

   private GeneratorAdapter newLifeCycleMethod(String methodName) {
      String desc = getMethodDescriptor(
         Object.class.getName(), new String[]{BeanResolutionContext.class.getName(), BeanContext.class.getName(), Object.class.getName()}
      );
      return new GeneratorAdapter(
         this.classWriter
            .visitMethod(
               1,
               methodName,
               desc,
               getMethodSignature(
                  getTypeDescriptor(this.beanFullClassName),
                  new String[]{
                     getTypeDescriptor(BeanResolutionContext.class.getName()),
                     getTypeDescriptor(BeanContext.class.getName()),
                     getTypeDescriptor(this.beanFullClassName)
                  }
               ),
               null
            ),
         1,
         methodName,
         desc
      );
   }

   private void invokeSuperInjectMethod(GeneratorAdapter methodVisitor, Method methodToInvoke) {
      methodVisitor.loadThis();
      methodVisitor.loadArg(0);
      methodVisitor.loadArg(1);
      methodVisitor.loadArg(2);
      this.pushInvokeMethodOnSuperClass(methodVisitor, methodToInvoke);
   }

   private void visitBuildFactoryMethodDefinition(ClassElement factoryClass, Element factoryMethod, ParameterElement... parameters) {
      if (this.buildMethodVisitor == null) {
         List<ParameterElement> parameterList = Arrays.asList(parameters);
         boolean isParametrized = this.isParametrized(parameters);
         boolean isIntercepted = this.isConstructorIntercepted(factoryMethod);
         Type factoryType = JavaModelUtils.getTypeReference(factoryClass);
         this.defineBuilderMethod(isParametrized);
         GeneratorAdapter buildMethodVisitor = this.buildMethodVisitor;
         this.invokeCheckIfShouldLoadIfNecessary(buildMethodVisitor);
         buildMethodVisitor.loadArg(1);
         pushCastToType(buildMethodVisitor, DefaultBeanContext.class);
         buildMethodVisitor.loadArg(0);
         buildMethodVisitor.push(factoryType);
         this.pushQualifier(buildMethodVisitor, factoryClass, () -> {
            buildMethodVisitor.push(factoryType);
            buildMethodVisitor.push("factory");
            invokeInterfaceStaticMethod(buildMethodVisitor, Argument.class, METHOD_CREATE_ARGUMENT_SIMPLE);
         });
         buildMethodVisitor.invokeVirtual(Type.getType(DefaultBeanContext.class), io.micronaut.asm.commons.Method.getMethod(METHOD_GET_BEAN));
         int factoryVar = buildMethodVisitor.newLocal(factoryType);
         buildMethodVisitor.storeLocal(factoryVar, factoryType);
         buildMethodVisitor.loadArg(0);
         buildMethodVisitor.invokeInterface(TYPE_RESOLUTION_CONTEXT, METHOD_BEAN_RESOLUTION_CONTEXT_MARK_FACTORY);
         buildMethodVisitor.loadLocal(factoryVar);
         pushCastToType(buildMethodVisitor, factoryClass);
         String methodDescriptor = getMethodDescriptorForReturnType(this.beanType, parameterList);
         boolean hasInjectScope = false;
         if (isIntercepted) {
            int constructorIndex = this.initInterceptedConstructorWriter(
               buildMethodVisitor, parameterList, new BeanDefinitionWriter.FactoryMethodDef(factoryType, factoryMethod, methodDescriptor, factoryVar)
            );
            int parametersIndex = this.createParameterArray(parameterList, buildMethodVisitor);
            this.invokeConstructorChain(buildMethodVisitor, constructorIndex, parametersIndex, parameterList);
         } else {
            if (!parameterList.isEmpty()) {
               hasInjectScope = this.pushConstructorArguments(buildMethodVisitor, parameters);
            }

            if (factoryMethod instanceof MethodElement) {
               buildMethodVisitor.visitMethodInsn(182, factoryType.getInternalName(), factoryMethod.getName(), methodDescriptor, false);
            } else {
               buildMethodVisitor.getField(factoryType, factoryMethod.getName(), this.beanType);
            }
         }

         this.buildInstanceLocalVarIndex = buildMethodVisitor.newLocal(this.beanType);
         buildMethodVisitor.storeLocal(this.buildInstanceLocalVarIndex);
         if (!this.isPrimitiveBean) {
            this.pushBeanDefinitionMethodInvocation(buildMethodVisitor, "injectBean");
            pushCastToType(buildMethodVisitor, this.beanType);
            buildMethodVisitor.storeLocal(this.buildInstanceLocalVarIndex);
         }

         this.destroyInjectScopeBeansIfNecessary(buildMethodVisitor, hasInjectScope);
         buildMethodVisitor.loadLocal(this.buildInstanceLocalVarIndex);
         this.initLifeCycleMethodsIfNecessary();
      }

   }

   private void visitBuildMethodDefinition(MethodElement constructor, boolean requiresReflection) {
      if (this.buildMethodVisitor == null) {
         boolean isIntercepted = this.isConstructorIntercepted(constructor);
         ParameterElement[] parameterArray = constructor.getParameters();
         List<ParameterElement> parameters = Arrays.asList(parameterArray);
         boolean isParametrized = this.isParametrized(parameterArray);
         this.defineBuilderMethod(isParametrized);
         GeneratorAdapter buildMethodVisitor = this.buildMethodVisitor;
         this.invokeCheckIfShouldLoadIfNecessary(buildMethodVisitor);
         if (isIntercepted) {
            int constructorIndex = this.initInterceptedConstructorWriter(buildMethodVisitor, parameters, null);
            int parametersIndex = this.createParameterArray(parameters, buildMethodVisitor);
            this.invokeConstructorChain(buildMethodVisitor, constructorIndex, parametersIndex, parameters);
         } else if (constructor.isStatic()) {
            this.pushConstructorArguments(buildMethodVisitor, parameterArray);
            String methodDescriptor = getMethodDescriptor(constructor.getReturnType(), parameters);
            buildMethodVisitor.invokeStatic(
               getTypeReference(constructor.getDeclaringType()), new io.micronaut.asm.commons.Method(constructor.getName(), methodDescriptor)
            );
         } else if (requiresReflection) {
            int parameterArrayLocalVarIndex = this.createParameterArray(parameters, buildMethodVisitor);
            int parameterTypeArrayLocalVarIndex = this.createParameterTypeArray(parameters, buildMethodVisitor);
            buildMethodVisitor.push(this.beanType);
            buildMethodVisitor.loadLocal(parameterTypeArrayLocalVarIndex);
            buildMethodVisitor.loadLocal(parameterArrayLocalVarIndex);
            buildMethodVisitor.invokeStatic(
               Type.getType(InstantiationUtils.class),
               io.micronaut.asm.commons.Method.getMethod(
                  ReflectionUtils.getRequiredInternalMethod(InstantiationUtils.class, "instantiate", Class.class, Class[].class, Object[].class)
               )
            );
            pushCastToType(buildMethodVisitor, this.beanType);
         } else {
            buildMethodVisitor.newInstance(this.beanType);
            buildMethodVisitor.dup();
            this.pushConstructorArguments(buildMethodVisitor, parameterArray);
            String constructorDescriptor = getConstructorDescriptor(parameters);
            buildMethodVisitor.invokeConstructor(this.beanType, new io.micronaut.asm.commons.Method("<init>", constructorDescriptor));
         }

         this.buildInstanceLocalVarIndex = buildMethodVisitor.newLocal(this.beanType);
         buildMethodVisitor.storeLocal(this.buildInstanceLocalVarIndex);
         this.pushBeanDefinitionMethodInvocation(buildMethodVisitor, "injectBean");
         pushCastToType(buildMethodVisitor, this.beanType);
         buildMethodVisitor.storeLocal(this.buildInstanceLocalVarIndex);
         buildMethodVisitor.loadLocal(this.buildInstanceLocalVarIndex);
         this.initLifeCycleMethodsIfNecessary();
         pushBoxPrimitiveIfNecessary(this.beanType, buildMethodVisitor);
      }

   }

   private void invokeCheckIfShouldLoadIfNecessary(GeneratorAdapter buildMethodVisitor) {
      AnnotationValue<Requires> requiresAnnotation = this.annotationMetadata.getAnnotation(Requires.class);
      if (requiresAnnotation != null && requiresAnnotation.stringValue("bean").isPresent() && requiresAnnotation.stringValue("beanProperty").isPresent()) {
         this.visitCheckIfShouldLoadMethodDefinition();
         buildMethodVisitor.loadThis();
         buildMethodVisitor.loadArg(0);
         buildMethodVisitor.loadArg(1);
         buildMethodVisitor.invokeVirtual(
            this.beanDefinitionType,
            io.micronaut.asm.commons.Method.getMethod(
               ReflectionUtils.getRequiredMethod(AbstractInitializableBeanDefinition.class, "checkIfShouldLoad", BeanResolutionContext.class, BeanContext.class)
            )
         );
      }

   }

   private void initLifeCycleMethodsIfNecessary() {
      if (this.isInterceptedLifeCycleByType(this.annotationMetadata, "POST_CONSTRUCT")) {
         this.visitPostConstructMethodDefinition(true);
      }

      if (!this.superBeanDefinition && this.isInterceptedLifeCycleByType(this.annotationMetadata, "PRE_DESTROY")) {
         this.visitPreDestroyMethodDefinition(true);
      }

   }

   private void invokeConstructorChain(
      GeneratorAdapter generatorAdapter, int constructorLocalIndex, int parametersLocalIndex, List<ParameterElement> parameters
   ) {
      generatorAdapter.loadArg(0);
      generatorAdapter.loadArg(1);
      if (StringUtils.isNotEmpty(this.interceptedType)) {
         generatorAdapter.loadLocal(parametersLocalIndex);
         generatorAdapter.push(parameters.size() - 1);
         generatorAdapter.arrayLoad(TYPE_OBJECT);
         pushCastToType(generatorAdapter, List.class);
      } else {
         generatorAdapter.visitInsn(1);
      }

      generatorAdapter.loadThis();
      generatorAdapter.loadLocal(constructorLocalIndex);
      if (this.getInterceptedType().isPresent()) {
         generatorAdapter.push(4);
      } else {
         generatorAdapter.push(0);
      }

      generatorAdapter.loadLocal(parametersLocalIndex);
      generatorAdapter.visitMethodInsn(
         184, "io/micronaut/aop/chain/ConstructorInterceptorChain", "instantiate", METHOD_DESCRIPTOR_CONSTRUCTOR_INSTANTIATE, false
      );
   }

   private int initInterceptedConstructorWriter(
      GeneratorAdapter buildMethodVisitor, List<ParameterElement> parameters, @Nullable BeanDefinitionWriter.FactoryMethodDef factoryMethodDef
   ) {
      BeanDefinitionWriter.InnerClassDef constructorInjectionPointInnerClass = this.newInnerClass(AbstractConstructorInjectionPoint.class);
      ClassWriter interceptedConstructorWriter = constructorInjectionPointInnerClass.innerClassWriter;
      io.micronaut.asm.commons.Method constructorMethod = io.micronaut.asm.commons.Method.getMethod(CONSTRUCTOR_ABSTRACT_CONSTRUCTOR_IP);
      boolean hasFactoryMethod = factoryMethodDef != null;
      Type factoryType = hasFactoryMethod ? factoryMethodDef.factoryType : null;
      String factoryFieldName = "$factory";
      GeneratorAdapter protectedConstructor;
      String interceptedConstructorDescriptor;
      if (hasFactoryMethod) {
         this.newFinalField(interceptedConstructorWriter, factoryType, "$factory");
         interceptedConstructorDescriptor = getConstructorDescriptor(new Type[]{TYPE_BEAN_DEFINITION, factoryType});
         protectedConstructor = new GeneratorAdapter(
            interceptedConstructorWriter.visitMethod(4, "<init>", interceptedConstructorDescriptor, null, null), 4, "<init>", interceptedConstructorDescriptor
         );
      } else {
         interceptedConstructorDescriptor = constructorMethod.getDescriptor();
         protectedConstructor = new GeneratorAdapter(
            interceptedConstructorWriter.visitMethod(4, "<init>", interceptedConstructorDescriptor, null, null), 4, "<init>", interceptedConstructorDescriptor
         );
      }

      if (hasFactoryMethod) {
         protectedConstructor.loadThis();
         protectedConstructor.loadArg(1);
         protectedConstructor.putField(constructorInjectionPointInnerClass.innerClassType, "$factory", factoryType);
      }

      protectedConstructor.loadThis();
      protectedConstructor.loadArg(0);
      protectedConstructor.invokeConstructor(Type.getType(AbstractConstructorInjectionPoint.class), constructorMethod);
      protectedConstructor.returnValue();
      protectedConstructor.visitMaxs(1, 1);
      protectedConstructor.visitEnd();
      GeneratorAdapter invokeMethod = this.startPublicMethod(interceptedConstructorWriter, METHOD_INVOKE_CONSTRUCTOR);
      if (hasFactoryMethod) {
         invokeMethod.loadThis();
         invokeMethod.getField(constructorInjectionPointInnerClass.innerClassType, "$factory", factoryType);
         pushCastToType(invokeMethod, factoryType);
      } else {
         invokeMethod.visitTypeInsn(187, this.beanType.getInternalName());
         invokeMethod.visitInsn(89);
      }

      for(int i = 0; i < parameters.size(); ++i) {
         invokeMethod.loadArg(0);
         invokeMethod.push(i);
         invokeMethod.arrayLoad(TYPE_OBJECT);
         pushCastToType(invokeMethod, (TypedElement)parameters.get(i));
      }

      if (hasFactoryMethod) {
         if (factoryMethodDef.factoryMethod instanceof MethodElement) {
            invokeMethod.visitMethodInsn(182, factoryType.getInternalName(), factoryMethodDef.factoryMethod.getName(), factoryMethodDef.methodDescriptor, false);
         } else {
            invokeMethod.getField(factoryType, factoryMethodDef.factoryMethod.getName(), this.beanType);
         }
      } else {
         String constructorDescriptor = getConstructorDescriptor(parameters);
         invokeMethod.visitMethodInsn(183, this.beanType.getInternalName(), "<init>", constructorDescriptor, false);
      }

      invokeMethod.returnValue();
      invokeMethod.visitMaxs(1, 1);
      invokeMethod.visitEnd();
      buildMethodVisitor.visitTypeInsn(187, constructorInjectionPointInnerClass.constructorInternalName);
      buildMethodVisitor.visitInsn(89);
      buildMethodVisitor.loadThis();
      if (hasFactoryMethod) {
         buildMethodVisitor.loadLocal(factoryMethodDef.factoryVar);
         pushCastToType(buildMethodVisitor, factoryType);
      }

      buildMethodVisitor.visitMethodInsn(183, constructorInjectionPointInnerClass.constructorInternalName, "<init>", interceptedConstructorDescriptor, false);
      int constructorIndex = buildMethodVisitor.newLocal(Type.getType(AbstractConstructorInjectionPoint.class));
      buildMethodVisitor.storeLocal(constructorIndex);
      return constructorIndex;
   }

   private void newFinalField(ClassWriter classWriter, Type fieldType, String fieldName) {
      classWriter.visitField(18, fieldName, fieldType.getDescriptor(), null, null);
   }

   private BeanDefinitionWriter.InnerClassDef newInnerClass(Class<?> superType) {
      ClassWriter interceptedConstructorWriter = new ClassWriter(3);
      String interceptedConstructorWriterName = this.newInnerClassName();
      this.innerClasses.put(interceptedConstructorWriterName, interceptedConstructorWriter);
      String constructorInternalName = getInternalName(interceptedConstructorWriterName);
      Type interceptedConstructorType = getTypeReferenceForName(interceptedConstructorWriterName, new String[0]);
      interceptedConstructorWriter.visit(52, 4114, constructorInternalName, null, Type.getInternalName(superType), null);
      interceptedConstructorWriter.visitAnnotation(TYPE_GENERATED.getDescriptor(), false);
      interceptedConstructorWriter.visitOuterClass(this.beanDefinitionInternalName, null, null);
      this.classWriter.visitInnerClass(constructorInternalName, this.beanDefinitionInternalName, null, 2);
      return new BeanDefinitionWriter.InnerClassDef(
         interceptedConstructorWriterName, interceptedConstructorWriter, constructorInternalName, interceptedConstructorType
      );
   }

   @NonNull
   private String newInnerClassName() {
      return this.beanDefinitionName + "$" + ++this.innerClassIndex;
   }

   private int createParameterTypeArray(List<ParameterElement> parameters, GeneratorAdapter buildMethodVisitor) {
      int pLen = parameters.size();
      pushNewArray(buildMethodVisitor, Class.class, pLen);

      for(int i = 0; i < pLen; ++i) {
         ParameterElement parameter = (ParameterElement)parameters.get(i);
         pushStoreInArray(buildMethodVisitor, i, pLen, () -> buildMethodVisitor.push(getTypeReference(parameter)));
      }

      int local = buildMethodVisitor.newLocal(Type.getType(Object[].class));
      buildMethodVisitor.storeLocal(local);
      return local;
   }

   private int createParameterArray(List<ParameterElement> parameters, GeneratorAdapter buildMethodVisitor) {
      int pLen = parameters.size();
      pushNewArray(buildMethodVisitor, Object.class, pLen);

      for(int i = 0; i < pLen; ++i) {
         ParameterElement parameter = (ParameterElement)parameters.get(i);
         int parameterIndex = i;
         pushStoreInArray(
            buildMethodVisitor,
            i,
            pLen,
            () -> this.pushConstructorArgument(buildMethodVisitor, parameter.getName(), parameter, parameter.getAnnotationMetadata(), parameterIndex)
         );
      }

      int local = buildMethodVisitor.newLocal(Type.getType(Object[].class));
      buildMethodVisitor.storeLocal(local);
      return local;
   }

   private boolean isConstructorIntercepted(Element constructor) {
      AnnotationMetadataHierarchy annotationMetadata = new AnnotationMetadataHierarchy(this.annotationMetadata, constructor.getAnnotationMetadata());
      String interceptType = "AROUND_CONSTRUCT";
      return this.isInterceptedLifeCycleByType(annotationMetadata, "AROUND_CONSTRUCT");
   }

   private boolean isInterceptedLifeCycleByType(AnnotationMetadata annotationMetadata, String interceptType) {
      return this.isLifeCycleCache
         .computeIfAbsent(
            interceptType,
            s -> {
               if (this.beanTypeElement.isAssignable("io.micronaut.aop.Interceptor")) {
                  return false;
               } else {
                  Element originatingElement = this.getOriginatingElements()[0];
                  boolean isFactoryMethod = originatingElement instanceof MethodElement && !(originatingElement instanceof ConstructorElement);
                  boolean isProxyTarget = annotationMetadata.booleanValue("io.micronaut.aop.Around", "proxyTarget").orElse(false) || isFactoryMethod;
                  boolean isAopType = StringUtils.isNotEmpty(this.interceptedType);
                  boolean isConstructorInterceptionCandidate = isProxyTarget && !isAopType || isAopType && !isProxyTarget;
                  AnnotationValue<Annotation> interceptorBindings = annotationMetadata.getAnnotation("io.micronaut.aop.InterceptorBindingDefinitions");
                  boolean hasAroundConstruct;
                  List<AnnotationValue<Annotation>> interceptorBindingAnnotations;
                  if (interceptorBindings != null) {
                     interceptorBindingAnnotations = interceptorBindings.getAnnotations("value");
                     hasAroundConstruct = interceptorBindingAnnotations.stream()
                        .anyMatch(avx -> avx.stringValue("kind").map(k -> k.equals(interceptType)).orElse(false));
                  } else {
                     interceptorBindingAnnotations = Collections.emptyList();
                     hasAroundConstruct = false;
                  }
      
                  if (isConstructorInterceptionCandidate) {
                     return hasAroundConstruct;
                  } else if (hasAroundConstruct) {
                     if (!this.isSuperFactory && annotationMetadata instanceof AnnotationMetadataHierarchy) {
                        AnnotationMetadata typeMetadata = ((AnnotationMetadataHierarchy)annotationMetadata).getRootMetadata();
                        AnnotationValue<Annotation> av = typeMetadata.getAnnotation("io.micronaut.aop.InterceptorBindingDefinitions");
                        if (av != null) {
                           interceptorBindingAnnotations = av.getAnnotations("value");
                        } else {
                           interceptorBindingAnnotations = Collections.emptyList();
                        }
                     }
      
                     return interceptorBindingAnnotations.stream().noneMatch(avx -> avx.stringValue("kind").map(k -> k.equals("AROUND")).orElse(false));
                  } else {
                     return false;
                  }
               }
            }
         );
   }

   private boolean pushConstructorArguments(GeneratorAdapter buildMethodVisitor, ParameterElement[] parameters) {
      int size = parameters.length;
      boolean hasInjectScope = false;
      if (size > 0) {
         for(int i = 0; i < parameters.length; ++i) {
            ParameterElement parameter = parameters[i];
            this.pushConstructorArgument(buildMethodVisitor, parameter.getName(), parameter, parameter.getAnnotationMetadata(), i);
            if (parameter.hasDeclaredAnnotation(InjectScope.class)) {
               hasInjectScope = true;
            }
         }
      }

      return hasInjectScope;
   }

   private void pushConstructorArgument(
      GeneratorAdapter buildMethodVisitor, String argumentName, ParameterElement argumentType, AnnotationMetadata annotationMetadata, int index
   ) {
      if (this.isAnnotatedWithParameter(annotationMetadata) && this.isParametrized) {
         buildMethodVisitor.loadArg(3);
         buildMethodVisitor.push(argumentName);
         buildMethodVisitor.invokeInterface(
            Type.getType(Map.class), io.micronaut.asm.commons.Method.getMethod(ReflectionUtils.getRequiredMethod(Map.class, "get", Object.class))
         );
         pushCastToType(buildMethodVisitor, argumentType);
      } else if (argumentType.getGenericType().isAssignable(BeanContext.class)) {
         buildMethodVisitor.loadArg(1);
      } else if (argumentType.getGenericType().isAssignable(BeanResolutionContext.class)) {
         buildMethodVisitor.loadArg(0);
      } else {
         boolean isArray = false;
         boolean hasGenericType = false;
         ClassElement genericType = argumentType.getGenericType();
         if (this.isValueType(annotationMetadata) && !this.isInnerType(genericType)) {
            Optional<String> property = argumentType.stringValue(Property.class, "name");
            if (property.isPresent()) {
               this.pushInvokeGetPropertyValueForConstructor(buildMethodVisitor, index, argumentType, (String)property.get());
            } else {
               Optional<String> valueValue = argumentType.stringValue(Value.class);
               if (valueValue.isPresent()) {
                  this.pushInvokeGetPropertyPlaceholderValueForConstructor(buildMethodVisitor, index, argumentType, (String)valueValue.get());
               }
            }

            return;
         }

         isArray = genericType.isArray();
         Method methodToInvoke;
         if (genericType.isAssignable(Collection.class) || isArray) {
            hasGenericType = true;
            ClassElement typeArgument = genericType.isArray() ? genericType.fromArray() : (ClassElement)genericType.getFirstTypeArgument().orElse(null);
            if (typeArgument == null || typeArgument.isPrimitive()) {
               methodToInvoke = GET_BEAN_FOR_CONSTRUCTOR_ARGUMENT;
               hasGenericType = false;
            } else if (typeArgument.isAssignable(BeanRegistration.class)) {
               methodToInvoke = GET_BEAN_REGISTRATIONS_FOR_CONSTRUCTOR_ARGUMENT;
            } else {
               methodToInvoke = GET_BEANS_OF_TYPE_FOR_CONSTRUCTOR_ARGUMENT;
            }
         } else if (genericType.isAssignable(Stream.class)) {
            hasGenericType = true;
            methodToInvoke = GET_STREAM_OF_TYPE_FOR_CONSTRUCTOR_ARGUMENT;
         } else if (genericType.isAssignable(Optional.class)) {
            hasGenericType = true;
            methodToInvoke = FIND_BEAN_FOR_CONSTRUCTOR_ARGUMENT;
         } else if (genericType.isAssignable(BeanRegistration.class)) {
            hasGenericType = true;
            methodToInvoke = GET_BEAN_REGISTRATION_FOR_CONSTRUCTOR_ARGUMENT;
         } else {
            methodToInvoke = GET_BEAN_FOR_CONSTRUCTOR_ARGUMENT;
         }

         buildMethodVisitor.loadThis();
         buildMethodVisitor.loadArg(0);
         buildMethodVisitor.loadArg(1);
         buildMethodVisitor.push(index);
         if (hasGenericType) {
            this.resolveConstructorArgumentGenericType(buildMethodVisitor, argumentType.getGenericType(), index);
         }

         this.pushQualifier(buildMethodVisitor, argumentType, () -> this.resolveConstructorArgument(buildMethodVisitor, index));
         this.pushInvokeMethodOnSuperClass(buildMethodVisitor, methodToInvoke);
         if (isArray && hasGenericType) {
            this.convertToArray(argumentType.getGenericType().fromArray(), buildMethodVisitor);
         }

         pushCastToType(buildMethodVisitor, argumentType);
      }

   }

   private void pushInvokeGetPropertyValueForConstructor(GeneratorAdapter injectMethodVisitor, int i, ParameterElement entry, String value) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      injectMethodVisitor.push(i);
      injectMethodVisitor.push(value);
      injectMethodVisitor.push(this.getCliPrefix(entry.getName()));
      this.pushInvokeMethodOnSuperClass(injectMethodVisitor, GET_PROPERTY_VALUE_FOR_CONSTRUCTOR_ARGUMENT);
      pushCastToType(injectMethodVisitor, entry);
   }

   private void pushInvokeGetPropertyPlaceholderValueForConstructor(GeneratorAdapter injectMethodVisitor, int i, ParameterElement entry, String value) {
      injectMethodVisitor.loadThis();
      injectMethodVisitor.loadArg(0);
      injectMethodVisitor.loadArg(1);
      injectMethodVisitor.push(i);
      injectMethodVisitor.push(value);
      this.pushInvokeMethodOnSuperClass(injectMethodVisitor, GET_PROPERTY_PLACEHOLDER_VALUE_FOR_CONSTRUCTOR_ARGUMENT);
      pushCastToType(injectMethodVisitor, entry);
   }

   private void resolveConstructorArgumentGenericType(GeneratorAdapter visitor, ClassElement type, int argumentIndex) {
      if (!this.resolveArgumentGenericType(visitor, type)) {
         this.resolveConstructorArgument(visitor, argumentIndex);
         this.resolveFirstTypeArgument(visitor);
         this.resolveInnerTypeArgumentIfNeeded(visitor, type);
      }

   }

   private void resolveConstructorArgument(GeneratorAdapter visitor, int argumentIndex) {
      Type constructorField = Type.getType(AbstractInitializableBeanDefinition.MethodOrFieldReference.class);
      Type methodRefType = Type.getType(AbstractInitializableBeanDefinition.MethodReference.class);
      visitor.getStatic(this.beanDefinitionType, "$CONSTRUCTOR", constructorField);
      pushCastToType(visitor, methodRefType);
      visitor.getField(methodRefType, "arguments", Type.getType(Argument[].class));
      visitor.push(argumentIndex);
      visitor.arrayLoad(Type.getType(Argument.class));
   }

   private void resolveMethodArgumentGenericType(GeneratorAdapter visitor, ClassElement type, int methodIndex, int argumentIndex) {
      if (!this.resolveArgumentGenericType(visitor, type)) {
         this.resolveMethodArgument(visitor, methodIndex, argumentIndex);
         this.resolveFirstTypeArgument(visitor);
         this.resolveInnerTypeArgumentIfNeeded(visitor, type);
      }

   }

   private void resolveMethodArgument(GeneratorAdapter visitor, int methodIndex, int argumentIndex) {
      Type methodsRef = Type.getType(AbstractInitializableBeanDefinition.MethodReference[].class);
      Type methodRefType = Type.getType(AbstractInitializableBeanDefinition.MethodReference.class);
      visitor.getStatic(this.beanDefinitionType, "$INJECTION_METHODS", methodsRef);
      visitor.push(methodIndex);
      visitor.arrayLoad(methodsRef);
      visitor.getField(methodRefType, "arguments", Type.getType(Argument[].class));
      visitor.push(argumentIndex);
      visitor.arrayLoad(Type.getType(Argument.class));
   }

   private void resolveFieldArgumentGenericType(GeneratorAdapter visitor, ClassElement type, int fieldIndex) {
      if (!this.resolveArgumentGenericType(visitor, type)) {
         this.resolveFieldArgument(visitor, fieldIndex);
         this.resolveFirstTypeArgument(visitor);
         this.resolveInnerTypeArgumentIfNeeded(visitor, type);
      }

   }

   private void resolveAnnotationArgument(GeneratorAdapter visitor, int index) {
      visitor.getStatic(this.beanDefinitionType, "$ANNOTATION_INJECTIONS", Type.getType(AbstractInitializableBeanDefinition.FieldReference[].class));
      visitor.push(index);
      visitor.arrayLoad(Type.getType(AbstractInitializableBeanDefinition.AnnotationReference.class));
      visitor.getField(Type.getType(AbstractInitializableBeanDefinition.AnnotationReference.class), "argument", Type.getType(Argument.class));
   }

   private void resolveFieldArgument(GeneratorAdapter visitor, int fieldIndex) {
      visitor.getStatic(this.beanDefinitionType, "$INJECTION_FIELDS", Type.getType(AbstractInitializableBeanDefinition.FieldReference[].class));
      visitor.push(fieldIndex);
      visitor.arrayLoad(Type.getType(AbstractInitializableBeanDefinition.FieldReference.class));
      visitor.getField(Type.getType(AbstractInitializableBeanDefinition.FieldReference.class), "argument", Type.getType(Argument.class));
   }

   private boolean resolveArgumentGenericType(GeneratorAdapter visitor, ClassElement type) {
      if (type.isArray()) {
         if (!type.getTypeArguments().isEmpty() && this.isInternalGenericTypeContainer(type.fromArray())) {
            return false;
         } else {
            ClassElement componentType = type.fromArray();
            if (componentType.isPrimitive()) {
               visitor.getStatic(TYPE_ARGUMENT, componentType.getName().toUpperCase(Locale.ENGLISH), TYPE_ARGUMENT);
            } else {
               visitor.push(JavaModelUtils.getTypeReference(componentType));
               visitor.push((String)null);
               invokeInterfaceStaticMethod(visitor, Argument.class, METHOD_CREATE_ARGUMENT_SIMPLE);
            }

            return true;
         }
      } else if (type.getTypeArguments().isEmpty()) {
         visitor.visitInsn(1);
         return true;
      } else {
         return false;
      }
   }

   private void resolveInnerTypeArgumentIfNeeded(GeneratorAdapter visitor, ClassElement type) {
      if (this.isInternalGenericTypeContainer((ClassElement)type.getFirstTypeArgument().get())) {
         this.resolveFirstTypeArgument(visitor);
      }

   }

   private boolean isInternalGenericTypeContainer(ClassElement type) {
      return type.isAssignable(BeanRegistration.class);
   }

   private void resolveFirstTypeArgument(GeneratorAdapter visitor) {
      visitor.invokeInterface(
         Type.getType(TypeVariableResolver.class),
         io.micronaut.asm.commons.Method.getMethod((Method)ReflectionUtils.findMethod(TypeVariableResolver.class, "getTypeParameters").get())
      );
      visitor.push(0);
      visitor.arrayLoad(Type.getType(Argument.class));
   }

   private boolean isValueType(AnnotationMetadata annotationMetadata) {
      if (annotationMetadata == null) {
         return false;
      } else {
         return annotationMetadata.hasDeclaredStereotype(Value.class) || annotationMetadata.hasDeclaredStereotype(Property.class);
      }
   }

   private boolean isAnnotatedWithParameter(AnnotationMetadata annotationMetadata) {
      return annotationMetadata != null ? annotationMetadata.hasDeclaredAnnotation(Parameter.class) : false;
   }

   private boolean isParametrized(ParameterElement... parameters) {
      return Arrays.stream(parameters).anyMatch(p -> this.isAnnotatedWithParameter(p.getAnnotationMetadata()));
   }

   private void defineBuilderMethod(boolean isParametrized) {
      if (isParametrized) {
         this.isParametrized = true;
      }

      ClassElement beanDefinitionParam = ClassElement.of(
         BeanDefinition.class, AnnotationMetadata.EMPTY_METADATA, Collections.singletonMap("T", this.beanTypeElement)
      );
      String methodDescriptor;
      String methodSignature;
      if (isParametrized) {
         methodDescriptor = getMethodDescriptor(
            Object.class.getName(),
            new String[]{BeanResolutionContext.class.getName(), BeanContext.class.getName(), BeanDefinition.class.getName(), Map.class.getName()}
         );
         methodSignature = getMethodSignature(
            getTypeDescriptor(this.beanTypeElement),
            new String[]{
               getTypeDescriptor(BeanResolutionContext.class.getName()),
               getTypeDescriptor(BeanContext.class.getName()),
               getTypeDescriptor(beanDefinitionParam),
               getTypeDescriptor(Map.class.getName())
            }
         );
      } else {
         methodDescriptor = getMethodDescriptor(
            Object.class.getName(), new String[]{BeanResolutionContext.class.getName(), BeanContext.class.getName(), BeanDefinition.class.getName()}
         );
         methodSignature = getMethodSignature(
            getTypeDescriptor(this.beanTypeElement),
            new String[]{
               getTypeDescriptor(BeanResolutionContext.class.getName()), getTypeDescriptor(BeanContext.class.getName()), getTypeDescriptor(beanDefinitionParam)
            }
         );
      }

      String methodName = isParametrized ? "doBuild" : "build";
      this.buildMethodVisitor = new GeneratorAdapter(
         this.classWriter.visitMethod(1, methodName, methodDescriptor, methodSignature, null), 1, methodName, methodDescriptor
      );
   }

   private void pushBeanDefinitionMethodInvocation(GeneratorAdapter buildMethodVisitor, String methodName) {
      buildMethodVisitor.loadThis();
      buildMethodVisitor.loadArg(0);
      buildMethodVisitor.loadArg(1);
      buildMethodVisitor.loadLocal(this.buildInstanceLocalVarIndex);
      pushBoxPrimitiveIfNecessary(this.beanType, buildMethodVisitor);
      buildMethodVisitor.visitMethodInsn(
         182, this.superBeanDefinition ? this.superType.getInternalName() : this.beanDefinitionInternalName, methodName, METHOD_DESCRIPTOR_INITIALIZE, false
      );
   }

   private void visitBeanDefinitionConstructorInternal(GeneratorAdapter staticInit, Object constructor, boolean requiresReflection) {
      if (constructor instanceof MethodElement) {
         MethodElement methodElement = (MethodElement)constructor;
         AnnotationMetadata constructorMetadata = methodElement.getAnnotationMetadata();
         DefaultAnnotationMetadata.contributeDefaults(this.annotationMetadata, constructorMetadata);
         DefaultAnnotationMetadata.contributeRepeatable(this.annotationMetadata, methodElement.getGenericReturnType());
         ParameterElement[] parameters = methodElement.getParameters();
         List<ParameterElement> parameterList = Arrays.asList(parameters);
         this.applyDefaultNamedToParameters(parameterList);
         this.pushNewMethodReference(
            staticInit, JavaModelUtils.getTypeReference(methodElement.getDeclaringType()), methodElement, requiresReflection, false, false
         );
      } else {
         if (!(constructor instanceof FieldElement)) {
            throw new IllegalArgumentException("Unexpected constructor: " + constructor);
         }

         FieldElement fieldConstructor = (FieldElement)constructor;
         this.pushNewFieldReference(
            staticInit, JavaModelUtils.getTypeReference(fieldConstructor.getDeclaringType()), fieldConstructor, this.constructorRequiresReflection
         );
      }

      staticInit.putStatic(this.beanDefinitionType, "$CONSTRUCTOR", Type.getType(AbstractInitializableBeanDefinition.MethodOrFieldReference.class));
      GeneratorAdapter publicConstructor = new GeneratorAdapter(this.classWriter.visitMethod(1, "<init>", "()V", null, null), 1, "<init>", "()V");
      publicConstructor.loadThis();
      publicConstructor.push(this.beanType);
      publicConstructor.getStatic(this.beanDefinitionType, "$CONSTRUCTOR", Type.getType(AbstractInitializableBeanDefinition.MethodOrFieldReference.class));
      publicConstructor.invokeConstructor(this.superBeanDefinition ? this.superType : this.beanDefinitionType, PROTECTED_ABSTRACT_BEAN_DEFINITION_CONSTRUCTOR);
      publicConstructor.visitInsn(177);
      publicConstructor.visitMaxs(5, 1);
      publicConstructor.visitEnd();
      if (!this.superBeanDefinition) {
         GeneratorAdapter protectedConstructor = new GeneratorAdapter(
            this.classWriter
               .visitMethod(
                  4, PROTECTED_ABSTRACT_BEAN_DEFINITION_CONSTRUCTOR.getName(), PROTECTED_ABSTRACT_BEAN_DEFINITION_CONSTRUCTOR.getDescriptor(), null, null
               ),
            4,
            PROTECTED_ABSTRACT_BEAN_DEFINITION_CONSTRUCTOR.getName(),
            PROTECTED_ABSTRACT_BEAN_DEFINITION_CONSTRUCTOR.getDescriptor()
         );
         AnnotationMetadata annotationMetadata = this.annotationMetadata != null ? this.annotationMetadata : AnnotationMetadata.EMPTY_METADATA;
         protectedConstructor.loadThis();
         protectedConstructor.loadArg(0);
         protectedConstructor.loadArg(1);
         if (this.annotationMetadata == null) {
            protectedConstructor.push((String)null);
         } else {
            protectedConstructor.getStatic(
               getTypeReferenceForName(this.getBeanDefinitionReferenceClassName(), new String[0]),
               "$ANNOTATION_METADATA",
               Type.getType(AnnotationMetadata.class)
            );
         }

         if (this.allMethodVisits.isEmpty()) {
            protectedConstructor.push((String)null);
         } else {
            protectedConstructor.getStatic(
               this.beanDefinitionType, "$INJECTION_METHODS", Type.getType(AbstractInitializableBeanDefinition.MethodReference[].class)
            );
         }

         if (this.fieldInjectionPoints.isEmpty()) {
            protectedConstructor.push((String)null);
         } else {
            protectedConstructor.getStatic(
               this.beanDefinitionType, "$INJECTION_FIELDS", Type.getType(AbstractInitializableBeanDefinition.FieldReference[].class)
            );
         }

         if (this.annotationInjectionPoints.isEmpty()) {
            protectedConstructor.push((String)null);
         } else {
            protectedConstructor.getStatic(
               this.beanDefinitionType, "$ANNOTATION_INJECTIONS", Type.getType(AbstractInitializableBeanDefinition.AnnotationReference[].class)
            );
         }

         if (this.executableMethodsDefinitionWriter == null) {
            protectedConstructor.push((String)null);
         } else {
            protectedConstructor.newInstance(this.executableMethodsDefinitionWriter.getClassType());
            protectedConstructor.dup();
            protectedConstructor.invokeConstructor(this.executableMethodsDefinitionWriter.getClassType(), METHOD_DEFAULT_CONSTRUCTOR);
         }

         if (!this.hasTypeArguments()) {
            protectedConstructor.push((String)null);
         } else {
            protectedConstructor.getStatic(this.beanDefinitionType, "$TYPE_ARGUMENTS", Type.getType(Map.class));
         }

         String scope = (String)annotationMetadata.getAnnotationNameByStereotype("javax.inject.Scope").orElse(null);
         if (scope != null) {
            protectedConstructor.push(scope);
            protectedConstructor.invokeStatic(TYPE_OPTIONAL, METHOD_OPTIONAL_OF);
         } else {
            protectedConstructor.invokeStatic(TYPE_OPTIONAL, METHOD_OPTIONAL_EMPTY);
         }

         protectedConstructor.push(this.isAbstract);
         protectedConstructor.push(annotationMetadata.hasDeclaredStereotype(Provided.class));
         protectedConstructor.push(this.isIterable(annotationMetadata));
         protectedConstructor.push(this.isSingleton(scope));
         protectedConstructor.push(annotationMetadata.hasDeclaredStereotype(Primary.class));
         protectedConstructor.push(this.isConfigurationProperties);
         protectedConstructor.push(this.isContainerType());
         protectedConstructor.push(this.preprocessMethods);
         protectedConstructor.invokeConstructor(this.isSuperFactory ? TYPE_ABSTRACT_BEAN_DEFINITION : this.superType, BEAN_DEFINITION_CLASS_CONSTRUCTOR);
         protectedConstructor.visitInsn(177);
         protectedConstructor.visitMaxs(20, 1);
         protectedConstructor.visitEnd();
      }

   }

   private boolean isContainerType() {
      return this.beanTypeElement.isArray() || DefaultArgument.CONTAINER_TYPES.stream().map(Class::getName).anyMatch(c -> c.equals(this.beanFullClassName));
   }

   private boolean isConfigurationProperties(AnnotationMetadata annotationMetadata) {
      return this.isIterable(annotationMetadata) || annotationMetadata.hasStereotype(ConfigurationReader.class);
   }

   private boolean isIterable(AnnotationMetadata annotationMetadata) {
      return annotationMetadata.hasDeclaredStereotype(EachProperty.class) || annotationMetadata.hasDeclaredStereotype(EachBean.class);
   }

   private void pushNewMethodReference(
      GeneratorAdapter staticInit,
      Type beanType,
      MethodElement methodElement,
      boolean requiresReflection,
      boolean isPostConstructMethod,
      boolean isPreDestroyMethod
   ) {
      for(ParameterElement value : methodElement.getParameters()) {
         DefaultAnnotationMetadata.contributeDefaults(this.annotationMetadata, value.getAnnotationMetadata());
         DefaultAnnotationMetadata.contributeRepeatable(this.annotationMetadata, value.getGenericType());
      }

      staticInit.newInstance(Type.getType(AbstractInitializableBeanDefinition.MethodReference.class));
      staticInit.dup();
      staticInit.push(beanType);
      staticInit.push(methodElement.getName());
      if (!methodElement.hasParameters()) {
         staticInit.visitInsn(1);
      } else {
         pushBuildArgumentsForMethod(
            this.beanFullClassName,
            this.beanDefinitionType,
            this.classWriter,
            staticInit,
            Arrays.asList(methodElement.getParameters()),
            this.defaultsStorage,
            this.loadTypeMethods
         );
      }

      this.pushAnnotationMetadata(staticInit, methodElement.getAnnotationMetadata());
      staticInit.push(requiresReflection);
      if (!isPreDestroyMethod && !isPostConstructMethod) {
         staticInit.invokeConstructor(Type.getType(AbstractInitializableBeanDefinition.MethodReference.class), METHOD_REFERENCE_CONSTRUCTOR);
      } else {
         staticInit.push(isPostConstructMethod);
         staticInit.push(isPreDestroyMethod);
         staticInit.invokeConstructor(Type.getType(AbstractInitializableBeanDefinition.MethodReference.class), METHOD_REFERENCE_CONSTRUCTOR_POST_PRE);
      }

   }

   private void pushNewFieldReference(GeneratorAdapter staticInit, Type declaringType, FieldElement fieldElement, boolean requiresReflection) {
      staticInit.newInstance(Type.getType(AbstractInitializableBeanDefinition.FieldReference.class));
      staticInit.dup();
      staticInit.push(declaringType);
      pushCreateArgument(
         this.beanFullClassName,
         this.beanDefinitionType,
         this.classWriter,
         staticInit,
         fieldElement.getName(),
         fieldElement.getGenericType(),
         fieldElement.getAnnotationMetadata(),
         fieldElement.getGenericType().getTypeArguments(),
         this.defaultsStorage,
         this.loadTypeMethods
      );
      staticInit.push(requiresReflection);
      staticInit.invokeConstructor(Type.getType(AbstractInitializableBeanDefinition.FieldReference.class), FIELD_REFERENCE_CONSTRUCTOR);
   }

   private void pushNewAnnotationReference(GeneratorAdapter staticInit, Type referencedType) {
      staticInit.newInstance(Type.getType(AbstractInitializableBeanDefinition.AnnotationReference.class));
      staticInit.dup();
      staticInit.push(referencedType);
      invokeInterfaceStaticMethod(
         staticInit, Argument.class, io.micronaut.asm.commons.Method.getMethod(ReflectionUtils.getRequiredInternalMethod(Argument.class, "of", Class.class))
      );
      staticInit.invokeConstructor(Type.getType(AbstractInitializableBeanDefinition.AnnotationReference.class), ANNOTATION_REFERENCE_CONSTRUCTOR);
   }

   private void pushAnnotationMetadata(GeneratorAdapter staticInit, AnnotationMetadata annotationMetadata) {
      if (annotationMetadata == AnnotationMetadata.EMPTY_METADATA || annotationMetadata.isEmpty()) {
         staticInit.push((String)null);
      } else if (annotationMetadata instanceof AnnotationMetadataHierarchy) {
         AnnotationMetadataWriter.instantiateNewMetadataHierarchy(
            this.beanDefinitionType, this.classWriter, staticInit, (AnnotationMetadataHierarchy)annotationMetadata, this.defaultsStorage, this.loadTypeMethods
         );
      } else if (annotationMetadata instanceof DefaultAnnotationMetadata) {
         AnnotationMetadataWriter.instantiateNewMetadata(
            this.beanDefinitionType, this.classWriter, staticInit, (DefaultAnnotationMetadata)annotationMetadata, this.defaultsStorage, this.loadTypeMethods
         );
      } else {
         staticInit.push((String)null);
      }

   }

   private String generateBeanDefSig(Type typeParameter) {
      if (this.beanTypeElement.isPrimitive()) {
         if (this.beanTypeElement.isArray()) {
            typeParameter = JavaModelUtils.getTypeReference(this.beanTypeElement);
         } else {
            typeParameter = (Type)ClassUtils.getPrimitiveType(typeParameter.getClassName())
               .map(ReflectionUtils::getWrapperType)
               .map(Type::getType)
               .orElseThrow(() -> new IllegalStateException("Not a primitive type: " + this.beanFullClassName));
         }
      }

      SignatureVisitor sv = new ArrayAwareSignatureWriter();
      this.visitSuperTypeParameters(sv, typeParameter);

      for(Class<?> interfaceType : this.interfaceTypes) {
         Type param;
         if (ProxyBeanDefinition.class != interfaceType && AdvisedBeanType.class != interfaceType) {
            param = typeParameter;
         } else {
            param = (Type)this.getInterceptedType().orElse(typeParameter);
         }

         SignatureVisitor bfi = sv.visitInterface();
         bfi.visitClassType(Type.getInternalName(interfaceType));
         SignatureVisitor iisv = bfi.visitTypeArgument('=');
         this.visitTypeParameter(param, iisv);
         bfi.visitEnd();
      }

      return sv.toString();
   }

   private void visitSuperTypeParameters(SignatureVisitor sv, Type... typeParameters) {
      SignatureVisitor psv = sv.visitSuperclass();
      psv.visitClassType(this.isSuperFactory ? TYPE_ABSTRACT_BEAN_DEFINITION.getInternalName() : this.superType.getInternalName());
      if (this.superType == TYPE_ABSTRACT_BEAN_DEFINITION || this.isSuperFactory) {
         for(Type typeParameter : typeParameters) {
            SignatureVisitor ppsv = psv.visitTypeArgument('=');
            this.visitTypeParameter(typeParameter, ppsv);
         }
      }

      psv.visitEnd();
   }

   private void visitTypeParameter(Type typeParameter, SignatureVisitor ppsv) {
      boolean isArray = typeParameter.getSort() == 9;
      boolean isPrimitiveArray = false;
      if (isArray) {
         for(int i = 0; i < typeParameter.getDimensions(); ++i) {
            ppsv.visitArrayType();
         }

         Type elementType = typeParameter.getElementType();

         while(elementType.getSort() == 9) {
            elementType = elementType.getElementType();
         }

         if (elementType.getSort() == 10) {
            ppsv.visitClassType(elementType.getInternalName());
         } else {
            ppsv.visitBaseType(elementType.getInternalName().charAt(0));
            isPrimitiveArray = true;
         }
      } else {
         ppsv.visitClassType(typeParameter.getInternalName());
      }

      if (isPrimitiveArray && ppsv instanceof ArrayAwareSignatureWriter) {
         ((ArrayAwareSignatureWriter)ppsv).visitEndArray();
      } else {
         ppsv.visitEnd();
      }

   }

   private static Method getBeanLookupMethod(String methodName, boolean requiresGenericType) {
      return requiresGenericType
         ? ReflectionUtils.getRequiredInternalMethod(
            AbstractInitializableBeanDefinition.class,
            methodName,
            BeanResolutionContext.class,
            BeanContext.class,
            Integer.TYPE,
            Argument.class,
            Qualifier.class
         )
         : ReflectionUtils.getRequiredInternalMethod(
            AbstractInitializableBeanDefinition.class, methodName, BeanResolutionContext.class, BeanContext.class, Integer.TYPE, Qualifier.class
         );
   }

   private static Method getBeanLookupMethodForArgument(String methodName, boolean requiresGenericType) {
      return requiresGenericType
         ? ReflectionUtils.getRequiredInternalMethod(
            AbstractInitializableBeanDefinition.class,
            methodName,
            BeanResolutionContext.class,
            BeanContext.class,
            Integer.TYPE,
            Integer.TYPE,
            Argument.class,
            Qualifier.class
         )
         : ReflectionUtils.getRequiredInternalMethod(
            AbstractInitializableBeanDefinition.class, methodName, BeanResolutionContext.class, BeanContext.class, Integer.TYPE, Integer.TYPE, Qualifier.class
         );
   }

   @Override
   public String getName() {
      return this.beanDefinitionName;
   }

   @Override
   public boolean isProtected() {
      return false;
   }

   @Override
   public boolean isPublic() {
      return true;
   }

   @Override
   public Object getNativeType() {
      return this;
   }

   @Override
   public Collection<Element> getInjectionPoints() {
      if (this.fieldInjectionPoints.isEmpty() && this.methodInjectionPoints.isEmpty()) {
         return Collections.emptyList();
      } else {
         Collection<Element> injectionPoints = new ArrayList();

         for(BeanDefinitionWriter.FieldVisitData fieldInjectionPoint : this.fieldInjectionPoints) {
            injectionPoints.add(fieldInjectionPoint.fieldElement);
         }

         for(BeanDefinitionWriter.MethodVisitData methodInjectionPoint : this.methodInjectionPoints) {
            injectionPoints.add(methodInjectionPoint.methodElement);
         }

         return Collections.unmodifiableCollection(injectionPoints);
      }
   }

   @Override
   public boolean isAbstract() {
      return this.isAbstract;
   }

   @Override
   public <T extends Annotation> Element annotate(String annotationType, Consumer<AnnotationValueBuilder<T>> consumer) {
      this.beanProducingElement.annotate(annotationType, consumer);
      return this;
   }

   @Override
   public Element removeAnnotation(String annotationType) {
      this.beanProducingElement.removeAnnotation(annotationType);
      return this;
   }

   @Override
   public <T extends Annotation> Element removeAnnotationIf(Predicate<AnnotationValue<T>> predicate) {
      this.beanProducingElement.removeAnnotationIf(predicate);
      return this;
   }

   @Override
   public Element removeStereotype(String annotationType) {
      this.beanProducingElement.removeStereotype(annotationType);
      return this;
   }

   @Override
   public ClassElement getDeclaringClass() {
      Element beanProducingElement = this.beanProducingElement;
      return this.getDeclaringType(beanProducingElement);
   }

   private ClassElement getDeclaringType(Element beanProducingElement) {
      if (beanProducingElement instanceof ClassElement) {
         return (ClassElement)beanProducingElement;
      } else if (beanProducingElement instanceof MemberElement) {
         return ((MemberElement)beanProducingElement).getDeclaringType();
      } else {
         return beanProducingElement instanceof BeanElementBuilder ? ((BeanElementBuilder)beanProducingElement).getDeclaringElement() : this.beanTypeElement;
      }
   }

   @Override
   public Element getProducingElement() {
      return this.beanProducingElement;
   }

   @Override
   public Set<ClassElement> getBeanTypes() {
      String[] types = this.annotationMetadata.stringValues(Bean.class, "typed");
      if (!ArrayUtils.isNotEmpty(types)) {
         Optional<ClassElement> superType = this.beanTypeElement.getSuperType();
         Collection<ClassElement> interfaces = this.beanTypeElement.getInterfaces();
         if (!superType.isPresent() && interfaces.isEmpty()) {
            return Collections.singleton(this.beanTypeElement);
         } else {
            Set<ClassElement> beanTypes = new HashSet();
            beanTypes.add(this.beanTypeElement);
            this.populateBeanTypes(new HashSet(), beanTypes, (ClassElement)superType.orElse(null), interfaces);
            return Collections.unmodifiableSet(beanTypes);
         }
      } else {
         HashSet<ClassElement> classElements = new HashSet();

         for(String type : types) {
            this.visitorContext.getClassElement(type).ifPresent(classElements::add);
         }

         return Collections.unmodifiableSet(classElements);
      }
   }

   private void populateBeanTypes(Set<String> processedTypes, Set<ClassElement> beanTypes, ClassElement superType, Collection<ClassElement> interfaces) {
      for(ClassElement anInterface : interfaces) {
         String n = anInterface.getName();
         if (!processedTypes.contains(n)) {
            processedTypes.add(n);
            beanTypes.add(anInterface);
            this.populateBeanTypes(processedTypes, beanTypes, null, anInterface.getInterfaces());
         }
      }

      if (superType != null) {
         String n = superType.getName();
         if (!processedTypes.contains(n)) {
            processedTypes.add(n);
            beanTypes.add(superType);
            ClassElement next = (ClassElement)superType.getSuperType().orElse(null);
            this.populateBeanTypes(processedTypes, beanTypes, next, superType.getInterfaces());
         }
      }

   }

   @Override
   public Optional<String> getScope() {
      return this.annotationMetadata.getAnnotationNameByStereotype("javax.inject.Scope");
   }

   @Override
   public Collection<String> getQualifiers() {
      return Collections.unmodifiableList(this.annotationMetadata.getAnnotationNamesByStereotype("javax.inject.Qualifier"));
   }

   @Override
   public BeanElementBuilder addAssociatedBean(ClassElement type, VisitorContext visitorContext) {
      if (visitorContext instanceof BeanElementVisitorContext) {
         Element[] originatingElements = this.getOriginatingElements();
         return ((BeanElementVisitorContext)visitorContext).addAssociatedBean(originatingElements[0], type);
      } else {
         return BeanElement.super.addAssociatedBean(type, visitorContext);
      }
   }

   @Override
   public Element[] getOriginatingElements() {
      return this.originatingElements.getOriginatingElements();
   }

   @Internal
   private static final class AnnotationVisitData {
      final TypedElement memberBeanType;
      final String memberPropertyName;
      final MethodElement memberPropertyGetter;
      final String requiredValue;
      final String notEqualsValue;

      public AnnotationVisitData(
         TypedElement memberBeanType,
         String memberPropertyName,
         MethodElement memberPropertyGetter,
         @Nullable String requiredValue,
         @Nullable String notEqualsValue
      ) {
         this.memberBeanType = memberBeanType;
         this.memberPropertyName = memberPropertyName;
         this.memberPropertyGetter = memberPropertyGetter;
         this.requiredValue = requiredValue;
         this.notEqualsValue = notEqualsValue;
      }
   }

   private class FactoryMethodDef {
      private final Type factoryType;
      private final Element factoryMethod;
      private final String methodDescriptor;
      private final int factoryVar;

      public FactoryMethodDef(Type factoryType, Element factoryMethod, String methodDescriptor, int factoryVar) {
         this.factoryType = factoryType;
         this.factoryMethod = factoryMethod;
         this.methodDescriptor = methodDescriptor;
         this.factoryVar = factoryVar;
      }
   }

   @Internal
   private static final class FieldVisitData {
      final TypedElement beanType;
      final FieldElement fieldElement;
      final boolean requiresReflection;

      FieldVisitData(TypedElement beanType, FieldElement fieldElement, boolean requiresReflection) {
         this.beanType = beanType;
         this.fieldElement = fieldElement;
         this.requiresReflection = requiresReflection;
      }
   }

   private class InnerClassDef {
      private final ClassWriter innerClassWriter;
      private final String constructorInternalName;
      private final Type innerClassType;
      private final String innerClassName;

      public InnerClassDef(String interceptedConstructorWriterName, ClassWriter innerClassWriter, String constructorInternalName, Type innerClassType) {
         this.innerClassName = interceptedConstructorWriterName;
         this.innerClassWriter = innerClassWriter;
         this.constructorInternalName = constructorInternalName;
         this.innerClassType = innerClassType;
      }
   }

   @Internal
   public static final class MethodVisitData {
      private final TypedElement beanType;
      private final boolean requiresReflection;
      private final MethodElement methodElement;
      private final boolean postConstruct;
      private final boolean preDestroy;

      MethodVisitData(TypedElement beanType, MethodElement methodElement, boolean requiresReflection) {
         this.beanType = beanType;
         this.requiresReflection = requiresReflection;
         this.methodElement = methodElement;
         this.postConstruct = false;
         this.preDestroy = false;
      }

      MethodVisitData(TypedElement beanType, MethodElement methodElement, boolean requiresReflection, boolean postConstruct, boolean preDestroy) {
         this.beanType = beanType;
         this.requiresReflection = requiresReflection;
         this.methodElement = methodElement;
         this.postConstruct = postConstruct;
         this.preDestroy = preDestroy;
      }

      public MethodElement getMethodElement() {
         return this.methodElement;
      }

      public TypedElement getBeanType() {
         return this.beanType;
      }

      public boolean isRequiresReflection() {
         return this.requiresReflection;
      }

      public boolean isPostConstruct() {
         return this.postConstruct;
      }

      public boolean isPreDestroy() {
         return this.preDestroy;
      }
   }
}
