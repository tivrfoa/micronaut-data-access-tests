package io.micronaut.http.filter;

public enum ServerFilterPhase {
   FIRST(-1000, -1249, -750),
   METRICS(9000, 8751, 9250),
   TRACING(19000, 18751, 19250),
   SESSION(29000, 28751, 29250),
   SECURITY(39000, 38751, 39250),
   RENDERING(49000, 48751, 49250),
   LAST(59000, 58751, 59250);

   private final int order;
   private final int before;
   private final int after;

   private ServerFilterPhase(int order, int before, int after) {
      this.order = order;
      this.before = before;
      this.after = after;
   }

   public int order() {
      return this.order;
   }

   public int before() {
      return this.before;
   }

   public int after() {
      return this.after;
   }
}
