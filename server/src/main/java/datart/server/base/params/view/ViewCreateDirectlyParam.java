package datart.server.base.params.view;

import datart.server.base.params.BaseCreateParam;
import lombok.*;

/**
 * @author suxinshuo
 * @date 2025/12/12 17:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ViewCreateDirectlyParam extends BaseCreateParam {

    private String name;

    private String description;

    private String orgId;

    private String sourceId;

    private String script;

    private String type;

    private String model;

    private String config;

    private String parentId;

    private Boolean isFolder;

    private Double index;

    private Byte status;

}
