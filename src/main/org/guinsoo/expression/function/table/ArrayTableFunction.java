/*
 * Copyright 2021 Guinsoo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Guinsoo Group
 */
package org.guinsoo.expression.function.table;

import java.util.ArrayList;

import org.guinsoo.api.ErrorCode;
import org.guinsoo.engine.Database;
import org.guinsoo.engine.SessionLocal;
import org.guinsoo.expression.Expression;
import org.guinsoo.expression.ExpressionColumn;
import org.guinsoo.message.DbException;
import org.guinsoo.result.LocalResult;
import org.guinsoo.result.ResultInterface;
import org.guinsoo.table.Column;
import org.guinsoo.value.Value;
import org.guinsoo.value.ValueCollectionBase;
import org.guinsoo.value.ValueInteger;
import org.guinsoo.value.ValueNull;

/**
 * A table value function.
 */
public final class ArrayTableFunction extends TableFunction {

    /**
     * UNNEST().
     */
    public static final int UNNEST = 0;

    /**
     * TABLE() (non-standard).
     */
    public static final int TABLE = UNNEST + 1;

    /**
     * TABLE_DISTINCT() (non-standard).
     */
    public static final int TABLE_DISTINCT = TABLE + 1;

    private Column[] columns;

    private static final String[] NAMES = { //
            "UNNEST", "TABLE", "TABLE_DISTINCT" //
    };

    private final int function;

    public ArrayTableFunction(int function) {
        super(new Expression[1]);
        this.function = function;
    }

    @Override
    public ResultInterface getValue(SessionLocal session) {
        return getTable(session, false);
    }

    @Override
    public void optimize(SessionLocal session) {
        super.optimize(session);
        if (args.length < 1) {
            throw DbException.get(ErrorCode.INVALID_PARAMETER_COUNT_2, getName(), ">0");
        }
    }

    @Override
    public StringBuilder getSQL(StringBuilder builder, int sqlFlags) {
        if (function == UNNEST) {
            super.getSQL(builder, sqlFlags);
            if (args.length < columns.length) {
                builder.append(" WITH ORDINALITY");
            }
        } else {
            builder.append(getName()).append('(');
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(columns[i].getCreateSQL()).append('=');
                args[i].getUnenclosedSQL(builder, sqlFlags);
            }
            builder.append(')');
        }
        return builder;
    }

    @Override
    public ResultInterface getValueTemplate(SessionLocal session) {
        return getTable(session, true);
    }

    public void setColumns(ArrayList<Column> columns) {
        this.columns = columns.toArray(new Column[0]);
    }

    private ResultInterface getTable(SessionLocal session, boolean onlyColumnList) {
        int totalColumns = columns.length;
        Expression[] header = new Expression[totalColumns];
        Database db = session.getDatabase();
        for (int i = 0; i < totalColumns; i++) {
            Column c = columns[i];
            ExpressionColumn col = new ExpressionColumn(db, c);
            header[i] = col;
        }
        LocalResult result = new LocalResult(session, header, totalColumns, totalColumns);
        if (!onlyColumnList && function == TABLE_DISTINCT) {
            result.setDistinct();
        }
        if (!onlyColumnList) {
            int len = totalColumns;
            boolean unnest = function == UNNEST, addNumber = false;
            if (unnest) {
                len = args.length;
                if (len < totalColumns) {
                    addNumber = true;
                }
            }
            Value[][] list = new Value[len][];
            int rows = 0;
            for (int i = 0; i < len; i++) {
                Value v = args[i].getValue(session);
                if (v == ValueNull.INSTANCE) {
                    list[i] = Value.EMPTY_VALUES;
                } else {
                    int type = v.getValueType();
                    if (type != Value.ARRAY && type != Value.ROW) {
                        v = v.convertToAnyArray(session);
                    }
                    Value[] l = ((ValueCollectionBase) v).getList();
                    list[i] = l;
                    rows = Math.max(rows, l.length);
                }
            }
            for (int row = 0; row < rows; row++) {
                Value[] r = new Value[totalColumns];
                for (int j = 0; j < len; j++) {
                    Value[] l = list[j];
                    Value v;
                    if (l.length <= row) {
                        v = ValueNull.INSTANCE;
                    } else {
                        Column c = columns[j];
                        v = l[row];
                        if (!unnest) {
                            v = v.convertForAssignTo(c.getType(), session, c);
                        }
                    }
                    r[j] = v;
                }
                if (addNumber) {
                    r[len] = ValueInteger.get(row + 1);
                }
                result.addRow(r);
            }
        }
        result.done();
        return result;
    }

    @Override
    public String getName() {
        return NAMES[function];
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }

    public int getFunctionType() {
        return function;
    }

}
