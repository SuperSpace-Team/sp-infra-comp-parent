# sp-infra-comp-excel

    1.提供Excel导出的能力;
    2.提供Excel属性自定义的能力,可自行封装
    
## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-excel</artifactId>
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

