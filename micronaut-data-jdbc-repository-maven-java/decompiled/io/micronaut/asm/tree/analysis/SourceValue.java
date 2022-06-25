package io.micronaut.asm.tree.analysis;

import io.micronaut.asm.tree.AbstractInsnNode;
import java.util.Set;

public class SourceValue implements Value {
   public final int size;
   public final Set<AbstractInsnNode> insns;

   public SourceValue(int size) {
      this(size, new SmallSet<AbstractInsnNode>());
   }

   public SourceValue(int size, AbstractInsnNode insnNode) {
      this.size = size;
      this.insns = new SmallSet<AbstractInsnNode>(insnNode);
   }

   public SourceValue(int size, Set<AbstractInsnNode> insnSet) {
      this.size = size;
      this.insns = insnSet;
   }

   @Override
   public int getSize() {
      return this.size;
   }

   public boolean equals(Object value) {
      if (!(value instanceof SourceValue)) {
         return false;
      } else {
         SourceValue sourceValue = (SourceValue)value;
         return this.size == sourceValue.size && this.insns.equals(sourceValue.insns);
      }
   }

   public int hashCode() {
      return this.insns.hashCode();
   }
}
