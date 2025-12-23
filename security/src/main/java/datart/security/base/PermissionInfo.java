package datart.security.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionInfo {

    private String id;

    private String orgId;

    private SubjectType subjectType;

    private String subjectId;

    private ResourceType resourceType;

    private String resourceId;

    private int permission;

}
