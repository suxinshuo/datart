package datart.server.job;

import datart.core.common.Application;
import datart.core.entity.SqlTaskResult;
import datart.server.config.SqlTaskConfig;
import datart.server.service.task.SqlTaskResultService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * 定时清理任务结果
 *
 * @author suxinshuo
 * @date 2026/1/9 16:28
 */
@Slf4j
public class TaskResultCleanJob implements Job, Closeable {

    @Override
    public void close() throws IOException {

    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SqlTaskConfig sqlTaskConfig = Application.getBean(SqlTaskConfig.class);
        int retentionDays = sqlTaskConfig.getRetentionDays();
        log.info("Start task result clean job retention days:{}", retentionDays);

        // 1. 从数据库中查询过期的任务结果
        SqlTaskResultService sqlTaskResultService = Application.getBean(SqlTaskResultService.class);
        List<SqlTaskResult> daysBeforeResults = sqlTaskResultService.getDaysBeforeResults(retentionDays);
        log.info("Task result clean job found {} results to clean", daysBeforeResults.size());

        // 2. 删除过期的任务结果
        for (SqlTaskResult daysBeforeResult : daysBeforeResults) {
            String resultId = daysBeforeResult.getId();
            boolean delete = sqlTaskResultService.delete(resultId);
            if (!delete) {
                log.error("Delete task result failed. resultId: {}", resultId);
            }
        }

        log.info("Task result clean job finished");
    }

}
