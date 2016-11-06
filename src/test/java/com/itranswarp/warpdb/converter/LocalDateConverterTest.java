package com.itranswarp.warpdb.converter;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

public class LocalDateConverterTest {

	LocalDateConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new LocalDateConverter();
	}

	@Test
	public void testConvert() {
		LocalDate javaDate = LocalDate.of(2016, 10, 20);
		java.sql.Date dbDate = converter.convertToDatabaseColumn(javaDate);
		Calendar c = Calendar.getInstance();
		c.setTime(dbDate);
		assertEquals(2016, c.get(Calendar.YEAR));
		assertEquals(9, c.get(Calendar.MONTH));
		assertEquals(20, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, c.get(Calendar.MINUTE));
		assertEquals(0, c.get(Calendar.SECOND));
		// db -> java:
		LocalDate read = converter.convertToEntityAttribute(dbDate);
		assertEquals(javaDate, read);
	}

}
