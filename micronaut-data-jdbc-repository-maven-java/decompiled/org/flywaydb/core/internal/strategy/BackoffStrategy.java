package org.flywaydb.core.internal.strategy;

public class BackoffStrategy {
   private int current;
   private final int exponent;
   private final int interval;

   public int next() {
      int temp = this.current;
      this.current = Math.min(this.current * this.exponent, this.interval);
      return temp;
   }

   public int peek() {
      return this.current;
   }

   public BackoffStrategy(int current, int exponent, int interval) {
      this.current = current;
      this.exponent = exponent;
      this.interval = interval;
   }
}
