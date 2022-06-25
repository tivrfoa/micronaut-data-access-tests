package io.micronaut.asm.tree.analysis;

import io.micronaut.asm.Type;
import io.micronaut.asm.tree.AbstractInsnNode;
import io.micronaut.asm.tree.IincInsnNode;
import io.micronaut.asm.tree.InvokeDynamicInsnNode;
import io.micronaut.asm.tree.LabelNode;
import io.micronaut.asm.tree.MethodInsnNode;
import io.micronaut.asm.tree.MultiANewArrayInsnNode;
import io.micronaut.asm.tree.VarInsnNode;
import java.util.ArrayList;
import java.util.List;

public class Frame<V extends Value> {
   private static final int MAX_STACK_SIZE = 65536;
   private V returnValue;
   private V[] values;
   private int numLocals;
   private int numStack;
   private int maxStack;

   public Frame(int numLocals, int maxStack) {
      this.values = new Value[numLocals + (maxStack >= 0 ? maxStack : 4)];
      this.numLocals = numLocals;
      this.numStack = 0;
      this.maxStack = maxStack >= 0 ? maxStack : 65536;
   }

   public Frame(Frame<? extends V> frame) {
      this(frame.numLocals, frame.values.length - frame.numLocals);
      this.init(frame);
   }

   public Frame<V> init(Frame<? extends V> frame) {
      this.returnValue = frame.returnValue;
      if (this.values.length < frame.values.length) {
         this.values = (Value[])frame.values.clone();
      } else {
         System.arraycopy(frame.values, 0, this.values, 0, frame.values.length);
      }

      this.numLocals = frame.numLocals;
      this.numStack = frame.numStack;
      this.maxStack = frame.maxStack;
      return this;
   }

   public void initJumpTarget(int opcode, LabelNode target) {
   }

   public void setReturn(V v) {
      this.returnValue = v;
   }

   public int getLocals() {
      return this.numLocals;
   }

   public int getMaxStackSize() {
      return this.maxStack;
   }

   public V getLocal(int index) {
      if (index >= this.numLocals) {
         throw new IndexOutOfBoundsException("Trying to get an inexistant local variable " + index);
      } else {
         return this.values[index];
      }
   }

   public void setLocal(int index, V value) {
      if (index >= this.numLocals) {
         throw new IndexOutOfBoundsException("Trying to set an inexistant local variable " + index);
      } else {
         this.values[index] = value;
      }
   }

   public int getStackSize() {
      return this.numStack;
   }

   public V getStack(int index) {
      return this.values[this.numLocals + index];
   }

   public void setStack(int index, V value) {
      this.values[this.numLocals + index] = value;
   }

   public void clearStack() {
      this.numStack = 0;
   }

   public V pop() {
      if (this.numStack == 0) {
         throw new IndexOutOfBoundsException("Cannot pop operand off an empty stack.");
      } else {
         return this.values[this.numLocals + --this.numStack];
      }
   }

   public void push(V value) {
      if (this.numLocals + this.numStack >= this.values.length) {
         if (this.numLocals + this.numStack >= this.maxStack) {
            throw new IndexOutOfBoundsException("Insufficient maximum stack size.");
         }

         V[] oldValues = this.values;
         this.values = new Value[2 * this.values.length];
         System.arraycopy(oldValues, 0, this.values, 0, oldValues.length);
      }

      this.values[this.numLocals + this.numStack++] = value;
   }

