# warpdb

DSL-driven RDBMS interface for Java:

![DEMO](https://github.com/michaelliao/warpdb/raw/master/warpdb.gif)

Build status:

[![Build Status](https://travis-ci.org/michaelliao/warpdb.svg?branch=master)](https://travis-ci.org/michaelliao/warpdb)

### Design principles

* JPA-annotation based configuration.
* DSL-style API reads like English.
* Support raw SQL for very complex query.
* No "attach/dettach".
* All entities are simple POJOs without proxy-ing.

### Database Support

* MySQL 5.x

### Configuration

Maven dependency:

```
<dependency>
    <groupId>com.itranswarp</groupId>
    <artifactId>warpdb</artifactId>
    <version>2.9</version>
</dependency>
```

Warpdb is built on top of Spring-JDBC. JdbcTemplate or DataSource is required when build warpdb instance:

```
<bean class="com.itranswarp.warpdb.WarpDb" init-method="init">
    <property name="basePackages">
        <list>
            <value>com.test.product.model</value>
            <value>com.test.order.model</value>
        </list>
    </property>
    <property name="jdbcTemplate" ref="jdbcTemplate" />
</bean>
```

Or using data source:

```
<bean class="com.itranswarp.warpdb.WarpDb" init-method="init">
    <property name="basePackages">
        <list>
            <value>com.test.product.model</value>
            <value>com.test.order.model</value>
        </list>
    </property>
    <property name="dataSource" ref="dataSource" />
</bean>
```

# Basic Usage

### Fully JPA Annotation Support

Entities are configured with JPA annotation:

```
@Entity
@Table(name="user")
public class User {
    @Id
    String id;

    @Column(nullable=false)
    String name;
}
```

### Query

Query by primary key:

```
// get user, or throw EntityNotFoundException if not found:
User user = warpdb.get(User.class, "123");

// get user, or return null if not found:
User another = warpdb.fetch(User.class, "456");
```

Warpdb supports criteria query and raw SQL query, both are type-safe:

```
List<User> users = warpdb.from(User.class)
        .where("name=?", "bob")
        .orderBy("updatedAt").desc()
        .list();
```

Get first result or null if not found:

```
User user = warpdb.from(User.class)
        .where("name=?", "bob")
        .orderBy("updatedAt").desc()
        .first();
```

Using raw SQL:

```
List<User> users = warpdb.query("select * from User order by name limit 100");
```

### Paged Query

Warpdb supports paged query by specify page index and page size:

```
// page 3, 10 items per page:
PagedResults<User> pr = warpdb.from(User.class)
        .orderBy("updatedAt")
        .list(3, 10);
System.out.println(pr.page.pageIndex); // 3
System.out.println(pr.page.itemsPerPage); // 10
System.out.println(pr.page.totalPages); // 92
System.out.println(pr.page.totalItems); // 912
List<User> list = pr.results; // current page items
```

A paged query will generate 2 SQLs when execute `list(pageIndex, pageSize)`:

```
SELECT COUNT(*) FROM User;
SELECT * FROM User ORDER BY updatedAt limit 20, 10
```

### Save

Using `save()` to insert one or more entities:

```
User user = new User();
user.setId(...);
user.setName(...);
Product product = new Product();
product.setId(...);
product.setName(...);
warpdb.save(user, product);
```

### Update

Using `update()` to update one or more entities:

```
User user = ...
user.setName(...);
Product product = ...
product.setName(...);
warpdb.update(user, product);
```

### Remove

Using `remove()` to remove one or more entities:

```
User user = ...
Product product = ...
warpdb.remove(user, product);
```

# Misc

### Enum Support

Enum is stored as `VARCHAR(50)` in database:

```
@Entity
public class User {
    RoleEnum role;
}
```

### Attribute Converter

Values used in Java and db can be converted by attribute converter:

```
@Entity
public class User {
    // stored as "DATE" in db:
    @Convert(converter = LocalDateConverter.class)
    @Column(columnDefinition = "date")
    public LocalDate birth;
}
```

### Listeners

Listeners must be added as entity method with annotation `PostLoad`, `PrePersist`, `PostPersist`, `PreUpdate`, `PostUpdate`, `PreRemove`, `PostRemove`:

```
@Entity
public class User {
    @Id
    String id;

    long createdAt;

    @PrePersist()
    public void prePersist() {
        if (this.id == null) {
            this.id = nextId();
        }
        this.createdAt = System.currentTimeMillis();
    }
}
```

### Schema Export

Using `ddl()` to export schema:

```
String ddl = warpdb.ddl();
```

