package io.micronaut.asm.tree;

import io.micronaut.asm.MethodVisitor;
import java.util.Map;

public class TypeInsnNode extends AbstractInsnNode {
   public String desc;

   public TypeInsnNode(int opcode, String descriptor) {
      super(opcode);
      this.desc = descriptor;
   }

   public void setOpcode(int opcode) {
      this.opcode = opcode;
   }

   @Override
   public int getType() {
      return 3;
   }

   @Override
   public void accept(MethodVisitor methodVisitor) {
      methodVisitor.visitTypeInsn(this.opcode, this.desc);
      this.acceptAnnotations(methodVisitor);
   }

   @Override
   public AbstractInsnNode clone(Map<LabelNode, LabelNode> clonedLabels) {
      return new TypeInsnNode(this.opcode, this.desc).cloneAnnotations(this);
   }
}
