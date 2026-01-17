package datart.server.config;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author suxinshuo
 * @date 2026/1/9 16:39
 */
@Slf4j
@Getter
@ToString
@Configuration
public class SqlTaskConfig {

    @Value("${datart.task.consumer.count:4}")
    private int consumerCount;

    @Value("${datart.task.queue_capacity:100}")
    private int queueCapacity;

    @Value("${datart.task.execution.max_time:600000}")
    private long maxRunningTime;

    /**
     * 任务保留天数
     */
    @Value("${datart.task.result.retention_days:30}")
    private int retentionDays;

     /**
     * 检查任务结果保留时间的间隔, 单位分钟
     */
    @Value("${datart.task.result.retention_check_interval_min:180}")
    private int retentionCheckIntervalMin;

    @Value("${hadoop.task-result-dir}")
    private String taskResultDir;

}
