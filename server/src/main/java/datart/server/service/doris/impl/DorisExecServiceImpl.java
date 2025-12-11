package datart.server.service.doris.impl;

import cn.hutool.db.Db;
import cn.hutool.db.DbUtil;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.json.JSONUtil;
import datart.core.entity.bo.DorisCreateUserBo;
import datart.core.entity.bo.DorisExecSourceParamBo;
import datart.data.provider.JdbcDataProvider;
import datart.server.service.doris.DorisExecService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
     * 创建用户, 并分配默认计算组
     *
     * @param sourceParam   Doris 执行, source 相关参数
     * @param createUserBos 用户参数
     */
    @Override
    public void createUser(DorisExecSourceParamBo sourceParam, List<DorisCreateUserBo> createUserBos) {
        log.info("创建用户, 并分配默认计算组, sourceParam: {}, createUserBos: {}", sourceParam, createUserBos);
        try {
            Db db = getDb(sourceParam);
            for (DorisCreateUserBo createUserBo : createUserBos) {
                String userName = createUserBo.getDorisUsername();
                String password = createUserBo.getDorisPassword();
                String defaultComputeGroup = createUserBo.getDorisDefaultComputeGroup();

                int executeFlag = db.execute("CREATE USER IF NOT EXISTS " + userName + " IDENTIFIED BY '" + password + "'");
                if (executeFlag == 0) {
                    log.error("创建用户失败, userName: {}, sourceParam: {}", userName, sourceParam);
                    throw new RuntimeException("创建用户失败");
                }

                int executeFlag2 = db.execute("GRANT USAGE_PRIV ON COMPUTE GROUP " + defaultComputeGroup + " TO " + userName);
                if (executeFlag2 == 0) {
                    log.error("分配计算组失败, userName: {}, defaultComputeGroup: {}, sourceParam: {}", userName, defaultComputeGroup, sourceParam);
                    throw new RuntimeException("分配计算组失败");
                }

                log.info("创建用户并分配计算组成功, userName: {}, defaultComputeGroup: {}", userName, defaultComputeGroup);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取 Db 实例
     *
     * @param sourceParam Doris 执行, source 相关参数
     * @return Db 实例
     */
    private Db getDb(DorisExecSourceParamBo sourceParam) {
        Map<String, Object> properties = sourceParam.getSource().getProperties();
        Object jdbcUrl = properties.get(JdbcDataProvider.URL);
        Object jdbcUser = properties.get(JdbcDataProvider.USER);
        Object jdbcPassword = properties.get(JdbcDataProvider.PASSWORD);
        Object driverClass = properties.get(JdbcDataProvider.DRIVER_CLASS);
        // 检查是否有 null 值
        if (jdbcUrl == null || jdbcUser == null || jdbcPassword == null || driverClass == null) {
            throw new RuntimeException("Doris 执行 source 配置中, URL, USER, PASSWORD, DRIVER_CLASS 不能为空");
        }
        return DbUtil.use(
                new SimpleDataSource(
                        jdbcUrl.toString(),
                        jdbcUser.toString(),
                        jdbcPassword.toString(),
                        driverClass.toString()
                )
        );
    }

}
