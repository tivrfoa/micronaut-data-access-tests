package com.example;

import com.github.tivrfoa.mapresultset.api.ManyToMany;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Relation;
import java.sql.Timestamp;
import java.util.List;

@MappedEntity
public class Person {
   @Id
   @GeneratedValue(GeneratedValue.Type.AUTO)
   private int id;
   private String name;
   @MappedProperty("born_timestamp")
   private Timestamp bornTimestamp;
   @Relation(
      value = Relation.Kind.ONE_TO_MANY,
      mappedBy = "person"
   )
   private List<Phone> phones;
   @Relation(
      value = Relation.Kind.MANY_TO_MANY,
      mappedBy = "listPerson"
   )
   @ManyToMany
   private List<Address> addresses;

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public void setId(int id) {
      this.id = id;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Timestamp getBornTimestamp() {
      return this.bornTimestamp;
   }

   public void setBornTimestamp(Timestamp bornTimestamp) {
      this.bornTimestamp = bornTimestamp;
   }

   public List<Phone> getPhones() {
      return this.phones;
   }

   public void setPhones(List<Phone> phones) {
      this.phones = phones;
   }

   public List<Address> getAddresses() {
      return this.addresses;
   }

   public void setAddresses(List<Address> addresses) {
      this.addresses = addresses;
   }

   public String toString() {
      return "Person [id=" + this.id + ", name=" + this.name + ", bornTimestamp=" + this.bornTimestamp + ", addresses=" + this.addresses + "]";
   }
}
