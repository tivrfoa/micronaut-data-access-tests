package io.micronaut.http.sse;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import java.time.Duration;

public interface Event<T> {
   String ID = "id";
   String EVENT = "event";
   String DATA = "data";
   String RETRY = "retry";

   T getData();

   String getId();

   String getName();

   String getComment();

   Duration getRetry();

   Event<T> retry(@Nullable Duration duration);

   Event<T> id(@Nullable String id);

   Event<T> name(@Nullable String name);

   Event<T> comment(@Nullable String comment);

   static <ET> Event<ET> of(ET data) {
      ArgumentUtils.check("data", data).notNull();
      return new DefaultEvent<>(data);
   }

   static <ET> Event<ET> of(Event event, ET data) {
      ArgumentUtils.check("data", data).notNull();
      return new DefaultEvent<>(data).id(event.getId()).comment(event.getComment()).name(event.getName()).retry(event.getRetry());
   }
}
