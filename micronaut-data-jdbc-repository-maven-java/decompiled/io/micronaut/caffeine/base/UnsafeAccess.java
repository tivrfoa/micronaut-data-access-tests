package io.micronaut.caffeine.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import sun.misc.Unsafe;

@Deprecated
public final class UnsafeAccess {
   static final String ANDROID = "THE_ONE";
   static final String OPEN_JDK = "theUnsafe";
   public static final Unsafe UNSAFE;

   public static long objectFieldOffset(Class<?> clazz, String fieldName) {
      try {
         return UNSAFE.objectFieldOffset(clazz.getDeclaredField(fieldName));
      } catch (SecurityException | NoSuchFieldException var3) {
         throw new Error(var3);
      }
   }

   static Unsafe load(String openJdk, String android) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
      Field field;
      try {
         field = Unsafe.class.getDeclaredField(openJdk);
      } catch (NoSuchFieldException var7) {
         try {
            field = Unsafe.class.getDeclaredField(android);
         } catch (NoSuchFieldException var6) {
            Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
            unsafeConstructor.setAccessible(true);
            return (Unsafe)unsafeConstructor.newInstance();
         }
      }

      field.setAccessible(true);
      return (Unsafe)field.get(null);
   }

   private UnsafeAccess() {
   }

   static {
      try {
         UNSAFE = load("theUnsafe", "THE_ONE");
      } catch (Exception var1) {
         throw new Error("Failed to load sun.misc.Unsafe", var1);
      }
   }
}
