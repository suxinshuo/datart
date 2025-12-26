package datart.data.provider.jdbc.adapters;

import datart.core.base.PageInfo;
import datart.core.data.provider.Dataframe;
import datart.data.provider.base.entity.ExecuteSqlParam;
import datart.data.provider.script.SqlStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MsSqlDataProviderAdapter extends JdbcDataProviderAdapter {

    @Override
    protected String readCurrDatabase(Connection conn, boolean isCatalog) throws SQLException {
        String databaseName = StringUtils.substringAfterLast(jdbcProperties.getUrl().toLowerCase(), "databasename=");
        databaseName = StringUtils.substringBefore(databaseName, ";");
        if (StringUtils.isBlank(databaseName)) {
            return null;
        }
        return super.readCurrDatabase(conn, isCatalog);
    }

    @Override
    public int executeCountSql(ExecuteSqlParam param) throws SQLException {
        String taskId = param.getTaskId();
        String sql = param.getSql();

        try (Connection connection = getConn(taskId)) {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.last();
            return resultSet.getRow();
        }
    }

    @Override
    protected Dataframe execute(ExecuteSqlParam param, PageInfo pageInfo) throws SQLException {
        param.setSql(SqlStringUtils.rebuildSqlWithFragment(param.getSql()));
        return super.execute(param, pageInfo);
    }
}
