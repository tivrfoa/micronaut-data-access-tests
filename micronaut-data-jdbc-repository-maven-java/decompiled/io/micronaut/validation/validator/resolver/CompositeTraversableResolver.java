package io.micronaut.validation.validator.resolver;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.CollectionUtils;
import jakarta.inject.Singleton;
import java.lang.annotation.ElementType;
import java.util.List;
import javax.validation.Path;
import javax.validation.TraversableResolver;

@Primary
@Singleton
@Internal
public class CompositeTraversableResolver implements TraversableResolver {
   private final List<TraversableResolver> traversableResolvers;

   public CompositeTraversableResolver(List<TraversableResolver> traversableResolvers) {
      this.traversableResolvers = CollectionUtils.isEmpty(traversableResolvers) ? null : traversableResolvers;
   }

   @Override
   public boolean isReachable(
      Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType
   ) {
      return this.traversableResolvers == null
         ? true
         : this.traversableResolvers
            .stream()
            .allMatch(r -> r.isReachable(traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType));
   }

   @Override
   public boolean isCascadable(
      Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType
   ) {
      return this.traversableResolvers == null
         ? true
         : this.traversableResolvers
            .stream()
            .allMatch(r -> r.isCascadable(traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType));
   }
}
