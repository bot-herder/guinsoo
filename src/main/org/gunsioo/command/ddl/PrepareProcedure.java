/*
 * Copyright 2004-2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Gunsioo Group
 */
package org.gunsioo.command.ddl;

import java.util.ArrayList;

import org.gunsioo.command.CommandInterface;
import org.gunsioo.command.Prepared;
import org.gunsioo.engine.Procedure;
import org.gunsioo.engine.SessionLocal;
import org.gunsioo.expression.Parameter;

/**
 * This class represents the statement
 * PREPARE
 */
public class PrepareProcedure extends DefineCommand {

    private String procedureName;
    private Prepared prepared;

    public PrepareProcedure(SessionLocal session) {
        super(session);
    }

    @Override
    public void checkParameters() {
        // no not check parameters
    }

    @Override
    public long update() {
        Procedure proc = new Procedure(procedureName, prepared);
        prepared.setParameterList(parameters);
        prepared.setPrepareAlways(prepareAlways);
        prepared.prepare();
        session.addProcedure(proc);
        return 0;
    }

    public void setProcedureName(String name) {
        this.procedureName = name;
    }

    public void setPrepared(Prepared prep) {
        this.prepared = prep;
    }

    @Override
    public ArrayList<Parameter> getParameters() {
        return new ArrayList<>(0);
    }

    @Override
    public int getType() {
        return CommandInterface.PREPARE;
    }

}
