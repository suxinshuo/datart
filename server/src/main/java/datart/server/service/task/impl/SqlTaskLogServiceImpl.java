package datart.server.service.task.impl;

import com.google.common.collect.Lists;
import datart.core.entity.SqlTaskLog;
import datart.core.entity.SqlTaskLogExample;
import datart.core.mappers.SqlTaskLogMapper;
import datart.server.service.BaseService;
import datart.server.service.task.SqlTaskLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author suxinshuo
 * @date 2025/12/19 12:26
 */
@Service
public class SqlTaskLogServiceImpl extends BaseService implements SqlTaskLogService {

    @Resource
    private SqlTaskLogMapper sqlTaskLogMapper;

    @Override
    public List<SqlTaskLog> getByTaskId(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return Lists.newArrayList();
        }

        SqlTaskLogExample example = new SqlTaskLogExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        return sqlTaskLogMapper.selectByExampleWithBLOBs(example);
    }

    @Override
    public void requirePermission(SqlTaskLog entity, int permission) {

    }
}
