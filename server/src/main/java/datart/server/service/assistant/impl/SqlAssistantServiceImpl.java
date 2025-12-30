package datart.server.service.assistant.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import datart.core.bo.assistant.DifyLLMAssistantRequestBo;
import datart.core.bo.assistant.DifyRequestBo;
import datart.core.bo.assistant.DifyResponseBo;
import datart.core.utils.JsonUtils;
import datart.server.base.params.SqlAssistantChatParam;
import datart.server.service.BaseService;
import datart.server.service.assistant.SqlAssistantService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author suxinshuo
 * @date 2025/12/29 16:58
 */
@Slf4j
@Service
public class SqlAssistantServiceImpl extends BaseService implements SqlAssistantService {

    @Value("${datart.assistant.dify.url}")
    private String difyUrl;

    @Value("${datart.assistant.dify.token.function}")
    private String functionToken;

    @Value("${datart.assistant.dify.token.analysis}")
    private String analysisToken;

    @Value("${datart.assistant.dify.token.other}")
    private String otherToken;

    @Override
    public void chat(String username, SqlAssistantChatParam chatParam, OutputStream outputStream) {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        try {
            String questionType = chatParam.getQuestionType();
            if (StringUtils.isBlank(difyUrl)) {
                sendOutMessage(writer, getMessages("message.chat.not-enable"));
                return;
            }

            String token = "";
            if (StringUtils.equals(questionType, "function")) {
                token = functionToken;
            } else if (StringUtils.equals(questionType, "analysis")) {
                token = analysisToken;
            } else {
                token = otherToken;
            }

            if (StringUtils.isBlank(token)) {
                sendOutMessage(writer, getMessages("message.chat.not-enable"));
                return;
            }

            // 请求大模型接口, 获取返回值
            DifyLLMAssistantRequestBo llmRequestBo = DifyLLMAssistantRequestBo.builder()
                    .questionType(chatParam.getQuestionType())
                    .content(chatParam.getContent())
                    .sqlType(chatParam.getSqlType())
                    .build();

            DifyRequestBo requestBo = DifyRequestBo.builder()
                    .inputs(null)
                    .query(JsonUtils.toJsonStr(llmRequestBo))
                    .user(chatParam.getUid())
                    .build();

            try (HttpResponse httpResponse = HttpUtil.createPost(difyUrl)
                    .header("Authorization", "Bearer " + token)
                    .body(JsonUtils.toJsonStr(requestBo))
                    .execute();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(httpResponse.bodyStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!StringUtils.startsWith(line, "data:")) {
                        continue;
                    }
                    line = line.substring(6);
                    DifyResponseBo responseBo = JsonUtils.toBean(line, DifyResponseBo.class);
                    if (Objects.isNull(responseBo)
                            || !StringUtils.equals(responseBo.getEvent(), "message")
                            || StringUtils.isBlank(responseBo.getAnswer())) {
                        continue;
                    }
                    sendOutMessage(writer, responseBo.getAnswer());
                }
            }
        } catch (Exception e) {
            log.error("发送消息失败, username: {}, chatParam: {}", username, chatParam, e);
            try {
                sendOutMessage(writer, getMessage("message.chat.answer-failed"));
            } catch (Exception e2) {
                log.error("发送失败消息失败", e2);
            }
        }
    }

    private void sendOutMessage(OutputStreamWriter writer, String data) throws IOException {
        data = StringUtils.replace(data, "\n", "$line_break$");
        writer.write("data: " + data + "\n");
        writer.flush();
    }

}
