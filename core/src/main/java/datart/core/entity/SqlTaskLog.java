package datart.core.entity;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SqlTaskLog extends BaseEntity {
    private String taskId;

    private Date logTime;

    private String logLevel;

    private String logContent;
}