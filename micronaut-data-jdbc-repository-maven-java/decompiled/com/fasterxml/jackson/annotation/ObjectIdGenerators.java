package com.fasterxml.jackson.annotation;

import java.util.UUID;

public class ObjectIdGenerators {
   private abstract static class Base<T> extends ObjectIdGenerator<T> {
      protected final Class<?> _scope;

      protected Base(Class<?> scope) {
         this._scope = scope;
      }

      @Override
      public final Class<?> getScope() {
         return this._scope;
      }

      @Override
      public boolean canUseFor(ObjectIdGenerator<?> gen) {
         return gen.getClass() == this.getClass() && gen.getScope() == this._scope;
      }

      @Override
      public abstract T generateId(Object var1);
   }

   public static final class IntSequenceGenerator extends ObjectIdGenerators.Base<Integer> {
      private static final long serialVersionUID = 1L;
      protected transient int _nextValue;

      public IntSequenceGenerator() {
         this(Object.class, -1);
      }

      public IntSequenceGenerator(Class<?> scope, int fv) {
         super(scope);
         this._nextValue = fv;
      }

      protected int initialValue() {
         return 1;
      }

      @Override
      public ObjectIdGenerator<Integer> forScope(Class<?> scope) {
         return this._scope == scope ? this : new ObjectIdGenerators.IntSequenceGenerator(scope, this._nextValue);
      }

      @Override
      public ObjectIdGenerator<Integer> newForSerialization(Object context) {
         return new ObjectIdGenerators.IntSequenceGenerator(this._scope, this.initialValue());
      }

      @Override
      public ObjectIdGenerator.IdKey key(Object key) {
         return key == null ? null : new ObjectIdGenerator.IdKey(this.getClass(), this._scope, key);
      }

      public Integer generateId(Object forPojo) {
         if (forPojo == null) {
            return null;
         } else {
            int id = this._nextValue++;
            return id;
         }
      }
   }

   public abstract static class None extends ObjectIdGenerator<Object> {
   }

   public abstract static class PropertyGenerator extends ObjectIdGenerators.Base<Object> {
      private static final long serialVersionUID = 1L;

      protected PropertyGenerator(Class<?> scope) {
         super(scope);
      }
   }

   public static final class StringIdGenerator extends ObjectIdGenerators.Base<String> {
      private static final long serialVersionUID = 1L;

      public StringIdGenerator() {
         this(Object.class);
      }

      private StringIdGenerator(Class<?> scope) {
         super(Object.class);
      }

      @Override
      public ObjectIdGenerator<String> forScope(Class<?> scope) {
         return this;
      }

      @Override
      public ObjectIdGenerator<String> newForSerialization(Object context) {
         return this;
      }

      public String generateId(Object forPojo) {
         return UUID.randomUUID().toString();
      }

      @Override
      public ObjectIdGenerator.IdKey key(Object key) {
         return key == null ? null : new ObjectIdGenerator.IdKey(this.getClass(), null, key);
      }

      @Override
      public boolean canUseFor(ObjectIdGenerator<?> gen) {
         return gen instanceof ObjectIdGenerators.StringIdGenerator;
      }
   }

   public static final class UUIDGenerator extends ObjectIdGenerators.Base<UUID> {
      private static final long serialVersionUID = 1L;

      public UUIDGenerator() {
         this(Object.class);
      }

      private UUIDGenerator(Class<?> scope) {
         super(Object.class);
      }

      @Override
      public ObjectIdGenerator<UUID> forScope(Class<?> scope) {
         return this;
      }

      @Override
      public ObjectIdGenerator<UUID> newForSerialization(Object context) {
         return this;
      }

      public UUID generateId(Object forPojo) {
         return UUID.randomUUID();
      }

      @Override
      public ObjectIdGenerator.IdKey key(Object key) {
         return key == null ? null : new ObjectIdGenerator.IdKey(this.getClass(), null, key);
      }

      @Override
      public boolean canUseFor(ObjectIdGenerator<?> gen) {
         return gen.getClass() == this.getClass();
      }
   }
}
