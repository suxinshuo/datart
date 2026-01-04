package datart.core.bo.assistant;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author suxinshuo
 * @date 2025/12/30 12:32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyLLMAssistantRequestBo {

    @Alias("question_type")
    private String questionType;

    private String content;

    @Alias("sql_type")
    private String sqlType = "doris";

}
