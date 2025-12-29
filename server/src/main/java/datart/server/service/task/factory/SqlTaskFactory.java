package datart.server.service.task.factory;

import cn.hutool.core.bean.BeanUtil;
import datart.core.entity.SqlTaskWithBLOBs;
import datart.core.entity.enums.SqlTaskFailType;
import datart.server.base.dto.task.SqlTaskHistoryResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;

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

        String failType = sqlTask.getFailType();
        SqlTaskFailType sqlTaskFailType = SqlTaskFailType.fromCode(failType);
        if (Objects.nonNull(sqlTaskFailType)) {
            String failTypeDesc = sqlTaskFailType.getDesc();
            sqlTaskHistoryResponse.setFailType(failTypeDesc);
        }

        return sqlTaskHistoryResponse;
    }

}
