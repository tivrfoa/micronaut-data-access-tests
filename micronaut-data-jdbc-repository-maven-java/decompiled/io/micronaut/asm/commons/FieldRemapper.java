package io.micronaut.asm.commons;

import io.micronaut.asm.AnnotationVisitor;
import io.micronaut.asm.FieldVisitor;
import io.micronaut.asm.TypePath;

public class FieldRemapper extends FieldVisitor {
   protected final Remapper remapper;

   public FieldRemapper(FieldVisitor fieldVisitor, Remapper remapper) {
      this(589824, fieldVisitor, remapper);
   }

   protected FieldRemapper(int api, FieldVisitor fieldVisitor, Remapper remapper) {
      super(api, fieldVisitor);
      this.remapper = remapper;
   }

   @Override
   public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
      AnnotationVisitor annotationVisitor = super.visitAnnotation(this.remapper.mapDesc(descriptor), visible);
      return annotationVisitor == null ? null : this.createAnnotationRemapper(descriptor, annotationVisitor);
   }

   @Override
   public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
      AnnotationVisitor annotationVisitor = super.visitTypeAnnotation(typeRef, typePath, this.remapper.mapDesc(descriptor), visible);
      return annotationVisitor == null ? null : this.createAnnotationRemapper(descriptor, annotationVisitor);
   }

   @Deprecated
   protected AnnotationVisitor createAnnotationRemapper(AnnotationVisitor annotationVisitor) {
      return new AnnotationRemapper(this.api, null, annotationVisitor, this.remapper);
   }

   protected AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor annotationVisitor) {
      return new AnnotationRemapper(this.api, descriptor, annotationVisitor, this.remapper).orDeprecatedValue(this.createAnnotationRemapper(annotationVisitor));
   }
}
