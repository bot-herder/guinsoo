/*
 * Copyright 2021 Guinsoo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Guinsoo Group
 */
package org.guinsoo.value;

/**
 * Extended parameters of the NUMERIC data type.
 */
public final class ExtTypeInfoNumeric extends ExtTypeInfo {

    /**
     * DECIMAL data type.
     */
    public static final ExtTypeInfoNumeric DECIMAL = new ExtTypeInfoNumeric();

    private ExtTypeInfoNumeric() {
    }

    @Override
    public StringBuilder getSQL(StringBuilder builder, int sqlFlags) {
        return builder.append("DECIMAL");
    }

}
