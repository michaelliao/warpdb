package com.itranswarp.warpdb;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import com.itranswarp.warpdb.converter.EnumToStringConverter;
import com.itranswarp.warpdb.util.ClassUtils;

/**
 * Represent a bean property.
 * 
 * @author liaoxuefeng
 */
class AccessibleProperty {

    // Field or Method:
    final AccessibleObject accessible;

    // java type:
    final Class<?> propertyType;

    // converter:
    final AttributeConverter<Object, Object> converter;

    // java bean property name:
    final String propertyName;

    // table column name:
    final String columnName;

    // column DDL:
    final String columnDefinition;

    // getter and do convert if necessary:
    final PropertyGetter convertGetter;

    // setter and do convert if necessary:
    final PropertySetter convertSetter;

    final boolean nullable;
    final boolean unique;

    boolean isId() {
        return this.accessible.isAnnotationPresent(Id.class);
    }

    // is id && is id marked as @GeneratedValue(strategy=GenerationType.IDENTITY)
    boolean isIdentityId() {
        if (!isId()) {
            return false;
        }
        GeneratedValue gv = this.accessible.getAnnotation(GeneratedValue.class);
        if (gv == null) {
            return false;
        }
        GenerationType gt = gv.strategy();
        return gt == GenerationType.IDENTITY;
    }

    boolean isVersion() {
        boolean isVersion = this.accessible.isAnnotationPresent(Version.class);
        if (isVersion) {
            if (!VERSION_TYPES.contains(this.propertyType)) {
                throw new RuntimeException("Unsupported @Version type: " + this.propertyType.getName());
            }
        }
        return isVersion;
    }

    boolean isInsertable() {
        if (isIdentityId()) {
            return false;
        }
        Column col = this.accessible.getAnnotation(Column.class);
        return col == null || col.insertable();
    }

    boolean isUpdatable() {
        if (isId()) {
            return false;
        }
        Column col = this.accessible.getAnnotation(Column.class);
        return col == null || col.updatable();
    }

    public AccessibleProperty(Field f) {
        this(f.getType(), f.getName(), f, (obj) -> {
            return f.get(obj);
        }, (obj, value) -> {
            f.set(obj, value);
        });
    }

