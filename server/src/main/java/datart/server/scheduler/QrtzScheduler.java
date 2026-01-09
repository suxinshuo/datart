package datart.server.scheduler;

import datart.server.config.SqlTaskConfig;
import datart.server.job.TaskResultCleanJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * qrtz 调度配置
 *
 * @author suxinshuo
 * @date 2026/1/9 16:58
 */
@Slf4j
@Component
public class QrtzScheduler {

    @Resource
    private Scheduler scheduler;

    @Resource
    private SqlTaskConfig sqlTaskConfig;

    @PostConstruct
    public void start() throws SchedulerException {
        JobKey jobKey = new JobKey("TASK_RESULT_CLEAN_JOB", "TASK_RESULT_CLEAN_JOB_GROUP");
        JobDetail jobDetail = JobBuilder.newJob()
                .withIdentity(jobKey)
                .ofType(TaskResultCleanJob.class)
                .build();

        int retentionCheckIntervalMin = sqlTaskConfig.getRetentionCheckIntervalMin();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("TASK_RESULT_CLEAN_TRIGGER")
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(retentionCheckIntervalMin))
                .startNow()
                .build();

        scheduler.deleteJob(jobKey);
        scheduler.scheduleJob(jobDetail, trigger);

        log.info("Start scheduler job {}. retentionCheckIntervalMin: {}", jobKey, retentionCheckIntervalMin);
    }

}
