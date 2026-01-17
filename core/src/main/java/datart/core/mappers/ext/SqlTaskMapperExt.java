package datart.core.mappers.ext;

import datart.core.entity.SqlTaskWithBLOBs;
import datart.core.mappers.SqlTaskMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author suxinshuo
 * @date 2026/1/17 09:21
 */
@Mapper
public interface SqlTaskMapperExt extends SqlTaskMapper {

    List<SqlTaskWithBLOBs> selectBySearchPage(String createUser, String executeType, String searchKeyword);

}
