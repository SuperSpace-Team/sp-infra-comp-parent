package com.sp.infra.comp.dubbo.policy;

import com.alibaba.dubbo.common.URL;
import com.sp.infra.comp.dubbo.utils.JVMUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description: 自定义拒绝策略
 * @author: luchao
 * @date: Created in 2/15/22 7:01 PM
 */
@Slf4j
public class AbortPolicyWithReport extends ThreadPoolExecutor.AbortPolicy {
    public static final String DUMP_DIRECTORY = "dump.directory";

    private final String threadName;

    private final URL url;

    private static volatile long lastPrintTime = 0;

    private Semaphore guard = new Semaphore(1);

    public AbortPolicyWithReport(String threadName, URL url){
        this.threadName = threadName;
        this.url = url;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        String msg = String.format("Thread pool is EXHAUSTED!" +
                        " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d)," +
                        " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s), in %s://%s:%d!",
                threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating(),
                url.getProtocol(), url.getIp(), url.getPort());
        log.warn(msg);
        dumpJStack();
        throw new RejectedExecutionException(msg);

    }

    private void dumpJStack() {
        long now = System.currentTimeMillis();

        //十分钟打一次
        if (now - lastPrintTime < 10 * 60 * 1000) {
            return;
        }

        if (!guard.tryAcquire()) {
            return;
        }

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                String dumpPath = url.getParameter(DUMP_DIRECTORY, System.getProperty("user.home"));

                SimpleDateFormat sdf;
                SimpleDateFormat sdfDir = new SimpleDateFormat("yyyy-MM-dd");

                String OS = System.getProperty("os.name").toLowerCase();

                // window system don't support ":" in file name
                if(OS.contains("win")){
                    sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                }else {
                    sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                }

                String dateStr = sdf.format(new Date());
                String dirStr = sdfDir.format(new Date());
                FileOutputStream jstackStream = null;

                try {
                    String localPath = "/dubbo-response";
                    File fileDir = new File(localPath);
                    if (fileDir.exists() && fileDir.isDirectory()) {
                        // 是目录
                        String destPath = fileDir + File.separator + "dubbo_log_" + dirStr;
                        File dateFileDir = new File(destPath);
                        if (!dateFileDir.exists()) {
                            dateFileDir.mkdir();
                        }
                        dumpPath = destPath;
                    }
                } catch (Exception e) {
                    log.error("AbortPolicyWithReport-run() error:{}, :{}", e, "未成功创建文件目录");
                }

                try {
                    jstackStream = new FileOutputStream(new File(dumpPath, "Dubbo_JStack.log" + "." + dateStr));
                    JVMUtil.jstack(jstackStream);
                } catch (Throwable t) {
                    log.error("Dump jstack error", t);
                } finally {
                    guard.release();
                    if (jstackStream != null) {
                        try {
                            jstackStream.flush();
                            jstackStream.close();
                        } catch (IOException e) {
                        }
                    }
                }

                lastPrintTime = System.currentTimeMillis();
            }
        });
    }
}
