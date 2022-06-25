# sp-infra-comp-rocketmq

    1.提供了ApplicationContextHelper存取Spring默认容器对象；
    2.提供了ConvertorI、ConvertHelper和BeanCopyUtilCallBack工具类用于做对象类型转换；
    3.提供了FieldNameUtil和FieldGetter以便利用SerializedLambda获取方法名称；
    4.提供validator相关的POJO属性校验器Assert和业务&异常错误校验器BizValidator。

## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-rocketmq</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 消费者使用
如果一个工程如果评估下来多个消费者的共用一个线程池压力不大，则可以配置如下：
### [示例]
```yaml
rocketmq:
  namesrvAddr: 10.251.76.2:9876;10.251.76.3:9876;10.251.76.4:9876;10.251.76.5:9876
  producer:
    groupName: dubbo-mt-adapter-producer-group-local
    maxMessageSize: 131072
    sendMsgTimeout: 10000
  consumer:
    groupName: dubbo-mt-adapter-consumer-group-local
    consumeThreadMin: 20
    consumeThreadMax: 64
    topicAndTagInfos:
      - topic: mytopic
        tag: mytag
        processorHandle: demoProcessorImpl
      - topic: mytopic2
        tag: mytag2
        processorHandle: demo2ProcessorImpl
      - topic: order_mt
        tag: orderCreate||orderCancel||orderConfirm||orderFullRefund||orderPartRefund||orderPickFinish||orderDeliveryStatus||orderFinish||privateDegrade
        processorHandle: mtPushOrderProcessorImpl
      - topic: retail_mt
        tag: addPushProduct||updatePushProduct||getPushTask
        processorHandle: mtPushProductsProcessorImpl
      - topic: thirdPartyMtSyncTopic
        tag: mtSyncTag
        processorHandle: mtNotificationSyncProcessor
```

如果一个项目中多消费压力比较大，如果其中一个消费者消费比较慢就会影响其它消费的消费，影响业务，这时就需要采用多消费者的配置，配置如下：
### [示例]
```yaml
rocketmq:
  multi:
    producer:
      producers:
        - nameAddr: 10.251.76.2:9876;10.251.76.3:9876;10.251.76.4:9876;10.251.76.5:9876
          groupName: dubbo-mt-adapter-producer-group-local1
          instanceName: producer1
        - nameAddr: 10.251.76.2:9876;10.251.76.3:9876;10.251.76.4:9876;10.251.76.5:9876
          groupName: dubbo-mt-adapter-producer-group-local2
          instanceName: producer2
    consumer:
      consumers:
      - nameAddr: 10.251.76.2:9876;10.251.76.3:9876;10.251.76.4:9876;10.251.76.5:9876
        groupName: dubbo-mt-adapter-consumer-group-local1
        instanceName: consumer1
        consumeThreadMin: 20
        consumeThreadMax: 64
        topicAndTagInfos:
          - topic: testMulti1
            tag: t1
            processorHandle: demoProcessorImpl
      - nameAddr: 10.251.76.2:9876;10.251.76.3:9876;10.251.76.4:9876;10.251.76.5:9876
        groupName: dubbo-mt-adapter-consumer-group-local1
        instanceName: consumer2
        consumeThreadMin: 20
        consumeThreadMax: 64
        topicAndTagInfos:
          - topic: testMulti1
            tag: t1
            processorHandle: demoProcessorImpl2
```
注意：instanceName必须配置，不然线程池隔离无效 ，instanceName建议{groupName}-consumer{数字}。另外consumeMode用于配置消费者模式：#1 非顺序 2 顺序消费  3 广播

### 1、消费代码示例
```java
@Service(value = "promoteProcessorImpl")
@Slf4j
public class PromoteProcessorImpl implements MessageProcessor<String> {
 
    @Resource
    private LogJdPushMsgMapper logJdPushMsgMapper;
 
    @Resource
    private JdPushContext jdPushContext;
 
    @Override
    public boolean handleMessage(String configs) {
        boolean handleFlag = true;
 
        TransMsgVO transMsgVO = new TransMsgVO(configs).invoke();
        Long id = transMsgVO.getId();
        String tags = transMsgVO.getTags();
        String message = transMsgVO.getConfigs();
 
        LogJdPushMsg logJdPushMsg = logJdPushMsgMapper.selectByPrimaryKey(id);
        if (logJdPushMsg.getDealException().equals(BizConstant.TWO)){
            log.info("促销消息" + configs + "已消费，无需重复处理");
            return true;
        }
 
        try {
 
            //消息处理
            jdPushContext.doService(tags,message);
            //更新处理结果为完成
            logJdPushMsgMapper.updateMsgWithExceptionById(id, BizConstant.TWO);
 
        } catch (JdAPIException e){
            log.error(ExceptionUtils.getFullStackTrace(e));
 
            //自定义异常为京东到家API接口访问异常，返回MQ false，进行重试
            handleFlag = false;
        } catch (Exception e) {
            handleFlag = false;
 
            log.error(ExceptionUtils.getFullStackTrace(e));
 
            if (logJdPushMsg.getDealException().equals(BizConstant.ONE)){
                //第二次处理消息异常
                return true;
            }
 
            logJdPushMsgMapper.updateMsgWithExceptionById(id, BizConstant.ONE);
        }
        return handleFlag;
    }
 
    @Override
    public Class<String> getClazz() {
        return String.class;
    }
 
    @Override
    public String transferMessage(String message) {
        return message;
    }
}

```

