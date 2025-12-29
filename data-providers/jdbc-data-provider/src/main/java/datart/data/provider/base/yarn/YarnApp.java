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

/**
 * @author suxinshuo
 * @date 2025/12/24 17:37
 */
@Data
public class YarnApp {

    private String id;
    private String user;
    private String name;
    private String queue;
    private String state;
    private String finalStatus;
    private Double progress;
    private String trackingUI;
    private String trackingUrl;
    private String diagnostics;
    private Long clusterId;
    private String applicationType;
    private String applicationTags;
    private Integer priority;
    private Long startedTime;
    private Long launchTime;
    private Long finishedTime;
    private Long elapsedTime;

}
