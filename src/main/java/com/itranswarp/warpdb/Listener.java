package com.itranswarp.warpdb;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface Listener {

	void invoke(Object obj) throws IllegalAccessException, InvocationTargetException;

}
