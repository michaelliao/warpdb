package com.itranswarp.warpdb;

@FunctionalInterface
public interface SqlObjectConverter {

	public Object convert(Object obj);
}
