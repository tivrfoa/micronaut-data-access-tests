package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import java.util.function.Supplier;

public class RuntimePersistentProperty<T> implements PersistentProperty {
   public static final RuntimePersistentProperty<Object>[] EMPTY_PROPERTY_ARRAY = new RuntimePersistentProperty[0];
   private final RuntimePersistentEntity<T> owner;
   private final BeanProperty<T, ?> property;
   private final Class<?> type;
   private final DataType dataType;
   private final boolean constructorArg;
   private final Argument<?> argument;
   private final Supplier<AttributeConverter<Object, Object>> converter;
   private String persistedName;

   RuntimePersistentProperty(RuntimePersistentEntity<T> owner, BeanProperty<T, ?> property, boolean constructorArg) {
      this.owner = owner;
      this.property = property;
      this.type = ReflectionUtils.getWrapperType(property.getType());
      this.dataType = PersistentProperty.super.getDataType();
      this.constructorArg = constructorArg;
      this.argument = property.asArgument();
      this.converter = (Supplier)property.classValue(MappedProperty.class, "converter")
         .map(converter -> SupplierUtil.memoized(() -> owner.resolveConverter(converter)))
         .orElse(null);
   }

   public Argument<?> getArgument() {
      return this.argument;
   }

   @Override
   public boolean isConstructorArgument() {
      return this.constructorArg;
   }

   @Override
   public final boolean isOptional() {
      return this.property.isNullable();
   }

   @Override
   public boolean isEnum() {
      return this.type.isEnum();
   }

   @Override
   public DataType getDataType() {
      return this.dataType;
   }

   @Override
   public boolean isReadOnly() {
      return this.property.isReadOnly();
   }

   @NonNull
   public Class<?> getType() {
      return this.type;
   }

   @NonNull
   @Override
   public String getName() {
      return this.property.getName();
   }

   @NonNull
   @Override
   public String getTypeName() {
      return this.property.getType().getName();
   }

   @NonNull
   public RuntimePersistentEntity<T> getOwner() {
      return this.owner;
   }

   @Override
   public boolean isAssignable(@NonNull String type) {
      throw new UnsupportedOperationException("Use isAssignable(Class) instead");
   }

   @Override
   public boolean isAssignable(@NonNull Class<?> type) {
      return type.isAssignableFrom(this.getProperty().getType());
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.property.getAnnotationMetadata();
   }

   public BeanProperty<T, ?> getProperty() {
      return this.property;
   }

   @Override
   public AttributeConverter<Object, Object> getConverter() {
      return this.converter == null ? null : (AttributeConverter)this.converter.get();
   }

   @NonNull
   @Override
   public String getPersistedName() {
      if (this.persistedName == null) {
         this.persistedName = this.owner.getNamingStrategy().mappedName(this);
      }

      return this.persistedName;
   }

   public String toString() {
      return this.getOwner().getSimpleName() + "." + this.getName();
   }
}
