package datart.core.bo.task;

import datart.core.entity.SqlTaskWithBLOBs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author suxinshuo
 * @date 2025/12/19 15:35
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueTaskBo {

    private SqlTaskWithBLOBs sqlTask;

    private String username;

}
