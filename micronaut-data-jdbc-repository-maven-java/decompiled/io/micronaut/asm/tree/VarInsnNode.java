package io.micronaut.asm.tree;

import io.micronaut.asm.MethodVisitor;
import java.util.Map;

public class VarInsnNode extends AbstractInsnNode {
   public int var;

   public VarInsnNode(int opcode, int var) {
      super(opcode);
      this.var = var;
   }

   public void setOpcode(int opcode) {
      this.opcode = opcode;
   }

   @Override
   public int getType() {
      return 2;
   }

   @Override
   public void accept(MethodVisitor methodVisitor) {
      methodVisitor.visitVarInsn(this.opcode, this.var);
      this.acceptAnnotations(methodVisitor);
   }

   @Override
   public AbstractInsnNode clone(Map<LabelNode, LabelNode> clonedLabels) {
      return new VarInsnNode(this.opcode, this.var).cloneAnnotations(this);
   }
}
