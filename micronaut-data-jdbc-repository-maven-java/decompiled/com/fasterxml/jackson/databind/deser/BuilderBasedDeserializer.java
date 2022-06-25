package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.deser.impl.BeanAsArrayBuilderDeserializer;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.IgnorePropertiesUtil;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class BuilderBasedDeserializer extends BeanDeserializerBase {
   private static final long serialVersionUID = 1L;
   protected final AnnotatedMethod _buildMethod;
   protected final JavaType _targetType;

   public BuilderBasedDeserializer(
      BeanDeserializerBuilder builder,
      BeanDescription beanDesc,
      JavaType targetType,
      BeanPropertyMap properties,
      Map<String, SettableBeanProperty> backRefs,
      Set<String> ignorableProps,
      boolean ignoreAllUnknown,
      boolean hasViews
   ) {
      this(builder, beanDesc, targetType, properties, backRefs, ignorableProps, ignoreAllUnknown, null, hasViews);
   }

   public BuilderBasedDeserializer(
      BeanDeserializerBuilder builder,
      BeanDescription beanDesc,
      JavaType targetType,
      BeanPropertyMap properties,
      Map<String, SettableBeanProperty> backRefs,
      Set<String> ignorableProps,
      boolean ignoreAllUnknown,
      Set<String> includableProps,
      boolean hasViews
   ) {
      super(builder, beanDesc, properties, backRefs, ignorableProps, ignoreAllUnknown, includableProps, hasViews);
      this._targetType = targetType;
      this._buildMethod = builder.getBuildMethod();
      if (this._objectIdReader != null) {
         throw new IllegalArgumentException("Cannot use Object Id with Builder-based deserialization (type " + beanDesc.getType() + ")");
      }
   }

   @Deprecated
   public BuilderBasedDeserializer(
      BeanDeserializerBuilder builder,
      BeanDescription beanDesc,
      BeanPropertyMap properties,
      Map<String, SettableBeanProperty> backRefs,
      Set<String> ignorableProps,
      boolean ignoreAllUnknown,
      boolean hasViews
   ) {
      this(builder, beanDesc, beanDesc.getType(), properties, backRefs, ignorableProps, ignoreAllUnknown, hasViews);
   }

   protected BuilderBasedDeserializer(BuilderBasedDeserializer src) {
      this(src, src._ignoreAllUnknown);
   }

   protected BuilderBasedDeserializer(BuilderBasedDeserializer src, boolean ignoreAllUnknown) {
      super(src, ignoreAllUnknown);
      this._buildMethod = src._buildMethod;
      this._targetType = src._targetType;
   }

   protected BuilderBasedDeserializer(BuilderBasedDeserializer src, NameTransformer unwrapper) {
      super(src, unwrapper);
      this._buildMethod = src._buildMethod;
      this._targetType = src._targetType;
   }

   public BuilderBasedDeserializer(BuilderBasedDeserializer src, ObjectIdReader oir) {
      super(src, oir);
      this._buildMethod = src._buildMethod;
      this._targetType = src._targetType;
   }

   public BuilderBasedDeserializer(BuilderBasedDeserializer src, Set<String> ignorableProps) {
      this(src, ignorableProps, src._includableProps);
   }

   public BuilderBasedDeserializer(BuilderBasedDeserializer src, Set<String> ignorableProps, Set<String> includableProps) {
      super(src, ignorableProps, includableProps);
      this._buildMethod = src._buildMethod;
      this._targetType = src._targetType;
   }

   public BuilderBasedDeserializer(BuilderBasedDeserializer src, BeanPropertyMap props) {
      super(src, props);
      this._buildMethod = src._buildMethod;
      this._targetType = src._targetType;
   }

   @Override
   public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
      return new BuilderBasedDeserializer(this, unwrapper);
   }

   @Override
   public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
      return new BuilderBasedDeserializer(this, oir);
   }

   @Override
   public BeanDeserializerBase withByNameInclusion(Set<String> ignorableProps, Set<String> includableProps) {
      return new BuilderBasedDeserializer(this, ignorableProps, includableProps);
   }

   @Override
   public BeanDeserializerBase withIgnoreAllUnknown(boolean ignoreUnknown) {
      return new BuilderBasedDeserializer(this, ignoreUnknown);
   }

   @Override
   public BeanDeserializerBase withBeanProperties(BeanPropertyMap props) {
      return new BuilderBasedDeserializer(this, props);
   }

   @Override
   protected BeanDeserializerBase asArrayDeserializer() {
      SettableBeanProperty[] props = this._beanProperties.getPropertiesInInsertionOrder();
      return new BeanAsArrayBuilderDeserializer(this, this._targetType, props, this._buildMethod);
   }

   @Override
   public Boolean supportsUpdate(DeserializationConfig config) {
      return Boolean.FALSE;
   }

   protected Object finishBuild(DeserializationContext ctxt, Object builder) throws IOException {
      if (null == this._buildMethod) {
         return builder;
      } else {
         try {
            return this._buildMethod.getMember().invoke(builder, (Object[])null);
         } catch (Exception var4) {
            return this.wrapInstantiationProblem(var4, ctxt);
         }
      }
   }

   @Override
   public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.isExpectedStartObjectToken()) {
         JsonToken t = p.nextToken();
         return this._vanillaProcessing
            ? this.finishBuild(ctxt, this.vanillaDeserialize(p, ctxt, t))
            : this.finishBuild(ctxt, this.deserializeFromObject(p, ctxt));
      } else {
         switch(p.currentTokenId()) {
            case 2:
            case 5:
               return this.finishBuild(ctxt, this.deserializeFromObject(p, ctxt));
            case 3:
               return this._deserializeFromArray(p, ctxt);
            case 4:
            case 11:
            default:
               return ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
            case 6:
               return this.finishBuild(ctxt, this.deserializeFromString(p, ctxt));
            case 7:
               return this.finishBuild(ctxt, this.deserializeFromNumber(p, ctxt));
            case 8:
               return this.finishBuild(ctxt, this.deserializeFromDouble(p, ctxt));
            case 9:
            case 10:
               return this.finishBuild(ctxt, this.deserializeFromBoolean(p, ctxt));
            case 12:
               return p.getEmbeddedObject();
         }
      }
   }

   @Override
   public Object deserialize(JsonParser p, DeserializationContext ctxt, Object value) throws IOException {
      JavaType valueType = this._targetType;
      Class<?> builderRawType = this.handledType();
      Class<?> instRawType = value.getClass();
      return builderRawType.isAssignableFrom(instRawType)
         ? ctxt.reportBadDefinition(
            valueType, String.format("Deserialization of %s by passing existing Builder (%s) instance not supported", valueType, builderRawType.getName())
         )
         : ctxt.reportBadDefinition(
            valueType, String.format("Deserialization of %s by passing existing instance (of %s) not supported", valueType, instRawType.getName())
         );
   }

   private final Object vanillaDeserialize(JsonParser p, DeserializationContext ctxt, JsonToken t) throws IOException {
      Object bean;
      for(bean = this._valueInstantiator.createUsingDefault(ctxt); p.currentToken() == JsonToken.FIELD_NAME; p.nextToken()) {
         String propName = p.currentName();
         p.nextToken();
         SettableBeanProperty prop = this._beanProperties.find(propName);
         if (prop != null) {
            try {
               bean = prop.deserializeSetAndReturn(p, ctxt, bean);
            } catch (Exception var8) {
               this.wrapAndThrow(var8, bean, propName, ctxt);
            }
         } else {
            this.handleUnknownVanilla(p, ctxt, bean, propName);
         }
      }

      return bean;
   }

   @Override
   public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (this._nonStandardCreation) {
         if (this._unwrappedPropertyHandler != null) {
            return this.deserializeWithUnwrapped(p, ctxt);
         } else {
            return this._externalTypeIdHandler != null ? this.deserializeWithExternalTypeId(p, ctxt) : this.deserializeFromObjectUsingNonDefault(p, ctxt);
         }
      } else {
         Object bean = this._valueInstantiator.createUsingDefault(ctxt);
         if (this._injectables != null) {
            this.injectValues(ctxt, bean);
         }

         if (this._needViewProcesing) {
            Class<?> view = ctxt.getActiveView();
            if (view != null) {
               return this.deserializeWithView(p, ctxt, bean, view);
            }
         }

         for(; p.currentToken() == JsonToken.FIELD_NAME; p.nextToken()) {
            String propName = p.currentName();
            p.nextToken();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
               try {
                  bean = prop.deserializeSetAndReturn(p, ctxt, bean);
               } catch (Exception var7) {
                  this.wrapAndThrow(var7, bean, propName, ctxt);
               }
            } else {
               this.handleUnknownVanilla(p, ctxt, bean, propName);
            }
         }

         return bean;
      }
   }

   @Override
   protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) throws IOException {
      PropertyBasedCreator creator = this._propertyBasedCreator;
      PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, this._objectIdReader);
      Class<?> activeView = this._needViewProcesing ? ctxt.getActiveView() : null;
      TokenBuffer unknown = null;
      JsonToken t = p.currentToken();

      Object builder;
      while(true) {
         if (t != JsonToken.FIELD_NAME) {
            Object builder;
            try {
               builder = creator.build(ctxt, buffer);
            } catch (Exception var12) {
               builder = this.wrapInstantiationProblem(var12, ctxt);
            }

            if (unknown != null) {
               if (builder.getClass() != this._beanType.getRawClass()) {
                  return this.handlePolymorphic(null, ctxt, builder, unknown);
               }

               return this.handleUnknownProperties(ctxt, builder, unknown);
            }

            return builder;
         }

         String propName = p.currentName();
         p.nextToken();
         SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
         if (!buffer.readIdProperty(propName) || creatorProp != null) {
            if (creatorProp != null) {
               if (activeView != null && !creatorProp.visibleInView(activeView)) {
                  p.skipChildren();
               } else if (buffer.assignParameter(creatorProp, creatorProp.deserialize(p, ctxt))) {
                  p.nextToken();

                  try {
                     builder = creator.build(ctxt, buffer);
                     break;
                  } catch (Exception var13) {
                     this.wrapAndThrow(var13, this._beanType.getRawClass(), propName, ctxt);
                  }
               }
            } else {
               SettableBeanProperty prop = this._beanProperties.find(propName);
               if (prop != null) {
                  buffer.bufferProperty(prop, prop.deserialize(p, ctxt));
               } else if (IgnorePropertiesUtil.shouldIgnore(propName, this._ignorableProps, this._includableProps)) {
                  this.handleIgnoredProperty(p, ctxt, this.handledType(), propName);
               } else if (this._anySetter != null) {
                  buffer.bufferAnyProperty(this._anySetter, propName, this._anySetter.deserialize(p, ctxt));
               } else {
                  if (unknown == null) {
                     unknown = ctxt.bufferForInputBuffering(p);
                  }

                  unknown.writeFieldName(propName);
                  unknown.copyCurrentStructure(p);
               }
            }
         }

         t = p.nextToken();
      }

      if (builder.getClass() != this._beanType.getRawClass()) {
         return this.handlePolymorphic(p, ctxt, builder, unknown);
      } else {
         if (unknown != null) {
            builder = this.handleUnknownProperties(ctxt, builder, unknown);
         }

         return this._deserialize(p, ctxt, builder);
      }
   }

   protected final Object _deserialize(JsonParser p, DeserializationContext ctxt, Object builder) throws IOException {
      if (this._injectables != null) {
         this.injectValues(ctxt, builder);
      }

      if (this._unwrappedPropertyHandler != null) {
         if (p.hasToken(JsonToken.START_OBJECT)) {
            p.nextToken();
         }

         TokenBuffer tokens = ctxt.bufferForInputBuffering(p);
         tokens.writeStartObject();
         return this.deserializeWithUnwrapped(p, ctxt, builder, tokens);
      } else if (this._externalTypeIdHandler != null) {
         return this.deserializeWithExternalTypeId(p, ctxt, builder);
      } else {
         if (this._needViewProcesing) {
            Class<?> view = ctxt.getActiveView();
            if (view != null) {
               return this.deserializeWithView(p, ctxt, builder, view);
            }
         }

         JsonToken t = p.currentToken();
         if (t == JsonToken.START_OBJECT) {
            t = p.nextToken();
         }

         for(; t == JsonToken.FIELD_NAME; t = p.nextToken()) {
            String propName = p.currentName();
            p.nextToken();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
               try {
                  builder = prop.deserializeSetAndReturn(p, ctxt, builder);
               } catch (Exception var8) {
                  this.wrapAndThrow(var8, builder, propName, ctxt);
               }
            } else {
               this.handleUnknownVanilla(p, ctxt, builder, propName);
            }
         }

         return builder;
      }
   }

   @Override
   protected Object _deserializeFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonDeserializer<Object> delegateDeser = this._arrayDelegateDeserializer;
      if (delegateDeser == null) {
         delegateDeser = this._delegateDeserializer;
         if (this._delegateDeserializer == null) {
            CoercionAction act = this._findCoercionFromEmptyArray(ctxt);
            boolean unwrap = ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
            if (unwrap || act != CoercionAction.Fail) {
               JsonToken t = p.nextToken();
               if (t == JsonToken.END_ARRAY) {
                  switch(act) {
                     case AsEmpty:
                        return this.getEmptyValue(ctxt);
                     case AsNull:
                     case TryConvert:
                        return this.getNullValue(ctxt);
                     default:
                        return ctxt.handleUnexpectedToken(this.getValueType(ctxt), JsonToken.START_ARRAY, p, null);
                  }
               }

               if (unwrap) {
                  Object value = this.deserialize(p, ctxt);
                  if (p.nextToken() != JsonToken.END_ARRAY) {
                     this.handleMissingEndArrayForSingle(p, ctxt);
                  }

                  return value;
               }
            }

            return ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
         }
      }

      Object builder = this._valueInstantiator.createUsingArrayDelegate(ctxt, delegateDeser.deserialize(p, ctxt));
      if (this._injectables != null) {
         this.injectValues(ctxt, builder);
      }

      return this.finishBuild(ctxt, builder);
   }

   protected final Object deserializeWithView(JsonParser p, DeserializationContext ctxt, Object bean, Class<?> activeView) throws IOException {
      for(JsonToken t = p.currentToken(); t == JsonToken.FIELD_NAME; t = p.nextToken()) {
         String propName = p.currentName();
         p.nextToken();
         SettableBeanProperty prop = this._beanProperties.find(propName);
         if (prop != null) {
            if (!prop.visibleInView(activeView)) {
               p.skipChildren();
            } else {
               try {
                  bean = prop.deserializeSetAndReturn(p, ctxt, bean);
               } catch (Exception var9) {
                  this.wrapAndThrow(var9, bean, propName, ctxt);
               }
            }
         } else {
            this.handleUnknownVanilla(p, ctxt, bean, propName);
         }
      }

      return bean;
   }

   protected Object deserializeWithUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (this._delegateDeserializer != null) {
         return this._valueInstantiator.createUsingDelegate(ctxt, this._delegateDeserializer.deserialize(p, ctxt));
      } else if (this._propertyBasedCreator != null) {
         return this.deserializeUsingPropertyBasedWithUnwrapped(p, ctxt);
      } else {
         TokenBuffer tokens = ctxt.bufferForInputBuffering(p);
         tokens.writeStartObject();
         Object bean = this._valueInstantiator.createUsingDefault(ctxt);
         if (this._injectables != null) {
            this.injectValues(ctxt, bean);
         }

         for(Class<?> activeView = this._needViewProcesing ? ctxt.getActiveView() : null; p.currentToken() == JsonToken.FIELD_NAME; p.nextToken()) {
            String propName = p.currentName();
            p.nextToken();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
               if (activeView != null && !prop.visibleInView(activeView)) {
                  p.skipChildren();
               } else {
                  try {
                     bean = prop.deserializeSetAndReturn(p, ctxt, bean);
                  } catch (Exception var10) {
                     this.wrapAndThrow(var10, bean, propName, ctxt);
                  }
               }
            } else if (IgnorePropertiesUtil.shouldIgnore(propName, this._ignorableProps, this._includableProps)) {
               this.handleIgnoredProperty(p, ctxt, bean, propName);
            } else {
               tokens.writeFieldName(propName);
               tokens.copyCurrentStructure(p);
               if (this._anySetter != null) {
                  try {
                     this._anySetter.deserializeAndSet(p, ctxt, bean, propName);
                  } catch (Exception var9) {
                     this.wrapAndThrow(var9, bean, propName, ctxt);
                  }
               }
            }
         }

         tokens.writeEndObject();
         return this._unwrappedPropertyHandler.processUnwrapped(p, ctxt, bean, tokens);
      }
   }

   protected Object deserializeUsingPropertyBasedWithUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
      PropertyBasedCreator creator = this._propertyBasedCreator;
      PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, this._objectIdReader);
      TokenBuffer tokens = ctxt.bufferForInputBuffering(p);
      tokens.writeStartObject();
      Object builder = null;

      for(JsonToken t = p.currentToken(); t == JsonToken.FIELD_NAME; t = p.nextToken()) {
         String propName = p.currentName();
         p.nextToken();
         SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
         if (!buffer.readIdProperty(propName) || creatorProp != null) {
            if (creatorProp != null) {
               if (buffer.assignParameter(creatorProp, creatorProp.deserialize(p, ctxt))) {
                  t = p.nextToken();

                  try {
                     builder = creator.build(ctxt, buffer);
                     return builder.getClass() != this._beanType.getRawClass()
                        ? this.handlePolymorphic(p, ctxt, builder, tokens)
                        : this.deserializeWithUnwrapped(p, ctxt, builder, tokens);
                  } catch (Exception var12) {
                     this.wrapAndThrow(var12, this._beanType.getRawClass(), propName, ctxt);
                  }
               }
            } else {
               SettableBeanProperty prop = this._beanProperties.find(propName);
               if (prop != null) {
                  buffer.bufferProperty(prop, prop.deserialize(p, ctxt));
               } else if (IgnorePropertiesUtil.shouldIgnore(propName, this._ignorableProps, this._includableProps)) {
                  this.handleIgnoredProperty(p, ctxt, this.handledType(), propName);
               } else {
                  tokens.writeFieldName(propName);
                  tokens.copyCurrentStructure(p);
                  if (this._anySetter != null) {
                     buffer.bufferAnyProperty(this._anySetter, propName, this._anySetter.deserialize(p, ctxt));
                  }
               }
            }
         }
      }

      tokens.writeEndObject();
      if (builder == null) {
         try {
            builder = creator.build(ctxt, buffer);
         } catch (Exception var11) {
            return this.wrapInstantiationProblem(var11, ctxt);
         }
      }

      return this._unwrappedPropertyHandler.processUnwrapped(p, ctxt, builder, tokens);
   }

   protected Object deserializeWithUnwrapped(JsonParser p, DeserializationContext ctxt, Object builder, TokenBuffer tokens) throws IOException {
      Class<?> activeView = this._needViewProcesing ? ctxt.getActiveView() : null;

      for(JsonToken t = p.currentToken(); t == JsonToken.FIELD_NAME; t = p.nextToken()) {
         String propName = p.currentName();
         SettableBeanProperty prop = this._beanProperties.find(propName);
         p.nextToken();
         if (prop != null) {
            if (activeView != null && !prop.visibleInView(activeView)) {
               p.skipChildren();
            } else {
               try {
                  builder = prop.deserializeSetAndReturn(p, ctxt, builder);
               } catch (Exception var10) {
                  this.wrapAndThrow(var10, builder, propName, ctxt);
               }
            }
         } else if (IgnorePropertiesUtil.shouldIgnore(propName, this._ignorableProps, this._includableProps)) {
            this.handleIgnoredProperty(p, ctxt, builder, propName);
         } else {
            tokens.writeFieldName(propName);
            tokens.copyCurrentStructure(p);
            if (this._anySetter != null) {
               this._anySetter.deserializeAndSet(p, ctxt, builder, propName);
            }
         }
      }

      tokens.writeEndObject();
      return this._unwrappedPropertyHandler.processUnwrapped(p, ctxt, builder, tokens);
   }

   protected Object deserializeWithExternalTypeId(JsonParser p, DeserializationContext ctxt) throws IOException {
      return this._propertyBasedCreator != null
         ? this.deserializeUsingPropertyBasedWithExternalTypeId(p, ctxt)
         : this.deserializeWithExternalTypeId(p, ctxt, this._valueInstantiator.createUsingDefault(ctxt));
   }

   protected Object deserializeWithExternalTypeId(JsonParser p, DeserializationContext ctxt, Object bean) throws IOException {
      Class<?> activeView = this._needViewProcesing ? ctxt.getActiveView() : null;
      ExternalTypeHandler ext = this._externalTypeIdHandler.start();

      for(JsonToken t = p.currentToken(); t == JsonToken.FIELD_NAME; t = p.nextToken()) {
         String propName = p.currentName();
         t = p.nextToken();
         SettableBeanProperty prop = this._beanProperties.find(propName);
         if (prop != null) {
            if (t.isScalarValue()) {
               ext.handleTypePropertyValue(p, ctxt, propName, bean);
            }

            if (activeView != null && !prop.visibleInView(activeView)) {
               p.skipChildren();
            } else {
               try {
                  bean = prop.deserializeSetAndReturn(p, ctxt, bean);
               } catch (Exception var11) {
                  this.wrapAndThrow(var11, bean, propName, ctxt);
               }
            }
         } else if (IgnorePropertiesUtil.shouldIgnore(propName, this._ignorableProps, this._includableProps)) {
            this.handleIgnoredProperty(p, ctxt, bean, propName);
         } else if (!ext.handlePropertyValue(p, ctxt, propName, bean)) {
            if (this._anySetter != null) {
               try {
                  this._anySetter.deserializeAndSet(p, ctxt, bean, propName);
               } catch (Exception var10) {
                  this.wrapAndThrow(var10, bean, propName, ctxt);
               }
            } else {
               this.handleUnknownProperty(p, ctxt, bean, propName);
            }
         }
      }

      return ext.complete(p, ctxt, bean);
   }

   protected Object deserializeUsingPropertyBasedWithExternalTypeId(JsonParser p, DeserializationContext ctxt) throws IOException {
      JavaType t = this._targetType;
      return ctxt.reportBadDefinition(t, String.format("Deserialization (of %s) with Builder, External type id, @JsonCreator not yet implemented", t));
   }
}
