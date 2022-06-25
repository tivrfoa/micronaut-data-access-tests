package io.micronaut.data.runtime.mapper;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.DataType;
import java.math.BigDecimal;
import java.sql.Array;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

public interface QueryStatement<PS, IDX> {
   QueryStatement<PS, IDX> setValue(PS statement, IDX index, Object value) throws DataAccessException;

   default QueryStatement<PS, IDX> setDynamic(@NonNull PS statement, @NonNull IDX index, @NonNull DataType dataType, Object value) {
      switch(dataType) {
         case STRING:
         case JSON:
            String str;
            if (value instanceof CharSequence) {
               str = value.toString();
            } else if (value instanceof Enum) {
               str = value.toString();
            } else {
               str = this.convertRequired(value, String.class);
            }

            return this.setString(statement, index, str);
         case INTEGER:
            if (value instanceof Number) {
               return this.setInt(statement, index, ((Number)value).intValue());
            } else {
               Integer integer = this.convertRequired(value, Integer.class);
               if (integer != null) {
                  return this.setInt(statement, index, integer);
               }

               throw new DataAccessException("Cannot set null value");
            }
         case BOOLEAN:
            if (value instanceof Boolean) {
               return this.setBoolean(statement, index, (Boolean)value);
            } else {
               Boolean b = this.convertRequired(value, Boolean.class);
               if (b != null) {
                  return this.setBoolean(statement, index, b);
               }

               throw new DataAccessException("Cannot set null value");
            }
         case DATE:
            if (value instanceof Date) {
               return this.setDate(statement, index, (Date)value);
            }

            return this.setDate(statement, index, this.convertRequired(value, Date.class));
         case TIMESTAMP:
            Instant instant;
            if (value == null) {
               instant = null;
            } else if (value instanceof ZonedDateTime) {
               instant = ((ZonedDateTime)value).toInstant();
            } else if (value instanceof Instant) {
               instant = (Instant)value;
            } else {
               instant = this.convertRequired(value, Instant.class);
            }

            return this.setTimestamp(statement, index, instant);
         case UUID:
            if (value instanceof CharSequence) {
               return this.setValue(statement, index, UUID.fromString(value.toString()));
            } else {
               if (value instanceof UUID) {
                  return this.setValue(statement, index, value);
               }

               throw new DataAccessException("Invalid UUID: " + value);
            }
         case DOUBLE:
            if (value instanceof Number) {
               return this.setDouble(statement, index, ((Number)value).doubleValue());
            } else {
               Double d = this.convertRequired(value, Double.class);
               if (d != null) {
                  return this.setDouble(statement, index, d);
               }

               throw new DataAccessException("Cannot set null value");
            }
         case BYTE_ARRAY:
            if (value instanceof byte[]) {
               return this.setBytes(statement, index, (byte[])value);
            }

            return this.setBytes(statement, index, this.convertRequired(value, byte[].class));
         case BIGDECIMAL:
            if (value instanceof BigDecimal) {
               return this.setBigDecimal(statement, index, (BigDecimal)value);
            } else {
               if (value instanceof Number) {
                  return this.setBigDecimal(statement, index, BigDecimal.valueOf(((Number)value).doubleValue()));
               }

               return this.setBigDecimal(statement, index, this.convertRequired(value, BigDecimal.class));
            }
         case LONG:
            if (value instanceof Number) {
               return this.setLong(statement, index, ((Number)value).longValue());
            } else {
               Long l = this.convertRequired(value, Long.class);
               if (l != null) {
                  return this.setLong(statement, index, l);
               }

               throw new DataAccessException("Cannot set null value");
            }
         case CHARACTER:
            if (value instanceof Character) {
               return this.setChar(statement, index, (Character)value);
            } else {
               Character c = this.convertRequired(value, Character.class);
               if (c != null) {
                  return this.setChar(statement, index, c);
               }

               throw new DataAccessException("Cannot set null value");
            }
         case FLOAT:
            if (value instanceof Number) {
               return this.setFloat(statement, index, ((Number)value).floatValue());
            } else {
               Float f = this.convertRequired(value, Float.class);
               if (f != null) {
                  return this.setFloat(statement, index, f);
               }

               throw new DataAccessException("Cannot set null value");
            }
         case SHORT:
            if (value instanceof Number) {
               return this.setShort(statement, index, ((Number)value).shortValue());
            } else {
               Short s = this.convertRequired(value, Short.class);
               if (s != null) {
                  return this.setShort(statement, index, s);
               }

               throw new DataAccessException("Cannot set null value");
            }
         case BYTE:
            if (value instanceof Number) {
               return this.setByte(statement, index, ((Number)value).byteValue());
            } else {
               Byte n = this.convertRequired(value, Byte.class);
               if (n != null) {
                  return this.setByte(statement, index, n);
               }

               throw new DataAccessException("Cannot set null value");
            }
         case OBJECT:
         default:
            if (!dataType.isArray()) {
               return this.setValue(statement, index, value);
            } else {
               if (value != null && !(value instanceof Array)) {
                  if (value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive()) {
                     if (value.getClass() == Character[].class) {
                        value = this.convertRequired(value, String[].class);
                     }
                  } else {
                     switch(dataType) {
                        case SHORT_ARRAY:
                           value = this.convertRequired(value, Short[].class);
                           break;
                        case LONG_ARRAY:
                           value = this.convertRequired(value, Long[].class);
                           break;
                        case FLOAT_ARRAY:
                           value = this.convertRequired(value, Float[].class);
                           break;
                        case INTEGER_ARRAY:
                           value = this.convertRequired(value, Integer[].class);
                           break;
                        case DOUBLE_ARRAY:
                           value = this.convertRequired(value, Double[].class);
                           break;
                        case BOOLEAN_ARRAY:
                           value = this.convertRequired(value, Boolean[].class);
                           break;
                        case STRING_ARRAY:
                        case CHARACTER_ARRAY:
                           value = this.convertRequired(value, String[].class);
                     }
                  }
               }

               return this.setArray(statement, index, value);
            }
      }
   }

