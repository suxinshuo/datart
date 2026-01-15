package datart.data.provider.base.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author suxinshuo
 * @date 2025/12/26 11:10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteSqlParam {

    private String taskId;

    private String sql;

    private String sparkShareLevel;

    @Builder.Default
    private Boolean adHocFlag = false;

}
