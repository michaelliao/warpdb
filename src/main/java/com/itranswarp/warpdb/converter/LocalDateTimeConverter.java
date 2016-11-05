package com.itranswarp.warpdb.converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.AttributeConverter;

public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, java.util.Date> {

	static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();

	@Override
	public java.util.Date convertToDatabaseColumn(LocalDateTime attribute) {
		return new java.util.Date(attribute.atZone(SYSTEM_ZONE_ID).toEpochSecond() * 1000);
	}

	@Override
	public LocalDateTime convertToEntityAttribute(java.util.Date dbData) {
		return Instant.ofEpochMilli(dbData.getTime()).atZone(SYSTEM_ZONE_ID).toLocalDateTime();
	}

}
