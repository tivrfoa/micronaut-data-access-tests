package com.example;

import com.github.tivrfoa.mapresultset.api.Table;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@Table
@MappedEntity
public class Phone {
    @Id
	private int id;
	private int number;
	
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

	@Override
	public String toString() {
		return "Phone [id=" + id + ", number=" + number + "]";
	}
}