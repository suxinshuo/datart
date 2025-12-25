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
package datart.data.provider.base.yarn;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * @author suxinshuo
 * @date 2025/12/24 17:48
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class YarnAppSparkJob extends YarnAppJob {

    private Long jobId;
    private String name;
    private String description;
    private String submissionTime;
    private String completionTime;
   private List<Long> stageIds;
    private String jobGroup;
    private String status;
    private Long numTasks;
    private Long numActiveTasks;
    private Long numCompletedTasks;
    private Long numSkippedTasks;
    private Long numFailedTasks;
    private Long numKilledTasks;
    private Long numCompletedIndices;
    private Long numActiveStages;
    private Long numCompletedStages;
    private Long numSkippedStages;
    private Long numFailedStages;
    private Map<String, Object> killedTasksSummary;

}
