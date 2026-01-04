package datart.core.bo.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author suxinshuo
 * @date 2025/12/19 14:10
 */
@Slf4j
@AllArgsConstructor
public class SqlTaskConsumerChecker implements Runnable {

    private ThreadPoolTaskExecutor sqlTaskExecutor;

    private int consumerCount;

    private Runnable processTasks;

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // 检查 sqlTaskExecutor 中启动的线程数量如果少于 consumerCount, 那么就再启动
            int activeCount = sqlTaskExecutor.getActiveCount();
            int maxPoolSize = sqlTaskExecutor.getMaxPoolSize();
            int diffCount = consumerCount - activeCount;
            if (diffCount > 0 && consumerCount < maxPoolSize) {
                log.info("SqlTaskConsumerChecker run, consumerCount: {}, activeCount: {}, diffCount: {}, maxPoolSize: {}",
                        consumerCount, activeCount, diffCount, maxPoolSize);
                for (int i = 0; i < diffCount; i++) {
                    sqlTaskExecutor.execute(() -> processTasks.run());
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.error("SqlTaskConsumerChecker run error", e);
            }
        }
    }
}
