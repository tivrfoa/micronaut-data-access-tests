package io.micronaut.aop;

public enum InterceptPhase {
   VALIDATE(-120),
   CACHE(-100),
   TRACE(-80),
   RETRY(-60),
   ASYNC(-40),
   TRANSACTION(-20);

   private final int position;

   private InterceptPhase(int position) {
      this.position = position;
   }

   public int getPosition() {
      return this.position;
   }
}
