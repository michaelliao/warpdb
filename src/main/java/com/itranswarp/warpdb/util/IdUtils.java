package com.itranswarp.warpdb.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generate a 16-char String id that composed by: "timestamp-incr".
 * 
 * @author michael
 */
public class IdUtils {

    static final char[] BASE32_CHARS = "0123456789abcdefghijklmnopqrstuv".toCharArray();

    static final AtomicLong sequence = new AtomicLong(System.currentTimeMillis() & 0x1fffffff);

    static final Random random = new Random();

    /**
     * Generate a 16-char String id that composed by:
     * 
     * timestamp: 9-chars; seq: 7-chars
     * 
     * @return 16-char String.
     */
    public static String next() {
        String timestamp = longToBase32(System.currentTimeMillis());
        String seq = longToBase32(sequence.getAndIncrement() & 0x1fffffff);
        StringBuilder builder = new StringBuilder("0000000000000000");
        setStringAt(builder, 0, timestamp, 9);
        setStringAt(builder, 9, seq, 7);
        return builder.toString();
    }

    static void setStringAt(StringBuilder buffer, int bufferStart, String str, int length) {
        int strStart = 0;
        int strEnd = str.length();
        if (str.length() < length) {
            // str="ff", length=5:
            bufferStart = bufferStart + (length - str.length());
            strEnd = str.length();
        } else {
            // str="f1c2d3", length=3:
            strStart = str.length() - length;
        }
        for (int i = bufferStart, n = strStart; n < strEnd; i++, n++) {
            buffer.setCharAt(i, str.charAt(n));
        }
    }

    /**
     * Is string a valid 16-char id.
     * 
     * @param s Id string.
     * @return true if it is a 16-char id.
     */
    public static boolean isValidId(String s) {
        if (s == null || s.length() != 16) {
            return false;
        }
        for (int i = 0; i < 15; i++) {
            char ch = s.charAt(i);
            if (ch >= 'a' && ch <= 'v') {
                continue;
            }
            if (ch >= '0' && ch <= '9') {
                continue;
            }
            return false;
        }
        return true;
    }

    static String longToBase32(long n) {
        char[] buffer = new char[13];
        for (int i = 12; i >= 0; i--) {
            int x = (int) (n & 0x1f);
            buffer[i] = BASE32_CHARS[x];
            n = n >> 5;
            if (n == 0) {
                return new String(buffer, i, 13 - i);
            }
        }
        return new String(buffer);
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 100; i++) {
            System.out.println(next());
        }
        Thread.sleep(1);
        for (int i = 0; i < 100; i++) {
            System.out.println(next());
        }
    }
}
