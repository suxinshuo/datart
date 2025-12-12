package datart.server.service.doris;

import datart.core.entity.DorisUserMapping;
import datart.core.bo.DorisUserMappingQueryConditionBo;
import datart.core.mappers.ext.DorisUserMappingMapperExt;
import datart.server.base.params.doris.DorisUserMappingCreateParam;
import datart.server.service.BaseCRUDService;

import java.util.List;

/**
 * doris 用户映射
 *
 * @author suxinshuo
 * @date 2025/12/5 16:21
 */
public interface DorisUserMappingService extends BaseCRUDService<DorisUserMapping, DorisUserMappingMapperExt> {

    /**
     * 批量创建 doris 用户映射
     *
     * @param createParams 创建参数
     */
    void batchCreateDorisUserMapping(List<DorisUserMappingCreateParam> createParams);

    /**
     * 根据查询条件获取 doris 用户映射
     *
     * @param condition 查询条件
     * @return Doris 用户映射
     */
    DorisUserMapping getByCondition(DorisUserMappingQueryConditionBo condition);

}
