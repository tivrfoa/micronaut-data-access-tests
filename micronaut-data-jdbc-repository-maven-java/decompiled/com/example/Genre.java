package com.example;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import javax.validation.constraints.NotNull;

@MappedEntity
public class Genre {
   @Id
   @GeneratedValue(GeneratedValue.Type.AUTO)
   private Long id;
   @NotNull
   private String name;

   public Long getId() {
      return this.id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String toString() {
      return "Genre{id=" + this.id + ", name='" + this.name + "'}";
   }
}
