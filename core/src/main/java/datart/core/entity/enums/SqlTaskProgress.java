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
     * 正式开始执行
     * 已经执行完了前面的 set sql
     */
    RUNNING_START(25),

    /**
     * 执行结束
     */
    RUNNING_COMPLETE(95),

    /**
     * 运行完成
     */
    FINISH(100);

    private final Integer progress;

    public Integer getProgress(boolean random) {
        // +- 10
        int randomValue = 0;
        if (Math.random() > 0.5) {
            randomValue += (int) (Math.random() * 10);
        } else {
            randomValue -= (int) (Math.random() * 10);
        }

        if (random) {
            int randomProgress = progress + randomValue;
            return getAvailableProgress(randomProgress);
        }
        return getAvailableProgress(progress);
    }

    public Integer getProgress() {
        return getProgress(false);
    }

    private Integer getAvailableProgress(Integer progress) {
        if (progress < 0) {
            return 0;
        }
        if (progress > 100) {
            return 100;
        }
        return progress;
    }

}
