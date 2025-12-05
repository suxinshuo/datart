package datart.core.mappers.ext;

import datart.core.entity.DorisUserMapping;
import datart.core.mappers.DorisUserMappingMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author suxinshuo
 * @date 2025/12/5 16:17
 */
@Mapper
public interface DorisUserMappingMapperExt extends DorisUserMappingMapper {

    int insertBatch(List<DorisUserMapping> records);

}
