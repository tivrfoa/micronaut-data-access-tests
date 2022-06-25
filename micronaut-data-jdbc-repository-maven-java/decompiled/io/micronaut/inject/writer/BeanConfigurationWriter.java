package io.micronaut.inject.writer;

import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.context.AbstractBeanConfiguration;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.BeanConfiguration;
import io.micronaut.inject.ast.Element;
import java.io.IOException;
import java.io.OutputStream;

@Internal
public class BeanConfigurationWriter extends AbstractAnnotationMetadataWriter {
   public static final String CLASS_SUFFIX = "$BeanConfiguration";
   private final String packageName;
   private final String configurationClassName;
   private final String configurationClassInternalName;

   public BeanConfigurationWriter(String packageName, Element originatingElement, AnnotationMetadata annotationMetadata) {
      super(packageName + '.' + "$BeanConfiguration", originatingElement, annotationMetadata, true);
      this.packageName = packageName;
      this.configurationClassName = this.targetClassType.getClassName();
      this.configurationClassInternalName = this.targetClassType.getInternalName();
   }

   @Override
   public void accept(ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {
      OutputStream outputStream = classWriterOutputVisitor.visitClass(this.configurationClassName, this.getOriginatingElements());
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

      classWriterOutputVisitor.visitServiceDescriptor(BeanConfiguration.class, this.configurationClassName, this.getOriginatingElement());
   }

   private ClassWriter generateClassBytes() {
      ClassWriter classWriter = new ClassWriter(1);

      try {
         Class<AbstractBeanConfiguration> superType = AbstractBeanConfiguration.class;
         Type beanConfigurationType = Type.getType(superType);
         this.startService(classWriter, BeanConfiguration.class, this.configurationClassInternalName, beanConfigurationType);
         this.writeAnnotationMetadataStaticInitializer(classWriter);
         this.writeConstructor(classWriter);
         this.writeGetAnnotationMetadataMethod(classWriter);
      } catch (NoSuchMethodException var4) {
         throw new ClassGenerationException("Error generating configuration class. Incompatible JVM or Micronaut version?: " + var4.getMessage(), var4);
      }

      for(GeneratorAdapter method : this.loadTypeMethods.values()) {
         method.visitMaxs(3, 1);
         method.visitEnd();
      }

      return classWriter;
   }

   private void writeConstructor(ClassWriter classWriter) throws NoSuchMethodException {
      GeneratorAdapter cv = this.startConstructor(classWriter);
      cv.loadThis();
      cv.push(this.packageName);
      this.invokeConstructor(cv, AbstractBeanConfiguration.class, new Class[]{String.class});
      cv.visitInsn(177);
      cv.visitMaxs(2, 1);
   }
}
