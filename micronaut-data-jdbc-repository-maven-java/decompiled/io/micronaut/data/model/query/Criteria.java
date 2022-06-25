package io.micronaut.data.model.query;

import io.micronaut.core.annotation.NonNull;
import java.util.Map;

public interface Criteria {
   @NonNull
   Criteria idEq(Object parameter);

   @NonNull
   Criteria versionEq(Object parameter);

   @NonNull
   Criteria isEmpty(@NonNull String propertyName);

   @NonNull
   Criteria isNotEmpty(@NonNull String propertyName);

   @NonNull
   Criteria isNull(@NonNull String propertyName);

   @NonNull
   Criteria isTrue(@NonNull String propertyName);

   @NonNull
   Criteria isFalse(@NonNull String propertyName);

   @NonNull
   Criteria isNotNull(String propertyName);

   @NonNull
   Criteria eq(String propertyName, Object parameter);

   @NonNull
   Criteria ne(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   Criteria between(@NonNull String propertyName, @NonNull Object start, @NonNull Object finish);

   @NonNull
   Criteria gte(@NonNull String property, @NonNull Object parameter);

   @NonNull
   Criteria ge(@NonNull String property, @NonNull Object parameter);

   @NonNull
   Criteria gt(@NonNull String property, @NonNull Object parameter);

   @NonNull
   Criteria lte(@NonNull String property, @NonNull Object parameter);

   @NonNull
   Criteria le(@NonNull String property, @NonNull Object parameter);

   @NonNull
   Criteria lt(@NonNull String property, @NonNull Object parameter);

   @NonNull
   Criteria like(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   Criteria startsWith(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   Criteria endsWith(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   Criteria contains(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   Criteria ilike(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   Criteria rlike(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   Criteria and(@NonNull Criteria other);

   @NonNull
   Criteria or(@NonNull Criteria other);

   @NonNull
   Criteria not(@NonNull Criteria other);

   @NonNull
   Criteria inList(@NonNull String propertyName, @NonNull QueryModel subquery);

   @NonNull
   Criteria inList(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   Criteria notIn(@NonNull String propertyName, @NonNull QueryModel subquery);

   @NonNull
   Criteria sizeEq(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   Criteria sizeGt(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   Criteria sizeGe(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   Criteria sizeLe(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   Criteria sizeLt(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   Criteria sizeNe(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   Criteria eqProperty(@NonNull String propertyName, @NonNull String otherPropertyName);

   @NonNull
   Criteria neProperty(@NonNull String propertyName, @NonNull String otherPropertyName);

   @NonNull
   Criteria gtProperty(@NonNull String propertyName, @NonNull String otherPropertyName);

   @NonNull
   Criteria geProperty(@NonNull String propertyName, @NonNull String otherPropertyName);

   @NonNull
   Criteria ltProperty(@NonNull String propertyName, @NonNull String otherPropertyName);

   @NonNull
   Criteria leProperty(String propertyName, @NonNull String otherPropertyName);

   @NonNull
   Criteria allEq(@NonNull Map<String, Object> propertyValues);

   @NonNull
   Criteria eqAll(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   Criteria gtAll(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   Criteria ltAll(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   Criteria geAll(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   Criteria leAll(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   Criteria gtSome(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   Criteria geSome(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   Criteria ltSome(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   Criteria leSome(@NonNull String propertyName, @NonNull Criteria propertyValue);
}
