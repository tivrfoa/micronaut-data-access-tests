package reactor.util.context;

import java.util.Map;
import java.util.stream.Stream;

interface CoreContext extends Context {
   @Override
   default boolean isEmpty() {
      return false;
   }

   @Override
   default Context putAll(ContextView other) {
      if (other.isEmpty()) {
         return this;
      } else if (other instanceof CoreContext) {
         CoreContext coreContext = (CoreContext)other;
         return coreContext.putAllInto(this);
      } else {
         ContextN newContext = new ContextN(this.size() + other.size());
         this.unsafePutAllInto(newContext);
         ((Stream)other.stream().sequential()).forEach(newContext);
         return (Context)(newContext.size() <= 5 ? Context.of((Map<?, ?>)newContext) : newContext);
      }
   }

   Context putAllInto(Context var1);

   void unsafePutAllInto(ContextN var1);
}
