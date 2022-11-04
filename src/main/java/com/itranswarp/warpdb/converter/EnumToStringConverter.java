package com.itranswarp.warpdb.converter;

import jakarta.persistence.AttributeConverter;

/**
 * Built-in converter to store Java enum type as VARCHAR in database.
 * 
 * @author liaoxuefeng
 *
 * @param <T> Generic type.
 */
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
