package io.micronaut.data.model.query;

import io.micronaut.data.model.PersistentPropertyPath;

final class BindingContextImpl implements BindingParameter.BindingContext {
   private int index = -1;
   private String name;
   private PersistentPropertyPath incomingMethodParameterProperty;
   private PersistentPropertyPath outgoingQueryParameterProperty;
   private boolean expandable;

   @Override
   public BindingParameter.BindingContext index(int index) {
      this.index = index;
      return this;
   }

   @Override
   public BindingParameter.BindingContext name(String name) {
      this.name = name;
      return this;
   }

   @Override
   public BindingParameter.BindingContext incomingMethodParameterProperty(PersistentPropertyPath propertyPath) {
      this.incomingMethodParameterProperty = propertyPath;
      return this;
   }

   @Override
   public BindingParameter.BindingContext outgoingQueryParameterProperty(PersistentPropertyPath propertyPath) {
      this.outgoingQueryParameterProperty = propertyPath;
      return this;
   }

   @Override
   public BindingParameter.BindingContext expandable() {
      this.expandable = true;
      return this;
   }

   @Override
   public int getIndex() {
      return this.index;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public PersistentPropertyPath getIncomingMethodParameterProperty() {
      return this.incomingMethodParameterProperty;
   }

   @Override
   public PersistentPropertyPath getOutgoingQueryParameterProperty() {
      return this.outgoingQueryParameterProperty;
   }

   @Override
   public boolean isExpandable() {
      return this.expandable;
   }
}
