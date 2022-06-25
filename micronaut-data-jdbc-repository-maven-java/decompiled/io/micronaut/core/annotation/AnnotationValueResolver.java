package io.micronaut.core.annotation;

import io.micronaut.core.value.ValueResolver;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public interface AnnotationValueResolver extends ValueResolver<CharSequence> {
   <E extends Enum> Optional<E> enumValue(@NonNull String member, @NonNull Class<E> enumType);

   default <E extends Enum> Optional<E> enumValue(@NonNull Class<E> enumType) {
      return this.enumValue("value", enumType);
   }

   <E extends Enum> E[] enumValues(@NonNull String member, @NonNull Class<E> enumType);

   default <E extends Enum> E[] enumValues(@NonNull Class<E> enumType) {
      return (E[])this.enumValues("value", enumType);
   }

   default Optional<Class<?>> classValue() {
      return this.classValue("value");
   }

   Optional<Class<?>> classValue(@NonNull String member);

   @NonNull
   default Class<?>[] classValues() {
      return this.classValues("value");
   }

   @NonNull
   Class<?>[] classValues(@NonNull String member);

   @NonNull
   AnnotationClassValue<?>[] annotationClassValues(@NonNull String member);

   Optional<AnnotationClassValue<?>> annotationClassValue(@NonNull String member);

   OptionalInt intValue(@NonNull String member);

   default Optional<Byte> byteValue() {
      return this.byteValue("value");
   }

   Optional<Byte> byteValue(@NonNull String member);

   default Optional<Character> charValue() {
      return this.charValue("value");
   }

   Optional<Character> charValue(@NonNull String member);

   default OptionalInt intValue() {
      return this.intValue("value");
   }

   OptionalLong longValue(@NonNull String member);

   default OptionalLong longValue() {
      return this.longValue("value");
   }

   Optional<Short> shortValue(@NonNull String member);

   default Optional<Short> shortValue() {
      return this.shortValue("value");
   }

   OptionalDouble doubleValue(@NonNull String member);

   default Optional<Float> floatValue() {
      return this.floatValue("value");
   }

   Optional<Float> floatValue(@NonNull String member);

   default OptionalDouble doubleValue() {
      return this.doubleValue("value");
   }

   Optional<String> stringValue(@NonNull String member);

   default Optional<String> stringValue() {
      return this.stringValue("value");
   }

   Optional<Boolean> booleanValue(@NonNull String member);

   default Optional<Boolean> booleanValue() {
      return this.booleanValue("value");
   }

   @NonNull
   String[] stringValues(@NonNull String member);

   @NonNull
   default String[] stringValues() {
      return this.stringValues("value");
   }

   @NonNull
   boolean[] booleanValues(@NonNull String member);

   @NonNull
   default boolean[] booleanValues() {
      return this.booleanValues("value");
   }

   @NonNull
   byte[] byteValues(@NonNull String member);

   @NonNull
   default byte[] byteValues() {
      return this.byteValues("value");
   }

   @NonNull
   char[] charValues(@NonNull String member);

   @NonNull
   default char[] charValues() {
      return this.charValues("value");
   }

   @NonNull
   int[] intValues(@NonNull String member);

   @NonNull
   default int[] intValues() {
      return this.intValues("value");
   }

   @NonNull
   double[] doubleValues(@NonNull String member);

   @NonNull
   default double[] doubleValues() {
      return this.doubleValues("value");
   }

   @NonNull
   long[] longValues(@NonNull String member);

   @NonNull
   default long[] longValues() {
      return this.longValues("value");
   }

   @NonNull
   float[] floatValues(@NonNull String member);

   @NonNull
   default float[] floatValues() {
      return this.floatValues("value");
   }

   @NonNull
   short[] shortValues(@NonNull String member);

   @NonNull
   default short[] shortValues() {
      return this.shortValues("value");
   }

   boolean isPresent(CharSequence member);

   default boolean isTrue() {
      return this.isTrue("value");
   }

   boolean isTrue(String member);

   default boolean isFalse() {
      return this.isFalse("value");
   }

   boolean isFalse(String member);

   <T> Optional<Class<? extends T>> classValue(@NonNull String member, @NonNull Class<T> requiredType);

   @NonNull
   Map<CharSequence, Object> getValues();
}
