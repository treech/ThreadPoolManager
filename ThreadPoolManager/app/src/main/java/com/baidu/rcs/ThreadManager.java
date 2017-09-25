package com.baidu.rcs;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * help for java.util.concurrent.Executors
 * http://blog.csdn.net/caihaijiang/article/details/30812293
 */
public class ThreadManager {

    public final static int TYPE_SINGLE = 0x0;
    public final static int TYPE_CACHEABLE = 0x1;
    public final static int TYPE_FIXED = 0x2;
    public final static int TYPE_SCHEDULED = 0x3;
    public final static int TYPE_PAUSE = 0x4;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 3;
    private static final int MAXIMUM_POOL_SIZE = Integer.MAX_VALUE;
    private static final int KEEP_ALIVE = 1;
    private ExecutorService mPool;
    private static ThreadManager sThreadPoolManager;

    private Queue<Runnable> tasks = new LinkedList<Runnable>();
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(128);


    public static synchronized ThreadManager getInstance(int type, int priority) {
        if (sThreadPoolManager == null) {
            sThreadPoolManager = new ThreadManager(type, priority);
        }
        return sThreadPoolManager;
    }

    private ThreadManager(int type, int priority) {
        if (mPool == null || mPool.isShutdown()) {
            mPool = createPool(type, priority);
        }
    }

    /**
     * @param type     (TYPE_CACHEABLE TYPE_FIXED TYPE_SCHEDULED TYPE_PAUSE TYPE_DEFAULT)
     * @param priority (Thread.MIN_PRIORITY Thread.NORM_PRIORITY Thread.MAX_PRIORITY)
     * @return
     */
    private ExecutorService createPool(int type, int priority) {

        priority = Math.max(Thread.MIN_PRIORITY, priority);
        priority = Math.min(Thread.MAX_PRIORITY, priority);

        switch (type) {
            case TYPE_CACHEABLE:
                return Executors.newCachedThreadPool(new DefaultFactory(priority));
            case TYPE_FIXED:
                return Executors.newFixedThreadPool(CORE_POOL_SIZE, new DefaultFactory(priority));
            case TYPE_SCHEDULED:
                return Executors.newScheduledThreadPool(CORE_POOL_SIZE, new DefaultFactory(priority));
            case TYPE_PAUSE:
                return new PausableThreadPoolExecutor(
                        CORE_POOL_SIZE,
                        MAXIMUM_POOL_SIZE,
                        KEEP_ALIVE,
                        TimeUnit.SECONDS,
                        sPoolWorkQueue);
            case TYPE_SINGLE:
            default:
                return Executors.newSingleThreadExecutor(new DefaultFactory(priority));
        }
    }

    private class DefaultFactory implements ThreadFactory {

        private int priority;

        DefaultFactory(int priority) {
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setPriority(priority);
            return thread;
        }
    }

    /**
     * this method is just for a single task
     *
     * @param runnable
     */
    public void excuteTask(Runnable runnable) {
        mPool.execute(runnable);
    }

    /**
     * this method is for tasks (for example,many pictures to be downloaded...)
     */
    public void executeTasks() {
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            mPool.execute(tasks.poll());
        }
    }

    public void addTask(Runnable task) {
        tasks.add(task);
    }

    public Queue<Runnable> getTasks() {
        return tasks;
    }

    /**
     * @return this is just for PausableThreadPoolExecutor,do not get this pool for other scenes
     */
    public ExecutorService getPool() {
        return mPool;
    }

    public void shutDown() {
        mPool.shutdown();
    }

}
