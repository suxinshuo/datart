package datart.core.bo.doris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * 创建 Doris 用户参数
 *
 * @author suxinshuo
 * @date 2025/12/11 16:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DorisCreateUserBo {

    private String dorisUsername;
    private String dorisPassword;
    private String dorisDefaultComputeGroup;

}
