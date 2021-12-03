package judge.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class TaskExecutor {

    private Map<ExecutorTaskType, ThreadPoolExecutor> executors = new HashMap<ExecutorTaskType, ThreadPoolExecutor>();
    /**
     * ScheduledThreadPoolExecutor 支持任务周期性调度的线程池
     * DelayedWorkQueue 一个有序的延时队列
     */
    private ScheduledExecutorService delayedTaskDispatcher = Executors.newScheduledThreadPool(1);
    
    protected <V> Future<V> submitNoDelay(final Task<V> task) {
        Validate.isTrue(task.delaySeconds <= 0);
        return getExecutor(task.taskType).submit(task);
    }
    
    protected <V> ScheduledFuture<Future<V>> submitDelay(final Task<V> task) {
        Validate.isTrue(task.delaySeconds > 0);
        // 特定时间延时后执行一次Callable
        return delayedTaskDispatcher.schedule(new Callable<Future<V>>() {
            @Override
            public Future<V> call() throws Exception {
                return getExecutor(task.taskType).submit(task);
            }
        }, task.delaySeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        delayedTaskDispatcher.shutdownNow();
        for (ThreadPoolExecutor executor : executors.values()) {
            executor.shutdownNow();
        }
    }
    
    private ThreadPoolExecutor getExecutor(ExecutorTaskType taskType) {
        if (!executors.containsKey(taskType)) {
            synchronized (executors) {
                if (!executors.containsKey(taskType)) {
                    // ThreadPoolExecutor 提供管理任务执行，线程调度，线程池管理等服务
                    ThreadPoolExecutor executor = new ThreadPoolExecutor(
                            // corePoolSize	核心线程数
                            taskType.maximumConcurrency,
                            // maximumPoolSize 最大线程数
                            Integer.MAX_VALUE,
                            // keepAliveTime 如果线程池当前拥有超过corePoolSize的线程，那么多余的线程在空闲时间超过keepAliveTime时会被终止
                            taskType.keepAliveSeconds,
                            // keepAliveTime时间单位
                            TimeUnit.SECONDS,
                            // workQueue 阻塞任务队列
                            new LinkedBlockingDeque<Runnable>());
                    // 将此超时策略应用于核心线程
                    executor.allowCoreThreadTimeOut(true);
                    executors.put(taskType, executor);
                }
            }
        }
        return executors.get(taskType);
    }
}
