package io.micronaut.inject.writer;

import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataReference;
import io.micronaut.inject.annotation.AnnotationMetadataWriter;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.ast.Element;
import java.util.HashMap;
import java.util.Map;

@Internal
public abstract class AbstractAnnotationMetadataWriter extends AbstractClassFileWriter {
   public static final String FIELD_ANNOTATION_METADATA = "$ANNOTATION_METADATA";
   protected final Type targetClassType;
   protected final AnnotationMetadata annotationMetadata;
   protected final Map<String, GeneratorAdapter> loadTypeMethods = new HashMap();
   protected final Map<String, Integer> defaults = new HashMap();
   private final boolean writeAnnotationDefault;

   protected AbstractAnnotationMetadataWriter(
      String className, OriginatingElements originatingElements, AnnotationMetadata annotationMetadata, boolean writeAnnotationDefaults
   ) {
      super(originatingElements);
      this.targetClassType = getTypeReferenceForName(className, new String[0]);
      this.annotationMetadata = annotationMetadata;
      this.writeAnnotationDefault = writeAnnotationDefaults;
   }

   protected AbstractAnnotationMetadataWriter(
      String className, Element originatingElement, AnnotationMetadata annotationMetadata, boolean writeAnnotationDefaults
   ) {
      super(originatingElement);
      this.targetClassType = getTypeReferenceForName(className, new String[0]);
      this.annotationMetadata = annotationMetadata;
      this.writeAnnotationDefault = writeAnnotationDefaults;
   }

   protected void writeGetAnnotationMetadataMethod(ClassWriter classWriter) {
      GeneratorAdapter annotationMetadataMethod = this.beginAnnotationMetadataMethod(classWriter);
      annotationMetadataMethod.loadThis();
      if (this.annotationMetadata == AnnotationMetadata.EMPTY_METADATA) {
         annotationMetadataMethod.getStatic(Type.getType(AnnotationMetadata.class), "EMPTY_METADATA", Type.getType(AnnotationMetadata.class));
      } else if (this.annotationMetadata instanceof AnnotationMetadataReference) {
         AnnotationMetadataReference reference = (AnnotationMetadataReference)this.annotationMetadata;
         String className = reference.getClassName();
         annotationMetadataMethod.getStatic(getTypeReferenceForName(className, new String[0]), "$ANNOTATION_METADATA", Type.getType(AnnotationMetadata.class));
      } else {
         annotationMetadataMethod.getStatic(this.targetClassType, "$ANNOTATION_METADATA", Type.getType(AnnotationMetadata.class));
      }

      annotationMetadataMethod.returnValue();
      annotationMetadataMethod.visitMaxs(1, 1);
      annotationMetadataMethod.visitEnd();
   }

   @NonNull
   protected GeneratorAdapter beginAnnotationMetadataMethod(ClassWriter classWriter) {
      return this.startPublicMethod(classWriter, "getAnnotationMetadata", AnnotationMetadata.class.getName(), new String[0]);
   }

   protected void writeAnnotationMetadataStaticInitializer(ClassWriter classWriter) {
      this.writeAnnotationMetadataStaticInitializer(classWriter, this.defaults);
   }

   protected void writeAnnotationMetadataStaticInitializer(ClassWriter classWriter, Map<String, Integer> defaults) {
      if (!(this.annotationMetadata instanceof AnnotationMetadataReference)) {
         GeneratorAdapter staticInit = this.visitStaticInitializer(classWriter);
         staticInit.visitCode();
         if (this.writeAnnotationDefault && this.annotationMetadata instanceof DefaultAnnotationMetadata) {
            DefaultAnnotationMetadata dam = (DefaultAnnotationMetadata)this.annotationMetadata;
            AnnotationMetadataWriter.writeAnnotationDefaults(this.targetClassType, classWriter, staticInit, dam, defaults, this.loadTypeMethods);
         }

         staticInit.visitLabel(new Label());
         this.initializeAnnotationMetadata(staticInit, classWriter, defaults);
         staticInit.visitInsn(177);
         staticInit.visitMaxs(1, 1);
         staticInit.visitEnd();
      }

   }

   protected void initializeAnnotationMetadata(GeneratorAdapter staticInit, ClassWriter classWriter, Map<String, Integer> defaults) {
      Type annotationMetadataType = Type.getType(AnnotationMetadata.class);
      classWriter.visitField(25, "$ANNOTATION_METADATA", annotationMetadataType.getDescriptor(), null, null);
      if (this.annotationMetadata instanceof DefaultAnnotationMetadata) {
         AnnotationMetadataWriter.instantiateNewMetadata(
            this.targetClassType, classWriter, staticInit, (DefaultAnnotationMetadata)this.annotationMetadata, defaults, this.loadTypeMethods
         );
      } else if (this.annotationMetadata instanceof AnnotationMetadataHierarchy) {
         AnnotationMetadataWriter.instantiateNewMetadataHierarchy(
            this.targetClassType, classWriter, staticInit, (AnnotationMetadataHierarchy)this.annotationMetadata, defaults, this.loadTypeMethods
         );
      } else {
         staticInit.getStatic(Type.getType(AnnotationMetadata.class), "EMPTY_METADATA", Type.getType(AnnotationMetadata.class));
      }

      staticInit.putStatic(this.targetClassType, "$ANNOTATION_METADATA", annotationMetadataType);
   }
}
