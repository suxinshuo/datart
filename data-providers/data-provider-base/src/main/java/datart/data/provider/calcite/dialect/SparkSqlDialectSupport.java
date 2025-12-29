package datart.data.provider.calcite.dialect;

import com.google.common.base.Preconditions;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.dialect.SparkSqlDialect;

/**
 * @author suxinshuo
 * @date 2025/12/25 15:13
 */
public class SparkSqlDialectSupport extends SparkSqlDialect implements FetchAndOffsetSupport {

    private SparkSqlDialectSupport(Context context) {
        super(context);
    }

    public SparkSqlDialectSupport() {
        this(DEFAULT_CONTEXT);
    }

    @Override
    public void unparseOffsetFetch(SqlWriter writer, SqlNode offset, SqlNode fetch) {
        Preconditions.checkArgument(fetch != null || offset != null);
        unparseLimit(writer, fetch);
    }
}
