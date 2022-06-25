package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.DefaultAccessorNamingStrategy;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

public final class BaseSettings implements Serializable {
   private static final long serialVersionUID = 1L;
   private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("UTC");
   protected final TypeFactory _typeFactory;
   protected final ClassIntrospector _classIntrospector;
   protected final AnnotationIntrospector _annotationIntrospector;
   protected final PropertyNamingStrategy _propertyNamingStrategy;
   protected final AccessorNamingStrategy.Provider _accessorNaming;
   protected final TypeResolverBuilder<?> _typeResolverBuilder;
   protected final PolymorphicTypeValidator _typeValidator;
   protected final DateFormat _dateFormat;
   protected final HandlerInstantiator _handlerInstantiator;
   protected final Locale _locale;
   protected final TimeZone _timeZone;
   protected final Base64Variant _defaultBase64;

   public BaseSettings(
      ClassIntrospector ci,
      AnnotationIntrospector ai,
      PropertyNamingStrategy pns,
      TypeFactory tf,
      TypeResolverBuilder<?> typer,
      DateFormat dateFormat,
      HandlerInstantiator hi,
      Locale locale,
      TimeZone tz,
      Base64Variant defaultBase64,
      PolymorphicTypeValidator ptv,
      AccessorNamingStrategy.Provider accNaming
   ) {
      this._classIntrospector = ci;
      this._annotationIntrospector = ai;
      this._propertyNamingStrategy = pns;
      this._typeFactory = tf;
      this._typeResolverBuilder = typer;
      this._dateFormat = dateFormat;
      this._handlerInstantiator = hi;
      this._locale = locale;
      this._timeZone = tz;
      this._defaultBase64 = defaultBase64;
      this._typeValidator = ptv;
      this._accessorNaming = accNaming;
   }

   @Deprecated
   public BaseSettings(
      ClassIntrospector ci,
      AnnotationIntrospector ai,
      PropertyNamingStrategy pns,
      TypeFactory tf,
      TypeResolverBuilder<?> typer,
      DateFormat dateFormat,
      HandlerInstantiator hi,
      Locale locale,
      TimeZone tz,
      Base64Variant defaultBase64,
      PolymorphicTypeValidator ptv
   ) {
      this(ci, ai, pns, tf, typer, dateFormat, hi, locale, tz, defaultBase64, ptv, new DefaultAccessorNamingStrategy.Provider());
   }

   public BaseSettings copy() {
      return new BaseSettings(
         this._classIntrospector.copy(),
         this._annotationIntrospector,
         this._propertyNamingStrategy,
         this._typeFactory,
         this._typeResolverBuilder,
         this._dateFormat,
         this._handlerInstantiator,
         this._locale,
         this._timeZone,
         this._defaultBase64,
         this._typeValidator,
         this._accessorNaming
      );
   }

   public BaseSettings withClassIntrospector(ClassIntrospector ci) {
      return this._classIntrospector == ci
         ? this
         : new BaseSettings(
            ci,
            this._annotationIntrospector,
            this._propertyNamingStrategy,
            this._typeFactory,
            this._typeResolverBuilder,
            this._dateFormat,
            this._handlerInstantiator,
            this._locale,
            this._timeZone,
            this._defaultBase64,
            this._typeValidator,
            this._accessorNaming
         );
   }

   public BaseSettings withAnnotationIntrospector(AnnotationIntrospector ai) {
      return this._annotationIntrospector == ai
         ? this
         : new BaseSettings(
            this._classIntrospector,
            ai,
            this._propertyNamingStrategy,
            this._typeFactory,
            this._typeResolverBuilder,
            this._dateFormat,
            this._handlerInstantiator,
            this._locale,
            this._timeZone,
            this._defaultBase64,
            this._typeValidator,
            this._accessorNaming
         );
   }

   public BaseSettings withInsertedAnnotationIntrospector(AnnotationIntrospector ai) {
      return this.withAnnotationIntrospector(AnnotationIntrospectorPair.create(ai, this._annotationIntrospector));
   }

   public BaseSettings withAppendedAnnotationIntrospector(AnnotationIntrospector ai) {
      return this.withAnnotationIntrospector(AnnotationIntrospectorPair.create(this._annotationIntrospector, ai));
   }

   public BaseSettings withPropertyNamingStrategy(PropertyNamingStrategy pns) {
      return this._propertyNamingStrategy == pns
         ? this
         : new BaseSettings(
            this._classIntrospector,
            this._annotationIntrospector,
            pns,
            this._typeFactory,
            this._typeResolverBuilder,
            this._dateFormat,
            this._handlerInstantiator,
            this._locale,
            this._timeZone,
            this._defaultBase64,
            this._typeValidator,
            this._accessorNaming
         );
   }

   public BaseSettings withAccessorNaming(AccessorNamingStrategy.Provider p) {
      return this._accessorNaming == p
         ? this
         : new BaseSettings(
            this._classIntrospector,
            this._annotationIntrospector,
            this._propertyNamingStrategy,
            this._typeFactory,
            this._typeResolverBuilder,
            this._dateFormat,
            this._handlerInstantiator,
            this._locale,
            this._timeZone,
            this._defaultBase64,
            this._typeValidator,
            p
         );
   }

