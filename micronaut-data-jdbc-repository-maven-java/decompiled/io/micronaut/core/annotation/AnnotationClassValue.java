package io.micronaut.core.annotation;

import io.micronaut.core.naming.Named;
import io.micronaut.core.util.ArgumentUtils;
import java.util.Objects;
import java.util.Optional;

public final class AnnotationClassValue<T> implements CharSequence, Named {
   public static final AnnotationClassValue<?>[] EMPTY_ARRAY = new AnnotationClassValue[0];
   private final String name;
   private final Class<T> theClass;
   private final T instance;
   private final boolean instantiated;

   @Internal
   public AnnotationClassValue(String name) {
      this(name, false);
   }

   public AnnotationClassValue(Class<T> theClass) {
      this.name = theClass.getName();
      this.theClass = theClass;
      this.instantiated = false;
      this.instance = null;
   }

   @Internal
   public AnnotationClassValue(@NonNull String name, boolean instantiated) {
      ArgumentUtils.requireNonNull("name", (T)name);
      this.name = name;
      this.theClass = null;
      this.instance = null;
      this.instantiated = instantiated;
   }

   public AnnotationClassValue(@NonNull T instance) {
      ArgumentUtils.requireNonNull("instance", instance);
      this.theClass = instance.getClass();
      this.name = this.theClass.getName();
      this.instance = instance;
      this.instantiated = true;
   }

   @NonNull
   public Optional<T> getInstance() {
      return Optional.ofNullable(this.instance);
   }

   public boolean isInstantiated() {
      return this.instantiated || this.getInstance().isPresent();
   }

   public Optional<Class<T>> getType() {
      return Optional.ofNullable(this.theClass);
   }

   @Override
   public String getName() {
      return this.name;
   }

   public int length() {
      return this.name.length();
   }

   public char charAt(int index) {
      return this.name.charAt(index);
   }

   public CharSequence subSequence(int start, int end) {
      return this.name.subSequence(start, end);
   }

   public String toString() {
      return this.name;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AnnotationClassValue<?> that = (AnnotationClassValue)o;
         return Objects.equals(this.name, that.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.name});
   }
}
