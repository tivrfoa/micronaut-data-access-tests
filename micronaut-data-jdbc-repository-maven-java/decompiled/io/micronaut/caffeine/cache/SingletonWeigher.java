package io.micronaut.caffeine.cache;

enum SingletonWeigher implements Weigher<Object, Object> {
   INSTANCE;

   @Override
   public int weigh(Object key, Object value) {
      return 1;
   }
}
