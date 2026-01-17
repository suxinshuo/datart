package datart.server.job;

import datart.core.common.Application;
import datart.server.config.SqlTaskConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 定时清理任务结果
 *
 * @author suxinshuo
 * @date 2026/1/9 16:28
 */
@Slf4j
public class TaskResultCleanJob implements Job, Closeable {

    @Override
    public void close() throws IOException {

    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SqlTaskConfig sqlTaskConfig = Application.getBean(SqlTaskConfig.class);
        log.info("Start task result clean job sqlTaskConfig: {}", sqlTaskConfig);

        int retentionDays = sqlTaskConfig.getRetentionDays();
        String taskResultDir = sqlTaskConfig.getTaskResultDir();
        FileSystem fileSystem = Application.getBean("hdfsFileSystem", FileSystem.class);

        try {
            Path dirPath = new Path(taskResultDir);
            if (!fileSystem.exists(dirPath)) {
                log.warn("Task result directory does not exist: {}", taskResultDir);
                return;
            }

            FileStatus[] fileStatuses = fileSystem.listStatus(dirPath);
            if (fileStatuses == null || fileStatuses.length == 0) {
                log.info("No directories found in task result directory: {}", taskResultDir);
                return;
            }

            LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            List<Path> dirsToDelete = new ArrayList<>();

            for (FileStatus fileStatus : fileStatuses) {
                if (!fileStatus.isDirectory()) {
                    continue;
                }

                String dirName = fileStatus.getPath().getName();
                try {
                    LocalDate dirDate = LocalDate.parse(dirName, formatter);
                    if (dirDate.isBefore(cutoffDate)) {
                        dirsToDelete.add(fileStatus.getPath());
                        log.info("Found directory to delete: {}, date: {}", dirName, dirDate);
                    }
                } catch (DateTimeParseException e) {
                    log.debug("Directory name does not match date format yyyyMMdd: {}", dirName);
                }
            }

            for (Path dirToDelete : dirsToDelete) {
                try {
                    boolean deleted = fileSystem.delete(dirToDelete, true);
                    if (deleted) {
                        log.info("Successfully deleted directory: {}", dirToDelete);
                    } else {
                        log.warn("Failed to delete directory: {}", dirToDelete);
                    }
                } catch (IOException e) {
                    log.error("Error deleting directory: {}", dirToDelete, e);
                }
            }

            log.info("Task result clean job finished, deleted {} directories", dirsToDelete.size());
        } catch (IOException e) {
            log.error("Error during task result clean job", e);
            throw new JobExecutionException("Failed to clean task results", e);
        }
    }

}
