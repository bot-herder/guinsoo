/*
 * Copyright 2021 Guinsoo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Guinsoo Group
 */
package org.guinsoo.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Properties;

import org.guinsoo.message.DbException;
import org.guinsoo.store.FileLister;
import org.guinsoo.store.fs.FileUtils;
import org.guinsoo.api.ErrorCode;

/**
 * Command line tools implement the tool interface so that they can be used in
 * the Guinsoo Console.
 */
public abstract class Tool {

    /**
     * The output stream where this tool writes to.
     */
    protected PrintStream out = System.out;

    private Properties resources;

    /**
     * Sets the standard output stream.
     *
     * @param out the new standard output stream
     */
    public void setOut(PrintStream out) {
        this.out = out;
    }

    /**
     * Run the tool with the given output stream and arguments.
     *
     * @param args the argument list
     */
    public abstract void runTool(String... args) throws SQLException;

    /**
     * Throw a SQLException saying this command line option is not supported.
     *
     * @param option the unsupported option
     * @return this method never returns normally
     */
    protected SQLException showUsageAndThrowUnsupportedOption(String option)
            throws SQLException {
        showUsage();
        throw throwUnsupportedOption(option);
    }

    /**
     * Throw a SQLException saying this command line option is not supported.
     *
     * @param option the unsupported option
     * @return this method never returns normally
     */
    protected SQLException throwUnsupportedOption(String option)
            throws SQLException {
        throw DbException.getJdbcSQLException(
                ErrorCode.FEATURE_NOT_SUPPORTED_1, option);
    }

    /**
     * Print to the output stream that no database files have been found.
     *
     * @param dir the directory or null
     * @param db the database name or null
     */
    protected void printNoDatabaseFilesFound(String dir, String db) {
        StringBuilder buff;
        dir = FileLister.getDir(dir);
        if (!FileUtils.isDirectory(dir)) {
            buff = new StringBuilder("Directory not found: ");
            buff.append(dir);
        } else {
            buff = new StringBuilder("No database files have been found");
            buff.append(" in directory ").append(dir);
            if (db != null) {
                buff.append(" for the database ").append(db);
            }
        }
        out.println(buff.toString());
    }

    /**
     * Print the usage of the tool. This method reads the description from the
     * resource file.
     */
    protected void showUsage() {
        if (resources == null) {
            resources = new Properties();
            String resourceName = "/org/guinsoo/res/javadoc.properties";
            try {
                byte[] buff = Utils.getResource(resourceName);
                if (buff != null) {
                    resources.load(new ByteArrayInputStream(buff));
                }
            } catch (IOException e) {
                out.println("Cannot load " + resourceName);
            }
        }
        String className = getClass().getName();
        out.println(resources.get(className));
        out.println("Usage: java "+getClass().getName() + " <options>");
        out.println(resources.get(className + ".main"));
        out.println("See also https://guinsoodatabase.com/javadoc/" +
                className.replace('.', '/') + ".html");
    }

    /**
     * Check if the argument matches the option.
     * If the argument starts with this option, but doesn't match,
     * then an exception is thrown.
     *
     * @param arg the argument
     * @param option the command line option
     * @return true if it matches
     */
    public static boolean isOption(String arg, String option) {
        if (arg.equals(option)) {
            return true;
        } else if (arg.startsWith(option)) {
            throw DbException.getUnsupportedException(
                    "expected: " + option + " got: " + arg);
        }
        return false;
    }

}
