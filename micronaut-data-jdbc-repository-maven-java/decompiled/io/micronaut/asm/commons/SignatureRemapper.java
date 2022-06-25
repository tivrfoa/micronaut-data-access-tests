package io.micronaut.asm.commons;

import io.micronaut.asm.signature.SignatureVisitor;
import java.util.ArrayList;

public class SignatureRemapper extends SignatureVisitor {
   private final SignatureVisitor signatureVisitor;
   private final Remapper remapper;
   private ArrayList<String> classNames = new ArrayList();

   public SignatureRemapper(SignatureVisitor signatureVisitor, Remapper remapper) {
      this(589824, signatureVisitor, remapper);
   }

   protected SignatureRemapper(int api, SignatureVisitor signatureVisitor, Remapper remapper) {
      super(api);
      this.signatureVisitor = signatureVisitor;
      this.remapper = remapper;
   }

   @Override
   public void visitClassType(String name) {
      this.classNames.add(name);
      this.signatureVisitor.visitClassType(this.remapper.mapType(name));
   }

   @Override
   public void visitInnerClassType(String name) {
      String outerClassName = (String)this.classNames.remove(this.classNames.size() - 1);
      String className = outerClassName + '$' + name;
      this.classNames.add(className);
      String remappedOuter = this.remapper.mapType(outerClassName) + '$';
      String remappedName = this.remapper.mapType(className);
      int index = remappedName.startsWith(remappedOuter) ? remappedOuter.length() : remappedName.lastIndexOf(36) + 1;
      this.signatureVisitor.visitInnerClassType(remappedName.substring(index));
   }

   @Override
   public void visitFormalTypeParameter(String name) {
      this.signatureVisitor.visitFormalTypeParameter(name);
   }

   @Override
   public void visitTypeVariable(String name) {
      this.signatureVisitor.visitTypeVariable(name);
   }

   @Override
   public SignatureVisitor visitArrayType() {
      this.signatureVisitor.visitArrayType();
      return this;
   }

   @Override
   public void visitBaseType(char descriptor) {
      this.signatureVisitor.visitBaseType(descriptor);
   }

   @Override
   public SignatureVisitor visitClassBound() {
      this.signatureVisitor.visitClassBound();
      return this;
   }

   @Override
   public SignatureVisitor visitExceptionType() {
      this.signatureVisitor.visitExceptionType();
      return this;
   }

   @Override
   public SignatureVisitor visitInterface() {
      this.signatureVisitor.visitInterface();
      return this;
   }

   @Override
   public SignatureVisitor visitInterfaceBound() {
      this.signatureVisitor.visitInterfaceBound();
      return this;
   }

   @Override
   public SignatureVisitor visitParameterType() {
      this.signatureVisitor.visitParameterType();
      return this;
   }

   @Override
   public SignatureVisitor visitReturnType() {
      this.signatureVisitor.visitReturnType();
      return this;
   }

   @Override
   public SignatureVisitor visitSuperclass() {
      this.signatureVisitor.visitSuperclass();
      return this;
   }

   @Override
   public void visitTypeArgument() {
      this.signatureVisitor.visitTypeArgument();
   }

   @Override
   public SignatureVisitor visitTypeArgument(char wildcard) {
      this.signatureVisitor.visitTypeArgument(wildcard);
      return this;
   }

   @Override
   public void visitEnd() {
      this.signatureVisitor.visitEnd();
      this.classNames.remove(this.classNames.size() - 1);
   }
}
