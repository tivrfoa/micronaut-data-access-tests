package io.micronaut.inject.qualifiers;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StreamUtils;
import io.micronaut.inject.BeanType;
import java.util.Comparator;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;

@Internal
public class ClosestTypeArgumentQualifier<T> extends TypeArgumentQualifier<T> {
   private static final Logger LOG = ClassUtils.getLogger(ClosestTypeArgumentQualifier.class);
   private final List<Class>[] hierarchies;

   ClosestTypeArgumentQualifier(Class... typeArguments) {
      super(typeArguments);
      this.hierarchies = new List[typeArguments.length];

      for(int i = 0; i < typeArguments.length; ++i) {
         this.hierarchies[i] = ClassUtils.resolveHierarchy(typeArguments[i]);
      }

   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return ((List)candidates.filter(candidate -> beanType.isAssignableFrom(candidate.getBeanType()))
            .map(
               candidate -> {
                  List<Class> typeArguments = this.getTypeArguments(beanType, (BT)candidate);
                  int result = this.compare(typeArguments);
                  if (LOG.isTraceEnabled() && result < 0) {
                     LOG.trace(
                        "Bean type {} is not compatible with candidate generic types [{}] of candidate {}",
                        beanType,
                        CollectionUtils.toString(typeArguments),
                        candidate
                     );
                  }
         
                  return new SimpleEntry(candidate, result);
               }
            )
            .filter(entry -> entry.getValue() > -1)
            .collect(StreamUtils.minAll(Comparator.comparingInt(Entry::getValue), Collectors.toList())))
         .stream()
         .map(Entry::getKey);
   }

   protected int compare(List<Class> classesToCompare) {
      Class[] typeArguments = this.getTypeArguments();
      if (classesToCompare.isEmpty() && typeArguments.length == 0) {
         return 0;
      } else if (classesToCompare.size() != typeArguments.length) {
         return -1;
      } else {
         int comparison = 0;

         for(int i = 0; i < classesToCompare.size(); ++i) {
            if (typeArguments[i] != Object.class) {
               Class left = (Class)classesToCompare.get(i);
               List<Class> hierarchy = this.hierarchies[i];
               int index = hierarchy.indexOf(left);
               if (index == -1) {
                  comparison = -1;
                  break;
               }

               comparison += index;
            }
         }

         return comparison;
      }
   }
}
