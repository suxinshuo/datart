package datart.core.entity;

/**
 * Source 相关的常量
 *
 * @author suxinshuo
 * @date 2025/12/2 12:33
 */
public interface SourceConstants {

    String PROP_DEFAULT_CATALOG = "_DEFAULT_CATALOG";

    /**
     * 是否开启动态数据源, 开启后数据源的用户名跟随登录用户
     */
    String PROP_DYNAMIC_USER_ENABLE = "_DYNAMIC_USER_ENABLE";

    /**
     * 动态数据源是否需要初始化用户, 默认为 false
     */
    String PROP_DYNAMIC_USER_INIT = "_DYNAMIC_USER_INIT";

    /**
     * doris 默认计算组
     */
    String DORIS_DEFAULT_COMPUTE_GROUP = "defaultComputeGroup";

    String DORIS_DEFAULT_PASSWORD = "S@TktH2j5*";

}
