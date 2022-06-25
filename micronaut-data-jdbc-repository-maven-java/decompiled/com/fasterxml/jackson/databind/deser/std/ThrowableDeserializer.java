package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;

public class ThrowableDeserializer extends BeanDeserializer {
   private static final long serialVersionUID = 1L;
   protected static final String PROP_NAME_MESSAGE = "message";
   protected static final String PROP_NAME_SUPPRESSED = "suppressed";

   public ThrowableDeserializer(BeanDeserializer baseDeserializer) {
      super(baseDeserializer);
      this._vanillaProcessing = false;
   }

   protected ThrowableDeserializer(BeanDeserializer src, NameTransformer unwrapper) {
      super(src, unwrapper);
   }

   @Override
   public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
      return this.getClass() != ThrowableDeserializer.class ? this : new ThrowableDeserializer(this, unwrapper);
   }

   @Override
   public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (this._propertyBasedCreator != null) {
         return this._deserializeUsingPropertyBased(p, ctxt);
      } else if (this._delegateDeserializer != null) {
         return this._valueInstantiator.createUsingDelegate(ctxt, this._delegateDeserializer.deserialize(p, ctxt));
      } else if (this._beanType.isAbstract()) {
         return ctxt.handleMissingInstantiator(this.handledType(), this.getValueInstantiator(), p, "abstract type (need to add/enable type information?)");
      } else {
         boolean hasStringCreator = this._valueInstantiator.canCreateFromString();
         boolean hasDefaultCtor = this._valueInstantiator.canCreateUsingDefault();
         if (!hasStringCreator && !hasDefaultCtor) {
            return ctxt.handleMissingInstantiator(
               this.handledType(),
               this.getValueInstantiator(),
               p,
               "Throwable needs a default constructor, a single-String-arg constructor; or explicit @JsonCreator"
            );
         } else {
            Throwable throwable = null;
            Object[] pending = null;
            Throwable[] suppressed = null;

            int pendingIx;
            for(pendingIx = 0; !p.hasToken(JsonToken.END_OBJECT); p.nextToken()) {
               String propName = p.currentName();
               SettableBeanProperty prop = this._beanProperties.find(propName);
               p.nextToken();
               if (prop != null) {
                  if (throwable != null) {
                     prop.deserializeAndSet(p, ctxt, throwable);
                  } else {
                     if (pending == null) {
                        int len = this._beanProperties.size();
                        pending = new Object[len + len];
                     }

                     pending[pendingIx++] = prop;
                     pending[pendingIx++] = prop.deserialize(p, ctxt);
                  }
               } else {
                  if ("message".equals(propName)) {
                     if (hasStringCreator) {
                        throwable = (Throwable)this._valueInstantiator.createFromString(ctxt, p.getValueAsString());
                        continue;
                     }
                  } else if ("suppressed".equals(propName)) {
                     suppressed = ctxt.readValue(p, Throwable[].class);
                     continue;
                  }

                  if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                     p.skipChildren();
                  } else if (this._anySetter != null) {
                     this._anySetter.deserializeAndSet(p, ctxt, throwable, propName);
                  } else {
                     this.handleUnknownProperty(p, ctxt, throwable, propName);
                  }
               }
            }

            if (throwable == null) {
               if (hasStringCreator) {
                  throwable = (Throwable)this._valueInstantiator.createFromString(ctxt, null);
               } else {
                  throwable = (Throwable)this._valueInstantiator.createUsingDefault(ctxt);
               }
            }

            if (pending != null) {
               int i = 0;

               for(int len = pendingIx; i < len; i += 2) {
                  SettableBeanProperty prop = (SettableBeanProperty)pending[i];
                  prop.set(throwable, pending[i + 1]);
               }
            }

            if (suppressed != null) {
               for(Throwable s : suppressed) {
                  throwable.addSuppressed(s);
               }
            }

            return throwable;
         }
      }
   }
}
