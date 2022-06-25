package io.micronaut.context.banner;

import io.micronaut.context.annotation.DefaultImplementation;

@DefaultImplementation(MicronautBanner.class)
public interface Banner {
   void print();
}
