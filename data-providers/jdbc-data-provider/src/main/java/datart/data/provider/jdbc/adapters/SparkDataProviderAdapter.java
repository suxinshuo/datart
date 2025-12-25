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

package datart.data.provider.jdbc.adapters;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.map.MapUtil;
import cn.hutool.db.DbRuntimeException;
import cn.hutool.setting.dialect.Props;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import datart.core.data.provider.SchemaItem;
import datart.core.entity.SourceConstants;
import datart.core.entity.User;
import datart.core.entity.enums.SqlTaskProgress;
import datart.data.provider.base.yarn.*;
import datart.data.provider.jdbc.JdbcProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author suxinshuo
 * @date 2025/12/23 17:53
 */
@Slf4j
public class SparkDataProviderAdapter extends JdbcDataProviderAdapter {

    private static final ThreadLocal<Map<String, Thread>> SPARK_TASK_PROGRESS_POLLING_THREADS = new InheritableThreadLocal<>();

    private List<Tuple> parserConf(String confStr) {
        String confStrTrim = StringUtils.trim(confStr);
        String[] confArray = StringUtils.split(confStrTrim, ";");
        if (Objects.isNull(confArray)) {
            return Lists.newArrayList();
        }
        return Arrays.stream(confArray).map(confOne -> {
            String[] pair = StringUtils.split(StringUtils.trim(confOne), "=");
            if (Objects.isNull(pair) || pair.length != 2) {
                return null;
            }
            return new Tuple(StringUtils.trim(pair[0]), StringUtils.trim(pair[1]));
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    protected Connection getConn(String taskId) throws SQLException {
        String url = this.getJdbcProperties().getUrl();
        SparkUrl sparkUrl = parserUrl(url);
        EngineShareLevel shareLevel = sparkUrl.getShareLevel();
        if (EngineShareLevel.commonShare().contains(shareLevel)) {
            // 如果是所有人共享, 还走原先的逻辑, 从线程池获取连接
            return super.getConn();
        }

        final Props prop = new Props();
        // Spark 的 user 应该从当前登录用户获取
        String user = this.getJdbcProperties().getUser();
        User currentUser = getProviderContext().getCurrentUser();
        if (Objects.nonNull(currentUser) && StringUtils.isNotBlank(currentUser.getUsername())) {
            user = currentUser.getUsername();
        }
        String password = this.getJdbcProperties().getPassword();
        if (StringUtils.isNotBlank(user)) {
            prop.setProperty("user", user);
        }
        if (StringUtils.isNotBlank(password)) {
            prop.setProperty("password", password);
        }

        Properties properties = this.getJdbcProperties().getProperties();
        if (MapUtil.isNotEmpty(properties)) {
            prop.putAll(properties);
        }

        String driverClass = this.getJdbcProperties().getDriverClass();
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new DbRuntimeException(e, "Get jdbc driver [{}] error!", driverClass);
        }

        // url 增加任务 taskId
        String sparkAppName = getSparkAppName(shareLevel, taskId, user);
        List<Tuple> sparkConf = Optional.ofNullable(sparkUrl.getSparkConf()).orElse(Lists.newLinkedList());
        sparkConf.add(new Tuple("spark.app.name", sparkAppName));
        String finalUrl = sparkUrl.toString();
        log.info("创建 Spark 连接 URL: {}", finalUrl);

        return DriverManager.getConnection(finalUrl, prop);
    }

    private String getSparkAppName(EngineShareLevel shareLevel, String taskId, String user) {
        String prefix = "Datart-Spark-";
        switch (shareLevel) {
            case CONNECTION:
                return prefix + "CONNECTION-" + taskId;
            case USER:
                return prefix + "USER-" + user;
            case GROUP:
                return prefix + "GROUP";
            default:
                return prefix + "COMMON";
        }
    }

    @Override
    public List<SchemaItem> readAllSchemas() throws SQLException {
        return readAllSchemasWithConn();
    }

    @Override
    protected void executeAllPreSqlHook(String taskId, Statement statement) throws SQLException {
        // 设置 QUERY_ID 属性
        if (StringUtils.isNotBlank(taskId)) {
            String setQueryIdSql = String.format("set %s=%s", SourceConstants.SPARK_ENV_QUERY_ID, taskId);
            boolean setQueryIdStatus = statement.execute(setQueryIdSql);
            if (!setQueryIdStatus) {
                log.warn("Spark 设置 QUERY_ID 失败");
            } else {
                log.info("Spark 设置 QUERY_ID({}) 成功", taskId);
            }
        }

        // 执行 select 1, 表示完成启动 application
        statement.execute("select 1");

        ResultSet resultSet = statement.executeQuery("select '${spark.yarn.tags}'");
        String appTag = "";
        if (resultSet.next()) {
            String tags = resultSet.getString(1);
            log.info("Spark 任务标签: {}", tags);
            if (StringUtils.isNotBlank(tags) && StringUtils.startsWithIgnoreCase(tags, "KYUUBI")) {
                String[] tagArr = StringUtils.split(tags, ",");
                appTag = Arrays.stream(tagArr)
                        .filter(t -> !StringUtils.equalsIgnoreCase(t, "KYUUBI"))
                        .findFirst()
                        .orElse("");
            }
        }
        if (StringUtils.isBlank(appTag)) {
            log.warn("Spark 任务标签中未包含 Kyuubi 标签, 无法监控任务进度");
        } else {
            final String finalAppTag = appTag;
            Thread sparkTaskProgressThread = new Thread(() -> pollSparkTaskProgress(taskId, finalAppTag));
            sparkTaskProgressThread.start();
            // 记录线程, 后续执行完成时, 中断线程
            Map<String, Thread> threadMap = SPARK_TASK_PROGRESS_POLLING_THREADS.get();
            if (Objects.isNull(threadMap)) {
                threadMap = Maps.newConcurrentMap();
            }
            threadMap.put(taskId, sparkTaskProgressThread);
            SPARK_TASK_PROGRESS_POLLING_THREADS.set(threadMap);
        }

        super.executeAllPreSqlHook(taskId, statement);
    }

    @Override
    protected void executeCompleteHook(String taskId, Statement statement) {
        try {
            // 任务执行完成, 中断轮询线程
            Thread sparkTaskProgressThread = SPARK_TASK_PROGRESS_POLLING_THREADS.get().remove(taskId);
            if (Objects.nonNull(sparkTaskProgressThread)) {
                sparkTaskProgressThread.interrupt();
                log.info("任务运行完成, 已中断 Spark 任务进度轮询线程, 任务 ID: {}", taskId);
            }
        } catch (Exception e) {
            log.warn("中断 Spark 任务进度轮询线程失败", e);
        }
        super.executeCompleteHook(taskId, statement);
    }

    private void pollSparkTaskProgress(String taskId, String appTag) {
        log.info("开始轮询获取 Spark 任务进度, 标签: {}", appTag);
        try {
            List<YarnRmNode> yarnRmNodes = getYarnRmNodes();
            if (CollUtil.isEmpty(yarnRmNodes)) {
                log.warn("未配置 Yarn RM 节点, 无法轮询获取 Spark 任务进度");
                return;
            }
            YarnRestClient yarnRestClient = new YarnRestClient(yarnRmNodes);
            // 记录连续获取不到 app 的次数
            int notFoundCount = 0;
            while (!Thread.currentThread().isInterrupted() && notFoundCount < 6) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // 轮询获取计算 Spark 任务进度
                List<YarnApp> yarnApps = yarnRestClient.getYarnAppsByTag(appTag);
                if (CollUtil.isEmpty(yarnApps)) {
                    notFoundCount++;
                    continue;
                }
                // 取出第一个作为要获取的 application, 因为默认 tag 是唯一的
                YarnApp yarnApp = yarnApps.get(0);
                if (!StringUtils.equalsIgnoreCase(yarnApp.getFinalStatus(), "UNDEFINED")) {
                    log.info("Spark 任务已完成, yarnApp: {}", yarnApp);
                    break;
                }
                List<? extends YarnAppJob> yarnAppJobs = yarnRestClient.getYarnAppJobs(yarnApp);
                if (CollUtil.isEmpty(yarnAppJobs)) {
                    log.warn("Spark 任务 {} 未获取到 job 信息", yarnApp.getId());
                    notFoundCount++;
                    continue;
                }
                notFoundCount = 0;
                String jobGroup = null;
                long totalNumTasks = 0L;
                long totalCompletedTasks = 0L;
                for (YarnAppJob yarnAppJob : yarnAppJobs) {
                    if (!(yarnAppJob instanceof YarnAppSparkJob)) {
                        continue;
                    }
                    YarnAppSparkJob yarnAppSparkJob = (YarnAppSparkJob) yarnAppJob;
                    // 只获取当前 JobGroup 的任务进度
                    String appJobGroup = yarnAppSparkJob.getJobGroup();
                    if (StringUtils.isBlank(jobGroup)) {
                        // 获取 JobGroup
                        String appTaskSql = yarnAppSparkJob.getDescription();
                        String appTaskId = getTaskId(appTaskSql);
                        if (!StringUtils.equals(taskId, appTaskId)) {
                            continue;
                        }
                        jobGroup = appJobGroup;
                        log.info("任务 taskId({}) 所属 Spark 任务 {} JobGroup: {}", taskId, yarnApp.getId(), jobGroup);
                    } else {
                        if (!StringUtils.equals(jobGroup, appJobGroup)) {
                            // 不是当前 JobGroup, 跳过
                            continue;
                        }
                    }

                    Long numTasks = yarnAppSparkJob.getNumTasks();
                    Long numCompletedTasks = yarnAppSparkJob.getNumCompletedTasks();
                    totalNumTasks += numTasks;
                    totalCompletedTasks += numCompletedTasks;
                }
                int sparkAppAvailableProgress = SqlTaskProgress.RUNNING_COMPLETE.getProgress() - SqlTaskProgress.RUNNING_START.getProgress();
                int progress = (int) (totalCompletedTasks / totalNumTasks * sparkAppAvailableProgress + SqlTaskProgress.RUNNING_START.getProgress());
                log.info("Spark 任务 {} 进度: {} / {}, 计算进度: {}", yarnApp.getId(), totalCompletedTasks, totalNumTasks, progress);
                // 更新进度
                getProviderContext().updateTaskProgress(taskId, progress);
            }
        } catch (Exception e) {
            log.warn("轮询获取 Spark 任务进度失败", e);
        }
    }

    private List<YarnRmNode> getYarnRmNodes() {
        JdbcProperties jdbcProp = this.jdbcProperties;
        if (Objects.isNull(jdbcProp)) {
            return Lists.newArrayList();
        }
        Properties prop = jdbcProp.getProperties();
        if (Objects.isNull(prop)) {
            return Lists.newArrayList();
        }

        String yarnRmUrl = prop.getProperty(SourceConstants.PROP_YARN_RM_URL);
        if (StringUtils.isBlank(yarnRmUrl)) {
            return Lists.newArrayList();
        }
        return Arrays.stream(StringUtils.split(yarnRmUrl, ",")).map(url -> {
            String trimUrl = StringUtils.trim(url);
            String[] ss = StringUtils.split(trimUrl, ":");
            if (Objects.isNull(ss)) {
                return null;
            }
            if (ss.length != 2) {
                return null;
            }
            return new YarnRmNode(ss[0], Long.parseLong(ss[1]));
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private SparkUrl parserUrl(String url) {
        String[] ss1 = StringUtils.split(url, "?");
        if (Objects.isNull(ss1)) {
            return SparkUrl.empty();
        }
        if (ss1.length == 1) {
            String[] ss2 = StringUtils.split(ss1[0], "#");
            String simpleUrl = StringUtils.trim(ss2[0]);
            if (ss2.length == 1) {
                return SparkUrl.builder().simpleUrl(simpleUrl).build();
            }

            List<Tuple> sparkConf = parserConf(ss2[1]);
            return SparkUrl.builder().simpleUrl(simpleUrl).sparkConf(sparkConf).build();
        }
        String simpleUrl = StringUtils.trim(ss1[0]);
        String[] ss2 = StringUtils.split(ss1[1], "#");
        List<Tuple> kyuubiConf = parserConf(ss2[0]);
        if (ss2.length == 1) {
            return SparkUrl.builder().simpleUrl(simpleUrl).kyuubiConf(kyuubiConf).build();
        }

        List<Tuple> sparkConf = parserConf(ss2[1]);
        return SparkUrl.builder()
                .simpleUrl(simpleUrl)
                .kyuubiConf(kyuubiConf)
                .sparkConf(sparkConf)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SparkUrl {
        private String simpleUrl;
        private List<Tuple> kyuubiConf;
        private List<Tuple> sparkConf;

        public static SparkUrl empty() {
            return new SparkUrl();
        }

        public EngineShareLevel getShareLevel() {
            // 从 kyuubi conf 中获取
            if (CollUtil.isEmpty(this.kyuubiConf)) {
                return EngineShareLevel.defaultLevel();
            }
            Optional<Tuple> shareLevelTuple = kyuubiConf.stream()
                    .filter(t -> StringUtils.equals(t.get(0), "kyuubi.engine.share.level"))
                    .findFirst();
            if (shareLevelTuple.isPresent()) {
                return EngineShareLevel.of(shareLevelTuple.get().get(1));
            }
            return EngineShareLevel.defaultLevel();
        }

        @Override
        public String toString() {
            String kyuubiConfStr = confToStr(this.kyuubiConf);
            String sparkConfStr = confToStr(this.sparkConf);

            String finalUrl = this.simpleUrl;
            if (StringUtils.isNotBlank(kyuubiConfStr)) {
                finalUrl = finalUrl + "?" + kyuubiConfStr;
            }
            if (StringUtils.isNotBlank(sparkConfStr)) {
                finalUrl = finalUrl + "#" + sparkConfStr;
            }

            return finalUrl;
        }

        private String confToStr(List<Tuple> confs) {
            if (CollUtil.isEmpty(confs)) {
                return "";
            }
            return confs.stream()
                    .map(t -> t.get(0) + "=" + t.get(1))
                    .collect(Collectors.joining(";"));
        }
    }

    public enum EngineShareLevel {

        CONNECTION, USER, GROUP, SERVER;

        /**
         * 所有人共享, 这种情况下连接共享线程池
         *
         * @return Set<EngineShareLevel>
         */
        public static Set<EngineShareLevel> commonShare() {
            return Sets.newHashSet(SERVER);
        }

        public static EngineShareLevel defaultLevel() {
            return USER;
        }

        public static EngineShareLevel of(String code) {
            return Arrays.stream(values())
                    .filter(e -> StringUtils.equals(code, e.name()))
                    .findFirst()
                    .orElse(defaultLevel());
        }

    }


}
