package datart.data.provider.base;

import datart.core.entity.DorisUserMapping;
import datart.core.entity.User;

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

}
