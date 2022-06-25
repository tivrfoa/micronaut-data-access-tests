package io.micronaut.core.graal;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectionConfig;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.util.CollectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

@Internal
public interface GraalReflectionConfigurer extends AnnotationMetadataProvider {
   String CLASS_SUFFIX = "$ReflectConfig";

   default void configure(GraalReflectionConfigurer.ReflectionConfigurationContext context) {
      AnnotationMetadata annotationMetadata = this.getAnnotationMetadata();

      for(AnnotationValue<ReflectionConfig> reflectConfig : annotationMetadata.getAnnotationValuesByType(ReflectionConfig.class)) {
         reflectConfig.stringValue("type")
            .ifPresent(
               className -> {
                  Class<?> t = context.findClassByName(className);
                  if (t != null) {
                     context.register(t);
                     Set<TypeHint.AccessType> accessType = CollectionUtils.setOf(reflectConfig.enumValues("accessType", TypeHint.AccessType.class));
                     if (accessType.contains(TypeHint.AccessType.ALL_PUBLIC_METHODS)) {
                        Method[] methods = t.getMethods();
      
                        for(Method method : methods) {
                           if (Modifier.isPublic(method.getModifiers())) {
                              context.register(method);
                           }
                        }
                     }
      
                     if (accessType.contains(TypeHint.AccessType.ALL_DECLARED_METHODS)) {
                        Method[] declaredMethods = t.getDeclaredMethods();
                        context.register(declaredMethods);
                     }
      
                     if (accessType.contains(TypeHint.AccessType.ALL_PUBLIC_FIELDS)) {
                        Field[] fields = t.getFields();
      
                        for(Field field : fields) {
                           if (Modifier.isPublic(field.getModifiers())) {
                              context.register(field);
                           }
                        }
                     }
      
                     if (accessType.contains(TypeHint.AccessType.ALL_DECLARED_FIELDS)) {
                        Field[] fields = t.getDeclaredFields();
                        context.register(fields);
                     }
      
                     if (accessType.contains(TypeHint.AccessType.ALL_PUBLIC_CONSTRUCTORS)) {
                        Constructor<?>[] constructors = t.getConstructors();
      
                        for(Constructor<?> constructor : constructors) {
                           if (Modifier.isPublic(constructor.getModifiers())) {
                              context.register(constructor);
                           }
                        }
                     }
      
                     if (accessType.contains(TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS)) {
                        Constructor<?>[] constructors = t.getDeclaredConstructors();
                        context.register(constructors);
                     }
      
                     for(AnnotationValue<ReflectionConfig.ReflectiveMethodConfig> mrc : reflectConfig.getAnnotations(
                        "methods", ReflectionConfig.ReflectiveMethodConfig.class
                     )) {
                        mrc.stringValue("name").ifPresent(n -> {
                           String[] typeNames = mrc.stringValues("parameterTypes");
                           Class<?>[] parameterTypes = new Class[typeNames.length];
      
                           for(int i = 0; i < typeNames.length; ++i) {
                              String typeName = typeNames[i];
                              Class<?> pt = context.findClassByName(typeName);
                              if (pt == null) {
                                 return;
                              }
      
                              parameterTypes[i] = pt;
                           }
      
                           if (n.equals("<init>")) {
                              ReflectionUtils.findConstructor(t, parameterTypes).ifPresent(xva$0 -> context.register(xva$0));
                           } else {
                              ReflectionUtils.findMethod(t, n, parameterTypes).ifPresent(xva$0 -> context.register(xva$0));
                           }
      
                        });
                     }
      
                     for(AnnotationValue<ReflectionConfig.ReflectiveFieldConfig> field : reflectConfig.getAnnotations(
                        "fields", ReflectionConfig.ReflectiveFieldConfig.class
                     )) {
                        field.stringValue("name").flatMap(n -> ReflectionUtils.findField(t, n)).ifPresent(xva$0 -> context.register(xva$0));
                     }
      
                  }
               }
            );
      }

   }

   public interface ReflectionConfigurationContext {
      @Nullable
      Class<?> findClassByName(@NonNull String name);

      void register(Class<?>... types);

      void register(Method... methods);

      void register(Field... fields);

      void register(Constructor<?>... constructors);
   }
}
