package com.example;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import com.github.tivrfoa.mapresultset.api.ManyToMany;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Relation;


@MappedEntity
public class Person {
	@Id
	private int id;
	private String name;
	@MappedProperty (value = "born_timestamp")
	private Timestamp bornTimestamp;
	@MappedProperty (value = "wakeup_time")
	private Time wakeUpTime;
	//@OneToMany
    @Relation (value = Relation.Kind.ONE_TO_MANY)
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

	public Time getWakeUpTime() {
		return wakeUpTime;
	}

	public void setWakeUpTime(Time wakeUpTime) {
		this.wakeUpTime = wakeUpTime;
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
				", wakeUpTime=" + wakeUpTime +
				", addresses=" + addresses + ", phones=" + phones +  "]";
	}

}