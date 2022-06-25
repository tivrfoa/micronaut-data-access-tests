package io.micronaut.asm.commons;

import io.micronaut.asm.Attribute;
import io.micronaut.asm.ByteVector;
import io.micronaut.asm.ClassReader;
import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;

public final class ModuleTargetAttribute extends Attribute {
   public String platform;

   public ModuleTargetAttribute(String platform) {
      super("ModuleTarget");
      this.platform = platform;
   }

   public ModuleTargetAttribute() {
      this(null);
   }

   @Override
   protected Attribute read(ClassReader classReader, int offset, int length, char[] charBuffer, int codeOffset, Label[] labels) {
      return new ModuleTargetAttribute(classReader.readUTF8(offset, charBuffer));
   }

   @Override
   protected ByteVector write(ClassWriter classWriter, byte[] code, int codeLength, int maxStack, int maxLocals) {
      ByteVector byteVector = new ByteVector();
      byteVector.putShort(this.platform == null ? 0 : classWriter.newUTF8(this.platform));
      return byteVector;
   }
}
