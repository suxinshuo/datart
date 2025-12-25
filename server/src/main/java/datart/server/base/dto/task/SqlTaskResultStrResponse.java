package datart.server.base.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * toon 格式的任务执行结果
 *
 * @author suxinshuo
 * @date 2025/12/22 14:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlTaskResultStrResponse {

    private String result;

}
