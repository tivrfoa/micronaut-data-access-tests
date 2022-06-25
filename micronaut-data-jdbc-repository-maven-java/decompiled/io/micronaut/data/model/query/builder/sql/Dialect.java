package io.micronaut.data.model.query.builder.sql;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.DataType;

public enum Dialect {
   H2(true, false, true),
   MYSQL(true, true, false),
   POSTGRES(true, false, true),
   SQL_SERVER(false, false, false),
   ORACLE(true, true, false),
   ANSI(true, false, true);

   private final boolean supportsBatch;
   private final boolean stringUUID;
   private final boolean supportsArrays;

   private Dialect(boolean supportsBatch, boolean stringUUID, boolean supportsArrays) {
      this.supportsBatch = supportsBatch;
      this.stringUUID = stringUUID;
      this.supportsArrays = supportsArrays;
   }

   public final boolean allowBatch() {
      return this.supportsBatch;
   }

   public final boolean supportsArrays() {
      return this.supportsArrays;
   }

   public final DataType getDataType(@NonNull DataType type) {
      return type == DataType.UUID && this.stringUUID ? DataType.STRING : type;
   }

   public final boolean requiresStringUUID(@NonNull DataType type) {
      return type == DataType.UUID && this.stringUUID;
   }
}
