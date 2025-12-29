package datart.core.bo.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 运行中的任务
 *
 * @author suxinshuo
 * @date 2025/12/19 13:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunningTaskBo {

    private String taskId;

    private Thread runThread;

    private Date startTime;

}
