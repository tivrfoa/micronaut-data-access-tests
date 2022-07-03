package com.example;

import com.github.tivrfoa.mapresultset.api.ManyToMany;
import com.github.tivrfoa.mapresultset.api.Table;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import java.util.List;

@MappedEntity
@Table(
   name = "address"
)
public class Address {
   @Id
   @com.github.tivrfoa.mapresultset.api.Id
   private int id;
   private String street;
   @Relation(
      value = Relation.Kind.MANY_TO_MANY,
      mappedBy = "addresses"
   )
   @ManyToMany
   private List<Person> listPerson;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getStreet() {
      return this.street;
   }

   public void setStreet(String street) {
      this.street = street;
   }

   public List<Person> getListPerson() {
      return this.listPerson;
   }

   public void setListPerson(List<Person> listPerson) {
      this.listPerson = listPerson;
   }

   public String toString() {
      List<? extends Object> names = this.listPerson == null ? List.of() : this.listPerson.stream().map(p -> p.getName()).toList();
      return "Address [id=" + this.id + ", street=" + this.street + ", listPerson names=" + names + "]";
   }
}
