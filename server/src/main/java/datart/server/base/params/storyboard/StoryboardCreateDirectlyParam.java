package datart.server.base.params.storyboard;

import datart.server.base.params.BaseCreateParam;
import lombok.*;

/**
 * @author suxinshuo
 * @date 2025/12/23 12:09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StoryboardCreateDirectlyParam extends BaseCreateParam {

    private String name;

    private String orgId;

    private String parentId;

    private Boolean isFolder;

    private Double index;

    private String config;

    private Byte status;

    private String operatorUserId;

}
