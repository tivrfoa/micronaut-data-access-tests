package io.micronaut.core.type;

public interface MutableHeaders extends Headers {
   MutableHeaders add(CharSequence header, CharSequence value);

   MutableHeaders remove(CharSequence header);

   default MutableHeaders set(CharSequence header, CharSequence value) {
      this.remove(header);
      this.add(header, value);
      return this;
   }
}