   public BaseSettings withTypeFactory(TypeFactory tf) {
      return this._typeFactory == tf
         ? this
         : new BaseSettings(
            this._classIntrospector,
            this._annotationIntrospector,
            this._propertyNamingStrategy,
            tf,
            this._typeResolverBuilder,
            this._dateFormat,
            this._handlerInstantiator,
            this._locale,
            this._timeZone,
            this._defaultBase64,
            this._typeValidator,
            this._accessorNaming
         );
   }

   public BaseSettings withTypeResolverBuilder(TypeResolverBuilder<?> typer) {
      return this._typeResolverBuilder == typer
         ? this
         : new BaseSettings(
            this._classIntrospector,
            this._annotationIntrospector,
            this._propertyNamingStrategy,
            this._typeFactory,
            typer,
            this._dateFormat,
            this._handlerInstantiator,
            this._locale,
            this._timeZone,
            this._defaultBase64,
            this._typeValidator,
            this._accessorNaming
         );
   }

   public BaseSettings withDateFormat(DateFormat df) {
      if (this._dateFormat == df) {
         return this;
      } else {
         if (df != null && this.hasExplicitTimeZone()) {
            df = this._force(df, this._timeZone);
         }

         return new BaseSettings(
            this._classIntrospector,
            this._annotationIntrospector,
            this._propertyNamingStrategy,
            this._typeFactory,
            this._typeResolverBuilder,
            df,
            this._handlerInstantiator,
            this._locale,
            this._timeZone,
            this._defaultBase64,
            this._typeValidator,
            this._accessorNaming
         );
      }
   }

   public BaseSettings withHandlerInstantiator(HandlerInstantiator hi) {
      return this._handlerInstantiator == hi
         ? this
         : new BaseSettings(
            this._classIntrospector,
            this._annotationIntrospector,
            this._propertyNamingStrategy,
            this._typeFactory,
            this._typeResolverBuilder,
            this._dateFormat,
            hi,
            this._locale,
            this._timeZone,
            this._defaultBase64,
            this._typeValidator,
            this._accessorNaming
         );
   }

   public BaseSettings with(Locale l) {
      return this._locale == l
         ? this
         : new BaseSettings(
            this._classIntrospector,
            this._annotationIntrospector,
            this._propertyNamingStrategy,
            this._typeFactory,
            this._typeResolverBuilder,
            this._dateFormat,
            this._handlerInstantiator,
            l,
            this._timeZone,
            this._defaultBase64,
            this._typeValidator,
            this._accessorNaming
         );
   }

   public BaseSettings with(TimeZone tz) {
      if (tz == this._timeZone) {
         return this;
      } else {
         DateFormat df = this._force(this._dateFormat, tz == null ? DEFAULT_TIMEZONE : tz);
         return new BaseSettings(
            this._classIntrospector,
            this._annotationIntrospector,
            this._propertyNamingStrategy,
            this._typeFactory,
            this._typeResolverBuilder,
            df,
            this._handlerInstantiator,
            this._locale,
            tz,
            this._defaultBase64,
            this._typeValidator,
            this._accessorNaming
         );
      }
   }

   public BaseSettings with(Base64Variant base64) {
      return base64 == this._defaultBase64
         ? this
         : new BaseSettings(
            this._classIntrospector,
            this._annotationIntrospector,
            this._propertyNamingStrategy,
            this._typeFactory,
            this._typeResolverBuilder,
            this._dateFormat,
            this._handlerInstantiator,
            this._locale,
            this._timeZone,
            base64,
            this._typeValidator,
            this._accessorNaming
         );
   }

   public BaseSettings with(PolymorphicTypeValidator v) {
      return v == this._typeValidator
         ? this
         : new BaseSettings(
            this._classIntrospector,
            this._annotationIntrospector,
            this._propertyNamingStrategy,
            this._typeFactory,
            this._typeResolverBuilder,
            this._dateFormat,
            this._handlerInstantiator,
            this._locale,
            this._timeZone,
            this._defaultBase64,
            v,
            this._accessorNaming
         );
   }

   public ClassIntrospector getClassIntrospector() {
      return this._classIntrospector;
   }

   public AnnotationIntrospector getAnnotationIntrospector() {
      return this._annotationIntrospector;
   }

   public PropertyNamingStrategy getPropertyNamingStrategy() {
      return this._propertyNamingStrategy;
   }

   public AccessorNamingStrategy.Provider getAccessorNaming() {
      return this._accessorNaming;
   }

   public TypeFactory getTypeFactory() {
      return this._typeFactory;
   }

   public TypeResolverBuilder<?> getTypeResolverBuilder() {
      return this._typeResolverBuilder;
   }

   public PolymorphicTypeValidator getPolymorphicTypeValidator() {
      return this._typeValidator;
   }

   public DateFormat getDateFormat() {
      return this._dateFormat;
   }

   public HandlerInstantiator getHandlerInstantiator() {
      return this._handlerInstantiator;
   }

   public Locale getLocale() {
      return this._locale;
   }

   public TimeZone getTimeZone() {
      TimeZone tz = this._timeZone;
      return tz == null ? DEFAULT_TIMEZONE : tz;
   }

   public boolean hasExplicitTimeZone() {
      return this._timeZone != null;
   }

   public Base64Variant getBase64Variant() {
      return this._defaultBase64;
   }

   private DateFormat _force(DateFormat df, TimeZone tz) {
      if (df instanceof StdDateFormat) {
         return ((StdDateFormat)df).withTimeZone(tz);
      } else {
         df = (DateFormat)df.clone();
         df.setTimeZone(tz);
         return df;
      }
   }
}
