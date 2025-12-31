package datart.core.common;

import com.google.common.collect.Maps;

import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author suxinshuo
 * @date 2025/12/31 21:07
 */
public class CommonVarUtils {

    /**
     * Spark 任务进度轮询线程
     */
    public static final Map<String, Thread> SPARK_TASK_PROGRESS_POLLING_THREADS = Maps.newConcurrentMap();

    /**
     * 存储正在执行的 SQL Statement
     */
    public static final Map<String, AtomicReference<Statement>> SQL_STATEMENTS = Maps.newConcurrentMap();

}
