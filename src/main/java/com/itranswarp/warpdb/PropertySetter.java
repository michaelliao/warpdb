package com.itranswarp.warpdb;

import java.lang.reflect.InvocationTargetException;

interface PropertySetter {
	void set(Object bean, Object value) throws IllegalAccessException, InvocationTargetException;
}
