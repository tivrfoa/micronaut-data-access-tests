package io.micronaut.inject.writer;

import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.asm.commons.Method;
import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.annotation.DefaultScope;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.DefaultArgument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.AdvisedBeanType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import io.micronaut.inject.annotation.AnnotationMetadataReference;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;

@Internal
public class BeanDefinitionReferenceWriter extends AbstractAnnotationMetadataWriter {
   public static final String REF_SUFFIX = "$Reference";
   private static final Method BEAN_DEFINITION_REF_CLASS_CONSTRUCTOR = new Method(
      "<init>",
      getConstructorDescriptor(
         new Class[]{
            String.class,
            String.class,
            AnnotationMetadata.class,
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
   private final String beanTypeName;
   private final String beanDefinitionName;
   private final String beanDefinitionClassInternalName;
   private final String beanDefinitionReferenceClassName;
   private final Type interceptedType;
   private final Type providedType;
   private boolean contextScope = false;
   private boolean requiresMethodProcessing;

   public BeanDefinitionReferenceWriter(BeanDefinitionVisitor visitor) {
      super(visitor.getBeanDefinitionName() + "$Reference", visitor, visitor.getAnnotationMetadata(), true);
      this.providedType = visitor.getProvidedType();
      this.beanTypeName = visitor.getBeanTypeName();
      this.beanDefinitionName = visitor.getBeanDefinitionName();
      this.beanDefinitionReferenceClassName = this.beanDefinitionName + "$Reference";
      this.beanDefinitionClassInternalName = getInternalName(this.beanDefinitionName) + "$Reference";
      this.interceptedType = (Type)visitor.getInterceptedType().orElse(null);
   }

   @Override
   public void accept(ClassWriterOutputVisitor outputVisitor) throws IOException {
      OutputStream outputStream = outputVisitor.visitClass(this.getBeanDefinitionQualifiedClassName(), this.getOriginatingElements());
      Throwable var3 = null;

      try {
         ClassWriter classWriter = this.generateClassBytes();
         outputStream.write(classWriter.toByteArray());
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (outputStream != null) {
            if (var3 != null) {
               try {
                  outputStream.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               outputStream.close();
            }
         }

      }

      outputVisitor.visitServiceDescriptor(BeanDefinitionReference.class, this.beanDefinitionReferenceClassName, this.getOriginatingElement());
   }

   public void setContextScope(boolean contextScope) {
      this.contextScope = contextScope;
   }

   public void setRequiresMethodProcessing(boolean shouldPreProcess) {
      this.requiresMethodProcessing = shouldPreProcess;
   }

   public String getBeanDefinitionQualifiedClassName() {
      String newClassName = this.beanDefinitionName;
      if (newClassName.endsWith("[]")) {
         newClassName = newClassName.substring(0, newClassName.length() - 2);
      }

      return newClassName + "$Reference";
   }

   private ClassWriter generateClassBytes() {
      ClassWriter classWriter = new ClassWriter(1);
      Type superType = Type.getType(AbstractInitializableBeanDefinitionReference.class);
      String[] interfaceInternalNames;
      if (this.interceptedType != null) {
         interfaceInternalNames = new String[]{Type.getType(AdvisedBeanType.class).getInternalName()};
      } else {
         interfaceInternalNames = StringUtils.EMPTY_STRING_ARRAY;
      }

      this.startService(classWriter, BeanDefinitionReference.class.getName(), this.beanDefinitionClassInternalName, superType, interfaceInternalNames);
      Type beanDefinitionType = getTypeReferenceForName(this.beanDefinitionName, new String[0]);
      this.writeAnnotationMetadataStaticInitializer(classWriter);
      GeneratorAdapter cv = this.startConstructor(classWriter);
      cv.loadThis();
      cv.push(this.beanTypeName);
      cv.push(this.beanDefinitionName);
      if (this.annotationMetadata == AnnotationMetadata.EMPTY_METADATA || this.annotationMetadata.isEmpty()) {
         cv.getStatic(Type.getType(AnnotationMetadata.class), "EMPTY_METADATA", Type.getType(AnnotationMetadata.class));
      } else if (this.annotationMetadata instanceof AnnotationMetadataReference) {
         AnnotationMetadataReference reference = (AnnotationMetadataReference)this.annotationMetadata;
         String className = reference.getClassName();
         cv.getStatic(getTypeReferenceForName(className, new String[0]), "$ANNOTATION_METADATA", Type.getType(AnnotationMetadata.class));
      } else {
         cv.getStatic(this.targetClassType, "$ANNOTATION_METADATA", Type.getType(AnnotationMetadata.class));
      }

      cv.push(this.annotationMetadata.hasDeclaredStereotype(Primary.class));
      cv.push(this.contextScope);
      cv.push(this.annotationMetadata.hasStereotype(Requires.class));
      cv.push(this.providedType.getSort() == 9 || DefaultArgument.CONTAINER_TYPES.stream().anyMatch(clazz -> clazz.getName().equals(this.beanTypeName)));
      cv.push(
         this.annotationMetadata.hasDeclaredStereotype("javax.inject.Singleton")
            || !this.annotationMetadata.hasDeclaredStereotype("javax.inject.Scope")
               && this.annotationMetadata.hasDeclaredStereotype(DefaultScope.class)
               && this.annotationMetadata
                  .stringValue(DefaultScope.class)
                  .map(t -> t.equals(Singleton.class.getName()) || t.equals("javax.inject.Singleton"))
                  .orElse(false)
      );
      cv.push(this.annotationMetadata.hasDeclaredStereotype(ConfigurationReader.class));
      cv.push(this.annotationMetadata.hasDeclaredAnnotation(Bean.class) && this.annotationMetadata.stringValues(Bean.class, "typed").length > 0);
      cv.push(this.requiresMethodProcessing);
      cv.invokeConstructor(Type.getType(AbstractInitializableBeanDefinitionReference.class), BEAN_DEFINITION_REF_CLASS_CONSTRUCTOR);
      cv.visitInsn(177);
      cv.visitMaxs(2, 1);
      GeneratorAdapter loadMethod = this.startPublicMethodZeroArgs(classWriter, BeanDefinition.class, "load");
      this.pushNewInstance(loadMethod, beanDefinitionType);
      loadMethod.returnValue();
      loadMethod.visitMaxs(2, 1);
      GeneratorAdapter getBeanDefinitionType = this.startPublicMethodZeroArgs(classWriter, Class.class, "getBeanDefinitionType");
      getBeanDefinitionType.push(beanDefinitionType);
      getBeanDefinitionType.returnValue();
      getBeanDefinitionType.visitMaxs(2, 1);
      GeneratorAdapter getBeanType = this.startPublicMethodZeroArgs(classWriter, Class.class, "getBeanType");
      getBeanType.push(this.providedType);
      getBeanType.returnValue();
      getBeanType.visitMaxs(2, 1);
      if (this.interceptedType != null) {
         super.implementInterceptedTypeMethod(this.interceptedType, classWriter);
      }

      for(GeneratorAdapter generatorAdapter : this.loadTypeMethods.values()) {
         generatorAdapter.visitMaxs(3, 1);
      }

      return classWriter;
   }
}
