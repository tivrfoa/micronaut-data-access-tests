package io.netty.handler.codec.serialization;

class ClassLoaderClassResolver implements ClassResolver {
   private final ClassLoader classLoader;

   ClassLoaderClassResolver(ClassLoader classLoader) {
      this.classLoader = classLoader;
   }

   @Override
   public Class<?> resolve(String className) throws ClassNotFoundException {
      try {
         return this.classLoader.loadClass(className);
      } catch (ClassNotFoundException var3) {
         return Class.forName(className, false, this.classLoader);
      }
   }
}
