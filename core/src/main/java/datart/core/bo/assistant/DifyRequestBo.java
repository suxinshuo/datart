package datart.core.bo.assistant;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author suxinshuo
 * @date 2025/12/30 12:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyRequestBo {

    private Map<String, Object> inputs;

    private String query;

    @Builder.Default
    @Alias("response_mode")
    private String responseMode = "streaming";

    private String user;

    @Alias("conversation_id")
    private String conversationId;

}
