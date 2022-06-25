package io.micronaut.jdbc.metadata;

@FunctionalInterface
public interface DataSourcePoolMetadataProvider {
   DataSourcePoolMetadata getDataSourcePoolMetadata();
}
