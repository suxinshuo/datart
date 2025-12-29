package datart.server.config;

import datart.server.common.ThreadPoolMdcExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author suxinshuo
 * @date 2025/12/19 13:06
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    @Value("${datart.task.thread.pool.size:20}")
    private int threadPoolSize;

    @Value("${datart.task.thread.pool.queue_capacity:100}")
    private int queueCapacity;

    @Bean("sqlTaskExecutor")
    public ThreadPoolTaskExecutor cacheExecutor() {
        log.info("start sqlTaskExecutor");

        // 需要注意一下请求 trace id 能不能传递到子线程中, 后续再看看要不要改造
        ThreadPoolTaskExecutor executor = new ThreadPoolMdcExecutor();
        // 配置核心线程数
        executor.setCorePoolSize(threadPoolSize);
        // 设置最大线程数
        executor.setMaxPoolSize(threadPoolSize);
        // 设置队列容量
        executor.setQueueCapacity(queueCapacity);
        // 设置线程活跃时间(秒)
        executor.setKeepAliveSeconds(300);
        // 配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("SqlTaskExecutor");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 执行初始化
        executor.initialize();

        return executor;
    }

}