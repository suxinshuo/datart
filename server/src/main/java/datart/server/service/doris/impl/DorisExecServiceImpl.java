package datart.server.service.doris.impl;

import datart.server.service.doris.DorisExecService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 直接跟 Doris 交互的逻辑
 *
 * @author suxinshuo
 * @date 2025/12/5 20:30
 */
@Slf4j
@Service
public class DorisExecServiceImpl implements DorisExecService {

    /**
     * 创建用户
     *
     * @param userName 用户名
     * @param password 密码
     */
    @Override
    public void createUser(String userName, String password) {
    }

    /**
     * 创建用户, 并分配默认计算组
     *
     * @param userName            用户名
     * @param password            密码
     * @param defaultComputeGroup 默认计算组
     */
    @Override
    public void createUser(String userName, String password, String defaultComputeGroup) {
    }

}