   public void execute(AbstractInsnNode insn, Interpreter<V> interpreter) throws AnalyzerException {
      switch(insn.getOpcode()) {
         case 0:
         case 167:
         case 169:
            break;
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
            this.push(interpreter.newOperation(insn));
            break;
         case 19:
         case 20:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 68:
         case 69:
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 196:
         default:
            throw new AnalyzerException(insn, "Illegal opcode " + insn.getOpcode());
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
            this.push(interpreter.copyOperation(insn, this.getLocal(((VarInsnNode)insn).var)));
            break;
         case 46: {
         case 47: {
         case 48: {
         case 49: {
         case 50: {
         case 51: {
         case 52: {
         case 53: {
         case 96: {
         case 97: {
         case 98: {
         case 99: {
         case 100: {
         case 101: {
         case 102: {
         case 103: {
         case 104: {
         case 105: {
         case 106: {
         case 107: {
         case 108: {
         case 109: {
         case 110: {
         case 111: {
         case 112: {
         case 113: {
         case 114: {
         case 115: {
         case 120: {
         case 121: {
         case 122: {
         case 123: {
         case 124: {
         case 125: {
         case 126: {
         case 127: {
         case 128: {
         case 129: {
         case 130: {
         case 131: {
         case 148: {
         case 149: {
         case 150: {
         case 151: {
         case 152: {
            V value2 = this.pop();
            V value1 = this.pop();
            this.push(interpreter.binaryOperation(insn, value1, value2));
            break;
         }
         case 54: {
         case 55: {
         case 56: {
         case 57: {
         case 58: {
            V value1 = interpreter.copyOperation(insn, this.pop());
            int var = ((VarInsnNode)insn).var;
            this.setLocal(var, value1);
            if (value1.getSize() == 2) {
               this.setLocal(var + 1, interpreter.newEmptyValue(var + 1));
            }

            if (var > 0) {
               Value local = this.getLocal(var - 1);
               if (local != null && local.getSize() == 2) {
                  this.setLocal(var - 1, interpreter.newEmptyValue(var - 1));
               }
            }
            break;
         }
         case 79: {
         case 80: {
         case 81: {
         case 82: {
         case 83: {
         case 84: {
         case 85: {
         case 86: {
            V value3 = this.pop();
            V value2 = this.pop();
            V value1 = this.pop();
            interpreter.ternaryOperation(insn, value1, value2, value3);
            break;
         }
         case 87:
            if (this.pop().getSize() == 2) {
               throw new AnalyzerException(insn, "Illegal use of POP");
            }
            break;
         case 88:
            if (this.pop().getSize() == 1 && this.pop().getSize() != 1) {
               throw new AnalyzerException(insn, "Illegal use of POP2");
            }
            break;
         case 89: {
            V value1 = this.pop();
            if (value1.getSize() != 1) {
               throw new AnalyzerException(insn, "Illegal use of DUP");
            }

            this.push(value1);
            this.push(interpreter.copyOperation(insn, value1));
            break;
         }
         case 90: {
            V value1 = this.pop();
            V value2 = this.pop();
            if (value1.getSize() != 1 || value2.getSize() != 1) {
               throw new AnalyzerException(insn, "Illegal use of DUP_X1");
            }

            this.push(interpreter.copyOperation(insn, value1));
            this.push(value2);
            this.push(value1);
            break;
         }
         case 91: {
            V value1 = this.pop();
            if (value1.getSize() != 1 || !this.executeDupX2(insn, value1, interpreter)) {
               throw new AnalyzerException(insn, "Illegal use of DUP_X2");
            }
            break;
         }
         case 92: {
            V value1 = this.pop();
            if (value1.getSize() == 1) {
               V value2x = this.pop();
               if (value2x.getSize() != 1) {
                  throw new AnalyzerException(insn, "Illegal use of DUP2");
               }

               this.push(value2x);
               this.push(value1);
               this.push(interpreter.copyOperation(insn, value2x));
               this.push(interpreter.copyOperation(insn, value1));
            } else {
               this.push(value1);
               this.push(interpreter.copyOperation(insn, value1));
            }
            break;
         }
         case 93: {
            V value1 = this.pop();
            if (value1.getSize() == 1) {
               V value2x = this.pop();
               if (value2x.getSize() != 1) {
                  throw new AnalyzerException(insn, "Illegal use of DUP2_X1");
               }

               V value3x = this.pop();
               if (value3x.getSize() != 1) {
                  throw new AnalyzerException(insn, "Illegal use of DUP2_X1");
               }

               this.push(interpreter.copyOperation(insn, value2x));
               this.push(interpreter.copyOperation(insn, value1));
               this.push(value3x);
               this.push(value2x);
               this.push(value1);
            } else {
               V value2x = this.pop();
               if (value2x.getSize() != 1) {
                  throw new AnalyzerException(insn, "Illegal use of DUP2_X1");
               }

               this.push(interpreter.copyOperation(insn, value1));
               this.push(value2x);
               this.push(value1);
            }
            break;
         }
         case 94: {
            V value1 = this.pop();
            if (value1.getSize() == 1) {
               V value2x = this.pop();
               if (value2x.getSize() != 1) {
                  throw new AnalyzerException(insn, "Illegal use of DUP2_X2");
               }

               V value3x = this.pop();
               if (value3x.getSize() == 1) {
                  V value4 = this.pop();
                  if (value4.getSize() != 1) {
                     throw new AnalyzerException(insn, "Illegal use of DUP2_X2");
                  }

                  this.push(interpreter.copyOperation(insn, value2x));
                  this.push(interpreter.copyOperation(insn, value1));
                  this.push(value4);
                  this.push(value3x);
                  this.push(value2x);
                  this.push(value1);
               } else {
                  this.push(interpreter.copyOperation(insn, value2x));
                  this.push(interpreter.copyOperation(insn, value1));
                  this.push(value3x);
                  this.push(value2x);
                  this.push(value1);
               }
            } else if (!this.executeDupX2(insn, value1, interpreter)) {
               throw new AnalyzerException(insn, "Illegal use of DUP2_X2");
            }
            break;
         }
         case 95: {
            V value2 = this.pop();
            V value1 = this.pop();
            if (value1.getSize() != 1 || value2.getSize() != 1) {
               throw new AnalyzerException(insn, "Illegal use of SWAP");
            }

            this.push(interpreter.copyOperation(insn, value2));
            this.push(interpreter.copyOperation(insn, value1));
            break;
         }
         case 116:
         case 117:
         case 118:
         case 119:
            this.push(interpreter.unaryOperation(insn, this.pop()));
            break;
         case 132: {
            int var = ((IincInsnNode)insn).var;
            this.setLocal(var, interpreter.unaryOperation(insn, this.getLocal(var)));
            break;
         }
         case 133:
         case 134:
         case 135:
         case 136:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
            this.push(interpreter.unaryOperation(insn, this.pop()));
            break;
         case 153:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
            interpreter.unaryOperation(insn, this.pop());
            break;
         case 159: {
         case 160: {
         case 161: {
         case 162: {
         case 163: {
         case 164: {
         case 165: {
         case 166: {
         case 181: {
            V value2 = this.pop();
            V value1 = this.pop();
            interpreter.binaryOperation(insn, value1, value2);
            break;
         }
         case 168:
            this.push(interpreter.newOperation(insn));
            break;
         case 170:
         case 171:
            interpreter.unaryOperation(insn, this.pop());
            break;
         case 172: {
         case 173: {
         case 174: {
         case 175: {
         case 176: {
            V value1 = this.pop();
            interpreter.unaryOperation(insn, value1);
            interpreter.returnOperation(insn, value1, this.returnValue);
            break;
         }
         case 177:
            if (this.returnValue != null) {
               throw new AnalyzerException(insn, "Incompatible return type");
            }
            break;
         case 178:
            this.push(interpreter.newOperation(insn));
            break;
         case 179:
            interpreter.unaryOperation(insn, this.pop());
            break;
         case 180:
            this.push(interpreter.unaryOperation(insn, this.pop()));
            break;
         case 182:
         case 183:
         case 184:
         case 185:
            this.executeInvokeInsn(insn, ((MethodInsnNode)insn).desc, interpreter);
            break;
         case 186:
            this.executeInvokeInsn(insn, ((InvokeDynamicInsnNode)insn).desc, interpreter);
            break;
         case 187:
            this.push(interpreter.newOperation(insn));
            break;
         case 188:
         case 189:
         case 190:
            this.push(interpreter.unaryOperation(insn, this.pop()));
            break;
         case 191:
            interpreter.unaryOperation(insn, this.pop());
            break;
         case 192:
         case 193:
            this.push(interpreter.unaryOperation(insn, this.pop()));
            break;
         case 194:
         case 195:
            interpreter.unaryOperation(insn, this.pop());
            break;
         case 197:
            List<V> valueList = new ArrayList();

            for(int i = ((MultiANewArrayInsnNode)insn).dims; i > 0; --i) {
               valueList.add(0, this.pop());
            }

            this.push(interpreter.naryOperation(insn, valueList));
            break;
         case 198:
         case 199:
            interpreter.unaryOperation(insn, this.pop());
      }

   }

   private boolean executeDupX2(AbstractInsnNode insn, V value1, Interpreter<V> interpreter) throws AnalyzerException {
      V value2 = this.pop();
      if (value2.getSize() == 1) {
         V value3 = this.pop();
         if (value3.getSize() == 1) {
            this.push(interpreter.copyOperation(insn, value1));
            this.push(value3);
            this.push(value2);
            this.push(value1);
            return true;
         } else {
            return false;
         }
      } else {
         this.push(interpreter.copyOperation(insn, value1));
         this.push(value2);
         this.push(value1);
         return true;
      }
   }

   private void executeInvokeInsn(AbstractInsnNode insn, String methodDescriptor, Interpreter<V> interpreter) throws AnalyzerException {
      ArrayList<V> valueList = new ArrayList();

      for(int i = Type.getArgumentTypes(methodDescriptor).length; i > 0; --i) {
         valueList.add(0, this.pop());
      }

      if (insn.getOpcode() != 184 && insn.getOpcode() != 186) {
         valueList.add(0, this.pop());
      }

      if (Type.getReturnType(methodDescriptor) == Type.VOID_TYPE) {
         interpreter.naryOperation(insn, valueList);
      } else {
         this.push(interpreter.naryOperation(insn, valueList));
      }

   }

   public boolean merge(Frame<? extends V> frame, Interpreter<V> interpreter) throws AnalyzerException {
      if (this.numStack != frame.numStack) {
         throw new AnalyzerException(null, "Incompatible stack heights");
      } else {
         boolean changed = false;

         for(int i = 0; i < this.numLocals + this.numStack; ++i) {
            V v = interpreter.merge(this.values[i], frame.values[i]);
            if (!v.equals(this.values[i])) {
               this.values[i] = v;
               changed = true;
            }
         }

         return changed;
      }
   }

   public boolean merge(Frame<? extends V> frame, boolean[] localsUsed) {
      boolean changed = false;

      for(int i = 0; i < this.numLocals; ++i) {
         if (!localsUsed[i] && !this.values[i].equals(frame.values[i])) {
            this.values[i] = frame.values[i];
            changed = true;
         }
      }

      return changed;
   }

   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();

      for(int i = 0; i < this.getLocals(); ++i) {
         stringBuilder.append(this.getLocal(i));
      }

      stringBuilder.append(' ');

      for(int i = 0; i < this.getStackSize(); ++i) {
         stringBuilder.append(this.getStack(i).toString());
      }

      return stringBuilder.toString();
   }
}
