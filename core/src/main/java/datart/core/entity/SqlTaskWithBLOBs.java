package datart.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SqlTaskWithBLOBs extends SqlTask {
    private String script;

    private String errorMessage;

    private String executeParam;
}