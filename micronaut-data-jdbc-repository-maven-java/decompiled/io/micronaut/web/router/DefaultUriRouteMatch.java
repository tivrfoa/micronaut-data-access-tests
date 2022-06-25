package io.micronaut.web.router;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.uri.UriMatchInfo;
import io.micronaut.http.uri.UriMatchVariable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Internal
class DefaultUriRouteMatch<T, R> extends AbstractRouteMatch<T, R> implements UriRouteMatch<T, R> {
   private final HttpMethod httpMethod;
   private final UriMatchInfo matchInfo;
   private final DefaultRouteBuilder.DefaultUriRoute uriRoute;
   private final Charset defaultCharset;

   DefaultUriRouteMatch(UriMatchInfo matchInfo, DefaultRouteBuilder.DefaultUriRoute uriRoute, Charset defaultCharset, ConversionService<?> conversionService) {
      super(uriRoute, conversionService);
      this.uriRoute = uriRoute;
      this.matchInfo = matchInfo;
      this.httpMethod = uriRoute.httpMethod;
      this.defaultCharset = defaultCharset;
   }

   @Override
   public UriRouteMatch<T, R> decorate(Function<RouteMatch<R>, R> executor) {
      final Map<String, Object> variables = this.getVariableValues();
      final List<Argument> arguments = this.getRequiredArguments();
      final RouteMatch thisRoute = this;
      return new DefaultUriRouteMatch<T, R>(this.matchInfo, this.uriRoute, this.defaultCharset, this.conversionService) {
         @Override
         public List<Argument> getRequiredArguments() {
            return arguments;
         }

         @Override
         public R execute(Map argumentValues) {
            return (R)executor.apply(thisRoute);
         }

         @Override
         public Map<String, Object> getVariableValues() {
            return variables;
         }
      };
   }

   @Override
   protected RouteMatch<R> newFulfilled(Map<String, Object> newVariables, List<Argument> requiredArguments) {
      return new DefaultUriRouteMatch<T, R>(this.matchInfo, this.uriRoute, this.defaultCharset, this.conversionService) {
         @Override
         public List<Argument> getRequiredArguments() {
            return requiredArguments;
         }

         @Override
         public Map<String, Object> getVariableValues() {
            return newVariables;
         }
      };
   }

   @Override
   public UriRouteMatch<T, R> fulfill(Map<String, Object> argumentValues) {
      return (UriRouteMatch<T, R>)super.fulfill(argumentValues);
   }

   @Override
   public String getUri() {
      return this.matchInfo.getUri();
   }

   @Override
   public Map<String, Object> getVariableValues() {
      Map<String, Object> variables = this.matchInfo.getVariableValues();
      if (CollectionUtils.isNotEmpty(variables)) {
         String charset = this.defaultCharset.toString();
         Map<String, Object> decoded = new LinkedHashMap(variables.size());
         variables.forEach((k, v) -> {
            if (v instanceof CharSequence) {
               try {
                  v = URLDecoder.decode(v.toString(), charset);
               } catch (UnsupportedEncodingException var5) {
               }
            }

            decoded.put(k, v);
         });
         return decoded;
      } else {
         return variables;
      }
   }

   @Override
   public List<UriMatchVariable> getVariables() {
      return this.matchInfo.getVariables();
   }

   @Override
   public Map<String, UriMatchVariable> getVariableMap() {
      return this.matchInfo.getVariableMap();
   }

   @Override
   public UriRoute getRoute() {
      return (UriRoute)this.abstractRoute;
   }

   @Override
   public HttpMethod getHttpMethod() {
      return this.httpMethod;
   }

   public String toString() {
      return this.httpMethod + " - " + this.matchInfo.getUri();
   }
}
