/*
 * Copyright 2021 Guinsoo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Guinsoo Group
 */
package org.guinsoo.command.ddl;

import java.util.ArrayList;

import org.guinsoo.api.ErrorCode;
import org.guinsoo.command.CommandInterface;
import org.guinsoo.engine.Constants;
import org.guinsoo.engine.SessionLocal;
import org.guinsoo.message.DbException;
import org.guinsoo.schema.Schema;
import org.guinsoo.schema.Sequence;
import org.guinsoo.table.Column;
import org.guinsoo.table.IndexColumn;

public abstract class CommandWithColumns extends SchemaCommand {

    private ArrayList<DefineCommand> constraintCommands;

    private AlterTableAddConstraint primaryKey;

    protected CommandWithColumns(SessionLocal session, Schema schema) {
        super(session, schema);
    }

    /**
     * Add a column to this table.
     *
     * @param column
     *            the column to add
     */
    public abstract void addColumn(Column column);

    /**
     * Add a constraint statement to this statement. The primary key definition is
     * one possible constraint statement.
     *
     * @param command
     *            the statement to add
     */
    public void addConstraintCommand(DefineCommand command) {
        if (!(command instanceof CreateIndex)) {
            AlterTableAddConstraint con = (AlterTableAddConstraint) command;
            if (con.getType() == CommandInterface.ALTER_TABLE_ADD_CONSTRAINT_PRIMARY_KEY) {
                if (setPrimaryKey(con)) {
                    return;
                }
            }
        }
        getConstraintCommands().add(command);
    }

    /**
     * For the given list of columns, disable "nullable" for those columns that
     * are primary key columns.
     *
     * @param columns the list of columns
     */
    protected void changePrimaryKeysToNotNull(ArrayList<Column> columns) {
        if (primaryKey != null) {
            IndexColumn[] pkColumns = primaryKey.getIndexColumns();
            for (Column c : columns) {
                for (IndexColumn idxCol : pkColumns) {
                    if (c.getName().equals(idxCol.columnName)) {
                        c.setNullable(false);
                    }
                }
            }
        }
    }

    /**
     * Create the constraints.
     */
    protected void createConstraints() {
        if (constraintCommands != null) {
            for (DefineCommand command : constraintCommands) {
                command.setTransactional(transactional);
                command.update();
            }
        }
    }

    /**
     * For the given list of columns, create sequences for identity
     * columns (if needed), and then get the list of all sequences of the
     * columns.
     *
     * @param columns the columns
     * @param temporary whether generated sequences should be temporary
     * @return the list of sequences (may be empty)
     */
    protected ArrayList<Sequence> generateSequences(ArrayList<Column> columns, boolean temporary) {
        ArrayList<Sequence> sequences = new ArrayList<>(columns == null ? 0 : columns.size());
        if (columns != null) {
            for (Column c : columns) {
                if (c.hasIdentityOptions()) {
                    int objId = session.getDatabase().allocateObjectId();
                    c.initializeSequence(session, getSchema(), objId, temporary);
                    if (!Constants.CLUSTERING_DISABLED.equals(session.getDatabase().getCluster())) {
                        throw DbException.getUnsupportedException("CLUSTERING && identity columns");
                    }
                }
                Sequence seq = c.getSequence();
                if (seq != null) {
                    sequences.add(seq);
                }
            }
        }
        return sequences;
    }

    private ArrayList<DefineCommand> getConstraintCommands() {
        if (constraintCommands == null) {
            constraintCommands = new ArrayList<>();
        }
        return constraintCommands;
    }

    /**
     * Set the primary key, but also check if a primary key with different
     * columns is already defined.
     * <p>
     * If an unnamed primary key with the same columns is already defined it is
     * removed from the list of constraints and this method returns
     * {@code false}.
     * </p>
     *
     * @param primaryKey
     *            the primary key
     * @return whether another primary key with the same columns was already set
     *         and the specified primary key should be ignored
     */
    private boolean setPrimaryKey(AlterTableAddConstraint primaryKey) {
        if (this.primaryKey != null) {
            IndexColumn[] oldColumns = this.primaryKey.getIndexColumns();
            IndexColumn[] newColumns = primaryKey.getIndexColumns();
            int len = newColumns.length;
            if (len != oldColumns.length) {
                throw DbException.get(ErrorCode.SECOND_PRIMARY_KEY);
            }
            for (int i = 0; i < len; i++) {
                if (!newColumns[i].columnName.equals(oldColumns[i].columnName)) {
                    throw DbException.get(ErrorCode.SECOND_PRIMARY_KEY);
                }
            }
            if (this.primaryKey.getConstraintName() != null) {
                return true;
            }
            // Remove unnamed primary key
            constraintCommands.remove(this.primaryKey);
        }
        this.primaryKey = primaryKey;
        return false;
    }

    public AlterTableAddConstraint getPrimaryKey() {
        return primaryKey;
    }

}