   @Nullable
   default <T> T convertRequired(@Nullable Object value, Class<T> type) {
      if (value == null) {
         return null;
      } else {
         return (T)(type.isInstance(value)
            ? value
            : this.getConversionService()
               .convert(value, type)
               .orElseThrow(
                  () -> new DataAccessException(
                        "Cannot convert type ["
                           + value.getClass()
                           + "] to target type: "
                           + type
                           + ". Consider defining a TypeConverter bean to handle this case."
                     )
               ));
      }
   }

   @NonNull
   default QueryStatement<PS, IDX> setLong(PS statement, IDX name, long value) {
      this.setValue(statement, name, value);
      return this;
   }

   @NonNull
   default QueryStatement<PS, IDX> setChar(PS statement, IDX name, char value) {
      return this.setValue(statement, name, value);
   }

   @NonNull
   default QueryStatement<PS, IDX> setDate(PS statement, IDX name, Date date) {
      return this.setValue(statement, name, date);
   }

   @Deprecated
   @NonNull
   default QueryStatement<PS, IDX> setTimestamp(PS statement, IDX name, Date date) {
      return this.setTimestamp(statement, name, date == null ? null : date.toInstant());
   }

   @NonNull
   default QueryStatement<PS, IDX> setTimestamp(PS statement, IDX name, Instant instant) {
      return this.setValue(statement, name, instant);
   }

   default QueryStatement<PS, IDX> setString(PS statement, IDX name, String string) {
      return this.setValue(statement, name, string);
   }

   @NonNull
   default QueryStatement<PS, IDX> setInt(PS statement, IDX name, int integer) {
      return this.setValue(statement, name, integer);
   }

   @NonNull
   default QueryStatement<PS, IDX> setBoolean(PS statement, IDX name, boolean bool) {
      return this.setValue(statement, name, bool);
   }

   @NonNull
   default QueryStatement<PS, IDX> setFloat(PS statement, IDX name, float f) {
      return this.setValue(statement, name, f);
   }

   @NonNull
   default QueryStatement<PS, IDX> setByte(PS statement, IDX name, byte b) {
      return this.setValue(statement, name, b);
   }

   @NonNull
   default QueryStatement<PS, IDX> setShort(PS statement, IDX name, short s) {
      return this.setValue(statement, name, s);
   }

   @NonNull
   default QueryStatement<PS, IDX> setDouble(PS statement, IDX name, double d) {
      return this.setValue(statement, name, d);
   }

   @NonNull
   default QueryStatement<PS, IDX> setBigDecimal(PS statement, IDX name, BigDecimal bd) {
      return this.setValue(statement, name, bd);
   }

   @NonNull
   default QueryStatement<PS, IDX> setBytes(PS statement, IDX name, byte[] bytes) {
      return this.setValue(statement, name, bytes);
   }

   @NonNull
   default QueryStatement<PS, IDX> setArray(PS statement, IDX name, Object array) {
      return this.setValue(statement, name, array);
   }

   default ConversionService<?> getConversionService() {
      return ConversionService.SHARED;
   }
}
