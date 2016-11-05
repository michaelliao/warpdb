package com.itranswarp.warpdb;

import java.lang.reflect.InvocationTargetException;

interface PropertyGetter {
	Object get(Object bean) throws IllegalAccessException, InvocationTargetException;
}
