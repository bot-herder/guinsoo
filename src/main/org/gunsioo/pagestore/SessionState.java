/*
 * Copyright 2004-2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Gunsioo Group
 */
package org.gunsioo.pagestore;

/**
 * The session state contains information about when was the last commit of a
 * session. It is only used during recovery.
 */
class SessionState {

    /**
     * The session id
     */
    public int sessionId;

    /**
     * The last log id where a commit for this session is found.
     */
    public int lastCommitLog;

    /**
     * The position where a commit for this session is found.
     */
    public int lastCommitPos;

    /**
     * The in-doubt transaction if there is one.
     */
    public PageStoreInDoubtTransaction inDoubtTransaction;

    /**
     * Check if this session state is already committed at this point.
     *
     * @param logId the log id
     * @param pos the position in the log
     * @return true if it is committed
     */
    public boolean isCommitted(int logId, int pos) {
        if (logId != lastCommitLog) {
            return lastCommitLog > logId;
        }
        return lastCommitPos >= pos;
    }

    @Override
    public String toString() {
        return "sessionId:" + sessionId + " log:" + lastCommitLog +
                " pos:" + lastCommitPos + " inDoubt:" + inDoubtTransaction;
    }
}
