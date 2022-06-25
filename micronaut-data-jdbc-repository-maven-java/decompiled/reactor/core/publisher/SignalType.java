package reactor.core.publisher;

public enum SignalType {
   SUBSCRIBE,
   REQUEST,
   CANCEL,
   ON_SUBSCRIBE,
   ON_NEXT,
   ON_ERROR,
   ON_COMPLETE,
   AFTER_TERMINATE,
   CURRENT_CONTEXT,
   ON_CONTEXT;

   public String toString() {
      switch(this) {
         case ON_SUBSCRIBE:
            return "onSubscribe";
         case ON_NEXT:
            return "onNext";
         case ON_ERROR:
            return "onError";
         case ON_COMPLETE:
            return "onComplete";
         case REQUEST:
            return "request";
         case CANCEL:
            return "cancel";
         case CURRENT_CONTEXT:
            return "currentContext";
         case ON_CONTEXT:
            return "onContextUpdate";
         case AFTER_TERMINATE:
            return "afterTerminate";
         default:
            return "subscribe";
      }
   }
}
