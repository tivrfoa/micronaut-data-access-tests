package io.micronaut.data.model;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.TypeDef;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

public enum DataType {
   BIGDECIMAL(BigDecimal.class, BigInteger.class),
   BOOLEAN(Boolean.class),
   BYTE(Byte.class),
   BYTE_ARRAY(true, byte[].class),
   CHARACTER(Character.class),
   DATE(Date.class, java.sql.Date.class, LocalDate.class),
   TIMESTAMP(Timestamp.class, Instant.class, OffsetDateTime.class, ZonedDateTime.class),
   DOUBLE(Double.class),
   FLOAT(Float.class),
   INTEGER(Integer.class),
   LONG(Long.class),
   SHORT(Short.class),
   STRING(String.class, CharSequence.class, URL.class, URI.class, Locale.class, TimeZone.class, Charset.class),
   OBJECT(),
   ENTITY(),
   JSON(),
   UUID(UUID.class),
   STRING_ARRAY(true, String[].class),
   SHORT_ARRAY(true, short[].class, Short[].class),
   INTEGER_ARRAY(true, int[].class, Integer[].class),
   LONG_ARRAY(true, long[].class, Long[].class),
   FLOAT_ARRAY(true, float[].class, Float[].class),
   DOUBLE_ARRAY(true, double[].class, Double[].class),
   CHARACTER_ARRAY(true, char[].class, Character[].class),
   BOOLEAN_ARRAY(true, boolean[].class, Boolean[].class);

   public static final DataType[] EMPTY_DATA_TYPE_ARRAY = new DataType[0];
   private static final Map<Class<?>, DataType> CLASS_DATA_TYPE_MAP = new HashMap();
   private final Set<Class<?>> javaTypes;
   private final boolean isArray;

   private DataType(Class<?>... javaTypes) {
      this(false, javaTypes);
   }

   private DataType(boolean isArray, Class<?>... javaTypes) {
      this.isArray = isArray;
      this.javaTypes = CollectionUtils.setOf(javaTypes);
   }

   public boolean isArray() {
      return this.isArray;
   }

   public static DataType forType(@NonNull Class<?> type) {
      Class<?> wrapper = ReflectionUtils.getWrapperType((Class)Objects.requireNonNull(type, "The type cannot be null"));
      TypeDef td = (TypeDef)wrapper.getAnnotation(TypeDef.class);
      return td != null ? td.type() : (DataType)CLASS_DATA_TYPE_MAP.getOrDefault(wrapper, OBJECT);
   }

   static {
      DataType[] values = values();

      for(DataType dt : values) {
         for(Class<?> javaType : dt.javaTypes) {
            CLASS_DATA_TYPE_MAP.put(javaType, dt);
         }
      }

   }
}
