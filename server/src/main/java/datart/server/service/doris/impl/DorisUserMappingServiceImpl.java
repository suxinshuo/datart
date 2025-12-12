package datart.server.service.doris.impl;

import cn.hutool.core.collection.CollUtil;
import datart.core.base.exception.Exceptions;
import datart.core.base.exception.ParamException;
import datart.core.common.UUIDGenerator;
import datart.core.entity.DorisUserMapping;
import datart.core.entity.DorisUserMappingExample;
import datart.core.entity.Source;
import datart.core.entity.SourceConstants;
import datart.core.entity.bo.DorisCreateUserBo;
import datart.core.entity.bo.DorisExecSourceParamBo;
import datart.core.entity.bo.DorisUserMappingQueryConditionBo;
import datart.core.mappers.ext.DorisUserMappingMapperExt;
import datart.security.util.AESUtil;
import datart.server.base.params.doris.DorisUserMappingCreateParam;
import datart.server.service.BaseService;
import datart.server.service.DataProviderService;
import datart.server.service.SourceService;
import datart.server.service.doris.DorisExecService;
import datart.server.service.doris.DorisUserMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Resource
    private DorisExecService dorisExecService;

    @Lazy
    @Resource
    private SourceService sourceService;

    @Resource
    private DataProviderService dataProviderService;

    /**
     * 批量创建 doris 用户映射
     *
     * @param createParams 创建参数
     */
    @Override
    @Transactional
    public void batchCreateDorisUserMapping(List<DorisUserMappingCreateParam> createParams) {
        log.info("批量创建 Doris System 映射用户. createParams: {}", createParams);
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
        log.info("Doris System 映射用户保存到元数据成功");

        // 通过 sourceId 找到 source 信息
        List<String> sourceIds = dorisUserMappings.stream().map(DorisUserMapping::getSourceId)
                .distinct().collect(Collectors.toList());
        List<Source> sources = sourceService.listByIds(sourceIds);
        Map<String, DorisExecSourceParamBo> sourceParamMap = sources.stream().collect(Collectors.toMap(
                Source::getId,
                source -> new DorisExecSourceParamBo(dataProviderService.parseDataProviderConfig(source)),
                (x1, x2) -> x2
        ));

        // 执行 doris sql 创建用户并分配计算组, 按照数据源分组执行
        Map<String, List<DorisUserMapping>> userMappingMap = dorisUserMappings.stream().collect(Collectors.groupingBy(DorisUserMapping::getSourceId));
        for (Map.Entry<String, List<DorisUserMapping>> entry : userMappingMap.entrySet()) {
            String sourceId = entry.getKey();
            List<DorisUserMapping> userMappings = entry.getValue();

            DorisExecSourceParamBo sourceParam = sourceParamMap.get(sourceId);
            if (Objects.isNull(sourceParam)) {
                log.error("sourceId: {} 对应的 DorisExecSourceParamBo 为空", sourceId);
                continue;
            }

            List<DorisCreateUserBo> dorisCreateUserBos = userMappings.stream()
                    .map(userMapping -> DorisCreateUserBo.builder()
                            .dorisUsername(userMapping.getDorisUsername())
                            .dorisPassword(AESUtil.decryptSafe(userMapping.getEncryptedPassword()))
                            .dorisDefaultComputeGroup(SourceConstants.DORIS_DEFAULT_COMPUTE_GROUP)
                            .build())
                    .collect(Collectors.toList());
            dorisExecService.createUser(sourceParam, dorisCreateUserBos);
            log.info("在数据源({})创建 Doris 用户成功, 并分配默认计算组: {}", sourceParam, dorisCreateUserBos);
        }
    }

    /**
     * 根据查询条件获取 doris 用户映射
     *
     * @param condition 查询条件
     * @return Doris 用户映射
     */
    @Override
    public DorisUserMapping getByCondition(DorisUserMappingQueryConditionBo condition) {
        DorisUserMappingExample example = new DorisUserMappingExample();
        example.createCriteria()
                .andSysUsernameEqualTo(condition.getUsername())
                .andSourceIdEqualTo(condition.getSourceId());
        List<DorisUserMapping> dorisUserMappings = dorisUserMappingMapper.selectByExample(example);
        if (CollUtil.isEmpty(dorisUserMappings)) {
            return null;
        }
        return dorisUserMappings.get(0);
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
