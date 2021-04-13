/*
 * Copyright 2004-2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Gunsioo Group
 */
package org.gunsioo.test.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.gunsioo.Driver;
import org.gunsioo.api.ErrorCode;
import org.gunsioo.test.TestBase;
import org.gunsioo.test.TestDb;

/**
 * Tests the database driver.
 */
public class TestDriver extends TestDb {

    /**
     * Run just this test.
     *
     * @param a ignored
     */
    public static void main(String... a) throws Exception {
        TestBase.createCaller().init().testFromMain();
    }

    @Override
    public void test() throws Exception {
        testSettingsAsProperties();
        testDriverObject();
        testURLs();
    }

    private void testSettingsAsProperties() throws Exception {
        Properties prop = new Properties();
        prop.put("user", getUser());
        prop.put("password", getPassword());
        prop.put("max_compact_time", "1234");
        prop.put("unknown", "1234");
        String url = getURL("jdbc:gunsioo:mem:driver", true);
        Connection conn = DriverManager.getConnection(url, prop);
        ResultSet rs;
        rs = conn.createStatement().executeQuery(
                "SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME = 'MAX_COMPACT_TIME'");
        rs.next();
        assertEquals(1234, rs.getInt(1));
        conn.close();
    }

    private void testDriverObject() throws Exception {
        Driver instance = Driver.load();
        assertTrue(DriverManager.getDriver("jdbc:gunsioo:~/test") == instance);
        Driver.unload();
        assertThrows(SQLException.class, () -> DriverManager.getDriver("jdbc:gunsioo:~/test"));
        Driver.load();
        assertTrue(DriverManager.getDriver("jdbc:gunsioo:~/test") == instance);
    }

    private void testURLs() throws Exception {
        java.sql.Driver instance = Driver.load();
        assertThrows(ErrorCode.URL_FORMAT_ERROR_2, instance).acceptsURL(null);
        assertThrows(ErrorCode.URL_FORMAT_ERROR_2, instance).connect(null, null);
        assertNull(instance.connect("jdbc:unknown", null));
    }

}
