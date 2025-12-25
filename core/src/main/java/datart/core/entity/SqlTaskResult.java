package datart.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SqlTaskResult extends BaseEntity {
    private String taskId;

    private Integer rowCount;

    private Integer columnCount;

    private String data;
}