package com.itranswarp.warpdb.converter;

import javax.persistence.AttributeConverter;

public class EnumToStringConverter<T extends Enum<T>> implements AttributeConverter<Enum<T>, String> {

	final Class<T> enumType;

	public EnumToStringConverter(Class<T> enumType) {
		this.enumType = enumType;
	}

	@Override
	public String convertToDatabaseColumn(Enum<T> attribute) {
		return attribute.name();
	}

	@Override
	public Enum<T> convertToEntityAttribute(String dbData) {
		return Enum.valueOf(enumType, dbData);
	}

}
