package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.Nullable;

@Indexed(InvocationInstrumenterFactory.class)
public interface InvocationInstrumenterFactory {
   @Nullable
   InvocationInstrumenter newInvocationInstrumenter();
}
