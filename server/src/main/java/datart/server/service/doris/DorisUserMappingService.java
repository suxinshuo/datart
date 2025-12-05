package datart.server.service.doris;

import datart.core.entity.DorisUserMapping;
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

}
