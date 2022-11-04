package com.itranswarp.warpdb;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.itranswarp.warpdb.invalid.dupcol.DupColNameEntity;
import com.itranswarp.warpdb.invalid.dupprop.DupPropNameEntity;
import com.itranswarp.warpdb.invalid.duptable.DupTableNameEntity;
import com.itranswarp.warpdb.invalid.missingid.MissingIdEntity;
import com.itranswarp.warpdb.invalid.multiid.MultiIdEntity;
import com.itranswarp.warpdb.invalid.multiupdate.MultiPreUpdateEntity;
import com.itranswarp.warpdb.invalid.multiversion.MultiVersionEntity;

public class WarpDbConfigTest {

    @Test
    public void testInvalidForMissingId() {
        WarpDb warpdb = new WarpDb();
        warpdb.basePackages = Arrays.asList(MissingIdEntity.class.getPackage().getName());
        assertThrows(ConfigurationException.class, () -> {
            warpdb.init();
        });
    }

    @Test
    public void testInvalidForMultiId() {
        WarpDb warpdb = new WarpDb();
        warpdb.basePackages = Arrays.asList(MultiIdEntity.class.getPackage().getName());
        assertThrows(ConfigurationException.class, () -> {
            warpdb.init();
        });
    }

    @Test
    public void testInvalidForMultiVersion() {
        WarpDb warpdb = new WarpDb();
        warpdb.basePackages = Arrays.asList(MultiVersionEntity.class.getPackage().getName());
        assertThrows(ConfigurationException.class, () -> {
            warpdb.init();
        });
    }

    @Test
    public void testInvalidForDuplicatePropertyName() {
        WarpDb warpdb = new WarpDb();
        warpdb.basePackages = Arrays.asList(DupPropNameEntity.class.getPackage().getName());
        assertThrows(ConfigurationException.class, () -> {
            warpdb.init();
        });
    }

    @Test
    public void testInvalidForDuplicateColumnName() {
        WarpDb warpdb = new WarpDb();
        warpdb.basePackages = Arrays.asList(DupColNameEntity.class.getPackage().getName());
        assertThrows(ConfigurationException.class, () -> {
            warpdb.init();
        });
    }

    @Test
    public void testInvalidForDuplicateTableName() {
        WarpDb warpdb = new WarpDb();
        warpdb.basePackages = Arrays.asList(DupTableNameEntity.class.getPackage().getName());
        assertThrows(ConfigurationException.class, () -> {
            warpdb.init();
        });
    }

    @Test
    public void testInvalidForMultiPreUpdate() {
        WarpDb warpdb = new WarpDb();
        warpdb.basePackages = Arrays.asList(MultiPreUpdateEntity.class.getPackage().getName());
        assertThrows(ConfigurationException.class, () -> {
            warpdb.init();
        });
    }
}
