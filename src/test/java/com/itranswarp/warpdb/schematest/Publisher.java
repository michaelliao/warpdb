package com.itranswarp.warpdb.schematest;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "default")
public class Publisher {

    @Id
    public String id;

    public String name;
}
