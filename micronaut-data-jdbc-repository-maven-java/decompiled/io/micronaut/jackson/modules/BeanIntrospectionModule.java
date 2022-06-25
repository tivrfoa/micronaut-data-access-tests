package io.micronaut.jackson.modules;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.MethodProperty;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import com.fasterxml.jackson.databind.introspect.DefaultAccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.introspect.VirtualAnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotatedElement;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.naming.Named;
import io.micronaut.core.reflect.exception.InstantiationException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.jackson.JacksonConfiguration;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
@Singleton
@Requires(
   property = "jackson.bean-introspection-module",
   notEquals = "false"
)
public class BeanIntrospectionModule extends SimpleModule {
   private static final Logger LOG = LoggerFactory.getLogger(BeanIntrospectionModule.class);
   boolean ignoreReflectiveProperties = false;

   public BeanIntrospectionModule() {
      this.setDeserializerModifier(new BeanIntrospectionModule.BeanIntrospectionDeserializerModifier());
      this.setSerializerModifier(new BeanIntrospectionModule.BeanIntrospectionSerializerModifier());
   }

   @Override
   public void setupModule(Module.SetupContext context) {
      super.setupModule(context);
      ObjectCodec owner = context.getOwner();
      if (owner instanceof ObjectMapper) {
         ObjectMapper mapper = (ObjectMapper)owner;
         mapper.setConfig(
            mapper.getSerializationConfig()
               .with(new BeanIntrospectionModule.BeanIntrospectionAccessorNamingStrategyProvider(mapper.getSerializationConfig().getAccessorNaming()))
         );
         mapper.setConfig(
            mapper.getDeserializationConfig()
               .with(new BeanIntrospectionModule.BeanIntrospectionAccessorNamingStrategyProvider(mapper.getDeserializationConfig().getAccessorNaming()))
         );
      }

   }

   @Nullable
   protected BeanIntrospection<Object> findIntrospection(Class<?> beanClass) {
      return (BeanIntrospection<Object>)BeanIntrospector.SHARED.findIntrospection(beanClass).orElse(null);
   }

   private JavaType newType(Argument<?> argument, TypeFactory typeFactory) {
      return JacksonConfiguration.constructType(argument, typeFactory);
   }

   private PropertyMetadata newPropertyMetadata(Argument<?> argument, AnnotationMetadata annotationMetadata) {
      Boolean required = argument.isNonNull() || annotationMetadata.booleanValue(JsonProperty.class, "required").orElse(false);
      int index = annotationMetadata.intValue(JsonProperty.class, "index").orElse(-1);
      return PropertyMetadata.construct(
         required,
         (String)annotationMetadata.stringValue(JsonPropertyDescription.class).orElse(null),
         index > -1 ? index : null,
         (String)annotationMetadata.stringValue(JsonProperty.class, "defaultValue").orElse(null)
      );
   }

   private AnnotatedMember createVirtualMember(
      TypeResolutionContext typeResolutionContext, Class<?> beanClass, String name, JavaType javaType, AnnotationMetadata annotationMetadata
   ) {
      return new VirtualAnnotatedMember(typeResolutionContext, beanClass, name, javaType) {
         @Override
         public boolean hasOneOf(Class<? extends Annotation>[] annoClasses) {
            return Arrays.stream(annoClasses).anyMatch(annotationMetadata::hasAnnotation);
         }
      };
   }

   private static boolean suppressNulls(JsonInclude.Value inclusion) {
      if (inclusion == null) {
         return false;
      } else {
         JsonInclude.Include incl = inclusion.getValueInclusion();
         return incl != JsonInclude.Include.ALWAYS && incl != JsonInclude.Include.USE_DEFAULTS;
      }
   }

   private static Object suppressableValue(JsonInclude.Value inclusion) {
      if (inclusion == null) {
         return false;
      } else {
         JsonInclude.Include incl = inclusion.getValueInclusion();
         return incl != JsonInclude.Include.ALWAYS && incl != JsonInclude.Include.NON_NULL && incl != JsonInclude.Include.USE_DEFAULTS
            ? BeanPropertyWriter.MARKER_FOR_EMPTY
            : null;
      }
   }

