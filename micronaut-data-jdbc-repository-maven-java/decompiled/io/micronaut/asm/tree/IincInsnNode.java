package io.micronaut.asm.tree;

import io.micronaut.asm.MethodVisitor;
import java.util.Map;

public class IincInsnNode extends AbstractInsnNode {
   public int var;
   public int incr;

   public IincInsnNode(int var, int incr) {
      super(132);
      this.var = var;
      this.incr = incr;
   }

   @Override
   public int getType() {
      return 10;
   }

   @Override
   public void accept(MethodVisitor methodVisitor) {
      methodVisitor.visitIincInsn(this.var, this.incr);
      this.acceptAnnotations(methodVisitor);
   }

   @Override
   public AbstractInsnNode clone(Map<LabelNode, LabelNode> clonedLabels) {
      return new IincInsnNode(this.var, this.incr).cloneAnnotations(this);
   }
}
