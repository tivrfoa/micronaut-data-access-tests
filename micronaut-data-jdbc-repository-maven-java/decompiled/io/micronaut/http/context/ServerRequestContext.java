package io.micronaut.http.context;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class ServerRequestContext {
   public static final String KEY = "micronaut.http.server.request";
   private static final ThreadLocal<HttpRequest> REQUEST = new ThreadLocal();

   private ServerRequestContext() {
   }

   public static void set(@Nullable HttpRequest request) {
      if (request == null) {
         REQUEST.remove();
      } else {
         REQUEST.set(request);
      }

   }

   public static void with(@Nullable HttpRequest request, @NonNull Runnable runnable) {
      HttpRequest existing = (HttpRequest)REQUEST.get();
      boolean isSet = false;

      try {
         if (request != existing) {
            isSet = true;
            set(request);
         }

         runnable.run();
      } finally {
         if (isSet) {
            set(existing);
         }

      }

   }

   public static Runnable instrument(@Nullable HttpRequest request, @NonNull Runnable runnable) {
      return () -> with(request, runnable);
   }

   public static <T> T with(@Nullable HttpRequest request, @NonNull Supplier<T> callable) {
      HttpRequest existing = (HttpRequest)REQUEST.get();
      boolean isSet = false;

      Object var4;
      try {
         if (request != existing) {
            isSet = true;
            set(request);
         }

         var4 = callable.get();
      } finally {
         if (isSet) {
            set(existing);
         }

      }

      return (T)var4;
   }

   public static <T> T with(@Nullable HttpRequest request, @NonNull Callable<T> callable) throws Exception {
      HttpRequest existing = (HttpRequest)REQUEST.get();
      boolean isSet = false;

      Object var4;
      try {
         if (request != existing) {
            isSet = true;
            set(request);
         }

         var4 = callable.call();
      } finally {
         if (isSet) {
            set(existing);
         }

      }

      return (T)var4;
   }

   public static <T> Optional<HttpRequest<T>> currentRequest() {
      return Optional.ofNullable(REQUEST.get());
   }
}