   private <T> T findSerializerFromAnnotation(BeanProperty<?, ?> beanProperty, Class<? extends Annotation> annotationType) {
      AnnotationValue<?> jsonSerializeAnnotation = beanProperty.getAnnotation(annotationType);
      if (jsonSerializeAnnotation != null) {
         Class using = (Class)jsonSerializeAnnotation.classValue("using").orElse(null);
         if (using != null) {
            BeanIntrospection<Object> usingIntrospection = this.findIntrospection(using);
            if (usingIntrospection != null) {
               return (T)usingIntrospection.instantiate();
            }

            if (LOG.isWarnEnabled()) {
               LOG.warn("Cannot construct {}, please add the @Introspected annotation to the class declaration", using.getName());
            }
         }
      }

      return null;
   }

   @NonNull
   private JsonFormat.Value parseJsonFormat(@NonNull AnnotationValue<JsonFormat> formatAnnotation) {
      return new JsonFormat.Value(
         (String)formatAnnotation.stringValue("pattern").orElse(""),
         (JsonFormat.Shape)formatAnnotation.enumValue("shape", JsonFormat.Shape.class).orElse(JsonFormat.Shape.ANY),
         (String)formatAnnotation.stringValue("locale").orElse("##default"),
         (String)formatAnnotation.stringValue("timezone").orElse("##default"),
         JsonFormat.Features.construct(
            formatAnnotation.enumValues("with", JsonFormat.Feature.class), formatAnnotation.enumValues("without", JsonFormat.Feature.class)
         ),
         ((OptBoolean)formatAnnotation.enumValue("lenient", OptBoolean.class).orElse(OptBoolean.DEFAULT)).asBoolean()
      );
   }

   @Nullable
   private PropertyNamingStrategy findNamingStrategy(MapperConfig<?> mapperConfig, BeanIntrospection<?> introspection) {
      AnnotationValue<JsonNaming> namingAnnotation = introspection.getAnnotation(JsonNaming.class);
      if (namingAnnotation != null) {
         Optional<Class<?>> clazz = namingAnnotation.classValue();
         if (clazz.isPresent()) {
            try {
               Constructor<?> emptyConstructor = ((Class)clazz.get()).getConstructor();
               return (PropertyNamingStrategy)emptyConstructor.newInstance();
            } catch (NoSuchMethodException var6) {
               return mapperConfig.getPropertyNamingStrategy();
            } catch (ReflectiveOperationException var7) {
               throw new RuntimeException("Failed to construct configured PropertyNamingStrategy", var7);
            }
         }
      }

      return mapperConfig.getPropertyNamingStrategy();
   }

   private String getName(MapperConfig<?> mapperConfig, @Nullable PropertyNamingStrategy namingStrategy, AnnotatedElement property) {
      String explicitName = (String)property.getAnnotationMetadata().stringValue(JsonProperty.class).orElse("");
      if (!explicitName.equals("")) {
         return explicitName;
      } else {
         String implicitName = property.getName();
         return namingStrategy != null ? namingStrategy.nameForGetterMethod(mapperConfig, null, implicitName) : implicitName;
      }
   }

   private class BeanIntrospectionAccessorNamingStrategyProvider extends AccessorNamingStrategy.Provider {
      private final AccessorNamingStrategy.Provider delegate;

      BeanIntrospectionAccessorNamingStrategyProvider(AccessorNamingStrategy.Provider delegate) {
         this.delegate = delegate;
      }

      @Override
      public AccessorNamingStrategy forPOJO(MapperConfig<?> config, AnnotatedClass valueClass) {
         return this.delegate.forPOJO(config, valueClass);
      }

      @Override
      public AccessorNamingStrategy forBuilder(MapperConfig<?> config, AnnotatedClass builderClass, BeanDescription valueTypeDesc) {
         return this.delegate.forBuilder(config, builderClass, valueTypeDesc);
      }

      @Override
      public AccessorNamingStrategy forRecord(MapperConfig<?> config, AnnotatedClass recordClass) {
         final BeanIntrospection<Object> introspection = BeanIntrospectionModule.this.findIntrospection(recordClass.getRawType());
         if (introspection != null) {
            return new DefaultAccessorNamingStrategy(config, recordClass, null, "get", "is", null) {
               final Set<String> names = (Set<String>)introspection.getBeanProperties().stream().map(Named::getName).collect(Collectors.toSet());

               @Override
               public String findNameForRegularGetter(AnnotatedMethod am, String name) {
                  return this.names.contains(name) ? name : super.findNameForRegularGetter(am, name);
               }
            };
         } else {
            try {
               return this.delegate.forRecord(config, recordClass);
            } catch (IllegalArgumentException var5) {
               if (var5.getMessage().startsWith("Failed to access RecordComponents of type")) {
                  throw new RuntimeException(
                     "Failed to construct AccessorNamingStrategy for record. This can happen when running in native-image. Either make this type @Introspected, or mark it for @ReflectiveAccess.",
                     var5
                  );
               } else {
                  throw var5;
               }
            }
         }
      }
   }

