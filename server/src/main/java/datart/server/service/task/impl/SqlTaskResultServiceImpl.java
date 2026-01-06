package datart.server.service.task.impl;

import com.google.common.collect.Lists;
import datart.core.entity.SqlTaskResult;
import datart.core.entity.SqlTaskResultExample;
import datart.core.mappers.SqlTaskResultMapper;
import datart.server.service.BaseService;
import datart.server.service.task.SqlTaskResultService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author suxinshuo
 * @date 2025/12/19 12:19
 */
@Service
public class SqlTaskResultServiceImpl extends BaseService implements SqlTaskResultService {

    @Resource
    private SqlTaskResultMapper sqlTaskResultMapper;

    /**
     * 根据任务 ID 获取 SQL 任务结果
     *
     * @param taskId 任务 ID
     * @return SQL 任务结果
     */
    @Override
    public List<SqlTaskResult> getByTaskId(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return Lists.newArrayList();
        }

        SqlTaskResultExample example = new SqlTaskResultExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        return sqlTaskResultMapper.selectByExampleWithBLOBs(example);
    }

    @Override
    public void requirePermission(SqlTaskResult entity, int permission) {

    }
}
