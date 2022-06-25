package com.fasterxml.jackson.core.io;

import java.math.BigDecimal;
import java.util.Arrays;

public final class BigDecimalParser {
   private final char[] chars;

   BigDecimalParser(char[] chars) {
      this.chars = chars;
   }

   public static BigDecimal parse(String valueStr) {
      return parse(valueStr.toCharArray());
   }

   public static BigDecimal parse(char[] chars, int off, int len) {
      if (off > 0 || len != chars.length) {
         chars = Arrays.copyOfRange(chars, off, off + len);
      }

      return parse(chars);
   }

   public static BigDecimal parse(char[] chars) {
      int len = chars.length;

      try {
         return len < 500 ? new BigDecimal(chars) : new BigDecimalParser(chars).parseBigDecimal(len / 10);
      } catch (NumberFormatException var4) {
         String desc = var4.getMessage();
         if (desc == null) {
            desc = "Not a valid number representation";
         }

         throw new NumberFormatException("Value \"" + new String(chars) + "\" can not be represented as `java.math.BigDecimal`, reason: " + desc);
      }
   }

   private BigDecimal parseBigDecimal(int splitLen) {
      boolean numHasSign = false;
      boolean expHasSign = false;
      boolean neg = false;
      int numIdx = 0;
      int expIdx = -1;
      int dotIdx = -1;
      int scale = 0;
      int len = this.chars.length;

      for(int i = 0; i < len; ++i) {
         char c = this.chars[i];
         switch(c) {
            case '+':
               if (expIdx >= 0) {
                  if (expHasSign) {
                     throw new NumberFormatException("Multiple signs in exponent");
                  }

                  expHasSign = true;
               } else {
                  if (numHasSign) {
                     throw new NumberFormatException("Multiple signs in number");
                  }

                  numHasSign = true;
                  numIdx = i + 1;
               }
               break;
            case '-':
               if (expIdx >= 0) {
                  if (expHasSign) {
                     throw new NumberFormatException("Multiple signs in exponent");
                  }

                  expHasSign = true;
               } else {
                  if (numHasSign) {
                     throw new NumberFormatException("Multiple signs in number");
                  }

                  numHasSign = true;
                  neg = true;
                  numIdx = i + 1;
               }
               break;
            case '.':
               if (dotIdx >= 0) {
                  throw new NumberFormatException("Multiple decimal points");
               }

               dotIdx = i;
               break;
            case 'E':
            case 'e':
               if (expIdx >= 0) {
                  throw new NumberFormatException("Multiple exponent markers");
               }

               expIdx = i;
               break;
            default:
               if (dotIdx >= 0 && expIdx == -1) {
                  ++scale;
               }
         }
      }

      int exp = 0;
      int numEndIdx;
      if (expIdx >= 0) {
         numEndIdx = expIdx;
         String expStr = new String(this.chars, expIdx + 1, len - expIdx - 1);
         exp = Integer.parseInt(expStr);
         scale = this.adjustScale(scale, (long)exp);
      } else {
         numEndIdx = len;
      }

      BigDecimal res;
      if (dotIdx >= 0) {
         int leftLen = dotIdx - numIdx;
         BigDecimal left = this.toBigDecimalRec(numIdx, leftLen, exp, splitLen);
         int rightLen = numEndIdx - dotIdx - 1;
         BigDecimal right = this.toBigDecimalRec(dotIdx + 1, rightLen, exp - rightLen, splitLen);
         res = left.add(right);
      } else {
         res = this.toBigDecimalRec(numIdx, numEndIdx - numIdx, exp, splitLen);
      }

      if (scale != 0) {
         res = res.setScale(scale);
      }

      if (neg) {
         res = res.negate();
      }

      return res;
   }

   private int adjustScale(int scale, long exp) {
      long adjScale = (long)scale - exp;
      if (adjScale <= 2147483647L && adjScale >= -2147483648L) {
         return (int)adjScale;
      } else {
         throw new NumberFormatException("Scale out of range: " + adjScale + " while adjusting scale " + scale + " to exponent " + exp);
      }
   }

   private BigDecimal toBigDecimalRec(int off, int len, int scale, int splitLen) {
      if (len > splitLen) {
         int mid = len / 2;
         BigDecimal left = this.toBigDecimalRec(off, mid, scale + len - mid, splitLen);
         BigDecimal right = this.toBigDecimalRec(off + mid, len - mid, scale, splitLen);
         return left.add(right);
      } else {
         return len == 0 ? BigDecimal.ZERO : new BigDecimal(this.chars, off, len).movePointRight(scale);
      }
   }
}
