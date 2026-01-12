package datart.core.common;

import com.google.common.collect.Maps;

import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author suxinshuo
 * @date 2025/12/31 21:07
 */
public class CommonVarUtils {

    /**
     * Spark 任务进度轮询线程
     */
    private static final Map<String, Thread> SPARK_TASK_PROGRESS_POLLING_THREADS = Maps.newConcurrentMap();

    /**
     * 存储正在执行的 SQL Statement
     */
    private static final Map<String, AtomicReference<Statement>> SQL_STATEMENTS = Maps.newConcurrentMap();

    public static void putSparkTaskProgressPollingThread(String taskId, Thread thread) {
        if ( Objects.isNull(taskId)) {
            return;
        }
        SPARK_TASK_PROGRESS_POLLING_THREADS.put(taskId, thread);
    }

    public static Thread removeSparkTaskProgressPollingThread(String taskId) {
        if ( Objects.isNull(taskId)) {
            return null;
        }
        return SPARK_TASK_PROGRESS_POLLING_THREADS.remove(taskId);
    }

    public static void putSqlStatement(String taskId, AtomicReference<Statement> stmtRef) {
        if ( Objects.isNull(taskId)) {
            return;
        }
        SQL_STATEMENTS.put(taskId, stmtRef);
    }

    public static AtomicReference<Statement> removeSqlStatement(String taskId) {
        if ( Objects.isNull(taskId)) {
            return null;
        }
        return SQL_STATEMENTS.remove(taskId);
    }

}
