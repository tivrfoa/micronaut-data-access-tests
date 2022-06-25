package io.micronaut.asm.commons;

import io.micronaut.asm.Attribute;
import io.micronaut.asm.ByteVector;
import io.micronaut.asm.ClassReader;
import io.micronaut.asm.ClassWriter;
import io.micronaut.asm.Label;
import java.util.ArrayList;
import java.util.List;

public final class ModuleHashesAttribute extends Attribute {
   public String algorithm;
   public List<String> modules;
   public List<byte[]> hashes;

   public ModuleHashesAttribute(String algorithm, List<String> modules, List<byte[]> hashes) {
      super("ModuleHashes");
      this.algorithm = algorithm;
      this.modules = modules;
      this.hashes = hashes;
   }

   public ModuleHashesAttribute() {
      this(null, null, null);
   }

   @Override
   protected Attribute read(ClassReader classReader, int offset, int length, char[] charBuffer, int codeAttributeOffset, Label[] labels) {
      String hashAlgorithm = classReader.readUTF8(offset, charBuffer);
      int currentOffset = offset + 2;
      int numModules = classReader.readUnsignedShort(currentOffset);
      currentOffset += 2;
      ArrayList<String> moduleList = new ArrayList(numModules);
      ArrayList<byte[]> hashList = new ArrayList(numModules);

      for(int i = 0; i < numModules; ++i) {
         String module = classReader.readModule(currentOffset, charBuffer);
         currentOffset += 2;
         moduleList.add(module);
         int hashLength = classReader.readUnsignedShort(currentOffset);
         currentOffset += 2;
         byte[] hash = new byte[hashLength];

         for(int j = 0; j < hashLength; ++j) {
            hash[j] = (byte)classReader.readByte(currentOffset);
            ++currentOffset;
         }

         hashList.add(hash);
      }

      return new ModuleHashesAttribute(hashAlgorithm, moduleList, hashList);
   }

   @Override
   protected ByteVector write(ClassWriter classWriter, byte[] code, int codeLength, int maxStack, int maxLocals) {
      ByteVector byteVector = new ByteVector();
      byteVector.putShort(classWriter.newUTF8(this.algorithm));
      if (this.modules == null) {
         byteVector.putShort(0);
      } else {
         int numModules = this.modules.size();
         byteVector.putShort(numModules);

         for(int i = 0; i < numModules; ++i) {
            String module = (String)this.modules.get(i);
            byte[] hash = (byte[])this.hashes.get(i);
            byteVector.putShort(classWriter.newModule(module)).putShort(hash.length).putByteArray(hash, 0, hash.length);
         }
      }

      return byteVector;
   }
}
