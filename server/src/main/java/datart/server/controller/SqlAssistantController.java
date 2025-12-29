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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Api
@Slf4j
@RestController
@RequestMapping(value = "/sql-assistant")
public class SqlAssistantController extends BaseController {

    @ApiOperation(value = "发送 SQL 问题并获取响应")
    @PostMapping(value = "/chat")
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody SqlAssistantChatParam chatParam) {
        // 1. 构建流式响应体
        StreamingResponseBody streamingResponseBody = outputStream -> {
            // 字符输出流（避免字节编码问题）
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            String questionType = chatParam.getQuestionType();

            // 2. 逐字推送，模拟流式输出
            try {
                switch (questionType) {
                    case "function":
                        sendFunctionResponse(writer);
                        break;
                    case "analysis":
                        sendAnalysisResponse(writer);
                        break;
                    case "other":
                        sendOtherResponse(writer);
                        break;
                    default:
                        sendDefaultResponse(writer);
                }
            } catch (InterruptedException e) {
                log.error("send error", e);
            }

            // 3. 关闭流（释放资源）
            writer.close();
        };

        // 4. 返回响应，指定分块传输编码
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                // 开启分块传输
                .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                .body(streamingResponseBody);
    }

    private void sendFunctionResponse(OutputStreamWriter writer) throws InterruptedException, IOException {
        String[] chunks = {
                "## 答案\n\n以下是 SQL 中常用的聚合函数：\n\n```sql\n",
                "-- 计算平均值\nSELECT AVG(column_name) FROM table_name;\n\n",
                "-- 计算总和\nSELECT SUM(column_name) FROM table_name;\n\n",
                "-- 计算最大值\nSELECT MAX(column_name) FROM table_name;\n\n",
                "-- 计算最小值\nSELECT MIN(column_name) FROM table_name;\n\n",
                "-- 计算总数量\nSELECT COUNT(column_name) FROM table_name;\n```"
        };

        for (String chunk : chunks) {
            sendSSEEvent(writer, chunk);
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }

    private void sendAnalysisResponse(OutputStreamWriter writer) throws InterruptedException, IOException {
        String[] chunks = {
                "根据您的查询结果，我生成了以下分析：\n\n",
                "```chart\n{\n  \"chartType\": \"line\",\n  \"chartData\": {\n    \"xAxis\": {\n      \"type\": \"category\",\n      \"data\": [\"Mon\", \"Tue\", \"Wed\", \"Thu\", \"Fri\", \"Sat\", \"Sun\"]\n    },\n    \"yAxis\": { \"type\": \"value\" },\n    \"series\": [{ \"data\": [120, 200, 150, 80, 70, 110, 130], \"type\": \"line\" }]\n  }\n}\n```\n\n",
                "从图表中可以看出，周二的销售额最高，周四最低。"
        };

        for (String chunk : chunks) {
            sendSSEEvent(writer, chunk);
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }

    private void sendOtherResponse(OutputStreamWriter writer) throws InterruptedException, IOException {
        String[] chunks = {
                "SQL（Structured Query Language）是用于管理关系型数据库的标准语言。\n\n```sql\n",
                "-- 创建表\nCREATE TABLE users (\n  id INT PRIMARY KEY,\n  name VARCHAR(50),\n  email VARCHAR(100)\n);\n\n",
                "-- 插入数据\nINSERT INTO users (id, name, email) VALUES (1, \"John\", \"john@example.com\");\n\n",
                "-- 查询数据\nSELECT * FROM users;\n```"
        };

        for (String chunk : chunks) {
            sendSSEEvent(writer, chunk);
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }

    private void sendDefaultResponse(OutputStreamWriter writer) throws InterruptedException, IOException {
        String[] chunks = {
                "感谢您的提问！\n\n",
                "我可以帮助您解决各种SQL相关问题，包括：\n",
                "- 查询函数用法\n",
                "- 分析查询结果\n",
                "- 其他SQL相关问题\n\n",
                "请详细描述您的问题，我会尽力为您解答。"
        };

        for (String chunk : chunks) {
            sendSSEEvent(writer, chunk);
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }

    private void sendSSEEvent(OutputStreamWriter writer, String data) throws IOException {
        data = StringUtils.replace(data, "\n", "$line_break$");
        writer.write("data: " + data);
        writer.flush();
    }
}