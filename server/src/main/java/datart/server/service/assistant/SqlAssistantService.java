package datart.server.service.assistant;

import datart.server.base.params.SqlAssistantChatParam;

import java.io.OutputStream;

/**
 * @author suxinshuo
 * @date 2025/12/29 16:57
 */
public interface SqlAssistantService {

    void chat(String username, SqlAssistantChatParam chatParam, OutputStream outputStream);

}
