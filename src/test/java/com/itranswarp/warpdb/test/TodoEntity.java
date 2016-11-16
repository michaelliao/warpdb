package com.itranswarp.warpdb.test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;

import com.itranswarp.warpdb.converter.LocalDateConverter;
import com.itranswarp.warpdb.converter.LocalDateTimeConverter;

@Entity
public class TodoEntity extends BaseEntity {

	@Column(name = "f_name", length = 100)
	public String name;

	@Convert(converter = LocalDateConverter.class)
	@Column(name = "f_Target_date", columnDefinition = "date")
	public LocalDate targetDate;

	@Convert(converter = LocalDateTimeConverter.class)
	@Column(columnDefinition = "datetime")
	public LocalDateTime targetDateTime;

	@Convert(converter = AddressConverter.class)
	@Column(columnDefinition = "varchar(100)")
	public Address address;
}

class AddressConverter extends CustomConverter {

	@Override
	public String convertToDatabaseColumn(Address attribute) {
		return attribute.city + ":" + attribute.street + ":" + attribute.zip;
	}

	@Override
	public Address convertToEntityAttribute(String dbData) {
		String[] ss = dbData.split(":");
		return new Address(ss[0], ss[1], ss[2]);
	}
}

abstract class CustomConverter implements AttributeConverter<Address, String> {

}