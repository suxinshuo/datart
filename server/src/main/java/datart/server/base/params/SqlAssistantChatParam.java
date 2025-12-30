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
package datart.server.base.params;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SqlAssistantChatParam {

    @ApiModelProperty(value = "用户唯一 ID, 一个聊天窗口对应一个", required = true)
    private String uid;

    @ApiModelProperty(value = "问题类型: function|analysis|other", required = true)
    private String questionType;

    @ApiModelProperty(value = "问题内容", required = true)
    private String content;

    @ApiModelProperty(value = "SQL 类型", required = false)
    private String sqlType = "doris";

}
