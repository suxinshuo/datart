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
package datart.core.common;

import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum SqlTaskStatus {
    
    QUEUED("QUEUED", "任务排队中"),
    
    RUNNING("RUNNING", "任务执行中"),
    
    SUCCESS("SUCCESS", "任务执行成功"),
    
    FAILED("FAILED", "任务执行失败"),

    NOT_FOUND("NOT_FOUND", "任务不存在"),

    UNKNOWN("UNKNOWN", "未知任务状态");
    
    private final String code;
    
    private final String desc;
    
    SqlTaskStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static final Map<String, SqlTaskStatus> CODE_MAP = Stream.of(values())
            .collect(Collectors.toMap(
                    SqlTaskStatus::getCode,
                    e -> e,
                    (x1, x2) -> x2
            ));
    
    public static SqlTaskStatus fromCode(String code) {
        return CODE_MAP.getOrDefault(code, UNKNOWN);
    }
}