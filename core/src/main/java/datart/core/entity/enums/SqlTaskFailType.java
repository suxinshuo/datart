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
package datart.core.entity.enums;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum SqlTaskFailType {

    SQL_SYNTAX_ERROR("SQL_SYNTAX_ERROR", "SQL 语法错误"),

    EXECUTION_FAILED("EXECUTION_FAILED", "执行失败"),

    EXECUTION_TIMEOUT("EXECUTION_TIMEOUT", "执行超时"),

    MANUAL_TERMINATION("MANUAL_TERMINATION", "手动终止"),

    SERVICE_RESTART("SERVICE_RESTART", "服务重启"),

    RESOURCE_INSUFFICIENT("RESOURCE_INSUFFICIENT", "资源不足"),

    UNKNOWN("UNKNOWN", "未知错误");

    private final String code;

    private final String desc;

    SqlTaskFailType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static final Map<String, SqlTaskFailType> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(
                    SqlTaskFailType::getCode,
                    e -> e,
                    (x1, x2) -> x2
            ));

    public static SqlTaskFailType fromCode(String code) {
        return CODE_MAP.get(code);
    }

    public static Set<SqlTaskFailType> getManualTypes() {
        return Sets.newHashSet(EXECUTION_TIMEOUT, MANUAL_TERMINATION, SERVICE_RESTART, RESOURCE_INSUFFICIENT);
    }

}