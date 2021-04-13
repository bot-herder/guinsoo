/*
 * Copyright 2004-2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Gunsioo Group
 */
package org.gunsioo.jdbc;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLNonTransientConnectionException;

import org.gunsioo.message.DbException;

/**
 * Represents a database exception.
 */
public final class JdbcSQLNonTransientConnectionException extends SQLNonTransientConnectionException
        implements JdbcException {

    private static final long serialVersionUID = 1L;

    private final String originalMessage;
    private final String stackTrace;
    private String message;
    private String sql;

    /**
     * Creates a SQLNonTransientConnectionException.
     *
     * @param message the reason
     * @param sql the SQL statement
     * @param state the SQL state
     * @param errorCode the error code
     * @param cause the exception that was the reason for this exception
     * @param stackTrace the stack trace
     */
    public JdbcSQLNonTransientConnectionException(String message, String sql, String state,
            int errorCode, Throwable cause, String stackTrace) {
        super(message, state, errorCode);
        this.originalMessage = message;
        this.stackTrace = stackTrace;
        // setSQL() also generates message
        setSQL(sql);
        initCause(cause);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getOriginalMessage() {
        return originalMessage;
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        DbException.printNextExceptions(this, s);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        DbException.printNextExceptions(this, s);
    }

    @Override
    public String getSQL() {
        return sql;
    }

    @Override
    public void setSQL(String sql) {
        this.sql = sql;
        message = DbException.buildMessageForException(this);
    }

    @Override
    public String toString() {
        if (stackTrace == null) {
            return super.toString();
        }
        return stackTrace;
    }

}
