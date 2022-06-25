package io.micronaut.http;

public interface HttpHeaderValues {
   String CONNECTION_KEEP_ALIVE = "keep-alive";
   String AUTHORIZATION_PREFIX_BEARER = "Bearer";
   String AUTHORIZATION_PREFIX_BASIC = "Basic";
   String CACHE_MAX_AGE = "max-age";
   String CACHE_MAX_STALE = "max-stale";
   String CACHE_MIN_FRESH = "min-fresh";
   String CACHE_MUST_REVALIDATE = "must-revalidate";
   String CACHE_NO_CACHE = "no-cache";
   String CACHE_NO_STORE = "no-store";
   String CACHE_NO_TRANSFORM = "no-transform";
   String CACHE_ONLY_IF_CACHED = "only-if-cached";
   String CACHE_PRIVATE = "private";
   String CACHE_PROXY_REVALIDATE = "proxy-revalidate";
   String CACHE_PUBLIC = "proxy-revalidate";
   String CACHE_S_MAXAGE = "s-maxage";
   String CACHE_STALE_IF_ERROR = "stale-if-error";
   String CACHE_STALE_WHILE_REVALIDATE = "stale-while-revalidate";
}
