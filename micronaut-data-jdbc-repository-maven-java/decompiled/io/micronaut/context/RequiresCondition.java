package io.micronaut.context;

import groovy.lang.GroovySystem;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.context.condition.OperatingSystem;
import io.micronaut.context.condition.TrueCondition;
import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.file.FileSystemResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.core.version.SemanticVersion;
import io.micronaut.core.version.VersionUtils;
import io.micronaut.inject.BeanConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kotlin.KotlinVersion;

public class RequiresCondition implements Condition {
   public static final String MEMBER_PROPERTY = "property";
   public static final String MEMBER_NOT_EQUALS = "notEquals";
   public static final String MEMBER_DEFAULT_VALUE = "defaultValue";
   public static final String MEMBER_PATTERN = "pattern";
   public static final String MEMBER_MISSING_PROPERTY = "missingProperty";
   public static final String MEMBER_ENV = "env";
   public static final String MEMBER_NOT_ENV = "notEnv";
   public static final String MEMBER_CONDITION = "condition";
   public static final String MEMBER_SDK = "sdk";
   public static final String MEMBER_VERSION = "version";
   public static final String MEMBER_MISSING_CLASSES = "missing";
   public static final String MEMBER_RESOURCES = "resources";
   public static final String MEMBER_CONFIGURATION = "configuration";
   public static final String MEMBER_CLASSES = "classes";
   public static final String MEMBER_ENTITIES = "entities";
   public static final String MEMBER_BEANS = "beans";
   public static final String MEMBER_MISSING_BEANS = "missingBeans";
   public static final String MEMBER_OS = "os";
   public static final String MEMBER_NOT_OS = "notOs";
   public static final String MEMBER_BEAN = "bean";
   public static final String MEMBER_BEAN_PROPERTY = "beanProperty";
   private final AnnotationMetadata annotationMetadata;

   public RequiresCondition(AnnotationMetadata annotationMetadata) {
      this.annotationMetadata = annotationMetadata;
   }

