package datart.server.service.data_provider;

import datart.core.entity.DorisUserMapping;
import datart.core.entity.User;
import datart.server.base.bo.doris.DorisUserMappingQueryConditionBo;
import datart.data.provider.base.IProviderContext;
import datart.security.manager.DatartSecurityManager;
import datart.security.util.AESUtil;
import datart.server.service.doris.DorisUserMappingService;
import datart.server.service.task.SqlTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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

}
