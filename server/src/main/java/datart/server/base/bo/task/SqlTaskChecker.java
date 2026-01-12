package datart.server.base.bo.task;

import cn.hutool.core.collection.CollUtil;
import datart.core.entity.enums.SqlTaskFailType;
import datart.core.entity.enums.SqlTaskStatus;
import datart.core.entity.SqlTaskWithBLOBs;
import datart.core.mappers.SqlTaskMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 任务检查器
 *
 * @author suxinshuo
 * @date 2025/12/19 13:57
 */
@Slf4j
@AllArgsConstructor
public class SqlTaskChecker implements Runnable {

    private final static long CANCEL_TASK_EXPIRE_TIME = 10 * 60 * 1000;

    private Map<String, RunningTaskBo> runningTasks;

    private Map<String, Long> cancelTasks;

    private SqlTaskMapper sqlTaskMapper;

    private long maxRunningTime;

    private BiConsumer<String, SqlTaskFailType> cancelSqlTaskFunc;

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                log.error("Check running tasks error", e);
            }

            // 检查删除超过一定时间的取消任务
            long currentTime = System.currentTimeMillis();
            cancelTasks.entrySet().removeIf(entry -> currentTime - entry.getValue() > CANCEL_TASK_EXPIRE_TIME);

            if (CollUtil.isEmpty(runningTasks)) {
                log.debug("No running tasks, sleep 20s");
                continue;
            }

            log.info("Check running tasks, size: {}. canceled tasks size: {}", runningTasks.size(), cancelTasks.size());
            runningTasks.forEach((taskId, runningTask) -> {
                // 先判断当前运行中的任务, 是否数据库中已经更新为失败状态, 成功只可能在当前实例更新, 所以不需要判断
                SqlTaskWithBLOBs sqlTask = sqlTaskMapper.selectByPrimaryKey(taskId);
                SqlTaskStatus taskStatus = SqlTaskStatus.fromCode(sqlTask.getStatus());
                if (Objects.equals(taskStatus, SqlTaskStatus.FAILED) || Objects.equals(taskStatus, SqlTaskStatus.UNKNOWN)) {
                    log.info("Task {} has been failed, remove from running tasks", taskId);
                    Thread runningThread = runningTask.getRunThread();
                    if (Objects.isNull(runningThread)) {
                        return;
                    }
                    if (!runningThread.isAlive()) {
                        return;
                    }
                    try {
                        runningThread.interrupt();
                    } catch (Exception e) {
                        log.error("Cancel task error. taskId: {}", taskId, e);
                    }
                    runningTasks.remove(taskId);
                    return;
                }

                // 判断是否执行超时
                long duration = System.currentTimeMillis() - runningTask.getStartTime().getTime();
                if (duration > maxRunningTime) {
                    log.warn("Task {} has been running for {} ms, which is longer than max running time {}", taskId, duration, maxRunningTime);
                    // 终止任务
                    cancelSqlTaskFunc.accept(taskId, SqlTaskFailType.EXECUTION_TIMEOUT);
                }
            });
        }
    }

}
