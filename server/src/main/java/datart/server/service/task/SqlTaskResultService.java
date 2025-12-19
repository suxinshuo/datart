package datart.server.service.task;

import datart.core.entity.SqlTaskResult;

import java.util.List;

/**
 * @author suxinshuo
 * @date 2025/12/19 12:18
 */
public interface SqlTaskResultService {

    /**
     * 插入 SQL 任务结果
     *
     * @param sqlTaskResult SQL 任务结果
     */
    void insertSelective(SqlTaskResult sqlTaskResult);

    /**
     * 根据任务 ID 获取 SQL 任务结果
     *
     * @param taskId 任务 ID
     * @return SQL 任务结果
     */
    List<SqlTaskResult> getByTaskId(String taskId);

}
