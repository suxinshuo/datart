package datart.core.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * Doris 用户映射查询条件
 *
 * @author suxinshuo
 * @date 2025/12/12 11:11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DorisUserMappingQueryConditionBo {

    private String username;

    private String sourceId;

}