   private class BeanIntrospectionDeserializerModifier extends BeanDeserializerModifier {
      private BeanIntrospectionDeserializerModifier() {
      }

      @Override
      public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
         if (builder.getValueInstantiator().getDelegateType(config) != null) {
            return builder;
         } else {
            final Class<?> beanClass = beanDesc.getBeanClass();
            final BeanIntrospection<Object> introspection = BeanIntrospectionModule.this.findIntrospection(beanClass);
            if (introspection == null) {
               return builder;
            } else {
               final PropertyNamingStrategy propertyNamingStrategy = BeanIntrospectionModule.this.findNamingStrategy(config, introspection);
               Iterator<SettableBeanProperty> properties = builder.getProperties();
               if ((BeanIntrospectionModule.this.ignoreReflectiveProperties || !properties.hasNext()) && introspection.getPropertyNames().length > 0) {
                  for(BeanProperty<Object, Object> beanProperty : introspection.getBeanProperties()) {
                     if (!beanProperty.isReadOnly()) {
                        builder.addOrReplaceProperty(
                           BeanIntrospectionModule.this.new VirtualSetter(
                              beanDesc.getClassInfo(),
                              config.getTypeFactory(),
                              beanProperty,
                              BeanIntrospectionModule.this.getName(config, propertyNamingStrategy, beanProperty),
                              BeanIntrospectionModule.this.findSerializerFromAnnotation(beanProperty, JsonDeserialize.class)
                           ),
                           true
                        );
                     }
                  }
               } else {
                  Map<String, BeanProperty<Object, Object>> remainingProperties = new LinkedHashMap();

                  for(BeanProperty<Object, Object> beanProperty : introspection.getBeanProperties()) {
                     if (!beanProperty.isAnnotationPresent(JsonIgnore.class)) {
                        remainingProperties.put(BeanIntrospectionModule.this.getName(config, propertyNamingStrategy, beanProperty), beanProperty);
                     }
                  }

                  while(properties.hasNext()) {
                     SettableBeanProperty settableBeanProperty = (SettableBeanProperty)properties.next();
                     if (settableBeanProperty instanceof MethodProperty) {
                        MethodProperty methodProperty = (MethodProperty)settableBeanProperty;
                        BeanProperty<Object, Object> beanProperty = (BeanProperty)remainingProperties.remove(settableBeanProperty.getName());
                        if (beanProperty != null && !beanProperty.isReadOnly()) {
                           SettableBeanProperty newProperty = new BeanIntrospectionModule.BeanIntrospectionSetter(methodProperty, beanProperty);
                           builder.addOrReplaceProperty(newProperty, true);
                        }
                     }
                  }

                  for(Entry<String, BeanProperty<Object, Object>> entry : remainingProperties.entrySet()) {
                     if (!((BeanProperty)entry.getValue()).isReadOnly()) {
                        SettableBeanProperty existing = builder.findProperty(PropertyName.construct((String)entry.getKey()));
                        if (existing == null) {
                           builder.addOrReplaceProperty(
                              BeanIntrospectionModule.this.new VirtualSetter(
                                 beanDesc.getClassInfo(),
                                 config.getTypeFactory(),
                                 (BeanProperty<?, ?>)entry.getValue(),
                                 (String)entry.getKey(),
                                 BeanIntrospectionModule.this.findSerializerFromAnnotation((BeanProperty<?, ?>)entry.getValue(), JsonDeserialize.class)
                              ),
                              true
                           );
                        }
                     }
                  }
               }

               final Argument<?>[] constructorArguments = introspection.getConstructorArguments();
               final TypeFactory typeFactory = config.getTypeFactory();
               final ValueInstantiator defaultInstantiator = builder.getValueInstantiator();
               builder.setValueInstantiator(
                  new StdValueInstantiator(config, typeFactory.constructType(beanClass)) {
                     SettableBeanProperty[] props;
   
                     @Override
                     public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
                        SettableBeanProperty[] existing = BeanIntrospectionModule.this.ignoreReflectiveProperties
                           ? null
                           : defaultInstantiator.getFromObjectArguments(config);
                        if (this.props == null) {
                           this.props = new SettableBeanProperty[constructorArguments.length];
   
                           for(int i = 0; i < constructorArguments.length; ++i) {
                              final Argument<?> argument = constructorArguments[i];
                              SettableBeanProperty existingProperty = existing != null && existing.length > i ? existing[i] : null;
                              final JavaType javaType = existingProperty != null
                                 ? existingProperty.getType()
                                 : BeanIntrospectionModule.this.newType(argument, typeFactory);
                              final AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
                              PropertyMetadata propertyMetadata = BeanIntrospectionModule.this.newPropertyMetadata(argument, annotationMetadata);
                              String simpleName = existingProperty != null
                                 ? existingProperty.getName()
                                 : BeanIntrospectionModule.this.getName(config, propertyNamingStrategy, argument);
   
                              TypeDeserializer typeDeserializer;
                              try {
                                 typeDeserializer = config.findTypeDeserializer(javaType);
                              } catch (JsonMappingException var13) {
                                 typeDeserializer = null;
                              }
   
                              PropertyName propertyName = PropertyName.construct(simpleName);
                              if (typeDeserializer == null) {
                                 SettableBeanProperty settableBeanProperty = builder.findProperty(propertyName);
                                 if (settableBeanProperty != null) {
                                    typeDeserializer = settableBeanProperty.getValueTypeDeserializer();
                                 }
                              }
   
                              this.props[i] = new CreatorProperty(propertyName, javaType, null, typeDeserializer, null, null, i, null, propertyMetadata) {
                                 private final BeanProperty<Object, Object> property = (BeanProperty<Object, Object>)introspection.getProperty(
                                       argument.getName()
                                    )
                                    .orElse(null);
   
                                 @Override
                                 public <A extends Annotation> A getAnnotation(Class<A> acls) {
                                    return annotationMetadata.synthesize(acls);
                                 }
   
                                 @Override
                                 public AnnotatedMember getMember() {
                                    return BeanIntrospectionModule.this.createVirtualMember(
                                       beanDesc.getClassInfo(), beanClass, argument.getName(), javaType, annotationMetadata
                                    );
                                 }
   
                                 @Override
                                 public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
                                    if (this.property != null) {
                                       this.property.set(instance, this.deserialize(p, ctxt));
                                    }
   
                                 }
   
                                 @Override
                                 public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
                                    if (this.property != null) {
                                       this.property.set(instance, this.deserialize(p, ctxt));
                                    }
   
                                    return null;
                                 }
   
                                 @Override
                                 public void set(Object instance, Object value) {
                                    if (this.property != null) {
                                       this.property.set(instance, value);
                                    }
   
                                 }
   
                                 @Override
                                 public Object setAndReturn(Object instance, Object value) throws IOException {
                                    if (this.property != null) {
                                       this.property.set(instance, value);
                                    }
   
                                    return null;
                                 }
                              };
                           }
                        }
   
                        return this.props;
                     }
   
                     @Override
                     public boolean canInstantiate() {
                        return true;
                     }
   
                     @Override
                     public boolean canCreateUsingDefault() {
                        return constructorArguments.length == 0;
                     }
   
                     @Override
                     public boolean canCreateFromObjectWith() {
                        return constructorArguments.length > 0;
                     }
   
                     @Override
                     public boolean canCreateUsingArrayDelegate() {
                        return defaultInstantiator.canCreateUsingArrayDelegate();
                     }
   
                     @Override
                     public boolean canCreateUsingDelegate() {
                        return false;
                     }
   
                     @Override
                     public JavaType getArrayDelegateType(DeserializationConfig config) {
                        return BeanIntrospectionModule.this.newType(constructorArguments[0], typeFactory);
                     }
   
                     @Override
                     public JavaType getDelegateType(DeserializationConfig config) {
                        return BeanIntrospectionModule.this.newType(constructorArguments[0], typeFactory);
                     }
   
                     @Override
                     public boolean canCreateFromString() {
                        return constructorArguments.length == 1 && constructorArguments[0].equalsType(Argument.STRING);
                     }
   
                     @Override
                     public boolean canCreateFromInt() {
                        return constructorArguments.length == 1
                           && (constructorArguments[0].equalsType(Argument.INT) || constructorArguments[0].equalsType(Argument.LONG));
                     }
   
                     @Override
                     public boolean canCreateFromLong() {
                        return constructorArguments.length == 1 && constructorArguments[0].equalsType(Argument.LONG);
                     }
   
                     @Override
                     public boolean canCreateFromDouble() {
                        return constructorArguments.length == 1 && constructorArguments[0].equalsType(Argument.DOUBLE);
                     }
   
                     @Override
                     public boolean canCreateFromBoolean() {
                        return constructorArguments.length == 1 && constructorArguments[0].equalsType(Argument.BOOLEAN);
                     }
   
                     @Override
                     public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
                        return introspection.instantiate();
                     }
   
