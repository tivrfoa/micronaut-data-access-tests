package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.Nullable;

@Indexed(ReactiveInvocationInstrumenterFactory.class)
public interface ReactiveInvocationInstrumenterFactory {
   @Nullable
   InvocationInstrumenter newReactiveInvocationInstrumenter();
}
