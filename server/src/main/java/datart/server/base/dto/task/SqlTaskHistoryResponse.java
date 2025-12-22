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
package datart.server.base.dto.task;

import datart.core.entity.enums.SqlTaskFailType;
import datart.core.entity.enums.SqlTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Sql 任务执行历史
 *
 * @author suxinshuo
 * @date 2025/12/22 10:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlTaskHistoryResponse {

    private String id;

    /**
     * sql 内容
     */
    private String query;

    private String scriptType;

    private SqlTaskStatus status;

    /**
     * 开始执行时间
     */
    private Date startTime;

    /**
     * 执行结束时间
     */
    private Date endTime;

    /**
     * 执行耗时
     */
    private Long duration;

    private SqlTaskFailType failType;

    private String errorMessage;

    /**
     * 发起时间
     */
    private Date submitTime;

}
