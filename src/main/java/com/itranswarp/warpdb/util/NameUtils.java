package com.itranswarp.warpdb.util;

public class NameUtils {

	/**
	 * HelloWorld -> helloWorld
	 */
	public static String toCamelCaseName(String name) {
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * HelloWorld -> hello_world
	 */
	public static String toUnderscoreName(String name) {
		StringBuilder sb = new StringBuilder(name.length() + 5);
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (Character.isUpperCase(ch)) {
				if (i > 0) {
					sb.append('_');
				}
				sb.append(Character.toLowerCase(ch));
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
}
