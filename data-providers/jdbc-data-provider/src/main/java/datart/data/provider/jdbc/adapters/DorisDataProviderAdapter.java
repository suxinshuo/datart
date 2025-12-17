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
import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import datart.core.data.provider.*;
import datart.core.data.provider.sql.OrderOperator;
import datart.core.entity.SourceConstants;
import datart.data.provider.jdbc.JdbcProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
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
    public List<SchemaItem> readAllSchemas() throws SQLException {
        // TODO: 后续优化方案
        // 如果是默认 catalog(即没有指定 catalog), 或者指定了 catalog = internal, 那么直接从数据库 information_schema.columns 查询
        // 如果是 hive catalog, 那么从 hive metastore 源数据库查询
        // 如果是其他的, 暂时还走原先的逻
        List<SchemaItem> schemaItems = Lists.newLinkedList();
        try (Connection conn = getConn()) {
            DatabaseMetaData metaData = conn.getMetaData();
            boolean isCatalog = isReadFromCatalog(conn);
            String currDatabase = readCurrDatabase(conn, isCatalog);
            String connSchema = conn.getSchema();

            Set<String> databases = this.readAllDatabasesFromMetaData(metaData, isCatalog, currDatabase);
            if (CollUtil.isEmpty(databases)) {
                return schemaItems;
            }

            for (String database : databases) {
                SchemaItem schemaItem = new SchemaItem();
                schemaItems.add(schemaItem);
                schemaItem.setDbName(database);
                schemaItem.setTables(new LinkedList<>());
                Set<String> tables = this.readAllTablesFromMetaData(metaData, isCatalog, database, connSchema);
                if (CollUtil.isEmpty(tables)) {
                    continue;
                }
                for (String table : tables) {
                    TableInfo tableInfo = new TableInfo();
                    schemaItem.getTables().add(tableInfo);
                    tableInfo.setTableName(table);
                    tableInfo.setColumns(this.readTableColumnFromMetaData(metaData, database, table));
                }
            }

            return schemaItems;
        }
    }

    private Set<String> readAllDatabasesFromMetaData(DatabaseMetaData metaData, boolean isCatalog, String currDatabase) throws SQLException {
        Set<String> databases = new HashSet<>();
        ResultSet rs = null;
        if (isCatalog) {
            rs = metaData.getCatalogs();
        } else {
            rs = metaData.getSchemas();
            log.info("Database 'catalogs' is empty, get databases with 'schemas'");
        }

        if (StringUtils.isNotBlank(currDatabase)) {
            return Collections.singleton(currDatabase);
        }

        while (rs.next()) {
            String database = rs.getString(1);
            databases.add(database);
        }
        return databases;
    }

    private Set<String> readAllTablesFromMetaData(DatabaseMetaData metadata, boolean isCatalog, String database, String connSchema) throws SQLException {
        Set<String> tables = new HashSet<>();
        String catalog = null;
        String schema = null;
        if (isCatalog) {
            catalog = database;
            schema = connSchema;
        } else {
            schema = database;
        }
        try (ResultSet rs = metadata.getTables(catalog, schema, "%", new String[]{"TABLE", "VIEW"})) {
            while (rs.next()) {
                String tableName = rs.getString(3);
                tables.add(tableName);
            }
        }
        return tables;
    }

    private Set<Column> readTableColumnFromMetaData(DatabaseMetaData metadata, String database, String table) throws SQLException {
        Set<Column> columnSet = new HashSet<>();
        try (ResultSet columns = metadata.getColumns(database, null, table, null)) {
            while (columns.next()) {
                Column column = readTableColumn(columns);
                columnSet.add(column);
            }
        }
        return columnSet;
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
