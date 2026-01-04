package datart.core.entity;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SqlTask extends BaseEntity {
    private String sourceId;

    private String viewId;

    private String scriptType;

    private String status;

    private Integer priority;

    private Integer timeout;

    private Integer maxSize;

    private Date startTime;

    private Date endTime;

    private Long duration;

    private String failType;

    private String execInstanceId;

    private Integer progress;

    private String orgId;

    private String executeType;
}