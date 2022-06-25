package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.Serializable;

public abstract class FilteredBeanPropertyWriter {
   public static BeanPropertyWriter constructViewBased(BeanPropertyWriter base, Class<?>[] viewsToIncludeIn) {
      return (BeanPropertyWriter)(viewsToIncludeIn.length == 1
         ? new FilteredBeanPropertyWriter.SingleView(base, viewsToIncludeIn[0])
         : new FilteredBeanPropertyWriter.MultiView(base, viewsToIncludeIn));
   }

   private static final class MultiView extends BeanPropertyWriter implements Serializable {
      private static final long serialVersionUID = 1L;
      protected final BeanPropertyWriter _delegate;
      protected final Class<?>[] _views;

      protected MultiView(BeanPropertyWriter delegate, Class<?>[] views) {
         super(delegate);
         this._delegate = delegate;
         this._views = views;
      }

      public FilteredBeanPropertyWriter.MultiView rename(NameTransformer transformer) {
         return new FilteredBeanPropertyWriter.MultiView(this._delegate.rename(transformer), this._views);
      }

      @Override
      public void assignSerializer(JsonSerializer<Object> ser) {
         this._delegate.assignSerializer(ser);
      }

      @Override
      public void assignNullSerializer(JsonSerializer<Object> nullSer) {
         this._delegate.assignNullSerializer(nullSer);
      }

      @Override
      public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
         if (this._inView(prov.getActiveView())) {
            this._delegate.serializeAsField(bean, gen, prov);
         } else {
            this._delegate.serializeAsOmittedField(bean, gen, prov);
         }
      }

      @Override
      public void serializeAsElement(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
         if (this._inView(prov.getActiveView())) {
            this._delegate.serializeAsElement(bean, gen, prov);
         } else {
            this._delegate.serializeAsPlaceholder(bean, gen, prov);
         }
      }

      @Override
      public void depositSchemaProperty(JsonObjectFormatVisitor v, SerializerProvider provider) throws JsonMappingException {
         if (this._inView(provider.getActiveView())) {
            super.depositSchemaProperty(v, provider);
         }

      }

      private final boolean _inView(Class<?> activeView) {
         if (activeView == null) {
            return true;
         } else {
            int len = this._views.length;

            for(int i = 0; i < len; ++i) {
               if (this._views[i].isAssignableFrom(activeView)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private static final class SingleView extends BeanPropertyWriter implements Serializable {
      private static final long serialVersionUID = 1L;
      protected final BeanPropertyWriter _delegate;
      protected final Class<?> _view;

      protected SingleView(BeanPropertyWriter delegate, Class<?> view) {
         super(delegate);
         this._delegate = delegate;
         this._view = view;
      }

      public FilteredBeanPropertyWriter.SingleView rename(NameTransformer transformer) {
         return new FilteredBeanPropertyWriter.SingleView(this._delegate.rename(transformer), this._view);
      }

      @Override
      public void assignSerializer(JsonSerializer<Object> ser) {
         this._delegate.assignSerializer(ser);
      }

      @Override
      public void assignNullSerializer(JsonSerializer<Object> nullSer) {
         this._delegate.assignNullSerializer(nullSer);
      }

      @Override
      public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
         Class<?> activeView = prov.getActiveView();
         if (activeView != null && !this._view.isAssignableFrom(activeView)) {
            this._delegate.serializeAsOmittedField(bean, gen, prov);
         } else {
            this._delegate.serializeAsField(bean, gen, prov);
         }

      }

      @Override
      public void serializeAsElement(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
         Class<?> activeView = prov.getActiveView();
         if (activeView != null && !this._view.isAssignableFrom(activeView)) {
            this._delegate.serializeAsPlaceholder(bean, gen, prov);
         } else {
            this._delegate.serializeAsElement(bean, gen, prov);
         }

      }

      @Override
      public void depositSchemaProperty(JsonObjectFormatVisitor v, SerializerProvider provider) throws JsonMappingException {
         Class<?> activeView = provider.getActiveView();
         if (activeView == null || this._view.isAssignableFrom(activeView)) {
            super.depositSchemaProperty(v, provider);
         }

      }
   }
}
