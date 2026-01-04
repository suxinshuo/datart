/*
 * Datart
 * <p>
 * Copyright 2021
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package datart.server.service.task.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.json.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import datart.core.bo.task.QueueTaskBo;
import datart.core.bo.task.RunningTaskBo;
import datart.core.bo.task.SqlTaskChecker;
import datart.core.bo.task.SqlTaskConsumerChecker;
import datart.core.common.CommonVarUtils;
import datart.core.entity.enums.SqlTaskExecuteType;
import datart.core.entity.enums.SqlTaskProgress;
import datart.core.entity.enums.SqlTaskStatus;
import datart.core.entity.enums.SqlTaskFailType;
import datart.core.common.UUIDGenerator;
import datart.core.entity.*;
import datart.core.data.provider.Dataframe;
import datart.core.mappers.SqlTaskMapper;
import datart.core.utils.JsonUtils;
import datart.server.base.dto.task.*;
import datart.server.base.params.TestExecuteParam;
import datart.server.service.BaseService;
import datart.server.service.DataProviderService;
import datart.server.service.task.SqlTaskLogService;
import datart.server.service.task.SqlTaskResultService;
import datart.server.service.task.SqlTaskService;
import datart.server.service.SourceService;
import datart.server.service.task.factory.SqlTaskFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Statement;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Slf4j
@Service
public class SqlTaskServiceImpl extends BaseService implements SqlTaskService {

    // 任务队列, 基于优先级
    private BlockingQueue<QueueTaskBo> taskQueue;

    // 执行中的任务映射, 用于跟踪和中断任务
    private final Map<String, RunningTaskBo> runningTasks = Maps.newConcurrentMap();
    private final Map<String, Long> cancelTasks = Maps.newConcurrentMap();

    @Resource
    private SqlTaskMapper sqlTaskMapper;

    @Resource
    private SqlTaskResultService sqlTaskResultService;

    @Resource
    private SqlTaskLogService sqlTaskLogService;

    @Resource
    private SourceService sourceService;

    @Resource
    private DataProviderService dataProviderService;

    @Resource
    private SqlTaskFactory sqlTaskFactory;

    @Value("${datart.task.consumer.count:4}")
    private int consumerCount;

    @Resource(name = "sqlTaskExecutor")
    private ThreadPoolTaskExecutor sqlTaskExecutor;

    @Value("${datart.task.queue_capacity:100}")
    private int queueCapacity;

    @Value("${datart.task.execution.max_time:600000}")
    private long maxRunningTime;

    /**
     * 初始化任务执行器
     */
    @PostConstruct
    public void init() {
        taskQueue = new PriorityBlockingQueue<>(queueCapacity, (o1, o2) ->
                o2.getSqlTask().getPriority() - o1.getSqlTask().getPriority());
        // 启动任务执行线程
        for (int i = 0; i < consumerCount; i++) {
            sqlTaskExecutor.execute(this::processTasks);
        }

        // 启动一个线程, 轮询运行中的任务, 检查是否超过最大运行时间, 如果超时, 就终止
        new Thread(new SqlTaskChecker(
                runningTasks,
                cancelTasks,
                sqlTaskMapper,
                maxRunningTime,
                this::cancelSqlTask
        )).start();

        // 启动一个线程, 检查 consumer 数量
        new Thread(new SqlTaskConsumerChecker(
                sqlTaskExecutor,
                consumerCount,
                this::processTasks
        )).start();
    }

    /**
     * 程序退出之前, 把所有在运行的任务更新状态
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SqlTaskExecutor");
        // 等待所有任务完成
        sqlTaskExecutor.shutdown();
        for (Map.Entry<String, RunningTaskBo> entry : runningTasks.entrySet()) {
            Date nowDate = new Date();

            String taskId = entry.getKey();
            RunningTaskBo runningTask = entry.getValue();
            SqlTaskWithBLOBs sqlTask = new SqlTaskWithBLOBs();
            sqlTask.setId(taskId);
            sqlTask.setStatus(SqlTaskStatus.FAILED.getCode());
            sqlTask.setFailType(SqlTaskFailType.SERVICE_RESTART.getCode());
            sqlTask.setErrorMessage(SqlTaskFailType.SERVICE_RESTART.getDesc());
            sqlTask.setEndTime(nowDate);
            sqlTask.setDuration(nowDate.getTime() - runningTask.getStartTime().getTime());
            sqlTask.setUpdateBy(SystemConstant.SYSTEM_USER_ID);
            sqlTask.setUpdateTime(nowDate);
            sqlTaskMapper.updateByPrimaryKeySelective(sqlTask);
        }
        log.info("设置所有任务为失败状态, 任务数量: {}", runningTasks.size());
    }

    @Override
    @Transactional
    public SqlTaskCreateResponse createSqlTask(TestExecuteParam executeParam) {
        log.info("createSqlTask, executeParam: {}", executeParam);
        // 获取数据源信息
        Source source = sourceService.retrieve(executeParam.getSourceId(), true);

        // 生成任务 ID
        String taskId = UUIDGenerator.generate();
        executeParam.setSqlTaskId(taskId);

        // 创建任务实体
        SqlTaskWithBLOBs task = new SqlTaskWithBLOBs();
        task.setId(taskId);
        task.setSourceId(executeParam.getSourceId());
        if (!StringUtils.startsWithIgnoreCase(executeParam.getViewId(), "GENERATED-")) {
            task.setViewId(executeParam.getViewId());
        }
        task.setScript(executeParam.getScript());
        task.setScriptType(executeParam.getScriptType().name());
        task.setStatus(SqlTaskStatus.QUEUED.getCode());
        task.setPriority(executeParam.getPriority());
        task.setTimeout((int) maxRunningTime);
        task.setMaxSize(executeParam.getSize());
        task.setOrgId(source.getOrgId());
        task.setExecuteParam(JsonUtils.toJsonStr(executeParam));
        task.setExecInstanceId(getInstantId());
        task.setCreateBy(getCurrentUser().getId());
        task.setCreateTime(new Date());
        task.setProgress(SqlTaskProgress.QUEUED.getProgress());
        task.setExecuteType(SqlTaskExecuteType.AD_HOC.getCode());

        // 保存任务信息
        sqlTaskMapper.insertSelective(task);

        // 将任务加入队列
        QueueTaskBo queueTask = QueueTaskBo.builder()
                .sqlTask(task)
                .username(getCurrentUser().getUsername())
                .build();
        boolean offer = taskQueue.offer(queueTask);
        if (!offer) {
            throw new RuntimeException("Failed to add task to queue");
        }
        log.info("加入任务队列成功, taskId: {}", taskId);

        // 返回响应
        return SqlTaskCreateResponse.builder()
                .taskId(taskId)
                .createTime(task.getCreateTime())
                .build();
    }

    @Override
    public SqlTaskStatusResponse getSqlTaskStatus(String taskId) {
        // 从数据库查询任务信息
        SqlTaskWithBLOBs task = sqlTaskMapper.selectByPrimaryKey(taskId);

        if (Objects.isNull(task)) {
            SqlTaskStatusResponse response = new SqlTaskStatusResponse();
            response.setTaskId(taskId);
            response.setStatus(SqlTaskStatus.NOT_FOUND.getCode());
            response.setStatusDesc(SqlTaskStatus.NOT_FOUND.getDesc());
            return response;
        }

        // 构建响应
        String statusStr = task.getStatus();
        SqlTaskStatus status = SqlTaskStatus.fromCode(statusStr);

        SqlTaskStatusResponse response = new SqlTaskStatusResponse();
        response.setTaskId(task.getId());
        response.setStatus(status.getCode());
        response.setStatusDesc(status.getDesc());
        response.setCreateBy(task.getCreateBy());
        response.setCreateTime(task.getCreateTime());
        response.setStartTime(task.getStartTime());
        response.setEndTime(task.getEndTime());
        response.setDuration(task.getDuration());
        response.setProgress(task.getProgress());

        // 如果任务执行成功, 返回结果
        if (SqlTaskStatus.SUCCESS.equals(status)) {
            // 从数据库查询结果
            List<SqlTaskResult> sqlTaskResults = sqlTaskResultService.getByTaskId(taskId);
            if (CollUtil.isNotEmpty(sqlTaskResults)) {
                String resultData = sqlTaskResults.get(0).getData();
                response.setTaskResult(JsonUtils.toBean(resultData, JSON.class));
            }
        } else if (SqlTaskStatus.FAILED.equals(status)) {
            // 如果任务执行失败, 返回错误信息
            response.setFailType(task.getFailType());
            response.setErrorMessage(task.getErrorMessage());
        }

        // 获取任务日志
        List<String> logs = Optional.ofNullable(sqlTaskLogService.getByTaskId(taskId))
                .orElse(Lists.newArrayList())
                .stream()
                .map(SqlTaskLog::getLogContent)
                .collect(Collectors.toList());
        response.setLog(logs);

        return response;
    }

    @Override
    public SqlTaskCancelResponse cancelSqlTask(String taskId) {
        cancelSqlTask(taskId, SqlTaskFailType.MANUAL_TERMINATION, getCurrentUser().getId());

        // 返回响应
        SqlTaskCancelResponse response = new SqlTaskCancelResponse();
        response.setTaskId(taskId);
        response.setCancelResult("SUCCESS");
        response.setCancelTime(new Date());

        return response;
    }

    /**
     * 获取当前用户 SQL 任务执行历史
     *
     * @return 任务执行历史响应
     */
    @Override
    public List<SqlTaskHistoryResponse> getSqlTaskHistory() {
        String currentUserId = getCurrentUser().getId();
        SqlTaskExample sqlTaskExample = new SqlTaskExample();
        SqlTaskExample.Criteria criteria = sqlTaskExample.createCriteria();
        criteria.andCreateByEqualTo(currentUserId)
                .andExecuteTypeEqualTo(SqlTaskExecuteType.AD_HOC.getCode());
        sqlTaskExample.setOrderByClause("`create_time` DESC");

        List<SqlTaskWithBLOBs> sqlTasks = sqlTaskMapper.selectByExampleWithBLOBs(sqlTaskExample);
        if (CollUtil.isEmpty(sqlTasks)) {
            return Lists.newArrayList();
        }
        return sqlTasks.stream()
                .map(sqlTaskFactory::getSqlTaskHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取当前用户 SQL 任务执行历史
     *
     * @param viewId View ID
     * @return 任务执行历史响应
     */
    @Override
    public List<SqlTaskHistoryResponse> getSqlTaskHistory(String viewId) {
        String currentUserId = getCurrentUser().getId();
        SqlTaskExample sqlTaskExample = new SqlTaskExample();
        SqlTaskExample.Criteria criteria = sqlTaskExample.createCriteria();
        criteria.andCreateByEqualTo(currentUserId)
                .andExecuteTypeEqualTo(SqlTaskExecuteType.AD_HOC.getCode())
                .andViewIdEqualTo(viewId);
        sqlTaskExample.setOrderByClause("`create_time` DESC");

        List<SqlTaskWithBLOBs> sqlTasks = sqlTaskMapper.selectByExampleWithBLOBs(sqlTaskExample);
        if (CollUtil.isEmpty(sqlTasks)) {
            return Lists.newArrayList();
        }
        return sqlTasks.stream()
                .map(sqlTaskFactory::getSqlTaskHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取任务执行结果
     *
     * @param taskId 任务 ID
     * @return 任务执行结果响应
     */
    @Override
    public SqlTaskResultStrResponse getSqlTaskResult(String taskId) {
        List<SqlTaskResult> sqlTaskResults = sqlTaskResultService.getByTaskId(taskId);
        if (CollUtil.isEmpty(sqlTaskResults)) {
            return new SqlTaskResultStrResponse("");
        }
        String resultData = sqlTaskResults.get(0).getData();
        try {
            Dataframe dataframe = JsonUtils.toBean(resultData, Dataframe.class);
            // dataframe 转 字符串 格式
            StringJoiner columnSj = new StringJoiner(",", "=== 列名(以','分隔) ===\n", "");
            dataframe.getColumns().stream().map(c -> {
                if (Objects.nonNull(c.getName()) && c.getName().length >= 1) {
                    return c.getName()[0];
                }
                return "";
            }).forEach(columnSj::add);

             StringJoiner rowSj = new StringJoiner("\n", "=== 数据(以'|'分隔列) ===\n", "");
            dataframe.getRows().stream().map(line -> {
                return line.stream().map(col -> {
                    if (Objects.nonNull(col)) {
                        return col.toString();
                    }
                    return "";
                }).collect(Collectors.joining("|"));
            }).forEach(rowSj::add);

            return new SqlTaskResultStrResponse(columnSj + "\n\n" + rowSj);
        } catch (Exception e) {
            log.error("getSqlTaskResult error. taskId: {}", taskId, e);
            return new SqlTaskResultStrResponse("");
        }
    }

    @Override
    public void updateTaskProgress(String taskId, Integer progress) {
        // 查询当前进度
        SqlTaskWithBLOBs sqlTask = sqlTaskMapper.selectByPrimaryKey(taskId);
        Integer oldProgress = sqlTask.getProgress();
        if (progress <= oldProgress) {
            // 要更新的进度 <= 当前进度, 不更新
            return;
        }

        SqlTaskExample example = new SqlTaskExample();
        example.createCriteria()
                .andIdEqualTo(taskId)
                .andProgressEqualTo(oldProgress);

        SqlTaskWithBLOBs updateSqlTask = new SqlTaskWithBLOBs();
        updateSqlTask.setProgress(progress);
        updateSqlTask.setUpdateBy(SystemConstant.SYSTEM_USER_ID);
        updateSqlTask.setUpdateTime(new Date());
        sqlTaskMapper.updateByExampleSelective(updateSqlTask, example);
    }

    private void cancelSqlTask(String taskId, SqlTaskFailType failType) {
        cancelSqlTask(taskId, failType, SystemConstant.SYSTEM_USER_ID);
    }

    private void cancelSqlTask(String taskId, SqlTaskFailType failType, String operatorUserId) {
        log.info("取消任务执行, taskId: {}, failType: {}", taskId, failType);
        // 从数据库查询任务信息
        SqlTaskWithBLOBs task = sqlTaskMapper.selectByPrimaryKey(taskId);

        // 更新任务状态为失败
        Date endTime = new Date();
        task.setStatus(SqlTaskStatus.FAILED.getCode());
        task.setFailType(failType.getCode());
        task.setErrorMessage("");
        task.setEndTime(endTime);
        task.setDuration(System.currentTimeMillis() - task.getCreateTime().getTime());
        task.setUpdateBy(operatorUserId);
        task.setUpdateTime(endTime);

        // 更新任务信息到数据库
        sqlTaskMapper.updateByPrimaryKeySelective(task);

        // 尝试中断执行线程
        if (runningTasks.containsKey(taskId)) {
            RunningTaskBo runningTask = runningTasks.remove(taskId);
            cancelTasks.put(taskId, System.currentTimeMillis());
            Thread runningThread = runningTask.getRunThread();
            if (Objects.nonNull(runningThread) && runningThread.isAlive()) {
                try {
                    runningThread.interrupt();
                } catch (Exception e) {
                    log.error("Cancel task error. taskId: {}", taskId, e);
                }
            }
        }
        // 并且中断线程中对应的 statement 执行
        AtomicReference<Statement> statementAtomicReference = CommonVarUtils.SQL_STATEMENTS.remove(taskId);
        if (Objects.isNull(statementAtomicReference)) {
            return;
        }
        Statement statement = statementAtomicReference.get();
        if (Objects.isNull(statement)) {
            return;
        }
        try {
            statement.cancel();
            log.info("任务({}) Statement 执行已取消", taskId);
        } catch (Exception e) {
            log.error("Cancel task error. taskId: {}", taskId, e);
        }
    }

    // 任务处理线程
    private void processTasks() {
        log.info("processTasks consumer start");
        while (!Thread.currentThread().isInterrupted()) {
            SqlTaskWithBLOBs task = null;
            try {
                // 从队列获取任务
                QueueTaskBo queueTask = taskQueue.take();
                log.info("从队列获取到任务, queueTask: {}", queueTask);
                task = queueTask.getSqlTask();

                String username = queueTask.getUsername();
                securityManager.runAs(username);

                // 更新任务状态为执行中
                Date runDate = new Date();
                task.setStatus(SqlTaskStatus.RUNNING.getCode());
                task.setProgress(SqlTaskProgress.START.getProgress(true));
                task.setStartTime(runDate);
                task.setUpdateBy(SystemConstant.SYSTEM_USER_ID);
                task.setUpdateTime(runDate);
                sqlTaskMapper.updateByPrimaryKeySelective(task);

                // 记录当前执行线程
                runningTasks.put(task.getId(), RunningTaskBo.builder()
                        .taskId(task.getId())
                        .runThread(Thread.currentThread())
                        .startTime(task.getStartTime())
                        .build());

                // 执行 SQL
                Dataframe dataframe = dataProviderService.testExecute(JsonUtils.toBean(task.getExecuteParam(), TestExecuteParam.class));
                log.info("任务({}) 执行完成", task.getId());

                Date endDate = new Date();

                // 保存执行结果
                SqlTaskResult result = new SqlTaskResult();
                result.setId(UUIDGenerator.generate());
                result.setTaskId(task.getId());
                // 将 Dataframe 转换为 JSON 字符串
                result.setData(JsonUtils.toJsonStr(dataframe));
                result.setRowCount(dataframe.getRows().size());
                result.setColumnCount(dataframe.getColumns().size());
                result.setCreateBy(task.getCreateBy());
                result.setCreateTime(endDate);
                sqlTaskResultService.insertSelective(result);

                // 更新任务状态为成功
                task.setStatus(SqlTaskStatus.SUCCESS.getCode());
                task.setEndTime(endDate);
                task.setDuration(endDate.getTime() - task.getStartTime().getTime());
                task.setProgress(SqlTaskProgress.FINISH.getProgress());
                task.setUpdateBy(SystemConstant.SYSTEM_USER_ID);
                task.setUpdateTime(endDate);
                sqlTaskMapper.updateByPrimaryKeySelective(task);
            } catch (Exception e) {
                if ((e instanceof InterruptedException)
                        || (Objects.nonNull(task) && cancelTasks.containsKey(task.getId()))) {
                    Thread.currentThread().interrupt();
                    log.info("当前线程被中断/任务被取消, task: {}", task);
                    break;
                }

                log.error("执行任务失败, task: {}", task, e);
                if (Objects.isNull(task)) {
                    continue;
                }

                // 如果之前已经更新为手动终止, 则不更新状态
                SqlTaskWithBLOBs nowTask = sqlTaskMapper.selectByPrimaryKey(task.getId());
                SqlTaskFailType nowSqlTaskFailType = SqlTaskFailType.fromCode(nowTask.getFailType());
                if (Objects.equals(SqlTaskStatus.FAILED.getCode(), nowTask.getStatus())
                        && SqlTaskFailType.getManualTypes().contains(nowSqlTaskFailType)) {
                    log.info("任务已手动终止, 不更新状态. nowTask: {}", nowTask);
                    continue;
                }

                // 更新任务状态为失败
                Date failedDate = new Date();
                task.setStatus(SqlTaskStatus.FAILED.getCode());
                task.setFailType(SqlTaskFailType.EXECUTION_FAILED.getCode());
                task.setErrorMessage(e.getMessage());
                task.setEndTime(failedDate);
                task.setDuration(System.currentTimeMillis() - task.getStartTime().getTime());
                task.setUpdateBy(SystemConstant.SYSTEM_USER_ID);
                task.setUpdateTime(failedDate);
                sqlTaskMapper.updateByPrimaryKeySelective(task);
            } finally {
                // 移除执行线程记录
                if (Objects.nonNull(task)) {
                    runningTasks.remove(task.getId());
                    log.info("任务({}) 更新状态完成", task.getId());
                }
                securityManager.releaseRunAs();
            }
        }
    }

    private String getInstantId() {
        return NetUtil.getLocalHostName();
    }

}