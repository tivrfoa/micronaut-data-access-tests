package com.example;

import io.micronaut.core.annotation.Introspected;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Introspected
public class GenreUpdateCommand {
   @NotNull
   private final Long id;
   @NotBlank
   private final String name;

   public GenreUpdateCommand(Long id, String name) {
      this.id = id;
      this.name = name;
   }

   public Long getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }
}
