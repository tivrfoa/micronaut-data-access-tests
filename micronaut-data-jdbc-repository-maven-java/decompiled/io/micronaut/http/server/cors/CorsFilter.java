package io.micronaut.http.server.cors;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ImmutableArgumentConversionContext;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.http.server.HttpServerConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;

@Filter({"/**"})
public class CorsFilter implements HttpServerFilter {
   private static final ArgumentConversionContext<HttpMethod> CONVERSION_CONTEXT_HTTP_METHOD = ImmutableArgumentConversionContext.of(HttpMethod.class);
   protected final HttpServerConfiguration.CorsConfiguration corsConfiguration;

   public CorsFilter(HttpServerConfiguration.CorsConfiguration corsConfiguration) {
      this.corsConfiguration = corsConfiguration;
   }

   @Override
   public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
      boolean originHeaderPresent = request.getHeaders().getOrigin().isPresent();
      if (originHeaderPresent) {
         MutableHttpResponse<?> response = (MutableHttpResponse)this.handleRequest(request).orElse(null);
         return response != null
            ? Publishers.just(response)
            : Publishers.then(chain.proceed(request), mutableHttpResponse -> this.handleResponse(request, mutableHttpResponse));
      } else {
         return chain.proceed(request);
      }
   }

   @Override
   public int getOrder() {
      return ServerFilterPhase.METRICS.after();
   }

   protected void handleResponse(HttpRequest<?> request, MutableHttpResponse<?> response) {
      HttpHeaders headers = request.getHeaders();
      Optional<String> originHeader = headers.getOrigin();
      originHeader.ifPresent(requestOrigin -> {
         Optional<CorsOriginConfiguration> optionalConfig = this.getConfiguration(requestOrigin);
         if (optionalConfig.isPresent()) {
            CorsOriginConfiguration config = (CorsOriginConfiguration)optionalConfig.get();
            if (CorsUtil.isPreflightRequest(request)) {
               Optional<HttpMethod> result = headers.getFirst("Access-Control-Request-Method", CONVERSION_CONTEXT_HTTP_METHOD);
               this.setAllowMethods((HttpMethod)result.get(), response);
               Optional<List<String>> allowedHeaders = headers.get("Access-Control-Request-Headers", ConversionContext.LIST_OF_STRING);
               allowedHeaders.ifPresent(val -> this.setAllowHeaders(val, response));
               this.setMaxAge(config.getMaxAge(), response);
            }

            this.setOrigin(requestOrigin, response);
            this.setVary(response);
            this.setExposeHeaders(config.getExposedHeaders(), response);
            this.setAllowCredentials(config, response);
         }

      });
   }

   protected Optional<MutableHttpResponse<?>> handleRequest(HttpRequest request) {
      HttpHeaders headers = request.getHeaders();
      Optional<String> originHeader = headers.getOrigin();
      if (originHeader.isPresent()) {
         String requestOrigin = (String)originHeader.get();
         boolean preflight = CorsUtil.isPreflightRequest(request);
         Optional<CorsOriginConfiguration> optionalConfig = this.getConfiguration(requestOrigin);
         if (optionalConfig.isPresent()) {
            CorsOriginConfiguration config = (CorsOriginConfiguration)optionalConfig.get();
            HttpMethod requestMethod = request.getMethod();
            List<HttpMethod> allowedMethods = config.getAllowedMethods();
            HttpMethod methodToMatch = preflight
               ? (HttpMethod)headers.getFirst("Access-Control-Request-Method", CONVERSION_CONTEXT_HTTP_METHOD).orElse(requestMethod)
               : requestMethod;
            if (!this.isAnyMethod(allowedMethods) && allowedMethods.stream().noneMatch(method -> method.equals(methodToMatch))) {
               return Optional.of(HttpResponse.status(HttpStatus.FORBIDDEN));
            }

            Optional<? extends ArrayList<HttpMethod>> availableHttpMethods = request.getAttribute(
               HttpAttributes.AVAILABLE_HTTP_METHODS, new ArrayList().getClass()
            );
            if (preflight
               && availableHttpMethods.isPresent()
               && ((ArrayList)availableHttpMethods.get()).stream().anyMatch(method -> method.equals(methodToMatch))) {
               Optional<List<String>> accessControlHeaders = headers.get("Access-Control-Request-Headers", ConversionContext.LIST_OF_STRING);
               List<String> allowedHeaders = config.getAllowedHeaders();
               if (!this.isAny(allowedHeaders)
                  && accessControlHeaders.isPresent()
                  && !((List)accessControlHeaders.get())
                     .stream()
                     .allMatch(header -> allowedHeaders.stream().anyMatch(allowedHeader -> allowedHeader.equalsIgnoreCase(header.trim())))) {
                  return Optional.of(HttpResponse.status(HttpStatus.FORBIDDEN));
               }

               MutableHttpResponse<Object> ok = HttpResponse.ok();
               this.handleResponse(request, ok);
               return Optional.of(ok);
            }
         }
      }

      return Optional.empty();
   }

   protected void setAllowCredentials(CorsOriginConfiguration config, MutableHttpResponse<?> response) {
      if (config.isAllowCredentials()) {
         response.header("Access-Control-Allow-Credentials", Boolean.toString(true));
      }

   }

   protected void setExposeHeaders(List<String> exposedHeaders, MutableHttpResponse<?> response) {
      if (this.corsConfiguration.isSingleHeader()) {
         String headerValue = String.join(",", exposedHeaders);
         if (StringUtils.isNotEmpty(headerValue)) {
            response.header("Access-Control-Expose-Headers", headerValue);
         }
      } else {
         exposedHeaders.forEach(header -> response.header("Access-Control-Expose-Headers", header));
      }

   }

   protected void setVary(MutableHttpResponse<?> response) {
      response.header("Vary", "Origin");
   }

   protected void setOrigin(String origin, MutableHttpResponse response) {
      response.header("Access-Control-Allow-Origin", origin);
   }

   protected void setAllowMethods(HttpMethod method, MutableHttpResponse response) {
      response.header("Access-Control-Allow-Methods", method);
   }

   protected void setAllowHeaders(List<?> optionalAllowHeaders, MutableHttpResponse response) {
      List<String> allowHeaders = (List)optionalAllowHeaders.stream().map(Object::toString).collect(Collectors.toList());
      if (this.corsConfiguration.isSingleHeader()) {
         String headerValue = String.join(",", allowHeaders);
         if (StringUtils.isNotEmpty(headerValue)) {
            response.header("Access-Control-Allow-Headers", headerValue);
         }
      } else {
         allowHeaders.forEach(header -> response.header("Access-Control-Allow-Headers", header));
      }

   }

   protected void setMaxAge(long maxAge, MutableHttpResponse response) {
      if (maxAge > -1L) {
         response.header("Access-Control-Max-Age", Long.toString(maxAge));
      }

   }

   private Optional<CorsOriginConfiguration> getConfiguration(String requestOrigin) {
      Map<String, CorsOriginConfiguration> corsConfigurations = this.corsConfiguration.getConfigurations();

      for(Entry<String, CorsOriginConfiguration> config : corsConfigurations.entrySet()) {
         List<String> allowedOrigins = ((CorsOriginConfiguration)config.getValue()).getAllowedOrigins();
         if (!allowedOrigins.isEmpty()) {
            boolean matches = false;
            if (this.isAny(allowedOrigins)) {
               matches = true;
            }

            if (!matches) {
               matches = allowedOrigins.stream().anyMatch(origin -> {
                  if (origin.equals(requestOrigin)) {
                     return true;
                  } else {
                     Pattern p = Pattern.compile(origin);
                     Matcher m = p.matcher(requestOrigin);
                     return m.matches();
                  }
               });
            }

            if (matches) {
               return Optional.of(config.getValue());
            }
         }
      }

      return Optional.empty();
   }

   private boolean isAny(List<String> values) {
      return values == CorsOriginConfiguration.ANY;
   }

   private boolean isAnyMethod(List<HttpMethod> allowedMethods) {
      return allowedMethods == CorsOriginConfiguration.ANY_METHOD;
   }
}
