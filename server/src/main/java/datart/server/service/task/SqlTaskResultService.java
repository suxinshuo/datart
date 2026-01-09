package datart.server.service.task;

import datart.core.entity.SqlTaskResult;
import datart.core.mappers.SqlTaskResultMapper;
import datart.server.service.BaseCRUDService;

import java.util.List;

/**
 * @author suxinshuo
 * @date 2025/12/19 12:18
 */
public interface SqlTaskResultService extends BaseCRUDService<SqlTaskResult, SqlTaskResultMapper> {

    /**
     * 根据任务 ID 获取 SQL 任务结果
     *
     * @param taskId 任务 ID
     * @return SQL 任务结果
     */
    List<SqlTaskResult> getByTaskId(String taskId);

    /**
     * 获取N天前的 SQL 任务结果
     *
     * @param days 天数
     * @return N天前的 SQL 任务结果
     */
    List<SqlTaskResult> getDaysBeforeResults(Integer days);

}
