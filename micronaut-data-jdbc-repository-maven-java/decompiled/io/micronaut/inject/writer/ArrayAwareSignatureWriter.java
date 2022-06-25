package io.micronaut.inject.writer;

import io.micronaut.asm.signature.SignatureVisitor;
import io.micronaut.core.annotation.Internal;

@Internal
final class ArrayAwareSignatureWriter extends SignatureVisitor {
   private final StringBuilder stringBuilder = new StringBuilder();
   private boolean hasFormals;
   private boolean hasParameters;
   private int argumentStack;

   public ArrayAwareSignatureWriter() {
      super(458752);
   }

   @Override
   public void visitFormalTypeParameter(final String name) {
      if (!this.hasFormals) {
         this.hasFormals = true;
         this.stringBuilder.append('<');
      }

      this.stringBuilder.append(name);
      this.stringBuilder.append(':');
   }

   @Override
   public SignatureVisitor visitClassBound() {
      return this;
   }

   @Override
   public SignatureVisitor visitInterfaceBound() {
      this.stringBuilder.append(':');
      return this;
   }

   @Override
   public SignatureVisitor visitSuperclass() {
      this.endFormals();
      return this;
   }

   @Override
   public SignatureVisitor visitInterface() {
      return this;
   }

   @Override
   public SignatureVisitor visitParameterType() {
      this.endFormals();
      if (!this.hasParameters) {
         this.hasParameters = true;
         this.stringBuilder.append('(');
      }

      return this;
   }

   @Override
   public SignatureVisitor visitReturnType() {
      this.endFormals();
      if (!this.hasParameters) {
         this.stringBuilder.append('(');
      }

      this.stringBuilder.append(')');
      return this;
   }

   @Override
   public SignatureVisitor visitExceptionType() {
      this.stringBuilder.append('^');
      return this;
   }

   @Override
   public void visitBaseType(final char descriptor) {
      this.stringBuilder.append(descriptor);
      this.argumentStack *= 2;
   }

   @Override
   public void visitTypeVariable(final String name) {
      this.stringBuilder.append('T');
      this.stringBuilder.append(name);
      this.stringBuilder.append(';');
   }

   @Override
   public SignatureVisitor visitArrayType() {
      this.stringBuilder.append('[');
      return this;
   }

   @Override
   public void visitClassType(final String name) {
      this.stringBuilder.append('L');
      this.stringBuilder.append(name);
      this.argumentStack *= 2;
   }

   @Override
   public void visitInnerClassType(final String name) {
      this.endArguments();
      this.stringBuilder.append('.');
      this.stringBuilder.append(name);
      this.argumentStack *= 2;
   }

   @Override
   public void visitTypeArgument() {
      if (this.argumentStack % 2 == 0) {
         this.argumentStack |= 1;
         this.stringBuilder.append('<');
      }

      this.stringBuilder.append('*');
   }

   public ArrayAwareSignatureWriter visitTypeArgument(final char wildcard) {
      if (this.argumentStack % 2 == 0) {
         this.argumentStack |= 1;
         this.stringBuilder.append('<');
      }

      if (wildcard != '=') {
         this.stringBuilder.append(wildcard);
      }

      return this;
   }

   public void visitEndArray() {
      this.endArguments();
   }

   @Override
   public void visitEnd() {
      this.endArguments();
      this.stringBuilder.append(';');
   }

   public String toString() {
      return this.stringBuilder.toString();
   }

   private void endFormals() {
      if (this.hasFormals) {
         this.hasFormals = false;
         this.stringBuilder.append('>');
      }

   }

   private void endArguments() {
      if (this.argumentStack % 2 == 1) {
         this.stringBuilder.append('>');
      }

      this.argumentStack /= 2;
   }
}
