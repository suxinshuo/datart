package datart.server.service.doris;

/**
 * 直接跟 Doris 交互的逻辑
 *
 * @author suxinshuo
 * @date 2025/12/5 20:28
 */
public interface DorisExecService {

    /**
     * 创建用户
     *
     * @param userName 用户名
     * @param password 密码
     */
    void createUser(String userName, String password);

    /**
     * 创建用户, 并分配默认计算组
     *
     * @param userName            用户名
     * @param password            密码
     * @param defaultComputeGroup 默认计算组
     */
    void createUser(String userName, String password, String defaultComputeGroup);

}
