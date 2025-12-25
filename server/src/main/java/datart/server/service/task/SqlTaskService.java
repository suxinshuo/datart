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
package datart.server.service.task;

import datart.server.base.dto.task.*;
import datart.server.base.params.TestExecuteParam;

import java.util.List;

public interface SqlTaskService {

    /**
     * 创建 SQL 执行任务
     *
     * @param executeParam 任务创建参数
     * @return 任务创建响应
     */
    SqlTaskCreateResponse createSqlTask(TestExecuteParam executeParam);

    /**
     * 查询任务状态
     *
     * @param taskId 任务 ID
     * @return 任务状态响应
     */
    SqlTaskStatusResponse getSqlTaskStatus(String taskId);

    /**
     * 取消任务执行
     *
     * @param taskId 任务 ID
     * @return 取消响应
     */
    SqlTaskCancelResponse cancelSqlTask(String taskId);

    /**
     * 获取当前用户 SQL 任务执行历史
     *
     * @return 任务执行历史响应
     */
    List<SqlTaskHistoryResponse> getSqlTaskHistory();

    /**
     * 获取当前用户 SQL 任务执行历史
     *
     * @param viewId View ID
     * @return 任务执行历史响应
     */
    List<SqlTaskHistoryResponse> getSqlTaskHistory(String viewId);

    /**
     * 获取任务执行结果
     *
     * @param taskId 任务 ID
     * @return 任务执行结果响应
     */
    SqlTaskResultStrResponse getSqlTaskResult(String taskId);

}