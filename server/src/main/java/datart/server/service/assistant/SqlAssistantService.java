package datart.server.service.assistant;

import datart.server.base.params.SqlAssistantChatParam;

/**
 * @author suxinshuo
 * @date 2025/12/29 16:57
 */
public interface SqlAssistantService {

    String[] chat(SqlAssistantChatParam chatParam);

}
