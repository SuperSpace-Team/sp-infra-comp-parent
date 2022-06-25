# sp-infra-comp-feign

    1.提供自定义的FeignRequestMappingHandlerMapping对带有@OpenAPI注解的API接口信息定义读取（限定接口在网关上不被过滤的条件：来源域名、用户权限）；
    2.提供OpenAPI注解用于加在Controller层的API接口定义上，以便网关读取后做接口鉴权--产品白名单(ProductType枚举定义)和权限编码白名单；
    3.提供LowerCasePropertyNamingStrategy使得JSON请求参数均转换为小写字母(若需要此功能需配置:http.key-lower-case.enabled=true)；
    4.提供SpWebResultErrorAttributes和SpErrorAttributes分别用于在Feign服务间调用时对响应体的异常的处理,放入code,msg和exception_detail额外附加的参数，以便调用方可以得到下游异常信息进行分析；
    5.提供ErrorDecoder对异常反序列化失败的情况包装友好返回；
    6.提供OriginServiceRequestInterceptor用于添加在Feign服务间调用前添加请求头X-SP-FeignOrigin；
    7.提供RequestOriginFilter作为请求来源过滤器判断来源服务是否在定义的允许服务名中（OriginService注解中配置的服务）
    8.OpenApiLoadData和OpenApiRegister用于在业务网关启动时从consul KV配置里加载所有带有权限配置的@OpenAPI的接口到网关本地缓存。
## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-feign</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 自定义Excel属性
### 1）先自定义注解
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportField {
    String fieldName();

    ExcelModel.ExcelType excelType() default ExcelModel.ExcelType.OTHER;

    int sort();
}

```
### 2）注解加到需要输出的Excel POJO类定义上
```java
@Setter
@Getter
@ToString(callSuper = true)
public class ListOrderSaleDetailSearchResponseVo implements Serializable {

    private static final long serialVersionUID = 5951847418830531645L;

    @ApiModelProperty("销售日期/订单创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ExportField(fieldName = "销售日期", sort = 1, excelType = ExcelModel.ExcelType.DATE)
    private Date orderAt;

    @ApiModelProperty("门店id/订单发货门店id")
    @ExportField(fieldName = "门店id", sort = 2)
    private String erpShopId;
}
```

