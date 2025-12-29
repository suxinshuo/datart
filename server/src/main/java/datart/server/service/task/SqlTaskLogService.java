package datart.server.service.task;

import datart.core.entity.SqlTaskLog;

import java.util.List;

/**
 * @author suxinshuo
 * @date 2025/12/19 12:26
 */
public interface SqlTaskLogService {

    List<SqlTaskLog> getByTaskId(String taskId);

}
