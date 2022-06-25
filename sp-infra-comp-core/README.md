# sp-infra-comp-core

    1.提供了ApplicationContextHelper存取Spring默认容器对象；
    2.提供了ConvertorI、ConvertHelper和BeanCopyUtilCallBack工具类用于做对象类型转换；
    3.提供了FieldNameUtil和FieldGetter以便利用SerializedLambda获取方法名称；
    4.提供validator相关的POJO属性校验器Assert和业务&异常错误校验器BizValidator。
    
## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 使用ApplicationContextHelper

```java
import ApplicationContextHelper;

public class Test {
    public void testGetContext() {
        ApplicationContextHelper.getContext();
    }

    public void testGetBeanFactory() {
        ApplicationContextHelper.getSpringFactory();
    }
}
```

## 使用ConvertHelper对象类型转换工具类
### 数据库实体对象转换为DTO对象

```java
@Component
public class DeployDetailServiceImpl implements DeployDetailService {
    @Autowired
    private DeployDetailRepository deployDetailRepository;

    @Override
    public List<DevopsEnvPodDTO> getPods(Long instanceId) {
        return ConvertHelper.convertList(deployDetailRepository.getPods(instanceId), DevopsEnvPodDTO.class);
    }
}

```

## 通过利用SerializedLambda获取方法名称
```java


```




