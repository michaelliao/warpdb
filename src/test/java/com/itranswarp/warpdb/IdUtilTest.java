package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import com.itranswarp.warpdb.entity.BaseEntity;

public class IdUtilTest {

	@Test
	public void testLongToBase32() {
		assertEquals("0", IdUtil.longToBase32(0));
		assertEquals("1", IdUtil.longToBase32(1));
		assertEquals("g", IdUtil.longToBase32(16));
		assertEquals("v", IdUtil.longToBase32(31));
		assertEquals("34", IdUtil.longToBase32(100));
		assertEquals("v8", IdUtil.longToBase32(1000));
		assertEquals("31kv", IdUtil.longToBase32(99999));
		assertEquals("r41o", IdUtil.longToBase32(888888));
		assertEquals("1vvvvvu", IdUtil.longToBase32(2147483646));
		assertEquals("1vvvvvv", IdUtil.longToBase32(2147483647));
		assertEquals("kigb5hbu", IdUtil.longToBase32(707070707070L));
		assertEquals("7vvvvvvvvvv", IdUtil.longToBase32(9007199254740991L));
		assertEquals("80000000000", IdUtil.longToBase32(9007199254740992L));
		assertEquals("80000000001", IdUtil.longToBase32(9007199254740993L));
		assertEquals("7vvvvvvvvvvvv", IdUtil.longToBase32(9223372036854775807L));
		assertEquals("7vvvvvvvvvvvv", IdUtil.longToBase32(Long.MAX_VALUE));
	}

	@Test
	public void testIntToBase32() {
		assertEquals("0", IdUtil.intToBase32(0));
		assertEquals("1", IdUtil.intToBase32(1));
		assertEquals("g", IdUtil.intToBase32(16));
		assertEquals("v", IdUtil.intToBase32(31));
		assertEquals("34", IdUtil.intToBase32(100));
		assertEquals("v8", IdUtil.intToBase32(1000));
		assertEquals("31kv", IdUtil.intToBase32(99999));
		assertEquals("r41o", IdUtil.intToBase32(888888));
		assertEquals("1vvvvvu", IdUtil.intToBase32(2147483646));
		assertEquals("1vvvvvv", IdUtil.intToBase32(2147483647));
		assertEquals("1vvvvvv", IdUtil.intToBase32(Integer.MAX_VALUE));
	}

	@Test
	public void testIsValidId() {
		String[] invalidStrs = { null, "", "1", "22", "333", "4444", IdUtil.next().substring(1), IdUtil.next() + "0",
				IdUtil.next().replace("0", "x"), IdUtil.next().replace("0", "F"), IdUtil.next().replace("0", "w") };
		for (String invalid : invalidStrs) {
			assertFalse(IdUtil.isValidId(invalid));
		}
		for (int i = 0; i < 100; i++) {
			assertTrue(IdUtil.isValidId(IdUtil.next()));
		}
	}

	@Test
	public void testNext() {
		final int COUNT = 100;
		final long TS = System.currentTimeMillis();
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < COUNT; i++) {
			String id = IdUtil.next();
			// check length:
			assertEquals(BaseEntity.ID_LENGTH, id.length());
			// check 'xxxxxxx-IP-xxx':
			assertTrue(id.substring(9).startsWith(IdUtil.IP));
			// check 'timestamp-xxx-xxxx':
			assertTrue(Long.parseLong(id.substring(0, 9), 32) - TS < 100);
			set.add(id);
		}
		assertEquals(COUNT, set.size());
	}

	@Test
	public void testNextLong() {
		int num = 10000;
		Set<Long> set = new HashSet<Long>(num);
		for (int i = 0; i < num; i++) {
			set.add(IdUtil.nextLong());
		}
		assertEquals(num, set.size());
	}

	@Test
	public void testNextLongInMultiThreads() throws Exception {
		final int threads = 400;
		final int num = 2000;
		final Map<Long, Boolean> map = new ConcurrentHashMap<Long, Boolean>(num * threads);
		List<Thread> list = new ArrayList<Thread>();
		for (int n = 0; n < threads; n++) {
			list.add(new Thread() {
				@Override
				public void run() {
					for (int i = 0; i < num; i++) {
						long x = IdUtil.nextLong();
						if (map.put(x, Boolean.TRUE) != null) {
							System.out.println("duplicate: " + Long.toHexString(x));
						}
					}
				}
			});
		}
		// start thread:
		for (Thread t : list) {
			t.start();
		}
		// wait thread:
		for (Thread t : list) {
			t.join();
		}
		assertEquals(num * threads, map.size());
	}
}
