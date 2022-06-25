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
   private double value;
   private String country;

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

   public double getValue() {
      return this.value;
   }

   public void setValue(double value) {
      this.value = value;
   }

   public String getCountry() {
      return this.country;
   }

   public void setCountry(String country) {
      this.country = country;
   }

   public String toString() {
      return "Genre [id=" + this.id + ", name=" + this.name + ", value=" + this.value + ", country=" + this.country + "]";
   }
}
