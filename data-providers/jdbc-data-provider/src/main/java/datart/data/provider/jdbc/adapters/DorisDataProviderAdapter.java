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
import com.google.common.collect.Maps;
import datart.core.data.provider.*;
import datart.core.data.provider.sql.OrderOperator;
import datart.core.entity.SourceConstants;
import datart.data.provider.jdbc.JdbcProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class DorisDataProviderAdapter extends JdbcDataProviderAdapter {

    private static final String INTERNAL_CATALOG = "internal";

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
        String defaultCatalog = getDefaultCatalog();
        if (dataSource instanceof DruidDataSource) {
            DruidDataSource druidDataSource = (DruidDataSource) dataSource;
            druidDataSource.setConnectionInitSqls(Lists.newArrayList(
                    String.format("switch %s", defaultCatalog)
            ));
            return druidDataSource;
        }
        return dataSource;
    }

    @Override
    protected Map<String, List<ForeignKey>> getImportedKeys(DatabaseMetaData metadata, String database, String table) throws SQLException {
        return Maps.newHashMap();
    }

    private String getDefaultCatalog() {
        // 切换默认 catalog
        JdbcProperties jdbcProp = this.jdbcProperties;
        if (Objects.isNull(jdbcProp)) {
            return INTERNAL_CATALOG;
        }
        Properties prop = jdbcProp.getProperties();
        if (Objects.isNull(prop)) {
            return INTERNAL_CATALOG;
        }

        String defaultCatalog = prop.getProperty(SourceConstants.PROP_DEFAULT_CATALOG);
        if (StringUtils.isBlank(defaultCatalog)) {
            return INTERNAL_CATALOG;
        }
        return defaultCatalog;
    }

}
