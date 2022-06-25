package io.micronaut.data.model.query;

import io.micronaut.core.annotation.NonNull;

public interface ProjectionList {
   ProjectionList id();

   ProjectionList count();

   ProjectionList countDistinct(String property);

   ProjectionList groupProperty(String property);

   ProjectionList distinct();

   ProjectionList distinct(String property);

   ProjectionList rowCount();

   ProjectionList property(String name);

   ProjectionList sum(String name);

   ProjectionList min(String name);

   ProjectionList max(String name);

   ProjectionList avg(String name);

   ProjectionList add(@NonNull QueryModel.Projection projection);
}
