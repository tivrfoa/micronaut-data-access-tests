package io.micronaut.data.runtime.http;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.runtime.config.DataConfiguration;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Requires(
   classes = {RequestArgumentBinder.class}
)
@Singleton
public class PageableRequestArgumentBinder implements TypedRequestArgumentBinder<Pageable> {
   public static final Argument<Pageable> TYPE = Argument.of(Pageable.class);
   private final DataConfiguration.PageableConfiguration configuration;
   private final Function<String, Sort.Order> sortMapper;

   protected PageableRequestArgumentBinder(DataConfiguration.PageableConfiguration configuration) {
      this.configuration = configuration;
      this.sortMapper = s -> {
         String[] tokens = configuration.getSortDelimiterPattern().split(s);
         if (tokens.length == 1) {
            return new Sort.Order(tokens[0], Sort.Order.Direction.ASC, configuration.isSortIgnoreCase());
         } else {
            try {
               Sort.Order.Direction direction = Sort.Order.Direction.valueOf(tokens[1].toUpperCase(Locale.ENGLISH));
               return new Sort.Order(tokens[0], direction, configuration.isSortIgnoreCase());
            } catch (IllegalArgumentException var4) {
               return new Sort.Order(tokens[0], Sort.Order.Direction.ASC, configuration.isSortIgnoreCase());
            }
         }
      };
   }

   @Override
   public Argument<Pageable> argumentType() {
      return TYPE;
   }

   public ArgumentBinder.BindingResult<Pageable> bind(ArgumentConversionContext<Pageable> context, HttpRequest<?> source) {
      HttpParameters parameters = source.getParameters();
      int page = Math.max(parameters.getFirst(this.configuration.getPageParameterName(), Integer.class).orElse(0), 0);
      int configuredMaxSize = this.configuration.getMaxPageSize();
      int defaultSize = this.configuration.getDefaultPageSize();
      int size = Math.min(parameters.getFirst(this.configuration.getSizeParameterName(), Integer.class).orElse(defaultSize), configuredMaxSize);
      String sortParameterName = this.configuration.getSortParameterName();
      boolean hasSort = parameters.contains(sortParameterName);
      Sort sort = null;
      if (hasSort) {
         List<String> sortParams = parameters.getAll(sortParameterName);
         List<Sort.Order> orders = (List)sortParams.stream().map(this.sortMapper).collect(Collectors.toList());
         sort = Sort.of(orders);
      }

      Pageable pageable;
      if (size < 1) {
         if (page == 0 && configuredMaxSize < 1 && sort == null) {
            pageable = Pageable.UNPAGED;
         } else {
            pageable = Pageable.from(page, defaultSize, sort);
         }
      } else {
         pageable = Pageable.from(page, size, sort);
      }

      return () -> Optional.of(pageable);
   }
}
