package com.itranswarp.warpdb.util;

public class NameUtils {

	/**
	 * to camel case name. e.g. "HelloWorld" -> "helloWorld.
	 * 
	 * @param name
	 *            Name starts with upper case.
	 * @return Camel case name.
	 */
	public static String toCamelCaseName(String name) {
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * To underscore name. e.g. "HelloWorld" -> "hello_world".
	 * 
	 * @param name
	 *            Name without underscore.
	 * @return Underscore name.
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
