package datart.server.base.bo.doris;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<String> dorisDefaultAuthCatalogs;

}
