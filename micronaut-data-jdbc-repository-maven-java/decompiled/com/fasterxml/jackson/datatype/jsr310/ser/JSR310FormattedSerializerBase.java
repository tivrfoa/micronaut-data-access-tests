package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

abstract class JSR310FormattedSerializerBase<T> extends JSR310SerializerBase<T> implements ContextualSerializer {
   private static final long serialVersionUID = 1L;
   protected final Boolean _useTimestamp;
   protected final Boolean _useNanoseconds;
   protected final DateTimeFormatter _formatter;
   protected final JsonFormat.Shape _shape;
   protected transient volatile JavaType _integerListType;

   protected JSR310FormattedSerializerBase(Class<T> supportedType) {
      this(supportedType, null);
   }

   protected JSR310FormattedSerializerBase(Class<T> supportedType, DateTimeFormatter formatter) {
      super(supportedType);
      this._useTimestamp = null;
      this._useNanoseconds = null;
      this._shape = null;
      this._formatter = formatter;
   }

   protected JSR310FormattedSerializerBase(JSR310FormattedSerializerBase<?> base, Boolean useTimestamp, DateTimeFormatter dtf, JsonFormat.Shape shape) {
      this(base, useTimestamp, null, dtf, shape);
   }

   protected JSR310FormattedSerializerBase(
      JSR310FormattedSerializerBase<?> base, Boolean useTimestamp, Boolean useNanoseconds, DateTimeFormatter dtf, JsonFormat.Shape shape
   ) {
      super(base.handledType());
      this._useTimestamp = useTimestamp;
      this._useNanoseconds = useNanoseconds;
      this._formatter = dtf;
      this._shape = shape;
   }

   protected abstract JSR310FormattedSerializerBase<?> withFormat(Boolean var1, DateTimeFormatter var2, JsonFormat.Shape var3);

   @Deprecated
   protected JSR310FormattedSerializerBase<?> withFeatures(Boolean writeZoneId) {
      return this;
   }

   protected JSR310FormattedSerializerBase<?> withFeatures(Boolean writeZoneId, Boolean writeNanoseconds) {
      return this;
   }

   @Override
   public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
      JsonFormat.Value format = this.findFormatOverrides(prov, property, this.handledType());
      if (format == null) {
         return this;
      } else {
         Boolean useTimestamp = null;
         JsonFormat.Shape shape = format.getShape();
         if (shape != JsonFormat.Shape.ARRAY && !shape.isNumeric()) {
            useTimestamp = shape == JsonFormat.Shape.STRING ? Boolean.FALSE : null;
         } else {
            useTimestamp = Boolean.TRUE;
         }

         DateTimeFormatter dtf = this._formatter;
         if (format.hasPattern()) {
            dtf = this._useDateTimeFormatter(prov, format);
         }

         JSR310FormattedSerializerBase<?> ser = this;
         if (shape != this._shape || useTimestamp != this._useTimestamp || dtf != this._formatter) {
            ser = this.withFormat(useTimestamp, dtf, shape);
         }

         Boolean writeZoneId = format.getFeature(JsonFormat.Feature.WRITE_DATES_WITH_ZONE_ID);
         Boolean writeNanoseconds = format.getFeature(JsonFormat.Feature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
         if (writeZoneId != null || writeNanoseconds != null) {
            ser = ser.withFeatures(writeZoneId, writeNanoseconds);
         }

         return ser;
      }
   }

   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      return this.createSchemaNode(provider.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) ? "array" : "string", true);
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      if (this.useTimestamp(visitor.getProvider())) {
         this._acceptTimestampVisitor(visitor, typeHint);
      } else {
         JsonStringFormatVisitor v2 = visitor.expectStringFormat(typeHint);
         if (v2 != null) {
            v2.format(JsonValueFormat.DATE_TIME);
         }
      }

   }

   protected void _acceptTimestampVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      JsonArrayFormatVisitor v2 = visitor.expectArrayFormat(this._integerListType(visitor.getProvider()));
      if (v2 != null) {
         v2.itemsFormat(JsonFormatTypes.INTEGER);
      }

   }

   protected JavaType _integerListType(SerializerProvider prov) {
      JavaType t = this._integerListType;
      if (t == null) {
         t = prov.getTypeFactory().constructCollectionType(List.class, Integer.class);
         this._integerListType = t;
      }

      return t;
   }

   protected SerializationFeature getTimestampsFeature() {
      return SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
   }

   protected boolean useTimestamp(SerializerProvider provider) {
      if (this._useTimestamp != null) {
         return this._useTimestamp;
      } else {
         if (this._shape != null) {
            if (this._shape == JsonFormat.Shape.STRING) {
               return false;
            }

            if (this._shape == JsonFormat.Shape.NUMBER_INT) {
               return true;
            }
         }

         return this._formatter == null && provider != null && provider.isEnabled(this.getTimestampsFeature());
      }
   }

   protected boolean _useTimestampExplicitOnly(SerializerProvider provider) {
      return this._useTimestamp != null ? this._useTimestamp : false;
   }

   protected boolean useNanoseconds(SerializerProvider provider) {
      if (this._useNanoseconds != null) {
         return this._useNanoseconds;
      } else {
         if (this._shape != null) {
            if (this._shape == JsonFormat.Shape.NUMBER_INT) {
               return false;
            }

            if (this._shape == JsonFormat.Shape.NUMBER_FLOAT) {
               return true;
            }
         }

         return provider != null && provider.isEnabled(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
      }
   }

   protected DateTimeFormatter _useDateTimeFormatter(SerializerProvider prov, JsonFormat.Value format) {
      String pattern = format.getPattern();
      Locale locale = format.hasLocale() ? format.getLocale() : prov.getLocale();
      DateTimeFormatter dtf;
      if (locale == null) {
         dtf = DateTimeFormatter.ofPattern(pattern);
      } else {
         dtf = DateTimeFormatter.ofPattern(pattern, locale);
      }

      if (format.hasTimeZone()) {
         dtf = dtf.withZone(format.getTimeZone().toZoneId());
      }

      return dtf;
   }
}
