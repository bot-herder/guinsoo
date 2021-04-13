/*
 * Copyright 2004-2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Gunsioo Group
 */
package org.gunsioo.index;

import java.util.ArrayList;

import org.gunsioo.command.query.AllColumnsForPlan;
import org.gunsioo.engine.SessionLocal;
import org.gunsioo.message.DbException;
import org.gunsioo.result.Row;
import org.gunsioo.result.SearchRow;
import org.gunsioo.result.SortOrder;
import org.gunsioo.table.Column;
import org.gunsioo.table.IndexColumn;
import org.gunsioo.table.MetaTable;
import org.gunsioo.table.TableFilter;

/**
 * The index implementation for meta data tables.
 */
public class MetaIndex extends Index {

    private final MetaTable meta;
    private final boolean scan;

    public MetaIndex(MetaTable meta, IndexColumn[] columns, boolean scan) {
        super(meta, 0, null, columns, IndexType.createNonUnique(true));
        this.meta = meta;
        this.scan = scan;
    }

    @Override
    public void close(SessionLocal session) {
        // nothing to do
    }

    @Override
    public void add(SessionLocal session, Row row) {
        throw DbException.getUnsupportedException("META");
    }

    @Override
    public void remove(SessionLocal session, Row row) {
        throw DbException.getUnsupportedException("META");
    }

    @Override
    public Cursor find(SessionLocal session, SearchRow first, SearchRow last) {
        ArrayList<Row> rows = meta.generateRows(session, first, last);
        return new MetaCursor(rows);
    }

    @Override
    public double getCost(SessionLocal session, int[] masks,
            TableFilter[] filters, int filter, SortOrder sortOrder,
            AllColumnsForPlan allColumnsSet) {
        if (scan) {
            return 10 * MetaTable.ROW_COUNT_APPROXIMATION;
        }
        return getCostRangeIndex(masks, MetaTable.ROW_COUNT_APPROXIMATION,
                filters, filter, sortOrder, false, allColumnsSet);
    }

    @Override
    public void truncate(SessionLocal session) {
        throw DbException.getUnsupportedException("META");
    }

    @Override
    public void remove(SessionLocal session) {
        throw DbException.getUnsupportedException("META");
    }

    @Override
    public int getColumnIndex(Column col) {
        if (scan) {
            // the scan index cannot use any columns
            return -1;
        }
        return super.getColumnIndex(col);
    }

    @Override
    public boolean isFirstColumn(Column column) {
        if (scan) {
            return false;
        }
        return super.isFirstColumn(column);
    }

    @Override
    public void checkRename() {
        throw DbException.getUnsupportedException("META");
    }

    @Override
    public boolean needRebuild() {
        return false;
    }

    @Override
    public String getCreateSQL() {
        return null;
    }

    @Override
    public long getRowCount(SessionLocal session) {
        return MetaTable.ROW_COUNT_APPROXIMATION;
    }

    @Override
    public long getRowCountApproximation(SessionLocal session) {
        return MetaTable.ROW_COUNT_APPROXIMATION;
    }

    @Override
    public long getDiskSpaceUsed() {
        return meta.getDiskSpaceUsed();
    }

    @Override
    public String getPlanSQL() {
        return "meta";
    }

}
