package datart.core.bo.assistant;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.util.List;

/**
 * @author suxinshuo
 * @date 2025/12/30 13:13
 */
@Data
public class DifyResponseBo {

    private String event;

    @Alias("conversation_id")
    private String conversationId;

    @Alias("message_id")
    private String messageId;

    @Alias("created_at")
    private Long createdAt;

    @Alias("task_id")
    private String taskId;

    private String id;

    private String answer;

    @Alias("from_variable_selector")
    private List<String> fromVariableSelector;

}
