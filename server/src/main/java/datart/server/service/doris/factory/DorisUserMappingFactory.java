package datart.server.service.doris.factory;

import datart.core.common.UUIDGenerator;
import datart.core.entity.DorisUserMapping;
import datart.server.base.params.doris.DorisUserMappingCreateParam;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author suxinshuo
 * @date 2025/12/12 14:45
 */
@Component
public class DorisUserMappingFactory {

    public DorisUserMapping getDorisUserMapping(DorisUserMappingCreateParam createParam, String userId) {
        DorisUserMapping dorisUserMapping = new DorisUserMapping();
        BeanUtils.copyProperties(createParam, dorisUserMapping);

        dorisUserMapping.setCreateBy(userId);
        dorisUserMapping.setUpdateBy(userId);

        dorisUserMapping.setId(UUIDGenerator.generate());

        return dorisUserMapping;
    }

}
