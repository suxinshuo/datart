package datart.server.base.bo.task;

import datart.core.data.provider.Dataframe;
import lombok.Data;

import java.util.Date;

/**
 * @author suxinshuo
 * @date 2026/1/12 16:12
 */
@Data
public class SqlTaskResultBo {

    private String id;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    private String taskId;

    private Dataframe dataframe;

    private Integer rowCount;

    private Integer columnCount;

}
