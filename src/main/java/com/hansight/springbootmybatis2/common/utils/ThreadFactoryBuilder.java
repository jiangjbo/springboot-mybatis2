package com.hansight.springbootmybatis2.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Arvin
 *
 */
public class ThreadFactoryBuilder {
	private static final Logger logger = LoggerFactory.getLogger(ThreadFactoryBuilder.class);

	private String nameFormat = null;

	private boolean daemon = true;

	private Integer priority = null;

	private UncaughtExceptionHandler uncaughtExceptionHandler = null;

	private ThreadFactory backingThreadFactory = null;

	public ThreadFactoryBuilder() {
	}

	public ThreadFactoryBuilder setNameFormat(String nameFormat) {
		this.nameFormat = nameFormat;
		return this;
	}

	public ThreadFactoryBuilder setDaemon(boolean daemon) {
		this.daemon = daemon;
		return this;
	}

	public ThreadFactoryBuilder setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	public ThreadFactoryBuilder setUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
		this.uncaughtExceptionHandler = uncaughtExceptionHandler;
		return this;
	}

	public ThreadFactoryBuilder setThreadFactory(ThreadFactory backingThreadFactory) {
		this.backingThreadFactory = backingThreadFactory;
		return this;
	}

	public ThreadFactory build() {
		return build(this);
	}

	private static ThreadFactory build(ThreadFactoryBuilder builder) {
		final String nameFormat = builder.nameFormat;
		final Boolean daemon = builder.daemon;
		final Integer priority = builder.priority;
		final UncaughtExceptionHandler uncaughtExceptionHandler = builder.uncaughtExceptionHandler;
		final ThreadFactory backingThreadFactory = (builder.backingThreadFactory != null) ? builder.backingThreadFactory
				: Executors.defaultThreadFactory();
		final AtomicLong count = (nameFormat != null) ? new AtomicLong(0) : null;

		return ((Runnable runnable) -> {
			Thread thread = backingThreadFactory.newThread(runnable);
			if (nameFormat != null) {
				thread.setName(String.format(nameFormat, count.getAndIncrement()));
			}
			thread.setDaemon(daemon);
			if (priority != null) {
				thread.setPriority(priority);
			}
			if (uncaughtExceptionHandler != null) {
				thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
			} else {
				thread.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
					logger.error("Thread[" + t.getName() + "] exited anomaly", e);
				});
			}
			return thread;
		});
	}
}
