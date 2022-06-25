package io.micronaut.data.runtime.support;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.type.Argument;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Internal
public class AbstractConversionContext implements ConversionContext {
   private final ConversionContext delegate;

   public AbstractConversionContext(ConversionContext conversionContext) {
      this.delegate = conversionContext;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.delegate.getAnnotationMetadata();
   }

   @Override
   public Map<String, Argument<?>> getTypeVariables() {
      return this.delegate.getTypeVariables();
   }

   @Override
   public Argument[] getTypeParameters() {
      return this.delegate.getTypeParameters();
   }

   @Override
   public Optional<Argument<?>> getFirstTypeVariable() {
      return this.delegate.getFirstTypeVariable();
   }

   @Override
   public Optional<Argument<?>> getTypeVariable(String name) {
      return this.delegate.getTypeVariable(name);
   }

   @Override
   public void reject(Exception exception) {
      this.delegate.reject(exception);
   }

   @Override
   public void reject(Object value, Exception exception) {
      this.delegate.reject(value, exception);
   }

   @Override
   public Iterator<ConversionError> iterator() {
      return this.delegate.iterator();
   }

   @Override
   public Optional<ConversionError> getLastError() {
      return this.delegate.getLastError();
   }

   @Override
   public boolean hasErrors() {
      return this.delegate.hasErrors();
   }
}
