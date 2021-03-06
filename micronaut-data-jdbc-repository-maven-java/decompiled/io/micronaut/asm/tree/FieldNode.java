package io.micronaut.asm.tree;

import io.micronaut.asm.AnnotationVisitor;
import io.micronaut.asm.Attribute;
import io.micronaut.asm.ClassVisitor;
import io.micronaut.asm.FieldVisitor;
import io.micronaut.asm.TypePath;
import java.util.List;

public class FieldNode extends FieldVisitor {
   public int access;
   public String name;
   public String desc;
   public String signature;
   public Object value;
   public List<AnnotationNode> visibleAnnotations;
   public List<AnnotationNode> invisibleAnnotations;
   public List<TypeAnnotationNode> visibleTypeAnnotations;
   public List<TypeAnnotationNode> invisibleTypeAnnotations;
   public List<Attribute> attrs;

   public FieldNode(int access, String name, String descriptor, String signature, Object value) {
      this(589824, access, name, descriptor, signature, value);
      if (this.getClass() != FieldNode.class) {
         throw new IllegalStateException();
      }
   }

   public FieldNode(int api, int access, String name, String descriptor, String signature, Object value) {
      super(api);
      this.access = access;
      this.name = name;
      this.desc = descriptor;
      this.signature = signature;
      this.value = value;
   }

   @Override
   public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
      AnnotationNode annotation = new AnnotationNode(descriptor);
      if (visible) {
         this.visibleAnnotations = Util.add(this.visibleAnnotations, annotation);
      } else {
         this.invisibleAnnotations = Util.add(this.invisibleAnnotations, annotation);
      }

      return annotation;
   }

   @Override
   public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
      TypeAnnotationNode typeAnnotation = new TypeAnnotationNode(typeRef, typePath, descriptor);
      if (visible) {
         this.visibleTypeAnnotations = Util.add(this.visibleTypeAnnotations, typeAnnotation);
      } else {
         this.invisibleTypeAnnotations = Util.add(this.invisibleTypeAnnotations, typeAnnotation);
      }

      return typeAnnotation;
   }

   @Override
   public void visitAttribute(Attribute attribute) {
      this.attrs = Util.add(this.attrs, attribute);
   }

   @Override
   public void visitEnd() {
   }

   public void check(int api) {
      if (api == 262144) {
         if (this.visibleTypeAnnotations != null && !this.visibleTypeAnnotations.isEmpty()) {
            throw new UnsupportedClassVersionException();
         }

         if (this.invisibleTypeAnnotations != null && !this.invisibleTypeAnnotations.isEmpty()) {
            throw new UnsupportedClassVersionException();
         }
      }

   }

   public void accept(ClassVisitor classVisitor) {
      FieldVisitor fieldVisitor = classVisitor.visitField(this.access, this.name, this.desc, this.signature, this.value);
      if (fieldVisitor != null) {
         if (this.visibleAnnotations != null) {
            int i = 0;

            for(int n = this.visibleAnnotations.size(); i < n; ++i) {
               AnnotationNode annotation = (AnnotationNode)this.visibleAnnotations.get(i);
               annotation.accept(fieldVisitor.visitAnnotation(annotation.desc, true));
            }
         }

         if (this.invisibleAnnotations != null) {
            int i = 0;

            for(int n = this.invisibleAnnotations.size(); i < n; ++i) {
               AnnotationNode annotation = (AnnotationNode)this.invisibleAnnotations.get(i);
               annotation.accept(fieldVisitor.visitAnnotation(annotation.desc, false));
            }
         }

         if (this.visibleTypeAnnotations != null) {
            int i = 0;

            for(int n = this.visibleTypeAnnotations.size(); i < n; ++i) {
               TypeAnnotationNode typeAnnotation = (TypeAnnotationNode)this.visibleTypeAnnotations.get(i);
               typeAnnotation.accept(fieldVisitor.visitTypeAnnotation(typeAnnotation.typeRef, typeAnnotation.typePath, typeAnnotation.desc, true));
            }
         }

         if (this.invisibleTypeAnnotations != null) {
            int i = 0;

            for(int n = this.invisibleTypeAnnotations.size(); i < n; ++i) {
               TypeAnnotationNode typeAnnotation = (TypeAnnotationNode)this.invisibleTypeAnnotations.get(i);
               typeAnnotation.accept(fieldVisitor.visitTypeAnnotation(typeAnnotation.typeRef, typeAnnotation.typePath, typeAnnotation.desc, false));
            }
         }

         if (this.attrs != null) {
            int i = 0;

            for(int n = this.attrs.size(); i < n; ++i) {
               fieldVisitor.visitAttribute((Attribute)this.attrs.get(i));
            }
         }

         fieldVisitor.visitEnd();
      }
   }
}
