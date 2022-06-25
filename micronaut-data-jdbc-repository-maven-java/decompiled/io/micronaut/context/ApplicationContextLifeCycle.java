package io.micronaut.context;

public interface ApplicationContextLifeCycle<T extends ApplicationContextLifeCycle> extends ApplicationContextProvider, LifeCycle {
   default T start() {
      return (T)this;
   }

   default T stop() {
      ApplicationContext applicationContext = this.getApplicationContext();
      if (applicationContext != null && applicationContext.isRunning()) {
         applicationContext.stop();
      }

      return (T)this;
   }
}
