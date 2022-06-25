package org.flywaydb.core.internal.util;

import java.util.Arrays;

public class Pair<L, R> implements Comparable<Pair<L, R>> {
   private final L left;
   private final R right;

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Pair<?, ?> pair = (Pair)o;
         return this.left.equals(pair.left) && this.right.equals(pair.right);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(new Object[]{this.left, this.right});
   }

   public int compareTo(Pair<L, R> o) {
      if (this.left instanceof Comparable) {
         int l = ((Comparable)this.left).compareTo(o.left);
         if (l != 0) {
            return l;
         }
      }

      if (this.right instanceof Comparable) {
         int r = ((Comparable)this.right).compareTo(o.right);
         if (r != 0) {
            return r;
         }
      }

      return 0;
   }

   private Pair(L left, R right) {
      this.left = left;
      this.right = right;
   }

   public static <L, R> Pair<L, R> of(L left, R right) {
      return new Pair<>(left, right);
   }

   public L getLeft() {
      return this.left;
   }

   public R getRight() {
      return this.right;
   }
}
