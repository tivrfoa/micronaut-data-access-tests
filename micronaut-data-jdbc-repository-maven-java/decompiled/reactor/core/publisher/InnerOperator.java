package reactor.core.publisher;

import reactor.util.context.Context;

interface InnerOperator<I, O> extends InnerConsumer<I>, InnerProducer<O> {
   @Override
   default Context currentContext() {
      return this.actual().currentContext();
   }
}
