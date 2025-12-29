package datart.data.provider.base.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private List<String> preSqls;

    private String sql;

    private String sparkShareLevel;

}
