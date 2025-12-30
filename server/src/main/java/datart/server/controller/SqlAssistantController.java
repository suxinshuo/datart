/*
 * Datart
 * <p>
 * Copyright 2021
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package datart.server.controller;

import datart.server.base.params.SqlAssistantChatParam;
import datart.server.service.assistant.SqlAssistantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;

@Api
@Slf4j
@RestController
@RequestMapping(value = "/sql-assistant")
public class SqlAssistantController extends BaseController {

    @Resource
    private SqlAssistantService sqlAssistantService;

    @ApiOperation(value = "发送 SQL 问题并获取响应")
    @PostMapping(value = "/chat")
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody SqlAssistantChatParam chatParam) {
        String username = getCurrentUser().getUsername();

        // 构建流式响应体
        StreamingResponseBody streamingResponseBody = outputStream ->
                sqlAssistantService.chat(username, chatParam, outputStream);

        // 返回响应, 指定分块传输编码
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                // 开启分块传输
                .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                .body(streamingResponseBody);
    }

}