# sp-infra-comp-consul

    1.封装了全局ConsulClient对象可以使用；
    2.提供了ConsulWatcher用于监听Consul KV配置项的变更,以便可以同步到本地缓存；
    3.提供了GenericRunnable<T>用于开启新线程对Consul配置做更新操作。
    
## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-consul</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 使用全局ConsulClient对象
```java
public class Test {
    private static final KeyValueClient consulClient = CommonConsulClient.getDefaultConsulClient();
    
    //consulClient操作
}
```

## 使用ConsulWatcher监听配置变更
### 代码示例(doWatchLossSwitch()方法实现中使用)

```java
package com.yonghui.arch.whole.link.range.loss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yonghui.arch.consul.watcher.ConsulWatcher;
import com.yonghui.arch.core.watcher.DefaultThreadFactory;
import com.yonghui.arch.core.watcher.GenericRunnable;
import com.yonghui.arch.logger.AL;
import com.yonghui.arch.utils.log.ThrowableStackTraceLogUtil;
import com.yonghui.arch.whole.link.range.common.Constants;
import com.yonghui.arch.whole.link.range.log.LogType;
import com.yonghui.arch.whole.link.range.utils.EnvUtils;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** @author sirius */
public class LossActuator {

    public static final LossActuator INSTANCE =
            new LossActuator();
    private static final String THREAD_PRE = "whole-link-loss-ds-update-";
    private static final String CANARY_FEATURE = EnvUtils.isCanaried() ? EnvUtils.getCanaryFeature() : Constants.DEFAULT_FEATURE_VAL;
    private static final String KEY_TEMP1 = "whole-link/loss/%s/%s";
    private static final String KEY_TEMP2 = "whole-link/loss/%s/%s/%s";
    private static final String CONFIG = "config";
    private static final String GLOBAL = "global";
    private String applicationName;
    private ScheduledThreadPoolExecutor scheduledAppRefreshGlobalConfig =
            new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory(THREAD_PRE + GLOBAL + CONFIG));
    private ScheduledThreadPoolExecutor scheduledAppRefreshLossSwitchConfig =
            new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory(THREAD_PRE + CONFIG));
    private static volatile boolean simpleWorker = false;
    public static volatile int DELAY_TIME = 5;


    public void doActuate(String applicationName) {
        this.applicationName = applicationName;
        synchronized (LossActuator.class) {
            if (simpleWorker) {
                AL.infox(LogType.LT, "The Simple Loss UpdateAction has been started ");
                return;
            }
            doWatchGlobalConfig();
            doWatchLossSwitch();
            simpleWorker = true;
        }
    }

    private void doWatchGlobalConfig() {

        AL.infox(LogType.LT, "Loss GlobalConfigWatcher is starting ");

        GenericRunnable<Configuration> runnable =
                new GenericRunnable<Configuration>(
                        String.format(KEY_TEMP1, GLOBAL, CONFIG),
                        "LossActuator-Global",
                        ConsulWatcher.INSTANCE,
                        source -> JSON.parseObject(source, Configuration.class),
                        cvrtedSource -> {
                            // done update GlobalConfig by Configuration
                            LossManager.GLOBAL_ON = cvrtedSource.isOn();
                        }){};

        // FIRSTLY PULL
        try {
            runnable.run();
        } catch (Exception e) {
            ThrowableStackTraceLogUtil.archLogError(LogType.LT, e);
        }

        // WATCHER LOGIC
        new Thread() {
            @Override
            public void run() {

                while (true) {
                    try {
                        scheduledAppRefreshGlobalConfig
                                .schedule(runnable, DELAY_TIME, TimeUnit.SECONDS)
                                .get();
                    } catch (Exception e) {
                        ThrowableStackTraceLogUtil.archLogError(LogType.LT, e);
                    }
                }
            }
        }.start();

        AL.infox(LogType.LT, "Loss GlobalConfigWatcher is started ");
    }

    private void doWatchLossSwitch() {

        AL.infox(LogType.LT, "LossSwitch Watcher is starting ");
        GenericRunnable runnable =
                new GenericRunnable<Configuration>(
                        String.format(KEY_TEMP2, applicationName, CANARY_FEATURE, CONFIG),
                        "LossActuator-APP",
                        ConsulWatcher.INSTANCE,
                        source -> JSON.parseObject(source, new TypeReference <Configuration>() {
                        }),
                        cvrtedSource -> {
                            LossManager.ON = cvrtedSource.isOn();
                        }) {};

        // FIRSTLY PULL
        try {
            runnable.run();
        } catch (Exception e) {
            ThrowableStackTraceLogUtil.archLogError(LogType.LT, e);
        }

        // WATCHER LOGIC
        new Thread() {
            @Override
            public void run() {

                while (true) {
                    try {
                        scheduledAppRefreshLossSwitchConfig
                                .schedule(runnable, DELAY_TIME, TimeUnit.SECONDS)
                                .get();
                    } catch (Exception e) {
                        ThrowableStackTraceLogUtil.archLogError(LogType.LT, e);
                    }
                }
            }
        }.start();

        AL.infox(LogType.LT, "LossSwitch Watcher is started ");
    }




}


```




