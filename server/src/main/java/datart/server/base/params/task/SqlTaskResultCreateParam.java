package datart.server.base.params.task;

import datart.core.data.provider.Dataframe;
import datart.server.base.params.BaseCreateParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author suxinshuo
 * @date 2026/1/6 15:12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SqlTaskResultCreateParam extends BaseCreateParam {

    private String taskId;

    private Integer rowCount;

    private Integer columnCount;

    private Dataframe dataframe;

}
