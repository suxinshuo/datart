package datart.core.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author suxinshuo
 * @date 2025/12/23 17:43
 */
@Getter
@AllArgsConstructor
public enum SqlTaskProgress {

    /**
     * 排队
     */
    QUEUED(0),

    /**
     * 结束排队, 开始运行
     */
    START(20),

    /**
     * 运行完成
     */
    FINISH(100);

    private final Integer progress;

}
