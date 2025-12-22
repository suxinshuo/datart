package datart.server.service.task.factory;

import cn.hutool.core.bean.BeanUtil;
import datart.core.entity.SqlTaskWithBLOBs;
import datart.server.base.dto.task.SqlTaskHistoryResponse;
import org.springframework.stereotype.Component;

/**
 * @author suxinshuo
 * @date 2025/12/22 10:45
 */
@Component
public class SqlTaskFactory {

    public SqlTaskHistoryResponse getSqlTaskHistoryResponse(SqlTaskWithBLOBs sqlTask) {
        SqlTaskHistoryResponse sqlTaskHistoryResponse = new SqlTaskHistoryResponse();
        BeanUtil.copyProperties(sqlTask, sqlTaskHistoryResponse);
        sqlTaskHistoryResponse.setQuery(sqlTask.getScript());
        sqlTaskHistoryResponse.setSubmitTime(sqlTask.getCreateTime());
        return sqlTaskHistoryResponse;
    }

}
