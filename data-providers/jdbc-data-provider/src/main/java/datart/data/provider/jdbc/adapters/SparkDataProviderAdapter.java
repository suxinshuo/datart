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
import com.google.common.collect.Sets;
import datart.core.data.provider.SchemaItem;
import datart.core.entity.SourceConstants;
import datart.core.entity.User;
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

        // TODO: 启动一个子线程轮询获取计算 Spark 任务进度

        super.executeAllPreSqlHook(taskId, statement);
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