   @Override
   public boolean matches(ConditionContext context) {
      AnnotationMetadataProvider component = context.getComponent();
      boolean isBeanReference = component instanceof BeanDefinitionReference;
      List<AnnotationValue<Requires>> requirements = this.annotationMetadata.getAnnotationValuesByType(Requires.class);
      if (!requirements.isEmpty()) {
         if (isBeanReference) {
            for(AnnotationValue<Requires> requirement : requirements) {
               this.processPreStartRequirements(context, requirement);
               if (context.isFailing()) {
                  return false;
               }
            }
         } else {
            for(AnnotationValue<Requires> requires : requirements) {
               this.processPostStartRequirements(context, requires);
               if (context.isFailing()) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   protected boolean matchesConfiguration(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("configuration")) {
         String configurationName = (String)requirements.stringValue("configuration").orElse(null);
         if (StringUtils.isEmpty(configurationName)) {
            return true;
         } else {
            BeanContext beanContext = context.getBeanContext();
            String minimumVersion = (String)requirements.stringValue("version").orElse(null);
            Optional<BeanConfiguration> beanConfiguration = beanContext.findBeanConfiguration(configurationName);
            if (!beanConfiguration.isPresent()) {
               context.fail("Required configuration [" + configurationName + "] is not active");
               return false;
            } else {
               String version = ((BeanConfiguration)beanConfiguration.get()).getVersion();
               if (version != null && StringUtils.isNotEmpty(minimumVersion)) {
                  boolean result = SemanticVersion.isAtLeast(version, minimumVersion);
                  context.fail(
                     "Required configuration [" + configurationName + "] version requirements not met. Required: " + minimumVersion + ", Current: " + version
                  );
                  return result;
               } else {
                  return true;
               }
            }
         }
      } else {
         return true;
      }
   }

   private void processPreStartRequirements(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (this.matchesPresenceOfClasses(context, requirements)) {
         if (this.matchesAbsenceOfClasses(context, requirements)) {
            if (this.matchesEnvironment(context, requirements)) {
               if (this.matchesPresenceOfEntities(context, requirements)) {
                  if (this.matchesProperty(context, requirements)) {
                     if (this.matchesMissingProperty(context, requirements)) {
                        if (this.matchesConfiguration(context, requirements)) {
                           if (this.matchesSdk(context, requirements)) {
                              if (this.matchesPresenceOfResources(context, requirements)) {
                                 if (this.matchesCurrentOs(context, requirements)) {
                                    this.matchesPresenceOfClasses(context, requirements, "beans");
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void processPostStartRequirements(ConditionContext context, AnnotationValue<Requires> requirements) {
      this.processPreStartRequirements(context, requirements);
      if (!context.isFailing()) {
         if (this.matchesPresenceOfBeans(context, requirements)) {
            if (this.matchesAbsenceOfBeans(context, requirements)) {
               this.matchesCustomConditions(context, requirements);
            }
         }
      }
   }

   private boolean matchesProperty(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("property")) {
         String property = (String)requirements.stringValue("property").orElse(null);
         if (StringUtils.isNotEmpty(property)) {
            String value = (String)requirements.stringValue().orElse(null);
            BeanContext beanContext = context.getBeanContext();
            if (!(beanContext instanceof PropertyResolver)) {
               context.fail("Bean requires property but BeanContext does not support property resolution");
               return false;
            }

            PropertyResolver propertyResolver = (PropertyResolver)beanContext;
            String defaultValue = (String)requirements.stringValue("defaultValue").orElse(null);
            if (!propertyResolver.containsProperties(property) && StringUtils.isEmpty(defaultValue)) {
               boolean hasNotEquals = requirements.contains("notEquals");
               if (hasNotEquals) {
                  return true;
               }

               context.fail("Required property [" + property + "] with value [" + value + "] not present");
               return false;
            }

            if (StringUtils.isNotEmpty(value)) {
               String resolved = this.resolvePropertyValue(property, propertyResolver, defaultValue);
               boolean result = resolved != null && resolved.equals(value);
               if (!result) {
                  context.fail("Property [" + property + "] with value [" + resolved + "] does not equal required value: " + value);
               }

               return result;
            }

            if (requirements.contains("notEquals")) {
               String notEquals = (String)requirements.stringValue("notEquals").orElse(null);
               String resolved = this.resolvePropertyValue(property, propertyResolver, defaultValue);
               boolean result = resolved == null || !resolved.equals(notEquals);
               if (!result) {
                  context.fail("Property [" + property + "] with value [" + resolved + "] should not equal: " + notEquals);
               }

               return result;
            }

            if (requirements.contains("pattern")) {
               String pattern = (String)requirements.stringValue("pattern").orElse(null);
               if (pattern == null) {
                  return true;
               }

               String resolved = this.resolvePropertyValue(property, propertyResolver, defaultValue);
               boolean result = resolved != null && resolved.matches(pattern);
               if (!result) {
                  context.fail("Property [" + property + "] with value [" + resolved + "] does not match required pattern: " + pattern);
               }

               return result;
            }
         }
      }

      return true;
   }

   private String resolvePropertyValue(String property, PropertyResolver propertyResolver, String defaultValue) {
      return (String)propertyResolver.getProperty(property, ConversionContext.STRING).orElse(defaultValue);
   }

   private boolean matchesMissingProperty(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("missingProperty")) {
         String property = (String)requirements.stringValue("missingProperty").orElse(null);
         if (StringUtils.isNotEmpty(property)) {
            BeanContext beanContext = context.getBeanContext();
            if (beanContext instanceof PropertyResolver) {
               PropertyResolver propertyResolver = (PropertyResolver)beanContext;
               if (propertyResolver.containsProperties(property)) {
                  context.fail("Property [" + property + "] present");
                  return false;
               }
            }
         }
      }

      return true;
   }

   private boolean matchesEnvironment(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("env")) {
         String[] env = requirements.stringValues("env");
         if (ArrayUtils.isNotEmpty(env)) {
            BeanContext beanContext = context.getBeanContext();
            if (beanContext instanceof ApplicationContext) {
               ApplicationContext applicationContext = (ApplicationContext)beanContext;
               Environment environment = applicationContext.getEnvironment();
               Set<String> activeNames = environment.getActiveNames();
               boolean result = Arrays.stream(env).anyMatch(activeNames::contains);
               if (!result) {
                  context.fail("None of the required environments [" + ArrayUtils.toString(env) + "] are active: " + activeNames);
               }

               return result;
            }
         }
      } else if (requirements.contains("notEnv")) {
         String[] env = requirements.stringValues("notEnv");
         if (ArrayUtils.isNotEmpty(env)) {
            BeanContext beanContext = context.getBeanContext();
            if (beanContext instanceof ApplicationContext) {
               ApplicationContext applicationContext = (ApplicationContext)beanContext;
               Environment environment = applicationContext.getEnvironment();
               Set<String> activeNames = environment.getActiveNames();
               boolean result = Arrays.stream(env).noneMatch(activeNames::contains);
               if (!result) {
                  context.fail("Disallowed environments [" + ArrayUtils.toString(env) + "] are active: " + activeNames);
               }

               return result;
            }
         }

         return true;
      }

      return true;
   }

   private boolean matchesCustomConditions(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("condition")) {
         AnnotationClassValue<?> annotationClassValue = (AnnotationClassValue)requirements.annotationClassValue("condition").orElse(null);
         if (annotationClassValue == null) {
            return true;
         } else {
            Object instance = annotationClassValue.getInstance().orElse(null);
            if (instance instanceof Condition) {
               boolean conditionResult = ((Condition)instance).matches(context);
               if (!conditionResult) {
                  context.fail("Custom condition [" + instance.getClass() + "] failed evaluation");
               }

               return conditionResult;
            } else {
               Class<?> conditionClass = (Class)annotationClassValue.getType().orElse(null);
               if (conditionClass != null && conditionClass != TrueCondition.class && Condition.class.isAssignableFrom(conditionClass)) {
                  Optional<? extends Condition> condition = InstantiationUtils.tryInstantiate(conditionClass);
                  if (condition.isPresent()) {
                     boolean conditionResult = ((Condition)condition.get()).matches(context);
                     if (!conditionResult) {
                        context.fail("Custom condition [" + conditionClass + "] failed evaluation");
                     }

                     return conditionResult;
                  } else {
                     Optional<Constructor<?>> constructor = ReflectionUtils.findConstructor(conditionClass, Object.class, Object.class);
                     boolean conditionResult = constructor.flatMap(ctor -> InstantiationUtils.tryInstantiate(ctor, null, null)).flatMap(obj -> {
                        Optional<Method> method = ReflectionUtils.findMethod(obj.getClass(), "call", ConditionContext.class);
                        if (method.isPresent()) {
                           Object result = ReflectionUtils.invokeMethod(obj, (Method)method.get(), context);
                           if (result instanceof Boolean) {
                              return Optional.of((Boolean)result);
                           }
                        }

                        return Optional.empty();
                     }).orElse(false);
                     if (!conditionResult) {
                        context.fail("Custom condition [" + conditionClass + "] failed evaluation");
                     }

                     return conditionResult;
                  }
               } else {
                  return true;
               }
            }
         }
      } else {
         return !context.isFailing();
      }
   }

   private boolean matchesSdk(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("sdk")) {
         Requires.Sdk sdk = (Requires.Sdk)requirements.enumValue("sdk", Requires.Sdk.class).orElse(null);
         String version = (String)requirements.stringValue("version").orElse(null);
         if (sdk != null && StringUtils.isNotEmpty(version)) {
            switch(sdk) {
               case GROOVY:
                  String groovyVersion = GroovySystem.getVersion();
                  boolean versionMatch = SemanticVersion.isAtLeast(groovyVersion, version);
                  if (!versionMatch) {
                     context.fail("Groovy version [" + groovyVersion + "] must be at least " + version);
                  }

                  return versionMatch;
               case KOTLIN:
                  String kotlinVersion = KotlinVersion.CURRENT.toString();
                  boolean isSupported = SemanticVersion.isAtLeast(kotlinVersion, version);
                  if (!isSupported) {
                     context.fail("Kotlin version [" + kotlinVersion + "] must be at least " + version);
                  }

                  return isSupported;
               case JAVA:
                  String javaVersion = CachedEnvironment.getProperty("java.version");

                  try {
                     boolean result = SemanticVersion.isAtLeast(javaVersion, version);
                     if (!result) {
                        context.fail("Java version [" + javaVersion + "] must be at least " + version);
                     }

                     return result;
                  } catch (Exception var13) {
                     if (javaVersion != null) {
                        int majorVersion = this.resolveJavaMajorVersion(javaVersion);
                        int requiredVersion = this.resolveJavaMajorVersion(version);
                        if (majorVersion >= requiredVersion) {
                           return true;
                        }

                        context.fail("Java major version [" + majorVersion + "] must be at least " + requiredVersion);
                     } else {
                        int requiredVersion = this.resolveJavaMajorVersion(version);
                        context.fail("Java major version must be at least " + requiredVersion);
                     }

                     return context.isFailing();
                  }
               default:
                  boolean versionCheck = VersionUtils.isAtLeastMicronautVersion(version);
                  if (!versionCheck) {
                     context.fail("Micronaut version [" + VersionUtils.MICRONAUT_VERSION + "] must be at least " + version);
                  }

                  return versionCheck;
            }
         }
      }

      return true;
   }

   private int resolveJavaMajorVersion(String javaVersion) {
      int majorVersion = 0;
      if (javaVersion.indexOf(46) > -1) {
         String[] tokens = javaVersion.split("\\.");
         String first = tokens[0];
         if (first.length() == 1) {
            majorVersion = first.charAt(0);
            if (Character.isDigit(majorVersion) && majorVersion == 49 && tokens.length > 1) {
               majorVersion = tokens[1].charAt(0);
            }
         } else {
            try {
               majorVersion = Integer.parseInt(first);
            } catch (NumberFormatException var7) {
            }
         }
      } else if (javaVersion.length() == 1) {
         char ch = javaVersion.charAt(0);
         if (Character.isDigit(ch)) {
            majorVersion = ch;
         }
      } else {
         try {
            majorVersion = Integer.parseInt(javaVersion);
         } catch (NumberFormatException var6) {
         }
      }

      return majorVersion;
   }

   private boolean matchesPresenceOfClasses(ConditionContext context, AnnotationValue<Requires> convertibleValues) {
      return this.matchesPresenceOfClasses(context, convertibleValues, "classes");
   }

   private boolean matchesAbsenceOfClasses(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("missing")) {
         AnnotationClassValue[] classValues = requirements.annotationClassValues("missing");
         if (!ArrayUtils.isNotEmpty(classValues)) {
            return this.matchAbsenceOfClassNames(context, requirements);
         }

         for(AnnotationClassValue classValue : classValues) {
            if (classValue.getType().isPresent()) {
               context.fail("Class [" + classValue.getName() + "] is not absent");
               return false;
            }
         }
      }

      return true;
   }

   private boolean matchAbsenceOfClassNames(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("missing")) {
         String[] classNameArray = requirements.stringValues("missing");
         ClassLoader classLoader = context.getBeanContext().getClassLoader();

         for(String name : classNameArray) {
            if (ClassUtils.isPresent(name, classLoader)) {
               context.fail("Class [" + name + "] is not absent");
               return false;
            }
         }
      }

      return true;
   }

   private boolean matchesPresenceOfClasses(ConditionContext context, AnnotationValue<Requires> requirements, String attr) {
      if (requirements.contains(attr)) {
         AnnotationClassValue[] classValues = requirements.annotationClassValues(attr);

         for(AnnotationClassValue classValue : classValues) {
            if (!classValue.getType().isPresent()) {
               context.fail("Class [" + classValue.getName() + "] is not present");
               return false;
            }
         }
      }

      return true;
   }

   private boolean matchesPresenceOfEntities(ConditionContext context, AnnotationValue<Requires> annotationValue) {
      if (annotationValue.contains("entities")) {
         Optional<AnnotationClassValue[]> classNames = annotationValue.get("entities", AnnotationClassValue[].class);
         if (classNames.isPresent()) {
            BeanContext beanContext = context.getBeanContext();
            if (beanContext instanceof ApplicationContext) {
               ApplicationContext applicationContext = (ApplicationContext)beanContext;
               AnnotationClassValue[] classValues = (AnnotationClassValue[])classNames.get();

               for(AnnotationClassValue<?> classValue : classValues) {
                  Optional<? extends Class<?>> entityType = classValue.getType();
                  if (!entityType.isPresent()) {
                     context.fail("Annotation type [" + classValue.getName() + "] not present on classpath");
                     return false;
                  }

                  Environment environment = applicationContext.getEnvironment();
                  Class annotationType = (Class)entityType.get();
                  if (!environment.scan(annotationType).findFirst().isPresent()) {
                     context.fail("No entities found in packages [" + String.join(", ", environment.getPackages()) + "] for annotation: " + annotationType);
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   private boolean matchesPresenceOfBeans(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("beans") || requirements.contains("bean")) {
         Class[] beans = requirements.classValues("beans");
         if (requirements.contains("bean")) {
            Class<?> memberBean = (Class)requirements.classValue("bean").orElse(null);
            if (memberBean != null) {
               beans = ArrayUtils.concat((Class[])beans, (Class[])(memberBean));
            }
         }

         if (ArrayUtils.isNotEmpty(beans)) {
            BeanContext beanContext = context.getBeanContext();

            for(Class type : beans) {
               if (!beanContext.containsBean(type)) {
                  context.fail("No bean of type [" + type + "] present within context");
                  return false;
               }
            }
         }
      }

      return true;
   }

   private boolean matchesAbsenceOfBeans(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("missingBeans")) {
         Class[] missingBeans = requirements.classValues("missingBeans");
         AnnotationMetadataProvider component = context.getComponent();
         if (ArrayUtils.isNotEmpty(missingBeans) && component instanceof BeanDefinition) {
            BeanDefinition bd = (BeanDefinition)component;
            DefaultBeanContext beanContext = (DefaultBeanContext)context.getBeanContext();

            for(Class<?> type : missingBeans) {
               for(BeanDefinition<?> beanDefinition : beanContext.findBeanCandidates(context.getBeanResolutionContext(), Argument.of(type), bd, true)) {
                  if (!beanDefinition.isAbstract()) {
                     context.fail("Existing bean [" + beanDefinition.getName() + "] of type [" + type + "] registered in context");
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   private boolean matchesPresenceOfResources(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("resources")) {
         String[] resourcePaths = requirements.stringValues("resources");
         if (ArrayUtils.isNotEmpty(resourcePaths)) {
            BeanContext beanContext = context.getBeanContext();
            List<ResourceLoader> resourceLoaders;
            if (beanContext instanceof ApplicationContext) {
               ResourceLoader resourceLoader = ((ApplicationContext)beanContext).getEnvironment();
               resourceLoaders = Arrays.asList(resourceLoader, FileSystemResourceLoader.defaultLoader());
            } else {
               resourceLoaders = Arrays.asList(ClassPathResourceLoader.defaultLoader(beanContext.getClassLoader()), FileSystemResourceLoader.defaultLoader());
            }

            ResourceResolver resolver = new ResourceResolver(resourceLoaders);

            for(String resourcePath : resourcePaths) {
               if (!resolver.getResource(resourcePath).isPresent()) {
                  context.fail("Resource [" + resourcePath + "] does not exist");
                  return false;
               }
            }
         }
      }

      return true;
   }

   private boolean matchesCurrentOs(ConditionContext context, AnnotationValue<Requires> requirements) {
      if (requirements.contains("os")) {
         List<Requires.Family> os = Arrays.asList(requirements.enumValues("os", Requires.Family.class));
         Requires.Family currentOs = OperatingSystem.getCurrent().getFamily();
         if (!os.isEmpty() && !os.contains(currentOs)) {
            context.fail("The current operating system [" + currentOs.name() + "] is not one of the required systems [" + os + "]");
            return false;
         }
      } else if (requirements.contains("notOs")) {
         Requires.Family currentOs = OperatingSystem.getCurrent().getFamily();
         List<Requires.Family> notOs = Arrays.asList(requirements.enumValues("notOs", Requires.Family.class));
         if (!notOs.isEmpty() && notOs.contains(currentOs)) {
            context.fail("The current operating system [" + currentOs.name() + "] is one of the disallowed systems [" + notOs + "]");
            return false;
         }
      }

      return true;
   }
}
