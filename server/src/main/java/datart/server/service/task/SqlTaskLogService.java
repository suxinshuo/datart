package datart.server.service.task;

import datart.core.entity.SqlTaskLog;
import datart.core.mappers.SqlTaskLogMapper;
import datart.server.service.BaseCRUDService;

import java.util.List;

/**
 * @author suxinshuo
 * @date 2025/12/19 12:26
 */
public interface SqlTaskLogService extends BaseCRUDService<SqlTaskLog, SqlTaskLogMapper> {

    List<SqlTaskLog> getByTaskId(String taskId);

}