    public AccessibleProperty(String name, Method getter, Method setter) {
        this(getter.getReturnType(), name, getter, (obj) -> {
            return getter.invoke(obj);
        }, (obj, value) -> {
            setter.invoke(obj, value);
        });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private AccessibleProperty(final Class<?> type, final String propertyName, final AccessibleObject accessible, final PropertyGetter getter,
            final PropertySetter setter) {
        accessible.setAccessible(true);
        this.accessible = accessible;
        // check:
        AttributeConverter<Object, Object> converter = getConverter(accessible);
        String columnDefinition = null;
        if (converter == null && type.isEnum()) {
            converter = new EnumToStringConverter(type);
            columnDefinition = "VARCHAR(50)";
        }
        final Class<?> propertyType = checkPropertyType(type, converter);
        final String columnName = getColumnName(accessible, propertyName);
        if (columnDefinition == null) {
            Class<?> ddlType = getConverterType(converter);
            if (ddlType == null) {
                ddlType = propertyType;
            }
            columnDefinition = getColumnDefinition(accessible, ddlType);
        } // init:
        this.nullable = isNullable();
        this.unique = isUnique();
        this.converter = converter;
        this.propertyType = propertyType;
        this.propertyName = propertyName;
        this.columnName = columnName;
        this.columnDefinition = columnDefinition;
        this.convertGetter = this.converter == null ? getter : (bean) -> {
            Object value = getter.get(bean);
            if (value != null) {
                value = this.converter.convertToDatabaseColumn(value);
            }
            return value;
        };
        this.convertSetter = this.converter == null ? setter : (bean, value) -> {
            if (value != null && this.converter != null) {
                value = this.converter.convertToEntityAttribute(value);
            }
            setter.set(bean, value);
        };
    }

    private boolean isNullable() {
        if (isId()) {
            return false;
        }
        Column col = this.accessible.getAnnotation(Column.class);
        return col == null || col.nullable();
    }

    private boolean isUnique() {
        if (isId()) {
            return true;
        }
        Column col = this.accessible.getAnnotation(Column.class);
        return col != null && col.unique();
    }

    @SuppressWarnings("unchecked")
    private AttributeConverter<Object, Object> getConverter(AccessibleObject accessible) {
        Convert converter = accessible.getAnnotation(Convert.class);
        if (converter != null) {
            Class<?> converterClass = converter.converter();
            if (!AttributeConverter.class.isAssignableFrom(converterClass)) {
                throw new RuntimeException("Converter class must be AttributeConverter rather than " + converterClass.getName());
            }
            try {
                Constructor<?> cs = converterClass.getDeclaredConstructor();
                cs.setAccessible(true);
                return (AttributeConverter<Object, Object>) cs.newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                throw new RuntimeException("Cannot instantiate Converter: " + converterClass.getName(), e);
            }
        }
        return null;
    }

    private static Class<?> getConverterType(AttributeConverter<Object, Object> converter) {
        if (converter != null) {
            List<Type> types = ClassUtils.getGenericInterfacesIncludeHierarchy(converter.getClass());
            for (Type type : types) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) type;
                    if (pt.getRawType() == AttributeConverter.class) {
                        Type dbType = pt.getActualTypeArguments()[1];
                        if (dbType instanceof Class) {
                            return (Class<?>) dbType;
                        }
                    }
                }

            }
        }
        return null;
    }

    private static Class<?> checkPropertyType(Class<?> typeClass, AttributeConverter<Object, Object> converter) {
        Class<?> converterType = getConverterType(converter);
        if (converterType != null) {
            typeClass = converterType;
        }
        if (typeClass.isEnum() || DEFAULT_COLUMN_TYPES.containsKey(typeClass)) {
            return typeClass;
        }
        throw new RuntimeException("Unsupported type: " + typeClass);
    }

    private static String getColumnName(AccessibleObject ao, String defaultName) {
        Column col = ao.getAnnotation(Column.class);
        if (col == null || col.name().isEmpty()) {
            return defaultName;
        }
        return col.name();
    }

    private static String getColumnDefinition(AccessibleObject ao, Class<?> type) {
        Column col = ao.getAnnotation(Column.class);
        String colDef = null;
        if (col == null || col.columnDefinition().isEmpty()) {
            if (type.isEnum()) {
                colDef = "VARCHAR(50)";
            } else {
                colDef = getDefaultColumnType(type, col);
            }
        } else {
            colDef = col.columnDefinition().toUpperCase();
        }
        return colDef;
    }

    private static String getDefaultColumnType(Class<?> type, Column col) {
        String ddl = DEFAULT_COLUMN_TYPES.get(type);
        if (ddl.equals("VARCHAR($1)")) {
            ddl = ddl.replace("$1", String.valueOf(col == null ? 255 : col.length()));
        }
        if (ddl.equals("DECIMAL($1,$2)")) {
            int preci = col == null ? 0 : col.precision();
            int scale = col == null ? 0 : col.scale();
            if (preci == 0) {
                preci = 10; // default DECIMAL precision of MySQL
            }
            ddl = ddl.replace("$1", String.valueOf(preci)).replace("$2", String.valueOf(scale));
        }
        return ddl;
    }

    static final Map<Class<?>, String> DEFAULT_COLUMN_TYPES = new HashMap<>();

    static final Set<Class<?>> VERSION_TYPES = new HashSet<>();

    static {
        DEFAULT_COLUMN_TYPES.put(String.class, "VARCHAR($1)");

        DEFAULT_COLUMN_TYPES.put(boolean.class, "BIT");
        DEFAULT_COLUMN_TYPES.put(Boolean.class, "BIT");

        DEFAULT_COLUMN_TYPES.put(byte.class, "TINYINT");
        DEFAULT_COLUMN_TYPES.put(Byte.class, "TINYINT");
        DEFAULT_COLUMN_TYPES.put(short.class, "SMALLINT");
        DEFAULT_COLUMN_TYPES.put(Short.class, "SMALLINT");
        DEFAULT_COLUMN_TYPES.put(int.class, "INTEGER");
        DEFAULT_COLUMN_TYPES.put(Integer.class, "INTEGER");
        DEFAULT_COLUMN_TYPES.put(long.class, "BIGINT");
        DEFAULT_COLUMN_TYPES.put(Long.class, "BIGINT");
        DEFAULT_COLUMN_TYPES.put(float.class, "REAL");
        DEFAULT_COLUMN_TYPES.put(Float.class, "REAL");
        DEFAULT_COLUMN_TYPES.put(double.class, "DOUBLE");
        DEFAULT_COLUMN_TYPES.put(Double.class, "DOUBLE");

        DEFAULT_COLUMN_TYPES.put(BigDecimal.class, "DECIMAL($1,$2)");
        DEFAULT_COLUMN_TYPES.put(java.sql.Date.class, "DATE");
        DEFAULT_COLUMN_TYPES.put(LocalDate.class, "DATE");
        DEFAULT_COLUMN_TYPES.put(LocalTime.class, "TIME");
        DEFAULT_COLUMN_TYPES.put(java.util.Date.class, "DATETIME");
        DEFAULT_COLUMN_TYPES.put(java.sql.Timestamp.class, "TIMESTAMP");

        DEFAULT_COLUMN_TYPES.put(java.sql.Blob.class, "BLOB");
        DEFAULT_COLUMN_TYPES.put(java.sql.Clob.class, "CLOB");

        VERSION_TYPES.addAll(Arrays.asList(long.class, Long.class, int.class, Integer.class, short.class, Short.class, java.sql.Timestamp.class));
    }
}
