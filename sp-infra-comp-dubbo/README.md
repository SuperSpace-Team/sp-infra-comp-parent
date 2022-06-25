# sp-infra-comp-core

    1.提供了Provider端的ExceptionResolveFilter拦截dubbo原生的异常信息,返回抛出的SystemException的RpcResult对象；
    2.提供了自定义的拒绝策略AbortPolicyWithReport并自动每10分钟打一次dump jstack文件到本地目录;
    3.提供DubboProviderTraceFilter和DubboConsumerTraceFilter分别存取MDC日志参数TRACE_ID透传标识。
    
## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-dubbo</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Dubbo Filter的使用

    SPI自动会加载，当发起请求时执行DubboConsumerTraceFilter会生效，当接收请求时执行DubboProviderTraceFilter

## 自定义的拒绝策略AbortPolicyWithReport的使用
    由于是重写了dubbo官方的AbortPolicyWithReport，所以可以在自定义线程池时自动生效。

### 代码示例
```java
package com.alibaba.dubbo.common.threadpool.support.limited;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.threadpool.ThreadPool;
import com.alibaba.dubbo.common.threadpool.support.AbortPolicyWithReport;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 此线程池一直增长，直到上限，增长后不收缩。
 *
 * @author <a href="mailto:gang.lvg@alibaba-inc.com">kimi</a>
 */
public class LimitedThreadPool implements ThreadPool {

    public Executor getExecutor(URL url) {
        String name = url.getParameter(Constants.THREAD_NAME_KEY, Constants.DEFAULT_THREAD_NAME);
        int cores = url.getParameter(Constants.CORE_THREADS_KEY, Constants.DEFAULT_CORE_THREADS);
        int threads = url.getParameter(Constants.THREADS_KEY, Constants.DEFAULT_THREADS);
        int queues = url.getParameter(Constants.QUEUES_KEY, Constants.DEFAULT_QUEUES);
        return new ThreadPoolExecutor(cores, threads, Long.MAX_VALUE, TimeUnit.MILLISECONDS,
                queues == 0 ? new SynchronousQueue<Runnable>() :
                        (queues < 0 ? new LinkedBlockingQueue<Runnable>()
                                : new LinkedBlockingQueue<Runnable>(queues)),
                new NamedThreadFactory(name, true), new AbortPolicyWithReport(name, url));
    }

}

```