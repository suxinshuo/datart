package datart.server.common;

import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * mdc 传递的线程池.
 *
 * @author suxinshuo
 * @date 2023-02-17 14:48
 */
public class ThreadPoolMdcExecutor extends ThreadPoolTaskExecutor {

    @Override
    public void execute(Runnable task) {
        super.execute(MdcUtil.wrap(task, MDC.getCopyOfContextMap()));
    }

    @Override
    public void execute(Runnable task, long startTimeout) {

        super.execute(MdcUtil.wrap(task, MDC.getCopyOfContextMap()), startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(MdcUtil.wrap(task, MDC.getCopyOfContextMap()));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(MdcUtil.wrap(task, MDC.getCopyOfContextMap()));
    }

}
