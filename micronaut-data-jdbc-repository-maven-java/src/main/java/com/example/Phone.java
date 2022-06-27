package com.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tivrfoa.mapresultset.api.Table;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;

@Table
@MappedEntity (value = "Phone")
public class Phone {
    @Id
	private int id;
	private int number;

	@Relation (value = Relation.Kind.MANY_TO_ONE)
	private Person person;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}

	@JsonIgnore
	public Person getPerson() {
		return person;
	}
	public void setPerson(Person person) {
		this.person = person;
	}

	@Override
	public String toString() {
		return "Phone [id=" + id + ", number=" + number + "]";
	}
}