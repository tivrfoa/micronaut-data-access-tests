package io.micronaut.asm.tree.analysis;

import io.micronaut.asm.tree.AbstractInsnNode;

public class AnalyzerException extends Exception {
   private static final long serialVersionUID = 3154190448018943333L;
   public final transient AbstractInsnNode node;

   public AnalyzerException(AbstractInsnNode insn, String message) {
      super(message);
      this.node = insn;
   }

   public AnalyzerException(AbstractInsnNode insn, String message, Throwable cause) {
      super(message, cause);
      this.node = insn;
   }

   public AnalyzerException(AbstractInsnNode insn, String message, Object expected, Value actual) {
      super((message == null ? "Expected " : message + ": expected ") + expected + ", but found " + actual);
      this.node = insn;
   }
}
