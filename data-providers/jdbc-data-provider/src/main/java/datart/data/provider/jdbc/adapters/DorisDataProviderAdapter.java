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

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import datart.core.data.provider.Dataframe;
import datart.core.data.provider.ExecuteParam;
import datart.core.data.provider.QueryScript;
import datart.core.data.provider.sql.OrderOperator;
import datart.core.entity.SourceConstants;
import datart.data.provider.jdbc.JdbcProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class DorisDataProviderAdapter extends JdbcDataProviderAdapter {

    @Override
    public Dataframe executeOnSource(QueryScript script, ExecuteParam executeParam) throws Exception {
        if (CollectionUtils.isEmpty(executeParam.getOrders())) {
            executeParam.setOrders(Collections.singletonList(new OrderOperator()));
        }
        return super.executeOnSource(script, executeParam);
    }

    /**
     * 对 datasource 的后处理
     *
     * @param dataSource DataSource
     * @return DataSource
     */
    @Override
    protected DataSource postProcessDs(DataSource dataSource) {
        // 切换默认 catalog
        JdbcProperties jdbcProp = this.jdbcProperties;
        if (Objects.isNull(jdbcProp)) {
            return dataSource;
        }
        Properties prop = jdbcProp.getProperties();
        if (Objects.isNull(prop)) {
            return dataSource;
        }

        String defaultCatalog = prop.getProperty(SourceConstants.PROP_DEFAULT_CATALOG);
        if (StringUtils.isBlank(defaultCatalog)) {
            return dataSource;
        }

        if (dataSource instanceof DruidDataSource) {
            DruidDataSource druidDataSource = (DruidDataSource) dataSource;
            druidDataSource.setConnectionInitSqls(Lists.newArrayList(
                    String.format("switch %s", defaultCatalog)
            ));
            return druidDataSource;
        }
        return dataSource;
    }

    /**
     * jdbcProperties 处理
     *
     * @param jdbcProperties JdbcProperties
     * @return JdbcProperties
     */
    @Override
    protected JdbcProperties processJdbcProp(JdbcProperties jdbcProperties) {
        if (Objects.isNull(jdbcProperties)) {
            return null;
        }
        Properties prop = jdbcProperties.getProperties();
        if (Objects.isNull(prop)) {
            return jdbcProperties;
        }
        String dynamicUserEnable = prop.getProperty(SourceConstants.PROP_DYNAMIC_USER_ENABLE);
        if (Boolean.parseBoolean(dynamicUserEnable)) {
            return jdbcProperties;
        }

        // TODO: 从表中获取用户名和密码



        // TODO: 待补全
        return jdbcProperties;
    }

}
