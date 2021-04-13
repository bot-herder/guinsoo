/*
 * Copyright 2004-2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
 * Initial Developer: Gunsioo Group
 */
package org.gunsioo.test.unit;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.gunsioo.api.ErrorCode;
import org.gunsioo.engine.ConnectionInfo;
import org.gunsioo.engine.Constants;
import org.gunsioo.engine.Database;
import org.gunsioo.engine.SessionLocal;
import org.gunsioo.message.DbException;
import org.gunsioo.store.fs.FileUtils;
import org.gunsioo.store.fs.Recorder;
import org.gunsioo.store.fs.rec.FilePathRec;
import org.gunsioo.test.TestBase;
import org.gunsioo.tools.Recover;
import org.gunsioo.util.IOUtils;
import org.gunsioo.util.Profiler;
import org.gunsioo.util.Utils;

/**
 * A test that calls another test, and after each write operation to the
 * database file, it copies the file, and tries to reopen it.
 */
public class TestReopen extends TestBase implements Recorder {

    // TODO this is largely a copy of org.gunsioo.util.RecoverTester

    private String testDatabase = "memFS:" + TestBase.BASE_TEST_DIR + "/reopen";
    private int writeCount = Utils.getProperty("h2.reopenOffset", 0);
    private final int testEvery = 1 << Utils.getProperty("h2.reopenShift", 6);
    private final long maxFileSize = Utils.getProperty("h2.reopenMaxFileSize",
            Integer.MAX_VALUE) * 1024L * 1024;
    private int verifyCount;
    private final HashSet<String> knownErrors = new HashSet<>();
    private volatile boolean testing;

    /**
     * Run just this test.
     *
     * @param a ignored
     */
    public static void main(String... a) throws Exception {
        TestBase.createCaller().init().testFromMain();
    }

    @Override
    public void test() throws Exception {
        System.setProperty("h2.delayWrongPasswordMin", "0");
        FilePathRec.register();
        FilePathRec.setRecorder(this);
        config.reopen = true;

        long time = System.nanoTime();
        Profiler p = new Profiler();
        p.startCollecting();
        new TestPageStoreCoverage().init(config).test();
        System.out.println(p.getTop(3));
        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time));
        System.out.println("counter: " + writeCount);
    }

    @Override
    public void log(int op, String fileName, byte[] data, long x) {
        if (op != Recorder.WRITE && op != Recorder.TRUNCATE) {
            return;
        }
        if (!fileName.endsWith(Constants.SUFFIX_PAGE_FILE) &&
                !fileName.endsWith(Constants.SUFFIX_MV_FILE)) {
            return;
        }
        if (testing) {
            // avoid deadlocks
            return;
        }
        testing = true;
        try {
            logDb(fileName);
        } finally {
            testing = false;
        }
    }

    private synchronized void logDb(String fileName) {
        writeCount++;
        if ((writeCount & (testEvery - 1)) != 0) {
            return;
        }
        if (FileUtils.size(fileName) > maxFileSize) {
            // System.out.println(fileName + " " + IOUtils.length(fileName));
            return;
        }
        System.out.println("+ write #" + writeCount + " verify #" + verifyCount);

        try {
            if (fileName.endsWith(Constants.SUFFIX_PAGE_FILE)) {
                IOUtils.copyFiles(fileName, testDatabase +
                        Constants.SUFFIX_PAGE_FILE);
            } else {
                IOUtils.copyFiles(fileName, testDatabase +
                        Constants.SUFFIX_MV_FILE);
            }
            verifyCount++;
            // avoid using the Engine class to avoid deadlocks
            String url = "jdbc:gunsioo:" + testDatabase +
                    ";FILE_LOCK=NO;TRACE_LEVEL_FILE=0";
            ConnectionInfo ci = new ConnectionInfo(url, null, getUser(), getPassword());
            Database database = new Database(ci, null);
            // close the database
            SessionLocal session = database.getSystemSession();
            session.prepare("script to '" + testDatabase + ".sql'").query(0);
            session.prepare("shutdown immediately").update();
            database.removeSession(null);
            // everything OK - return
            return;
        } catch (DbException e) {
            SQLException e2 = DbException.toSQLException(e);
            int errorCode = e2.getErrorCode();
            if (errorCode == ErrorCode.WRONG_USER_OR_PASSWORD) {
                return;
            } else if (errorCode == ErrorCode.FILE_ENCRYPTION_ERROR_1) {
                return;
            }
            e.printStackTrace(System.out);
            throw e;
        } catch (Exception e) {
            // failed
            int errorCode = 0;
            if (e instanceof SQLException) {
                errorCode = ((SQLException) e).getErrorCode();
            }
            if (errorCode == ErrorCode.WRONG_USER_OR_PASSWORD) {
                return;
            } else if (errorCode == ErrorCode.FILE_ENCRYPTION_ERROR_1) {
                return;
            }
            e.printStackTrace(System.out);
        }
        System.out.println(
                "begin ------------------------------ " + writeCount);
        try {
            Recover.execute(fileName.substring(0, fileName.lastIndexOf('/')), null);
        } catch (SQLException e) {
            // ignore
        }
        testDatabase += "X";
        try {
            if (fileName.endsWith(Constants.SUFFIX_PAGE_FILE)) {
                IOUtils.copyFiles(fileName, testDatabase +
                        Constants.SUFFIX_PAGE_FILE);
            } else {
                IOUtils.copyFiles(fileName, testDatabase +
                        Constants.SUFFIX_MV_FILE);
            }
            // avoid using the Engine class to avoid deadlocks
            String url = "jdbc:gunsioo:" + testDatabase + ";FILE_LOCK=NO";
            ConnectionInfo ci = new ConnectionInfo(url, null, null, null);
            Database database = new Database(ci, null);
            // close the database
            database.removeSession(null);
        } catch (Exception e) {
            int errorCode = 0;
            if (e instanceof DbException) {
                e = ((DbException) e).getSQLException();
                errorCode = ((SQLException) e).getErrorCode();
            }
            if (errorCode == ErrorCode.WRONG_USER_OR_PASSWORD) {
                return;
            } else if (errorCode == ErrorCode.FILE_ENCRYPTION_ERROR_1) {
                return;
            }
            StringBuilder buff = new StringBuilder();
            StackTraceElement[] list = e.getStackTrace();
            for (int i = 0; i < 10 && i < list.length; i++) {
                buff.append(list[i].toString()).append('\n');
            }
            String s = buff.toString();
            if (!knownErrors.contains(s)) {
                System.out.println(writeCount + " code: " + errorCode + " " +
                        e.toString());
                e.printStackTrace(System.out);
                knownErrors.add(s);
            } else {
                System.out.println(writeCount + " code: " + errorCode);
            }
        }
    }

}
