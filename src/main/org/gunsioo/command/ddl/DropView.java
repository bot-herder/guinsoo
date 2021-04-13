/*
 * Copyright 2004-2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Gunsioo Group
 */
package org.gunsioo.command.ddl;

import java.util.ArrayList;
import org.gunsioo.api.ErrorCode;
import org.gunsioo.command.CommandInterface;
import org.gunsioo.constraint.ConstraintActionType;
import org.gunsioo.engine.DbObject;
import org.gunsioo.engine.SessionLocal;
import org.gunsioo.message.DbException;
import org.gunsioo.schema.Schema;
import org.gunsioo.table.Table;
import org.gunsioo.table.TableType;
import org.gunsioo.table.TableView;

/**
 * This class represents the statement
 * DROP VIEW
 */
public class DropView extends SchemaCommand {

    private String viewName;
    private boolean ifExists;
    private ConstraintActionType dropAction;

    public DropView(SessionLocal session, Schema schema) {
        super(session, schema);
        dropAction = session.getDatabase().getSettings().dropRestrict ?
                ConstraintActionType.RESTRICT :
                ConstraintActionType.CASCADE;
    }

    public void setIfExists(boolean b) {
        ifExists = b;
    }

    public void setDropAction(ConstraintActionType dropAction) {
        this.dropAction = dropAction;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public long update() {
        Table view = getSchema().findTableOrView(session, viewName);
        if (view == null) {
            if (!ifExists) {
                throw DbException.get(ErrorCode.VIEW_NOT_FOUND_1, viewName);
            }
        } else {
            if (TableType.VIEW != view.getTableType()) {
                throw DbException.get(ErrorCode.VIEW_NOT_FOUND_1, viewName);
            }
            session.getUser().checkSchemaOwner(view.getSchema());

            if (dropAction == ConstraintActionType.RESTRICT) {
                for (DbObject child : view.getChildren()) {
                    if (child instanceof TableView) {
                        throw DbException.get(ErrorCode.CANNOT_DROP_2, viewName, child.getName());
                    }
                }
            }

            // TODO: Where is the ConstraintReferential.CASCADE style drop processing ? It's
            // supported from imported keys - but not for dependent db objects

            TableView tableView = (TableView) view;
            ArrayList<Table> copyOfDependencies = new ArrayList<>(tableView.getTables());

            view.lock(session, true, true);
            session.getDatabase().removeSchemaObject(session, view);

            // remove dependent table expressions
            for (Table childTable: copyOfDependencies) {
                if (TableType.VIEW == childTable.getTableType()) {
                    TableView childTableView = (TableView) childTable;
                    if (childTableView.isTableExpression() && childTableView.getName() != null) {
                        session.getDatabase().removeSchemaObject(session, childTableView);
                    }
                }
            }
            // make sure its all unlocked
            session.getDatabase().unlockMeta(session);
        }
        return 0;
    }

    @Override
    public int getType() {
        return CommandInterface.DROP_VIEW;
    }

}
