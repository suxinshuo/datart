package datart.server.service.doris;

import datart.server.base.bo.doris.DorisCreateUserBo;
import datart.server.base.bo.doris.DorisExecSourceParamBo;

import java.util.List;

/**
 * 直接跟 Doris 交互的逻辑
 *
 * @author suxinshuo
 * @date 2025/12/5 20:28
 */
public interface DorisExecService {

    /**
     * 创建用户, 并分配默认计算组
     *
     * @param sourceParam   Doris 执行, source 相关参数
     * @param createUserBos 用户参数
     */
    void createUser(DorisExecSourceParamBo sourceParam, List<DorisCreateUserBo> createUserBos);

}
