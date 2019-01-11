package com.hansight.springbootmybatis2.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created by richie on 17-12-8.
 */
public class ExecutorsUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorsUtil.class);

    public static ScheduledExecutorService scheduledExecutorService(int corePoolSize, ThreadFactory threadFactory) {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize, threadFactory);

        // add shutdown hook when JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));

        return service;
    }

    public static ScheduledThreadPoolExecutor newScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
        // add shutdown hook when JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdownNow));

        return executor;
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        ExecutorService service = Executors.newCachedThreadPool(threadFactory);
        // add shutdown hook when JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));

        return service;
    }

    public static ExecutorService newFixedThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        ExecutorService service = Executors.newFixedThreadPool(corePoolSize, threadFactory);
        // add shutdown hook when JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));

        return service;
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(threadFactory);

        // add shutdown hook when JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));

        return service;
    }

    public static Thread createAndStartThread(Runnable runnable, String name, boolean daemon) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(daemon);
        thread.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
            logger.error("Thread[" + t.getName() + "] exited anomaly", e);
        });
        thread.start();
        return thread;
    }
}
