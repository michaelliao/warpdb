package com.itranswarp.warpdb.test;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UNI_NameEmail", columnNames = { "name", "email" }), indexes = {
		@Index(name = "IDX_TagId", columnList = "the_tag"), @Index(unique = true, columnList = "name, the_tag") })
public class User extends BaseEntity {

	@Column(length = 100, nullable = false)
	public String name;

	@Column(length = 100, unique = true, nullable = false, updatable = false)
	public String email;

	private int age;

	public int getAge() {
		return this.age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	private double score;

	public double getScore() {
		return this.score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Transient
	public String fullName;

	@Column(name = "the_tag")
	public String tag;

	@Transient
	public List<Class<?>> callbacks = new ArrayList<>();

	@PostLoad
	void postLoad() {
		callbacks.add(PostLoad.class);
	}

	@PrePersist
	void prePersist() {
		callbacks.add(PrePersist.class);
		if (this.id == null) {
			this.id = nextId();
		}
		this.createdAt = this.updatedAt = System.currentTimeMillis();
	}

	@PostPersist
	void postPersist() {
		callbacks.add(PostPersist.class);
	}

	@PreUpdate
	void preUpdate() {
		callbacks.add(PreUpdate.class);
		this.updatedAt = System.currentTimeMillis();
	}

	@PostUpdate
	void postUpdate() {
		callbacks.add(PostUpdate.class);
	}

	@PreRemove
	void preRemove() {
		callbacks.add(PreRemove.class);
	}

	@PostRemove
	void postRemove() {
		callbacks.add(PostRemove.class);
	}

}
