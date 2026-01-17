package datart.server.service.task;

import datart.core.data.provider.Column;
import datart.core.data.provider.Dataframe;
import datart.core.entity.SqlTaskResult;
import datart.core.mappers.SqlTaskResultMapper;
import datart.server.base.bo.task.SqlTaskResultBo;
import datart.server.service.BaseCRUDService;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author suxinshuo
 * @date 2025/12/19 12:18
 */
public interface SqlTaskResultService extends BaseCRUDService<SqlTaskResult, SqlTaskResultMapper> {

    /**
     * 根据任务 ID 获取 SQL 任务结果
     *
     * @param taskId   任务 ID
     * @param truncate 是否截断结果
     * @return SQL 任务结果
     */
    List<SqlTaskResultBo> getByTaskId(String taskId, Boolean truncate);

    /**
     * 流式保存结果到 HDFS
     *
     * @param taskId   任务 ID
     * @param rs       ResultSet
     * @param columns  列定义
     * @param hdfsPath HDFS 保存路径
     * @return 保存的记录行数
     */
    int writeDataframeStream(String taskId, ResultSet rs, List<Column> columns, String hdfsPath);

    /**
     * 将 Dataframe 流式写为 JSON 到 HDFS
     *
     * @param hdfsPath  HDFS 路径
     * @param dataframe 数据
     */
    void writeDataframe(String hdfsPath, Dataframe dataframe);

}
