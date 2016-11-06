package com.itranswarp.warpdb;

import static org.junit.Assert.*;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.junit.Before;
import org.junit.Test;

public class WarpDbQueryTest extends WarpDbTestBase {

	@Before
	public void prepareData() {
		warpdb.save();
	}

	@Test
	public void testQuery1() throws Exception {
	}

}
