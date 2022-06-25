package io.micronaut.asm.commons;

import io.micronaut.asm.Attribute;
import io.micronaut.asm.ByteVector;
import io.micronaut.asm.ClassReader;
import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;

public final class ModuleResolutionAttribute extends Attribute {
   public static final int RESOLUTION_DO_NOT_RESOLVE_BY_DEFAULT = 1;
   public static final int RESOLUTION_WARN_DEPRECATED = 2;
   public static final int RESOLUTION_WARN_DEPRECATED_FOR_REMOVAL = 4;
   public static final int RESOLUTION_WARN_INCUBATING = 8;
   public int resolution;

   public ModuleResolutionAttribute(int resolution) {
      super("ModuleResolution");
      this.resolution = resolution;
   }

   public ModuleResolutionAttribute() {
      this(0);
   }

   @Override
   protected Attribute read(ClassReader classReader, int offset, int length, char[] charBuffer, int codeOffset, Label[] labels) {
      return new ModuleResolutionAttribute(classReader.readUnsignedShort(offset));
   }

   @Override
   protected ByteVector write(ClassWriter classWriter, byte[] code, int codeLength, int maxStack, int maxLocals) {
      ByteVector byteVector = new ByteVector();
      byteVector.putShort(this.resolution);
      return byteVector;
   }
}
