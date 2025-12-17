package datart.server.base.params.doris;

import datart.server.base.params.BaseCreateParam;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author suxinshuo
 * @date 2025/12/5 17:42
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DorisUserMappingCreateParam extends BaseCreateParam {

    @NotBlank
    private String sysUsername;

    @NotBlank
    private String sourceId;

    @NotBlank
    private String dorisUsername;

    @NotBlank
    private String encryptedPassword;

}
