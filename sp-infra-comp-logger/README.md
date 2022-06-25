# sp-infra-comp-logger

    1.提供LogBizDefine业务日志埋点注解,开发人员使用后会拦截入参进行记录日志；
    2.通过CompLogger在sp-infra-comp-*内部记录特定格式的日志，通过ThrowableStackLogger减少堆栈层次字符串。
    
## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-logger</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 业务日志Logger
### ServiceImpl方法使用注解

```java
import LogBizDefine;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogBizDefine(logBizType = "SynStatisticsInfoEsJob", handler = HemAssistantDigestLogHandler.class)
public class TestUseBizLogger {
    public String test(String a, String b, Integer c) {
        //业务代码...
        return "ok";
    }
}

```
#### 注：
1. logBizType可自定义,实现ILogBizType接口即可；
2. handler也可以自定义，实现ILogBizHandler接口即可。

#### 输出结果
通过BizLogger.logBizInfo()会自动打印LOG_FORMAT = "{}|{}|{}|{}|{}|{}|{}|{}|{}|{}"的日志，输出的属性请参考LogResultModel。

## 组件日志Logger
### 使用CompLogger

```java
import CompLogger;

public class TestUseCompLogger {
    public String testCompLogger() {
        CompLogger.info("my", "test here!");
    }
}
```
#### 输出结果
```json
{"datetime":"{}","type":"{}","timestamp":"{}","level":"{}","message":"{}"}
```