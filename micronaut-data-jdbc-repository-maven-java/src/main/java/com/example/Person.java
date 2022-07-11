package com.example;

import java.sql.Timestamp;
import java.util.List;

import io.github.tivrfoa.mapresultset.api.ManyToMany;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Relation;


@MappedEntity
public class Person {
	@Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
	private int id;
	private String name;
	@MappedProperty (value = "born_timestamp")
	private Timestamp bornTimestamp;
	//@OneToMany
    @Relation (value = Relation.Kind.ONE_TO_MANY, mappedBy = "person")
	private List<Phone> phones;
	// @ManyToOne
	// private Country country;
	@ManyToMany
    @Relation (value = Relation.Kind.MANY_TO_MANY, mappedBy = "listPerson")
	private List<Address> addresses;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getBornTimestamp() {
		return bornTimestamp;
	}

	public void setBornTimestamp(Timestamp bornTimestamp) {
		this.bornTimestamp = bornTimestamp;
	}

	public List<Phone> getPhones() {
		return phones;
	}

	public void setPhones(List<Phone> phones) {
		this.phones = phones;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", bornTimestamp=" + bornTimestamp +
		//		", addresses=" + addresses + ", phones=" + phones +  "]";
				", addresses=" + addresses + "]";
	}

}