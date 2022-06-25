package io.micronaut.inject.writer;

import io.micronaut.asm.Type;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.Toggleable;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.ast.TypedElement;
import io.micronaut.inject.configuration.ConfigurationMetadataBuilder;
import io.micronaut.inject.visitor.VisitorContext;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public interface BeanDefinitionVisitor extends OriginatingElements, Toggleable {
   String PROXY_SUFFIX = "$Intercepted";

   @Nullable
   Element getOriginatingElement();

   void visitBeanFactoryMethod(ClassElement factoryClass, MethodElement factoryMethod);

   void visitBeanFactoryMethod(ClassElement factoryClass, MethodElement factoryMethod, ParameterElement[] parameters);

   void visitBeanFactoryField(ClassElement factoryClass, FieldElement factoryField);

   void visitBeanDefinitionConstructor(MethodElement constructor, boolean requiresReflection, VisitorContext visitorContext);

   void visitDefaultConstructor(AnnotationMetadata annotationMetadata, VisitorContext visitorContext);

   @NonNull
   String getBeanDefinitionReferenceClassName();

   boolean isInterface();

   boolean isSingleton();

   void visitBeanDefinitionInterface(Class<? extends BeanDefinition> interfaceType);

   void visitSuperBeanDefinition(String name);

   void visitSuperBeanDefinitionFactory(String beanName);

   String getBeanTypeName();

   Type getProvidedType();

   void setValidated(boolean validated);

   void setInterceptedType(String typeName);

   Optional<Type> getInterceptedType();

   boolean isValidated();

   String getBeanDefinitionName();

   void visitBeanDefinitionEnd();

   void writeTo(File compilationDir) throws IOException;

   void accept(ClassWriterOutputVisitor visitor) throws IOException;

   void visitSetterValue(TypedElement declaringType, MethodElement methodElement, boolean requiresReflection, boolean isOptional);

   void visitPostConstructMethod(TypedElement declaringType, MethodElement methodElement, boolean requiresReflection, VisitorContext visitorContext);

   void visitPreDestroyMethod(TypedElement beanType, MethodElement methodElement, boolean requiresReflection, VisitorContext visitorContext);

   void visitMethodInjectionPoint(TypedElement beanType, MethodElement methodElement, boolean requiresReflection, VisitorContext visitorContext);

   int visitExecutableMethod(TypedElement declaringBean, MethodElement methodElement, VisitorContext visitorContext);

   void visitFieldInjectionPoint(TypedElement declaringType, FieldElement fieldElement, boolean requiresReflection);

   void visitAnnotationMemberPropertyInjectionPoint(
      TypedElement annotationMemberBeanType, String annotationMemberProperty, @Nullable String requiredValue, @Nullable String notEqualsValue
   );

   void visitFieldValue(TypedElement declaringType, FieldElement fieldElement, boolean requiresReflection, boolean isOptional);

   String getPackageName();

   String getBeanSimpleName();

   AnnotationMetadata getAnnotationMetadata();

   void visitConfigBuilderField(
      ClassElement type, String field, AnnotationMetadata annotationMetadata, ConfigurationMetadataBuilder<?> metadataBuilder, boolean isInterface
   );

   void visitConfigBuilderMethod(
      ClassElement type, String methodName, AnnotationMetadata annotationMetadata, ConfigurationMetadataBuilder<?> metadataBuilder, boolean isInterface
   );

   void visitConfigBuilderMethod(
      String prefix, ClassElement returnType, String methodName, @Nullable ClassElement paramType, Map<String, ClassElement> generics, String path
   );

   void visitConfigBuilderDurationMethod(String prefix, ClassElement returnType, String methodName, String path);

   void visitConfigBuilderEnd();

   default boolean requiresMethodProcessing() {
      return false;
   }

   void setRequiresMethodProcessing(boolean shouldPreProcess);

   void visitTypeArguments(Map<String, Map<String, ClassElement>> typeArguments);

   @NonNull
   default ClassElement[] getTypeArguments() {
      return new ClassElement[0];
   }
}
