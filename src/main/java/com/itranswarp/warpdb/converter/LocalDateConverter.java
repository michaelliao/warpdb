package com.itranswarp.warpdb.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import jakarta.persistence.AttributeConverter;

/**
 * Built-in converter to store Java LocalDate type to database DATE type.
 * 
 * @author liaoxuefeng
 */
public class LocalDateConverter implements AttributeConverter<LocalDate, java.sql.Date> {

	static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();

	@Override
	public java.sql.Date convertToDatabaseColumn(LocalDate attribute) {
		return new java.sql.Date(attribute.atStartOfDay(SYSTEM_ZONE_ID).toEpochSecond() * 1000);
	}

	@Override
	public LocalDate convertToEntityAttribute(java.sql.Date dbData) {
		return Instant.ofEpochMilli(dbData.getTime()).atZone(SYSTEM_ZONE_ID).toLocalDate();
	}

}
