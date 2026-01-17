package datart.server.service.data_provider;

import datart.core.data.provider.Column;
import datart.core.data.provider.Dataframe;
import datart.core.entity.DorisUserMapping;
import datart.core.entity.User;
import datart.server.base.bo.doris.DorisUserMappingQueryConditionBo;
import datart.server.config.SparkConfig;
import datart.data.provider.base.IProviderContext;
import datart.security.manager.DatartSecurityManager;
import datart.security.util.AESUtil;
import datart.server.service.doris.DorisUserMappingService;
import datart.server.service.task.SqlTaskResultService;
import datart.server.service.task.SqlTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提供数据访问上下文, 如果要使用 jdbc provider, 就要实现这个接口
 *
 * @author suxinshuo
 * @date 2025/12/12 11:08
 */
@Slf4j
@Component
public class ProviderContext implements IProviderContext {

    @Resource
    private DatartSecurityManager datartSecurityManager;

    @Resource
    private DorisUserMappingService dorisUserMappingService;

    @Resource
    private SqlTaskService sqlTaskService;

    @Resource
    private SparkConfig sparkConfig;

    @Resource
    private SqlTaskResultService sqlTaskResultService;

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户
     */
    @Override
    public User getCurrentUser() {
        return datartSecurityManager.getCurrentUser();
    }

    /**
     * 获取当前登录用户的 Doris 映射
     *
     * @param sourceId 数据源 ID
     * @return 当前登录用户的 Doris 映射
     */
    @Override
    public DorisUserMapping getCurrentDorisUserMappingBySourceId(String sourceId) {
        User currentUser = getCurrentUser();
        String username = currentUser.getUsername();

        DorisUserMappingQueryConditionBo condition = new DorisUserMappingQueryConditionBo();
        condition.setUsername(username);
        condition.setSourceId(sourceId);
        DorisUserMapping dorisUserMapping = dorisUserMappingService.getByCondition(condition);
        if (dorisUserMapping == null) {
            log.warn("Doris user mapping not found for user {} and source {}", username, sourceId);
            return null;
        }

        return dorisUserMapping;
    }

    /**
     * 解密字符串
     *
     * @param src 加密后的字符串
     * @return 解密后的字符串
     */
    @Override
    public String decrypt(String src) {
        try {
            return AESUtil.decrypt(src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新任务进度
     *
     * @param taskId   任务 ID
     * @param progress 执行进度
     */
    @Override
    public void updateTaskProgress(String taskId, Integer progress) {
        sqlTaskService.updateTaskProgress(taskId, progress);
    }

    /**
     * 判断SQL语句是否为Spark静态属性配置
     *
     * @param sql SQL语句
     * @return true表示是Spark静态属性配置，false表示不是
     */
    @Override
    public boolean isSparkStaticProperty(String sql) {
        if (StringUtils.isBlank(sql)) {
            return false;
        }

        String trimmedSql = sql.trim();

        if (StringUtils.startsWith(trimmedSql, "--")) {
            return false;
        }

        Pattern setPattern = Pattern.compile("(?i)^\\s*SET\\s+([a-zA-Z_][a-zA-Z0-9_.]*)\\s*=\\s*(.+)$");
        Matcher matcher = setPattern.matcher(trimmedSql);

        if (!matcher.matches()) {
            return false;
        }

        String configKey = matcher.group(1);

        Set<String> staticProperties = sparkConfig.getStaticProperties();
        if (staticProperties == null || staticProperties.isEmpty()) {
            return false;
        }

        return staticProperties.stream()
                .anyMatch(prop -> StringUtils.startsWithIgnoreCase(configKey, prop));
    }

    /**
     * 流式保存结果到 HDFS
     *
     * @param taskId   任务 ID
     * @param rs       ResultSet
     * @param columns  列定义
     * @param hdfsPath HDFS 保存路径
     * @return 保存的记录行数
     */
    @Override
    public int saveResultToHdfs(String taskId, ResultSet rs, java.util.List<Column> columns, String hdfsPath) {
        return sqlTaskResultService.writeDataframeStream(taskId, rs, columns, hdfsPath);
    }

    @Override
    public void saveDataframeToHdfs(String hdfsPath, Dataframe dataframe) {
        sqlTaskResultService.writeDataframe(hdfsPath, dataframe);
    }
}
