package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
final class MultipleInvocationInstrumenter implements InvocationInstrumenter {
   private static final Logger LOG = LoggerFactory.getLogger(InvocationInstrumenter.class);
   private final Collection<InvocationInstrumenter> invocationInstrumenters;

   MultipleInvocationInstrumenter(Collection<InvocationInstrumenter> invocationInstrumenters) {
      this.invocationInstrumenters = invocationInstrumenters;
   }

   @NonNull
   @Override
   public Instrumentation newInstrumentation() {
      List<Instrumentation> instrumentationList = new ArrayList(this.invocationInstrumenters.size());

      for(InvocationInstrumenter instrumenter : this.invocationInstrumenters) {
         try {
            instrumentationList.add(instrumenter.newInstrumentation());
         } catch (Exception var5) {
            LOG.warn("InvocationInstrumenter.newInstrumentation invocation error: {}", var5.getMessage(), var5);
         }
      }

      return cleanup -> {
         ListIterator<Instrumentation> iterator = instrumentationList.listIterator(instrumentationList.size());

         while(iterator.hasPrevious()) {
            try {
               ((Instrumentation)iterator.previous()).close(cleanup);
            } catch (Exception var4) {
               LOG.warn("Instrumentation.close invocation error: {}", var4.getMessage(), var4);
            }
         }

      };
   }
}
