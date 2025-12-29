package datart.data.provider.calcite.dialect;

import com.google.common.base.Preconditions;
import datart.data.provider.jdbc.JdbcDriverInfo;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWriter;

/**
 * @author suxinshuo
 * @date 2025/12/25 15:40
 */
public class DorisSqlDialectSupport extends CustomSqlDialect implements FetchAndOffsetSupport {

    public DorisSqlDialectSupport(JdbcDriverInfo driverInfo) {
        super(driverInfo);
    }

    @Override
    public void unparseOffsetFetch(SqlWriter writer, SqlNode offset, SqlNode fetch) {
        Preconditions.checkArgument(fetch != null || offset != null);
        unparseLimit(writer, fetch);
    }

}
