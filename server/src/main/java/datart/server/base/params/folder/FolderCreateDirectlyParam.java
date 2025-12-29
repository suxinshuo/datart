package datart.server.base.params.folder;

import datart.server.base.params.BaseCreateParam;
import lombok.*;

/**
 * @author suxinshuo
 * @date 2025/12/23 11:59
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FolderCreateDirectlyParam extends BaseCreateParam {

    private String name;

    private String orgId;

    private String relType;

    private String subType;

    private String relId;

    private String avatar;

    private String parentId;

    private Double index;

}
