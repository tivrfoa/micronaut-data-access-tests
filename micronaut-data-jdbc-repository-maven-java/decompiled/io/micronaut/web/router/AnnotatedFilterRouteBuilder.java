package io.micronaut.web.router;

import io.micronaut.context.BeanContext;
import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.processor.BeanDefinitionProcessor;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.context.ServerContextPathProvider;
import io.micronaut.http.filter.FilterPatternStyle;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.inject.BeanDefinition;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AnnotatedFilterRouteBuilder extends DefaultRouteBuilder implements BeanDefinitionProcessor<Filter> {
   private final ServerContextPathProvider contextPathProvider;

   @Inject
   public AnnotatedFilterRouteBuilder(
      BeanContext beanContext,
      ExecutionHandleLocator executionHandleLocator,
      RouteBuilder.UriNamingStrategy uriNamingStrategy,
      ConversionService<?> conversionService,
      @Nullable ServerContextPathProvider contextPathProvider
   ) {
      super(executionHandleLocator, uriNamingStrategy, conversionService);
      this.contextPathProvider = contextPathProvider;
   }

   public void process(BeanDefinition<?> beanDefinition, BeanContext beanContext) {
      if (!HttpClientFilter.class.isAssignableFrom(beanDefinition.getBeanType())) {
         String[] patterns = this.getPatterns(beanDefinition);
         if (ArrayUtils.isNotEmpty(patterns)) {
            HttpMethod[] methods = beanDefinition.enumValues(Filter.class, "methods", HttpMethod.class);
            FilterPatternStyle patternStyle = (FilterPatternStyle)beanDefinition.enumValue(Filter.class, "patternStyle", FilterPatternStyle.class)
               .orElse(FilterPatternStyle.ANT);
            String first = patterns[0];
            FilterRoute filterRoute = this.addFilter(first, beanContext, beanDefinition);
            if (patterns.length > 1) {
               for(int i = 1; i < patterns.length; ++i) {
                  String pattern = patterns[i];
                  filterRoute.pattern(pattern);
               }
            }

            if (ArrayUtils.isNotEmpty(methods)) {
               filterRoute.methods(methods);
            }

            filterRoute.patternStyle(patternStyle);
         }

      }
   }

   protected String[] getPatterns(BeanDefinition<?> beanDefinition) {
      String[] values = beanDefinition.stringValues(Filter.class);
      String contextPath = this.contextPathProvider != null ? this.contextPathProvider.getContextPath() : null;
      if (contextPath != null) {
         for(int i = 0; i < values.length; ++i) {
            if (!values[i].startsWith(contextPath)) {
               String newValue = StringUtils.prependUri(contextPath, values[i]);
               if (newValue.charAt(0) != '/') {
                  newValue = "/" + newValue;
               }

               values[i] = newValue;
            }
         }
      }

      return values;
   }
}
