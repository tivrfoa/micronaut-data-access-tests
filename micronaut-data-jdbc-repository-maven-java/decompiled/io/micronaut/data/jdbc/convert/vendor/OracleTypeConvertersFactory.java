package io.micronaut.data.jdbc.convert.vendor;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.runtime.convert.DataTypeConverter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import oracle.sql.DATE;
import oracle.sql.TIMESTAMP;

@Factory
@Requires(
   classes = {DATE.class}
)
final class OracleTypeConvertersFactory {
   @Prototype
   DataTypeConverter<DATE, Timestamp> fromOracleDateToTimestamp() {
      return (date, targetType, context) -> Optional.of(date.timestampValue());
   }

   @Prototype
   DataTypeConverter<DATE, LocalDateTime> fromOracleDateToLocalDateTime() {
      return (date, targetType, context) -> Optional.of(date.timestampValue().toLocalDateTime());
   }

   @Prototype
   DataTypeConverter<DATE, Instant> fromOracleDateToInstant() {
      return (date, targetType, context) -> Optional.of(date.timestampValue().toInstant());
   }

   @Prototype
   DataTypeConverter<TIMESTAMP, Timestamp> fromOracleTimestampToTimestamp() {
      return (timestamp, targetType, context) -> {
         try {
            return Optional.of(timestamp.timestampValue());
         } catch (SQLException var4) {
            throw new DataAccessException("Cannot extract timestamp from: " + timestamp);
         }
      };
   }

   @Prototype
   DataTypeConverter<TIMESTAMP, LocalDateTime> fromOracleTimestampToLocalDateTime() {
      return (timestamp, targetType, context) -> {
         try {
            return Optional.of(timestamp.timestampValue().toLocalDateTime());
         } catch (SQLException var4) {
            throw new DataAccessException("Cannot extract timestamp from: " + timestamp);
         }
      };
   }

   @Prototype
   DataTypeConverter<TIMESTAMP, Instant> fromOracleTimestampToInstant() {
      return (timestamp, targetType, context) -> {
         try {
            return Optional.of(timestamp.timestampValue().toInstant());
         } catch (SQLException var4) {
            throw new DataAccessException("Cannot extract timestamp from: " + timestamp);
         }
      };
   }
}
