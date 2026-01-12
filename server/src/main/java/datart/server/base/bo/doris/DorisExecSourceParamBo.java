package datart.server.base.bo.doris;

import datart.core.data.provider.DataProviderSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Doris 执行, source 相关参数
 *
 * @author suxinshuo
 * @date 2025/12/11 15:28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DorisExecSourceParamBo {

    private DataProviderSource source;

}
