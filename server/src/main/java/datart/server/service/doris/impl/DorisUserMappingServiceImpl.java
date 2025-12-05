package datart.server.service.doris.impl;

import cn.hutool.core.collection.CollUtil;
import datart.core.base.exception.Exceptions;
import datart.core.base.exception.ParamException;
import datart.core.common.UUIDGenerator;
import datart.core.entity.DorisUserMapping;
import datart.core.entity.DorisUserMappingExample;
import datart.core.mappers.ext.DorisUserMappingMapperExt;
import datart.server.base.params.doris.DorisUserMappingCreateParam;
import datart.server.service.BaseService;
import datart.server.service.doris.DorisUserMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author suxinshuo
 * @date 2025/12/5 16:22
 */
@Slf4j
@Service
public class DorisUserMappingServiceImpl extends BaseService implements DorisUserMappingService {

    @Resource
    private DorisUserMappingMapperExt dorisUserMappingMapper;

    /**
     * 批量创建 doris 用户映射
     *
     * @param createParams 创建参数
     */
    @Override
    @Transactional
    public void batchCreateDorisUserMapping(List<DorisUserMappingCreateParam> createParams) {
        log.info("批量创建 Doris System Username 映射用户. createParams: {}", createParams);
        if (CollUtil.isEmpty(createParams)) {
            return;
        }

        if (checkExist(createParams)) {
            log.error("这一批用户中有已经存在的 sysUsername 和 sourceId. createParams: {}", createParams);
            Exceptions.tr(ParamException.class, "error.param.occupied", "resource.user-or-source");
        }
        List<DorisUserMapping> dorisUserMappings = createParams.stream().map(createParam -> {
            DorisUserMapping dorisUserMapping = new DorisUserMapping();
            BeanUtils.copyProperties(createParam, dorisUserMapping);

            dorisUserMapping.setCreateBy(getCurrentUser().getId());
            dorisUserMapping.setCreateTime(new Date());
            dorisUserMapping.setId(UUIDGenerator.generate());

            return dorisUserMapping;
        }).collect(Collectors.toList());

        dorisUserMappingMapper.insertBatch(dorisUserMappings);
    }

    /**
     * 检查是否已经存在这个系统用户名和数据源
     *
     * @param createParams 创建参数
     * @return 是否存在
     */
    private boolean checkExist(List<DorisUserMappingCreateParam> createParams) {
        DorisUserMappingExample example = new DorisUserMappingExample();
        for (DorisUserMappingCreateParam createParam : createParams) {
            DorisUserMappingExample.Criteria criteria = example.createCriteria()
                    .andSysUsernameEqualTo(createParam.getSysUsername())
                    .andSourceIdEqualTo(createParam.getSourceId());
            example.or(criteria);
        }
        List<DorisUserMapping> dorisUserMappings = dorisUserMappingMapper.selectByExample(example);
        return CollUtil.isNotEmpty(dorisUserMappings);
    }

    @Override
    public void requirePermission(DorisUserMapping entity, int permission) {

    }

}
