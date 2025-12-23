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

import datart.server.base.dto.ResponseData;
import datart.server.base.params.TestExecuteParam;
import datart.server.service.task.SqlTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api
@RestController
@RequestMapping(value = "/execute")
public class SqlTaskController extends BaseController {

    @Resource
    private SqlTaskService sqlTaskService;
    
    @ApiOperation(value = "创建 SQL 执行任务")
    @PostMapping(value = "/sql")
    public ResponseData<?> createSqlTask(@RequestBody TestExecuteParam executeParam) throws Exception {
        return ResponseData.success(sqlTaskService.createSqlTask(executeParam));
    }
    
    @ApiOperation(value = "查询任务状态")
    @GetMapping(value = "/tasks/{taskId}")
    public ResponseData<?> getSqlTaskStatus(@PathVariable String taskId) throws Exception {
        checkBlank(taskId, "taskId");
        return ResponseData.success(sqlTaskService.getSqlTaskStatus(taskId));
    }
    
    @ApiOperation(value = "取消任务执行")
    @PostMapping(value = "/tasks/{taskId}/cancel")
    public ResponseData<?> cancelSqlTask(@PathVariable String taskId) throws Exception {
        checkBlank(taskId, "taskId");
        return ResponseData.success(sqlTaskService.cancelSqlTask(taskId));
    }

    @ApiOperation(value = "获取当前用户 SQL 任务执行历史")
    @GetMapping(value = "/tasks/history")
    public ResponseData<?> getSqlTaskHistory() throws Exception {
        return ResponseData.success(sqlTaskService.getSqlTaskHistory());
    }

    @ApiOperation(value = "获取当前用户指定 View 的 SQL 任务执行历史")
    @GetMapping(value = "/tasks/{viewId}/history")
    public ResponseData<?> getSqlTaskHistory(@PathVariable String viewId) throws Exception {
        checkBlank(viewId, "viewId");
        return ResponseData.success(sqlTaskService.getSqlTaskHistory(viewId));
    }

    @ApiOperation(value = "获取任务执行结果(字符串格式)")
    @GetMapping(value = "/tasks/{taskId}/result_str")
    public ResponseData<?> getSqlTaskResultStr(@PathVariable String taskId) throws Exception {
        checkBlank(taskId, "taskId");
        return ResponseData.success(sqlTaskService.getSqlTaskResult(taskId));
    }

}