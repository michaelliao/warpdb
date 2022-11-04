package com.itranswarp.warpdb.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocalDateTimeConverterTest {

    LocalDateTimeConverter converter;

    @BeforeEach
    public void setUp() throws Exception {
        converter = new LocalDateTimeConverter();
    }

    @Test
    public void testConvert() {
        LocalDateTime javaDateTime = LocalDateTime.of(2016, 10, 20, 11, 12, 13);
        java.util.Date dbDateTime = converter.convertToDatabaseColumn(javaDateTime);
        Calendar c = Calendar.getInstance();
        c.setTime(dbDateTime);
        assertEquals(2016, c.get(Calendar.YEAR));
        assertEquals(9, c.get(Calendar.MONTH));
        assertEquals(20, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(11, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(12, c.get(Calendar.MINUTE));
        assertEquals(13, c.get(Calendar.SECOND));
        // db -> java:
        LocalDateTime read = converter.convertToEntityAttribute(dbDateTime);
        assertEquals(javaDateTime, read);
    }

}