### 2. 顺序消费只需要配置修改一下：把groupName属性改成orderlyGroupName，把topicAndTagInfos改成orderlyTopicAndTagInfos即可
```yaml
rocketmq:
  namesrvAddr: 10.251.76.2:9876;10.251.76.3:9876;10.251.76.4:9876;10.251.76.5:9876
  producer:
    groupName: dubbo-ele-adapter-producer-group
    maxMessageSize: 131072
    sendMsgTimeout: 10000
  consumer:
    groupName: dubbo-ele-adapter-consumer-group
    consumeThreadMin: 20
    consumeThreadMax: 64
    topicAndTagInfos:
      - topic: ele-pick
        tag: order.pick.finish
        processorHandle: orderPickFinishConsumer
      - topic: ele-confirm
        tag: order.confirm
        processorHandle: orderConfirmConsumer
      - topic: thirdPartyEleSyncTopic
        tag: eleSyncTag
        processorHandle: eleNotifactionSyncProcessor
    orderlyGroupName: sp-sod-ele-adapter-consumer-orderly-group
    orderlyTopicAndTagInfos:
      - topic: ele-order
        tag: order.create||order.status.push||order.user.cancel||order.partrefund.push||order.deliveryStatus.push
        processorHandle: orderMessageProcessor
```

## 生产者使用
一个工程如果评估下来多个消费者的共用一个线程池压力不大，则可以配置参考消费配置
代码示例：
### [示例]
```java
@Slf4j
@Service
public class MtPushServiceImpl implements MtPushService {
    @Resource
    private RocketMQProducerUtil producerUtil;
 
    @Resource
    private LogMtPushMsgMapper logMtPushMsgMapper;
 
    @Transactional
    @Override
    public void dealMtPushMsg(String topic, String tags, String key,String name, JSONObject jsonObject) {
        LogMtPushMsg logMtPushMsg = new LogMtPushMsg();
        logMtPushMsg.setMsgTopic(topic);
        logMtPushMsg.setMsgTags(tags);
        logMtPushMsg.setMsgName(name);
        logMtPushMsg.setMsgContent(jsonObject.toJSONString());
        int count = logMtPushMsgMapper.insertSelective(logMtPushMsg);
        if (count != 1){
            throw new CommonException(BizError.MT_MSG_DEAL_EXCEPTION);
        }
        jsonObject.put(BizConstant.MSG_ID,logMtPushMsg.getId());
        jsonObject.put(BizConstant.MSG_TAGS,tags);
        producerUtil.sendMessage(topic, tags, key, jsonObject.toJSONString());
    }
 
    @Transactional
    @Override
    public String dealMtPushMsg(MtPushMsgDto mtPushMsgDto) {
        LogMtPushMsg logMtPushMsg = transPushMsg(mtPushMsgDto);
        int count = logMtPushMsgMapper.insertSelective(logMtPushMsg);
        if (count != 1){
            throw new CommonException(BizError.MT_MSG_DEAL_EXCEPTION);
        }
        JSONObject jsonObject = mtPushMsgDto.getMsgContent();
        jsonObject.put(BizConstant.MSG_ID,logMtPushMsg.getId());
        jsonObject.put(BizConstant.MSG_TAGS,mtPushMsgDto.getMsgTags());
        producerUtil.sendMessage(mtPushMsgDto.getMsgTopic(), mtPushMsgDto.getMsgTags(), mtPushMsgDto.getKey(), jsonObject.toJSONString());
        return "";
    }
 
    private LogMtPushMsg transPushMsg(MtPushMsgDto mtPushMsgDto) {
        LogMtPushMsg logMtPushMsg = new LogMtPushMsg();
        logMtPushMsg.setMsgTopic(mtPushMsgDto.getMsgTopic());
        logMtPushMsg.setMsgTags(mtPushMsgDto.getMsgTags());
        logMtPushMsg.setMsgName(mtPushMsgDto.getMsgName());
        logMtPushMsg.setMsgContent(mtPushMsgDto.getMsgContent().toJSONString());
        return logMtPushMsg;
    }
}
```

### 如果不同的生产者共用一个线程池有问题的话，可以配置多生产隔离配置：
```yaml
rocketmq:
  multi:
    producer:
      producers:
        - nameAddr: 10.251.76.2:9876;10.251.76.3:9876;10.251.76.4:9876;10.251.76.5:9876
          groupName: dubbo-mt-adapter-producer-group-local1
          instanceName: producer1
        - nameAddr: 10.251.76.2:9876;10.251.76.3:9876;10.251.76.4:9876;10.251.76.5:9876
          groupName: dubbo-mt-adapter-producer-group-local2
          instanceName: producer2
```

### 示例代码：
```java
new RocketMultiMQProducerUtil("producer1").sendMessage("testMulti1", "t1","key1", JSON.toJSONString(demoBO));
            new RocketMultiMQProducerUtil("producer2").sendMessage("testMulti2", "t2","key1",JSON.toJSONString(demoBO));
```
说明：instanceName必须设置 