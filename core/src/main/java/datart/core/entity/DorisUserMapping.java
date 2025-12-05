package datart.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DorisUserMapping extends BaseEntity {
    private String sysUsername;

    private String sourceId;

    private String dorisUsername;

    private String encryptedPassword;
}