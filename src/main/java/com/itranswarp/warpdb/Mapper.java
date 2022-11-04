package com.itranswarp.warpdb;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.itranswarp.warpdb.util.NameUtils;

final class Mapper<T> {

    final Class<T> entityClass;
    final String tableName;

    // @Id property:
    final AccessibleProperty[] ids;
    // @Version property:
    final AccessibleProperty version;

    // all properties including @Id, key is property name (NOT column name)
    final List<AccessibleProperty> allProperties;

    // lower-case property name -> AccessibleProperty
    final Map<String, AccessibleProperty> allPropertiesMap;

    final List<AccessibleProperty> insertableProperties;
    final List<AccessibleProperty> updatableProperties;

    // lower-case property name -> AccessibleProperty
    final Map<String, AccessibleProperty> updatablePropertiesMap;

    final BeanRowMapper<T> rowMapper;

    final String selectSQL;
    final String insertSQL;
    final String insertIgnoreSQL;
    final String updateSQL;
    final String deleteSQL;
    final String whereIdsEquals;

    final Listener prePersist;
    final Listener preUpdate;
    final Listener preRemove;
    final Listener postLoad;
    final Listener postPersist;
    final Listener postUpdate;
    final Listener postRemove;

    public Mapper(Class<T> clazz) {
        super();
        List<AccessibleProperty> all = getPropertiesIncludeHierarchy(clazz);
        // check duplicate name:
        Set<String> propertyNamesSet = new HashSet<>();
        for (String propertyName : all.stream().map((p) -> {
            return p.propertyName;
        }).toArray(String[]::new)) {
            if (!propertyNamesSet.add(propertyName.toLowerCase())) {
                throw new ConfigurationException("Duplicate property name found: " + propertyName + " in class: " + clazz.getName());
            }
        }
        Set<String> columnNamesSet = new HashSet<>();
        for (String columnName : all.stream().map((p) -> {
            return p.columnName;
        }).toArray(String[]::new)) {
            if (!columnNamesSet.add(columnName.toLowerCase())) {
                throw new ConfigurationException("Duplicate column name found: " + columnName);
            }
        }
        // check @Id:
        AccessibleProperty[] ids = all.stream().filter((p) -> {
            return p.isId();
        }).sorted((p1, p2) -> {
            return p1.columnName.compareTo(p2.columnName);
        }).toArray(AccessibleProperty[]::new);
        if (ids.length == 0) {
            throw new ConfigurationException("No @Id found.");
        }
        if (ids.length > 1 && all.stream().filter((p) -> {
            return p.isId() && p.isIdentityId();
        }).count() > 0) {
            throw new ConfigurationException("Mutiple @Id cannot be identity.");
        }
        // get @Version:
        AccessibleProperty[] versions = all.stream().filter((p) -> {
            return p.isVersion();
        }).toArray(AccessibleProperty[]::new);
        if (versions.length > 1) {
            throw new ConfigurationException("Multiple @Version found.");
        }
        this.version = versions.length == 0 ? null : versions[0];

        this.allProperties = all;
        this.allPropertiesMap = buildPropertiesMap(this.allProperties);

        this.insertableProperties = all.stream().filter((p) -> {
            if (p.isIdentityId()) {
                return false;
            }
            return p.isInsertable();
        }).collect(Collectors.toList());

        this.updatableProperties = all.stream().filter((p) -> {
            return p.isUpdatable();
        }).collect(Collectors.toList());

        this.updatablePropertiesMap = buildPropertiesMap(this.updatableProperties);

        // init:
        this.ids = ids;
        this.entityClass = clazz;
        this.tableName = getTableName(clazz);

        this.whereIdsEquals = String.join(" AND ", Arrays.stream(this.ids).map(id -> id.columnName + " = ?").toArray(String[]::new));

        this.selectSQL = "SELECT * FROM " + this.tableName + " WHERE " + this.whereIdsEquals;

        String insertPostfix = this.tableName + " (" + String.join(", ", this.insertableProperties.stream().map((p) -> {
            return p.columnName;
        }).toArray(String[]::new)) + ") VALUES (" + numOfQuestions(this.insertableProperties.size()) + ")";
        this.insertSQL = "INSERT INTO " + insertPostfix;
        this.insertIgnoreSQL = "INSERT IGNORE INTO " + insertPostfix;

        this.updateSQL = "UPDATE " + this.tableName + " SET " + String.join(", ", this.updatableProperties.stream().map((p) -> {
            return p.columnName + " = ?";
        }).toArray(String[]::new)) + " WHERE " + this.whereIdsEquals;

        this.deleteSQL = "DELETE FROM " + this.tableName + " WHERE " + this.whereIdsEquals;

        this.rowMapper = new BeanRowMapper<>(this.entityClass, this.allProperties);

        List<Method> methods = this.findMethods(clazz);
        this.prePersist = findListener(methods, PrePersist.class);
        this.preUpdate = findListener(methods, PreUpdate.class);
        this.preRemove = findListener(methods, PreRemove.class);
        this.postLoad = findListener(methods, PostLoad.class);
        this.postPersist = findListener(methods, PostPersist.class);
        this.postUpdate = findListener(methods, PostUpdate.class);
        this.postRemove = findListener(methods, PostRemove.class);
    }

