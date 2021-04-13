/*
 * Copyright 2004-2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Gunsioo Group
 */
package org.gunsioo.expression.function;

import org.gunsioo.api.ErrorCode;
import org.gunsioo.engine.SessionLocal;
import org.gunsioo.expression.Expression;
import org.gunsioo.expression.ExpressionVisitor;
import org.gunsioo.expression.Variable;
import org.gunsioo.message.DbException;
import org.gunsioo.value.Value;

/**
 * A SET function.
 */
public final class SetFunction extends Function2 {

    public SetFunction(Expression arg1, Expression arg2) {
        super(arg1, arg2);
    }

    @Override
    public Value getValue(SessionLocal session) {
        Variable var = (Variable) left;
        Value v = right.getValue(session);
        session.setVariable(var.getName(), v);
        return v;
    }

    @Override
    public Expression optimize(SessionLocal session) {
        left = left.optimize(session);
        right = right.optimize(session);
        type = right.getType();
        if (!(left instanceof Variable)) {
            throw DbException.get(ErrorCode.CAN_ONLY_ASSIGN_TO_VARIABLE_1, left.getTraceSQL());
        }
        return this;
    }

    @Override
    public String getName() {
        return "SET";
    }

    @Override
    public boolean isEverything(ExpressionVisitor visitor) {
        if (!super.isEverything(visitor)) {
            return false;
        }
        switch (visitor.getType()) {
        case ExpressionVisitor.DETERMINISTIC:
        case ExpressionVisitor.QUERY_COMPARABLE:
        case ExpressionVisitor.READONLY:
            return false;
        case ExpressionVisitor.EVALUATABLE:
        case ExpressionVisitor.GET_DEPENDENCIES:
        case ExpressionVisitor.INDEPENDENT:
        case ExpressionVisitor.NOT_FROM_RESOLVER:
        case ExpressionVisitor.OPTIMIZABLE_AGGREGATE:
        case ExpressionVisitor.SET_MAX_DATA_MODIFICATION_ID:
        case ExpressionVisitor.GET_COLUMNS1:
        case ExpressionVisitor.GET_COLUMNS2:
            return true;
        default:
            throw DbException.getInternalError("type=" + visitor.getType());
        }
    }

}
