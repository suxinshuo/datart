package datart.data.provider.base;

import datart.core.data.provider.Column;
import datart.core.data.provider.Dataframe;
import datart.core.entity.DorisUserMapping;
import datart.core.entity.User;

import java.sql.ResultSet;

/**
 *
 * 提供数据访问上下文, 如果要使用 jdbc provider, 就要实现这个接口
 *
 * @author suxinshuo
 * @date 2025/12/12 10:24
 */
public interface IProviderContext {

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户
     */
    User getCurrentUser();

    /**
     * 获取当前登录用户的 Doris 映射
     *
     * @param sourceId 数据源 ID
     * @return 当前登录用户的 Doris 映射
     */
    DorisUserMapping getCurrentDorisUserMappingBySourceId(String sourceId);

    /**
     * 解密字符串
     *
     * @param src 加密后的字符串
     * @return 解密后的字符串
     */
    String decrypt(String src);

    /**
     * 更新任务进度
     *
     * @param taskId   任务 ID
     * @param progress 执行进度
     */
    void updateTaskProgress(String taskId, Integer progress);

    /**
     * 判断SQL语句是否为Spark静态属性配置
     *
     * @param sql SQL语句
     * @return true表示是Spark静态属性配置，false表示不是
     */
    boolean isSparkStaticProperty(String sql);

    /**
     * 流式保存结果到 HDFS
     *
     * @param taskId   任务 ID
     * @param rs       ResultSet
     * @param columns  列定义
     * @param hdfsPath HDFS 保存路径
     * @return 保存的记录行数
     */
    int saveResultToHdfs(String taskId, ResultSet rs, java.util.List<Column> columns, String hdfsPath);

    void saveDataframeToHdfs(String hdfsPath, Dataframe dataframe);

}
