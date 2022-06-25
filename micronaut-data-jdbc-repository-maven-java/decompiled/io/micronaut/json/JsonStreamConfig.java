package io.micronaut.json;

public final class JsonStreamConfig {
   public static final JsonStreamConfig DEFAULT = new JsonStreamConfig(false, false);
   private final boolean useBigDecimalForFloats;
   private final boolean useBigIntegerForInts;

   private JsonStreamConfig(boolean useBigDecimalForFloats, boolean useBigIntegerForInts) {
      this.useBigDecimalForFloats = useBigDecimalForFloats;
      this.useBigIntegerForInts = useBigIntegerForInts;
   }

   public boolean useBigDecimalForFloats() {
      return this.useBigDecimalForFloats;
   }

   public JsonStreamConfig withUseBigDecimalForFloats(boolean useBigDecimalForFloats) {
      return new JsonStreamConfig(useBigDecimalForFloats, this.useBigIntegerForInts);
   }

   public boolean useBigIntegerForInts() {
      return this.useBigIntegerForInts;
   }

   public JsonStreamConfig withUseBigIntegerForInts(boolean useBigIntegerForInts) {
      return new JsonStreamConfig(this.useBigDecimalForFloats, useBigIntegerForInts);
   }
}