                     @Override
                     public Object createUsingDelegate(DeserializationContext ctxt, Object delegate) throws IOException {
                        return introspection.instantiate(false, delegate);
                     }
   
                     @Override
                     public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws IOException {
                        return introspection.instantiate(false, args);
                     }
   
                     @Override
                     public Object createUsingArrayDelegate(DeserializationContext ctxt, Object delegate) throws IOException {
                        return introspection.instantiate(false, delegate);
                     }
   
                     @Override
                     public Object createFromString(DeserializationContext ctxt, String value) throws IOException {
                        return introspection.instantiate(false, value);
                     }
   
                     @Override
                     public Object createFromInt(DeserializationContext ctxt, int value) throws IOException {
                        try {
                           return introspection.instantiate(false, value);
                        } catch (InstantiationException var6) {
                           try {
                              return introspection.instantiate(false, (long)value);
                           } catch (InstantiationException var5) {
                              throw var6;
                           }
                        }
                     }
   
                     @Override
                     public Object createFromLong(DeserializationContext ctxt, long value) throws IOException {
                        return introspection.instantiate(false, value);
                     }
   
                     @Override
                     public Object createFromDouble(DeserializationContext ctxt, double value) throws IOException {
                        return introspection.instantiate(false, value);
                     }
   
                     @Override
                     public Object createFromBoolean(DeserializationContext ctxt, boolean value) throws IOException {
                        return introspection.instantiate(false, value);
                     }
                  }
               );
               return builder;
            }
         }
      }
   }

   private class BeanIntrospectionPropertyWriter extends BeanPropertyWriter {
      protected final Class<?>[] _views;
      final BeanProperty<Object, Object> beanProperty;
      final SerializableString fastName;
      private final JavaType type;
      private final boolean unwrapping;

      BeanIntrospectionPropertyWriter(
         BeanPropertyWriter src, BeanProperty<Object, Object> introspection, JsonSerializer<Object> ser, TypeFactory typeFactory, Class<?>[] views
      ) {
         this(src.getSerializedName(), src, introspection, ser, typeFactory, views);
      }

      BeanIntrospectionPropertyWriter(
         SerializableString name,
         BeanPropertyWriter src,
         BeanProperty<Object, Object> introspection,
         JsonSerializer<Object> ser,
         TypeFactory typeFactory,
         Class<?>[] views
      ) {
         super(src);
         this._serializer = ser != null ? ser : src.getSerializer();
         this.beanProperty = introspection;
         this.fastName = name;
         this._views = views;
         this.type = JacksonConfiguration.constructType(this.beanProperty.asArgument(), typeFactory);
         this._dynamicSerializers = ser == null ? PropertySerializerMap.emptyForProperties() : null;
         this.unwrapping = introspection.hasAnnotation(JsonUnwrapped.class);
      }

      BeanIntrospectionPropertyWriter(
         AnnotatedMember virtualMember,
         SerializationConfig config,
         String name,
         BeanProperty<Object, Object> introspection,
         TypeFactory typeFactory,
         JsonSerializer<?> ser
      ) {
         super(
            SimpleBeanPropertyDefinition.construct(config, virtualMember),
            virtualMember,
            AnnotationCollector.emptyAnnotations(),
            null,
            ser,
            null,
            null,
            BeanIntrospectionModule.suppressNulls(config.getDefaultPropertyInclusion()),
            BeanIntrospectionModule.suppressableValue(config.getDefaultPropertyInclusion()),
            null
         );
         this.beanProperty = introspection;
         this.fastName = new SerializedString(name);
         this._views = null;
         this.type = JacksonConfiguration.constructType(this.beanProperty.asArgument(), typeFactory);
         this._dynamicSerializers = PropertySerializerMap.emptyForProperties();
         this.unwrapping = introspection.hasAnnotation(JsonUnwrapped.class);
      }

      @Override
      public boolean isUnwrapping() {
         return this.unwrapping;
      }

      @Override
      public String getName() {
         return this.fastName.getValue();
      }

      @Override
      public PropertyName getFullName() {
         return new PropertyName(this.getName());
      }

      @Override
      public void fixAccess(SerializationConfig config) {
      }

      @Override
      public JavaType getType() {
         return this.type;
      }

      private boolean inView(Class<?> activeView) {
         if (activeView != null && this._views != null) {
            int len = this._views.length;

            for(int i = 0; i < len; ++i) {
               if (this._views[i].isAssignableFrom(activeView)) {
                  return true;
               }
            }

            return false;
         } else {
            return true;
         }
      }

      @Override
      public final void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
         if (!this.inView(prov.getActiveView())) {
            this.serializeAsOmittedField(bean, gen, prov);
         } else {
            Object value = this.beanProperty.get(bean);
            if (value != null) {
               JsonSerializer<Object> ser = this._serializer;
               if (ser == null) {
                  Class<?> cls = value.getClass();
                  PropertySerializerMap map = this._dynamicSerializers;
                  ser = map.serializerFor(cls);
                  if (ser == null) {
                     ser = this._findAndAddDynamic(map, cls, prov);
                  }
               }

               if (this._suppressableValue != null) {
                  if (MARKER_FOR_EMPTY == this._suppressableValue) {
                     if (ser.isEmpty(prov, value)) {
                        return;
                     }
                  } else if (this._suppressableValue.equals(value)) {
                     return;
                  }
               }

               if (value != bean || !this._handleSelfReference(bean, gen, prov, ser)) {
                  if (this.isUnwrapping()) {
                     JsonSerializer<Object> unwrappingSerializer = ser.unwrappingSerializer(null);
                     unwrappingSerializer.serialize(value, gen, prov);
                  } else {
                     gen.writeFieldName(this.fastName);
                     if (this._typeSerializer == null) {
                        ser.serialize(value, gen, prov);
                     } else {
                        ser.serializeWithType(value, gen, prov, this._typeSerializer);
                     }
                  }

               }
            } else {
               boolean willSuppressNulls = this.willSuppressNulls();
               if (!willSuppressNulls && this._nullSerializer != null) {
                  if (!this.isUnwrapping()) {
                     gen.writeFieldName(this.fastName);
                     this._nullSerializer.serialize(null, gen, prov);
                  }
               } else if (!willSuppressNulls) {
                  gen.writeFieldName(this.fastName);
                  prov.defaultSerializeNull(gen);
               }

            }
         }
      }

      @Override
      public final void serializeAsElement(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
         if (!this.inView(prov.getActiveView())) {
            this.serializeAsOmittedField(bean, gen, prov);
         } else {
            Object value = this.beanProperty.get(bean);
            if (value != null) {
               JsonSerializer<Object> ser = this._serializer;
               if (ser == null) {
                  Class<?> cls = value.getClass();
                  PropertySerializerMap map = this._dynamicSerializers;
                  ser = map.serializerFor(cls);
                  if (ser == null) {
                     ser = this._findAndAddDynamic(map, cls, prov);
                  }
               }

               if (this._suppressableValue != null) {
                  if (MARKER_FOR_EMPTY == this._suppressableValue) {
                     if (ser.isEmpty(prov, value)) {
                        this.serializeAsPlaceholder(bean, gen, prov);
                        return;
                     }
                  } else if (this._suppressableValue.equals(value)) {
                     this.serializeAsPlaceholder(bean, gen, prov);
                     return;
                  }
               }

               if (value != bean || !this._handleSelfReference(bean, gen, prov, ser)) {
                  if (this._typeSerializer == null) {
                     ser.serialize(value, gen, prov);
                  } else {
                     ser.serializeWithType(value, gen, prov, this._typeSerializer);
                  }

               }
            } else {
               boolean willSuppressNulls = this.willSuppressNulls();
               if (!willSuppressNulls && this._nullSerializer != null) {
                  this._nullSerializer.serialize(null, gen, prov);
               } else if (willSuppressNulls) {
                  this.serializeAsPlaceholder(bean, gen, prov);
               } else {
                  prov.defaultSerializeNull(gen);
               }

            }
         }
      }

      @Override
      public JsonFormat.Value findPropertyFormat(MapperConfig<?> config, Class<?> baseType) {
         JsonFormat.Value v1 = config.getDefaultPropertyFormat(baseType);
         JsonFormat.Value v2 = null;
         AnnotationValue<JsonFormat> formatAnnotation = this.beanProperty.getAnnotation(JsonFormat.class);
         if (formatAnnotation != null) {
            v2 = BeanIntrospectionModule.this.parseJsonFormat(formatAnnotation);
         }

         if (v1 == null) {
            return v2 == null ? EMPTY_FORMAT : v2;
         } else {
            return v2 == null ? v1 : v1.withOverrides(v2);
         }
      }
   }

   private class BeanIntrospectionSerializerModifier extends BeanSerializerModifier {
      private BeanIntrospectionSerializerModifier() {
      }

      @Override
      public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc, BeanSerializerBuilder builder) {
         final Class<?> beanClass = beanDesc.getBeanClass();
         BeanIntrospection<Object> introspection = BeanIntrospectionModule.this.findIntrospection(beanClass);
         if (introspection == null) {
            return super.updateBuilder(config, beanDesc, builder);
         } else {
            PropertyNamingStrategy namingStrategy = BeanIntrospectionModule.this.findNamingStrategy(config, introspection);
            BeanSerializerBuilder newBuilder = new BeanSerializerBuilder(beanDesc) {
               @Override
               public JsonSerializer<?> build() {
                  this.setConfig(config);

                  try {
                     return super.build();
                  } catch (RuntimeException var2) {
                     if (BeanIntrospectionModule.LOG.isErrorEnabled()) {
                        BeanIntrospectionModule.LOG.error("Error building bean serializer for type [" + beanClass + "]: " + var2.getMessage(), var2);
                     }

                     throw var2;
                  }
               }
            };
            newBuilder.setAnyGetter(builder.getAnyGetter());
            List<BeanPropertyWriter> properties = builder.getProperties();
            Collection<BeanProperty<Object, Object>> beanProperties = introspection.getBeanProperties();
            if (BeanIntrospectionModule.this.ignoreReflectiveProperties || CollectionUtils.isEmpty(properties) && CollectionUtils.isNotEmpty(beanProperties)) {
               if (BeanIntrospectionModule.LOG.isDebugEnabled()) {
                  BeanIntrospectionModule.LOG.debug("Bean {} has no properties, while BeanIntrospection does. Recreating from introspection.", beanClass);
               }

               TypeResolutionContext typeResolutionContext = new TypeResolutionContext.Empty(config.getTypeFactory());
               List<BeanPropertyWriter> newProperties = new ArrayList(beanProperties.size());

               for(BeanProperty<Object, Object> beanProperty : beanProperties) {
                  if (!beanProperty.hasAnnotation(JsonIgnore.class)) {
                     String propertyName = BeanIntrospectionModule.this.getName(config, namingStrategy, beanProperty);
                     BeanPropertyWriter writer = BeanIntrospectionModule.this.new BeanIntrospectionPropertyWriter(
                        BeanIntrospectionModule.this.createVirtualMember(
                           typeResolutionContext,
                           beanProperty.getDeclaringType(),
                           propertyName,
                           BeanIntrospectionModule.this.newType(beanProperty.asArgument(), config.getTypeFactory()),
                           beanProperty
                        ),
                        config,
                        propertyName,
                        beanProperty,
                        config.getTypeFactory(),
                        BeanIntrospectionModule.this.findSerializerFromAnnotation(beanProperty, JsonSerialize.class)
                     );
                     newProperties.add(writer);
                  }
               }

               newBuilder.setProperties(newProperties);
            } else {
               if (BeanIntrospectionModule.LOG.isDebugEnabled()) {
                  BeanIntrospectionModule.LOG.debug("Updating {} properties with BeanIntrospection data for type: {}", properties.size(), beanClass);
               }

               List<BeanPropertyWriter> newProperties = new ArrayList(properties);
               Map<String, BeanProperty<Object, Object>> named = new LinkedHashMap();

               for(BeanProperty<Object, Object> beanProperty : beanProperties) {
                  named.put(BeanIntrospectionModule.this.getName(config, namingStrategy, beanProperty), beanProperty);
               }

               for(int i = 0; i < properties.size(); ++i) {
                  BeanPropertyWriter existing = (BeanPropertyWriter)properties.get(i);
                  Optional<BeanProperty<Object, Object>> property = Optional.ofNullable(named.get(existing.getName()));
                  if (property.isPresent()
                     && !((BeanProperty)property.get()).isAnnotationPresent(JsonIgnore.class)
                     && !existing.getClass().getName().equals("com.fasterxml.jackson.dataformat.xml.ser.XmlBeanPropertyWriter")) {
                     BeanProperty<Object, Object> beanProperty = (BeanProperty)property.get();
                     newProperties.set(
                        i,
                        BeanIntrospectionModule.this.new BeanIntrospectionPropertyWriter(
                           existing, beanProperty, existing.getSerializer(), config.getTypeFactory(), existing.getViews()
                        )
                     );
                  } else {
                     newProperties.set(i, existing);
                  }
               }

               newBuilder.setProperties(newProperties);
            }

            newBuilder.setFilteredProperties(builder.getFilteredProperties());
            return newBuilder;
         }
      }
   }

   private static class BeanIntrospectionSetter extends SettableBeanProperty.Delegating {
      final BeanProperty beanProperty;

      BeanIntrospectionSetter(SettableBeanProperty methodProperty, BeanProperty beanProperty) {
         super(methodProperty);
         this.beanProperty = beanProperty;
      }

      @Override
      protected SettableBeanProperty withDelegate(SettableBeanProperty d) {
         return new BeanIntrospectionModule.BeanIntrospectionSetter(d, this.beanProperty);
      }

      @Override
      public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
         this.beanProperty.set(instance, this.deserialize(p, ctxt));
      }

      @Override
      public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
         this.beanProperty.set(instance, this.deserialize(p, ctxt));
         return null;
      }

      @Override
      public void set(Object instance, Object value) {
         this.beanProperty.set(instance, value);
      }

      @Override
      public Object setAndReturn(Object instance, Object value) {
         this.beanProperty.set(instance, value);
         return null;
      }
   }

   private class VirtualSetter extends SettableBeanProperty {
      final BeanProperty beanProperty;
      final TypeResolutionContext typeResolutionContext;

      VirtualSetter(
         TypeResolutionContext typeResolutionContext,
         TypeFactory typeFactory,
         BeanProperty<?, ?> beanProperty,
         String propertyName,
         JsonDeserializer<Object> valueDeser
      ) {
         super(
            new PropertyName(propertyName),
            BeanIntrospectionModule.this.newType(beanProperty.asArgument(), typeFactory),
            BeanIntrospectionModule.this.newPropertyMetadata(beanProperty.asArgument(), beanProperty.getAnnotationMetadata()),
            valueDeser
         );
         this.beanProperty = beanProperty;
         this.typeResolutionContext = typeResolutionContext;
      }

      VirtualSetter(PropertyName propertyName, BeanIntrospectionModule.VirtualSetter src) {
         super(propertyName, src._type, src._metadata, src._valueDeserializer);
         this.beanProperty = src.beanProperty;
         this.typeResolutionContext = src.typeResolutionContext;
      }

      VirtualSetter(NullValueProvider nullValueProvider, BeanIntrospectionModule.VirtualSetter src) {
         super(src, src._valueDeserializer, nullValueProvider);
         this.beanProperty = src.beanProperty;
         this.typeResolutionContext = src.typeResolutionContext;
      }

      VirtualSetter(JsonDeserializer<Object> deser, BeanIntrospectionModule.VirtualSetter src) {
         super(src._propName, src._type, src._metadata, deser);
         this.beanProperty = src.beanProperty;
         this.typeResolutionContext = src.typeResolutionContext;
      }

      @Override
      public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
         return BeanIntrospectionModule.this.new VirtualSetter(deser, this);
      }

      @Override
      public SettableBeanProperty withName(PropertyName newName) {
         return BeanIntrospectionModule.this.new VirtualSetter(newName, this);
      }

      @Override
      public SettableBeanProperty withNullProvider(NullValueProvider nva) {
         return BeanIntrospectionModule.this.new VirtualSetter(nva, this);
      }

      @Override
      public AnnotatedMember getMember() {
         return BeanIntrospectionModule.this.createVirtualMember(
            this.typeResolutionContext, this.beanProperty.getDeclaringType(), this._propName.getSimpleName(), this._type, this.beanProperty
         );
      }

      @Override
      public <A extends Annotation> A getAnnotation(Class<A> acls) {
         return this.beanProperty.getAnnotationMetadata().synthesize(acls);
      }

      @Override
      public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
         this.beanProperty.set(instance, this.deserialize(p, ctxt));
      }

      @Override
      public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
         this.beanProperty.set(instance, this.deserialize(p, ctxt));
         return null;
      }

      @Override
      public void set(Object instance, Object value) throws IOException {
         this.beanProperty.set(instance, value);
      }

      @Override
      public Object setAndReturn(Object instance, Object value) throws IOException {
         this.beanProperty.set(instance, value);
         return null;
      }

      @Override
      public JsonFormat.Value findPropertyFormat(MapperConfig<?> config, Class<?> baseType) {
         JsonFormat.Value v1 = config.getDefaultPropertyFormat(baseType);
         JsonFormat.Value v2 = null;
         AnnotationValue<JsonFormat> formatAnnotation = this.beanProperty.getAnnotation(JsonFormat.class);
         if (formatAnnotation != null) {
            v2 = BeanIntrospectionModule.this.parseJsonFormat(formatAnnotation);
         }

         if (v1 == null) {
            return v2 == null ? EMPTY_FORMAT : v2;
         } else {
            return v2 == null ? v1 : v1.withOverrides(v2);
         }
      }
   }
}
