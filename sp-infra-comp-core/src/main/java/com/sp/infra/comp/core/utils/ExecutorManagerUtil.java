package com.sp.infra.comp.core.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 线程池工具类 * <br>
 *
 * @author Wang Chong
 * @Description 1、如果当前运行的线程少于corePoolSize，则创建新线程来执行任务（Waring：执行这一步骤，需要获得全局锁）；
 * <br/>
 * 2、如果运行的线程等于或者多于corePoolSize，则将任务加到阻塞队列 BlockingQueue 中，进行排队操作；
 * <br/>
 * 3、如果无法加入BlockingQueue（队列已经满了），则尝试创建新的线程来处理任务（Waring：执行这一步骤，
 * 也需要获取全局锁）；<br/>
 * 4、尝试失败，即当前运行的线程数超过了maximumPoolSize，任务就会被拒绝，并采取相应的饱和策略（这里采用AbortPolicy策略 会抛出异常）。<br/>
 */
@Slf4j
public class ExecutorManagerUtil {

    /**
     * 当前机器的CPU数
     */
    private final static int CPU_CORE = Runtime.getRuntime().availableProcessors();
    private final static int CORE_POOL_SIZE = CPU_CORE;
    private final static int MAX_POOL_SIZE = CPU_CORE * 4;
    private static volatile ThreadPoolExecutor executor = null;

    static {
        // 初始化
        getExecutorInstanceForIo();
    }

    /**
     * @return
     * @Description 双重检查锁定创建线程池单例
     */
    private static void getExecutorInstanceForIo() {
        if (executor == null) {
            synchronized (ExecutorManagerUtil.class) {
                if (executor == null) {
                    NamedThreadFactory factory = new NamedThreadFactory(Thread.currentThread().getName() + "-raising");
                    executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 60, TimeUnit.SECONDS,
                            new LinkedBlockingDeque<>(3000), factory, new ThreadPoolExecutor.AbortPolicy());
                }
            }
        }
    }

    /**
     * @param commands
     * @param sharding
     * @Description 分片执行任务
     */
    public static <T> void executeShardingTasks(List<FutureTask<T>> commands,
            Integer sharding) throws ExecutionException, InterruptedException {
        List<List<FutureTask<T>>> result = ExecutorManagerUtil.getSharding(commands, sharding);
        for (List<FutureTask<T>> list2 : result) {
            ExecutorManagerUtil.execute(list2);
        }

    }

    /**
     * @param commands
     * @return
     * @Description 利用线程池执行任务
     */
    public static <T> List<T> execute(List<FutureTask<T>> commands) {
        if (CollectionUtils.isEmpty(commands)) {
            return new ArrayList<T>(0);
        }
        List<Future<T>> futures = executeOnly(commands);
        return getResults(futures);
    }

    private static <T> List<T> getResults(List<Future<T>> futures) {
        if (CollectionUtils.isEmpty(futures)) {
            return new ArrayList<>(0);
        }

        List<T> rtn = new ArrayList<T>(futures.size());
        for (Future<T> future : futures) {
            try {
                rtn.add(future.get());
            } catch (InterruptedException e) {
                //if the current thread was interrupted while waiting

            } catch (ExecutionException e) {
                //if the computation threw an exception
            }
        }
        return rtn;
    }

    /**
     * 只执行 不拿到结果
     *
     * @param commands 任务集合
     * @param <T>      任务返回类型
     * @return 用于获取结果的任务集合
     */
    public static <T> List<Future<T>> executeOnly(List<FutureTask<T>> commands) {
        if (CollectionUtils.isEmpty(commands)) {
            return new ArrayList<>(0);
        }

        for (Future<T> future : commands) {
            getExecutor().execute((FutureTask<T>) future);
        }

        List<Future<T>> futures = new ArrayList<>(commands.size());
        futures.addAll(commands);
        return futures;
    }

    /**
     * 支持执行单个任务
     *
     * @param task
     * @date 2017年5月30日 下午8:40:38
     */
    public static <T> void executeSingleTask(Runnable task) {
        getExecutor().execute(task);

    }

    /**
     * @param list
     * @param sharding
     * @return
     * @Description List 分片方法
     */
    public static <T> List<List<T>> getSharding(List<T> list, Integer sharding) {
        List<List<T>> result = new ArrayList<List<T>>();
        int size = list.size();
        int start = 0;
        while (size > start) {
            Integer end = start + sharding;
            List<T> tmp;
            if (size < end) {
                tmp = list.subList(start, size);
            } else {
                tmp = list.subList(start, end);

            }
            result.add(tmp);
            start = end;
        }
        return result;
    }

    public static ExecutorService getExecutor() {
        NamedThreadFactory factory = new NamedThreadFactory(Thread.currentThread().getName() + "-raising");
        executor.setThreadFactory(factory);
        return executor;
    }
}