    static List<String> columnDefinitionSortBy = Arrays.asList("BIT", "BOOL", "TINYINT", "SMALLINT", "MEDIUMINT", "INT", "INTEGER", "BIGINT", "FLOAT", "REAL",
            "DOUBLE", "DECIMAL", "YEAR", "DATE", "TIME", "DATETIME", "TIMESTAMP", "VARCHAR", "CHAR", "BLOB", "TEXT", "MEDIUMTEXT");

    static int columnDefinitionSortIndex(String definition) {
        int pos = definition.indexOf('(');
        if (pos > 0) {
            definition = definition.substring(0, pos);
        }
        int index = columnDefinitionSortBy.indexOf(definition.toUpperCase());
        return index == (-1) ? Integer.MAX_VALUE : index;
    }

    public String ddl() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("CREATE TABLE ").append(this.tableName).append(" (\n");
        sb.append(String.join(",\n", this.allProperties.stream().sorted(new Comparator<AccessibleProperty>() {
            @Override
            public int compare(AccessibleProperty o1, AccessibleProperty o2) {
                // sort by ID first:
                if (o1.isId()) {
                    return -1;
                }
                if (o2.isId()) {
                    return 1;
                }
                // sort by columnDefinition:
                int index1 = columnDefinitionSortIndex(o1.columnDefinition);
                int index2 = columnDefinitionSortIndex(o2.columnDefinition);
                if (index1 != index2) {
                    return Integer.compare(index1, index2);
                }
                if (o1.columnDefinition.startsWith("VARCHAR") || o1.columnDefinition.startsWith("CHAR")) {
                    // sort by length:
                    if (o1.columnDefinition.length() != o2.columnDefinition.length()) {
                        return Integer.compare(o1.columnDefinition.length(), o2.columnDefinition.length());
                    }
                }
                // sort by columnName:
                return o1.columnName.compareTo(o2.columnName);
            }
        }).map((p) -> {
            return "  " + p.columnName + " " + p.columnDefinition // definition
                    + (p.isIdentityId() ? " AUTO_INCREMENT" : "") // identity
                    + (p.nullable ? " NULL" : " NOT NULL") // nullable?
                    + (!p.isId() && p.unique ? " UNIQUE" : ""); // unique?
        }).toArray(String[]::new)));
        sb.append(",\n");
        // add unique key:
        sb.append(getUniqueKey());
        // add index:
        sb.append(getIndex());
        // add primary key:
        sb.append("  PRIMARY KEY(").append(String.join(", ", Arrays.stream(this.ids).map(id -> id.columnName).toArray(String[]::new))).append(")\n");
        sb.append(");\n");
        return sb.toString();
    }

    String getUniqueKey() {
        Table table = this.entityClass.getAnnotation(Table.class);
        if (table != null) {
            return Arrays.stream(table.uniqueConstraints()).map((c) -> {
                String name = c.name().isEmpty() ? "UNI_" + String.join("_", c.columnNames()) : c.name();
                return "  CONSTRAINT " + name + " UNIQUE (" + String.join(", ", c.columnNames()) + "),\n";
            }).reduce("", (acc, s) -> {
                return acc + s;
            });
        }
        return "";
    }

    String getIndex() {
        Table table = this.entityClass.getAnnotation(Table.class);
        if (table != null) {
            return Arrays.stream(table.indexes()).map((c) -> {
                if (c.unique()) {
                    String name = c.name().isEmpty() ? "UNI_" + c.columnList().replace(" ", "").replace(",", "_") : c.name();
                    return "  CONSTRAINT " + name + " UNIQUE (" + c.columnList() + "),\n";
                } else {
                    String name = c.name().isEmpty() ? "IDX_" + c.columnList().replace(" ", "").replace(",", "_") : c.name();
                    return "  INDEX " + name + " (" + c.columnList() + "),\n";
                }
            }).reduce("", (acc, s) -> {
                return acc + s;
            });
        }
        return "";
    }

    Object[] getIdsValue(Object bean) throws IllegalAccessException, InvocationTargetException {
        Object[] values = new Object[this.ids.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = this.ids[i].convertGetter.get(bean);
        }
        return values;
    }

    Map<String, AccessibleProperty> buildPropertiesMap(List<AccessibleProperty> props) {
        Map<String, AccessibleProperty> map = new HashMap<>();
        for (AccessibleProperty prop : props) {
            map.put(prop.propertyName.toLowerCase(), prop);
        }
        return map;
    }

    Listener findListener(List<Method> methods, Class<? extends Annotation> anno) {
        Method target = null;
        for (Method m : methods) {
            if (m.isAnnotationPresent(anno)) {
                if (target == null) {
                    target = m;
                } else {
                    throw new ConfigurationException("Found multiple @" + anno.getSimpleName());
                }
            }
        }
        if (target == null) {
            return EMPTY_LISTENER;
        }
        // check target:
        if (target.getParameterTypes().length > 0) {
            throw new ConfigurationException("Invalid listener method: " + target.getName() + ". Expect zero args.");
        }
        if (Modifier.isStatic(target.getModifiers())) {
            throw new ConfigurationException("Invalid listener method: " + target.getName() + ". Cannot be static.");
        }
        target.setAccessible(true);
        Method listener = target;
        return (obj) -> {
            listener.invoke(obj);
        };
    }

    static final Listener EMPTY_LISTENER = new Listener() {
        @Override
        public void invoke(Object obj) throws IllegalAccessException, InvocationTargetException {
        }
    };

    List<Method> findMethods(Class<T> clazz) {
        List<Method> list = new ArrayList<>(50);
        findMethodsIncludeHierarchy(clazz, list);
        return list;
    }

    void findMethodsIncludeHierarchy(Class<?> clazz, List<Method> methods) {
        Method[] ms = clazz.getDeclaredMethods();
        for (Method m : ms) {
            methods.add(m);
        }
        if (clazz.getSuperclass() != Object.class) {
            findMethodsIncludeHierarchy(clazz.getSuperclass(), methods);
        }
    }

    String numOfQuestions(int n) {
        String[] qs = new String[n];
        return String.join(", ", Arrays.stream(qs).map((s) -> {
            return "?";
        }).toArray(String[]::new));
    }

    String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null) {
            String schema = table.schema();
            String tableName = table.name();
            if (tableName.isEmpty()) {
                tableName = NameUtils.toCamelCaseName(clazz.getSimpleName());
            }
            return schema.isEmpty() ? tableName : schema + "." + tableName;
        }
        return NameUtils.toCamelCaseName(clazz.getSimpleName());
    }

    List<AccessibleProperty> getPropertiesIncludeHierarchy(Class<?> clazz) {
        List<AccessibleProperty> properties = new ArrayList<>();
        addFieldPropertiesIncludeHierarchy(clazz, properties);
        // find methods:
        List<AccessibleProperty> foundMethods = Arrays.stream(clazz.getMethods()).filter((m) -> {
            int mod = m.getModifiers();
            // exclude @Transient:
            if (m.isAnnotationPresent(Transient.class)) {
                return false;
            }
            // exclude static:
            if (Modifier.isStatic(mod)) {
                return false;
            }
            // exclude getClass():
            if (m.getName().equals("getClass")) {
                return false;
            }
            // check if getter:
            if (m.getParameterTypes().length > 0) {
                return false;
            }
            if (m.getName().startsWith("get") && m.getName().length() >= 4) {
                return true;
            }
            if (m.getName().startsWith("is") && m.getName().length() >= 3 && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                return true;
            }
            return false;
        }).map((getter) -> {
            Class<?> type = getter.getReturnType();
            String name;
            if (getter.getName().startsWith("get")) {
                name = Character.toLowerCase(getter.getName().charAt(3)) + getter.getName().substring(4);
            } else {
                // isXxx()
                name = Character.toLowerCase(getter.getName().charAt(2)) + getter.getName().substring(3);
            }
            // find setter:
            String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            Method setter;
            try {
                setter = clazz.getMethod(setterName, type);
            } catch (NoSuchMethodException e) {
                throw new ConfigurationException(e);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            return new AccessibleProperty(name, getter, setter);
        }).collect(Collectors.toList());
        properties.addAll(foundMethods);
        return properties;
    }

    void addFieldPropertiesIncludeHierarchy(Class<?> clazz, List<AccessibleProperty> collector) {
        List<AccessibleProperty> foundFields = Arrays.stream(clazz.getDeclaredFields()).filter((f) -> {
            int mod = f.getModifiers();
            // exclude final, static:
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
                return false;
            }
            // exclude @Transient:
            if (Modifier.isPublic(mod) && f.isAnnotationPresent(Transient.class)) {
                return false;
            }
            // exclude transient:
            if (Modifier.isPublic(mod) && Modifier.isTransient(mod)) {
                return false;
            }
            // include public:
            if (Modifier.isPublic(mod)) {
                return true;
            }
            if (f.isAnnotationPresent(Column.class) || f.isAnnotationPresent(Id.class)) {
                return true;
            }
            return false;
        }).map((f) -> {
            return new AccessibleProperty(f);
        }).collect(Collectors.toList());
        collector.addAll(foundFields);
        if (clazz.getSuperclass() != Object.class) {
            addFieldPropertiesIncludeHierarchy(clazz.getSuperclass(), collector);
        }
    }

}
