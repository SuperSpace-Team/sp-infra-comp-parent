# SP Starter

    封装了elasticsearch常用的查询，及复合查询
    
## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-elasticsearch</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 查询操作符枚举
```
GT("gt", ">")
GTE("gte", ">=")
LT("lt", "<")
LTE("lte", "<=")

/**
 * 精确匹配字段查询; 指定查询一个字段对应单个词条
 */
TERM_QUERY("eq", "termQuery"),

/**
 * 精确匹配字段查询(多term查询); 查询一个字段对应多个词条
 */
IN_TERMS("in", "termsQuery"),

/**
 * 当前字段值范围查询
 */
RANGE_QUERY("rangeQuery", "rangeQuery"),

/**
 * SHOULD_IN_TERM 查询
 */
SHOULD_IN_TERM("sin", "shouldInTerm"),

/**
 * IN Wildcard 查询
 */
SHOULD_IN_WILDCARD("shouldInWildcard", "shouldInWildcard"),

/**
 * 匹配非当前值的查询 not in,多个值逗号分割
 */
MUST_NOT_TERM("notIn", "mustNotTerm"),

/**
 * 匹配非当前值的查询 not in,多个值逗号分割
 */
MUST_NOT_WILDCARD("mustNotWildcard", "mustNotWildcard"),

/**
 * 模糊查询
 */
WILDCARD_QUERY("like", "wildcardQuery"),

/**
 * 解析查询
 */
QUERY_STRING_QUERY("queryStringQuery", "queryStringQuery");
```

## 数据源配置
```yaml
elasticSearch:
  # es 集群地址，固定格式： 10.0.0.1:12001,10.0.0.1:12002,10.0.0.1:12003
  hosts: http://10.0.91.249:9200
  # 最大连接数
  maxConnectNum: 3
  # 最大路由连接数
  maxConnectPerRoute: 5
  # 连接超时时间(毫秒)
  connectTimeoutMillis: 60000
  # 连接超时时间(毫秒)
  socketTimeoutMillis: 60000
  # 获取连接的超时时间(毫秒)
  connectionRequestTimeoutMillis: 30000
  # 请求超时时间(毫秒)
  maxRetryTimeoutMillis: 30000 
  # 最大返回结果数据量 (最大值10000)
  maxResultCount: 10000 
  # 是否打印DSL
  showDsl: false
```

## 请求参数

> ElasticQueryDto
```java
    // 分页查询
    private ElasticQueryPageDto queryPageDto;
    
    // 排序的字段 key: 字段，value: 排序方式
    private Map<String, String> sidx;
    
    // 是否先检查index是否存在
    private boolean checkIndex = false;
    
    // 查询的index
    private String[] indices;
    
    // 查询的字段
    private String[] includeFields;
    
    // 排除的字段
    private String[] excludeFields;
    
    // 更多查询
    private List<ElasticQueryOperatorDto> operators;
```

> ElasticQueryOperatorDto

    如：查询字段a
    a < 10
    a < 10 AND a != 5

<br>

    // 查询字段
    private String key;
    
    // 操作符号 eq, gt, gte, lt, lte, like, in, not in
    private String operate;
    
    // 查询值,operator 对应值
    private String value;
    
    // 操作符号 lt, lte
    private String operateByRight;
    
    // 查询值 operatorByRight 对应值
    private String valueByRight;

## 0.0.3新增searchAfter用法
### 使用场景
- 利用searchAfter可以解决查询的数据量超出index默认返回数量1万条的场景
- 使用searchAfter的时候要与排序结合使用（推荐使用"_id"排序）
- 下一次的searchAfter参数是上次查询结果返回的SearchAfterResult.searchAfter
- 第一次查询，searchAfter可以为空字符串，但不能为null
- 如果满足条件的结果有三万条，则一共需要查询三次，searchAfter相当于分页参数
- 经过测试排序后的查询比默认查询的方式速度更快

### 代码示例

```java

package com.yonghui.itwork.v0730.v2.api.controller.v1;

import com.yonghui.core.utils.R;
import com.yonghui.core.utils.SpPageBase;
import ElasticQueryDto;
import ElasticQueryOperatorDto;
import ElasticQueryPageDto;
import ConstantsUtils;
import ElasticSearchRequestUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ConstantsUtils.QueryTypeEnum.IN_TERMS;

@RestController
@RequestMapping(value = "/v1/es")
@Slf4j
public class ElasticsearchController {

    @Resource
    private ElasticSearchRequestUtils elasticSearchRequestUtils;

    @GetMapping("/createIndex")
    @ApiOperation(value = "创建索引")
    public R createIndex(@RequestParam String indexName) {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        // 设置分片数，副本数
        request.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );
        // 创建索引时创建文档类型映射
        request.mapping(
                "type_" + indexName,
                "{\n" +
                        "            \"properties\":{\n" +
                        "                \"test_code\":{\n" +
                        "                    \"type\":\"text\",\n" +
                        "                    \"fields\":{\n" +
                        "                        \"keyword\":{\n" +
                        "                            \"type\":\"keyword\",\n" +
                        "                            \"ignore_above\":256\n" +
                        "                        }\n" +
                        "                    }\n" +
                        "                },\n" +
                        "                \"test_name\":{\n" +
                        "                    \"type\":\"text\",\n" +
                        "                    \"fields\":{\n" +
                        "                        \"keyword\":{\n" +
                        "                            \"type\":\"keyword\",\n" +
                        "                            \"ignore_above\":256\n" +
                        "                        }\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            }\n" +
                        "}",
                XContentType.JSON
        );

        // 为索引设置一个别名
        request.alias(
                new Alias("as_" + indexName)
        );
        boolean result = elasticSearchRequestUtils.createIndex(request);
        return R.success(result);
    }

    /**
     * 精确匹配字段查询; 指定查询一个字段对应单个词条
     * TERM_QUERY("eq", "termQuery"),
     * 精确匹配字段查询(多term查询); 查询一个字段对应多个词条
     * IN_TERMS("in", "termsQuery"),
     * 当前字段值范围查询
     * RANGE_QUERY("rangeQuery", "rangeQuery"),
     * SHOULD_IN_TERM 查询
     * SHOULD_IN_TERM("sin", "shouldInTerm"),
     * IN Wildcard 查询
     * SHOULD_IN_WILDCARD("shouldInWildcard", "shouldInWildcard"),
     * 匹配非当前值的查询 not in,多个值逗号分割
     * MUST_NOT_TERM("notIn", "mustNotTerm"),
     * 匹配非当前值的查询 not in,多个值逗号分割
     * MUST_NOT_WILDCARD("mustNotWildcard", "mustNotWildcard"),
     * 模糊查询
     * WILDCARD_QUERY("like", "wildcardQuery"),
     * 解析查询
     * QUERY_STRING_QUERY("queryStringQuery", "queryStringQuery");
     */
    @PostMapping("/search")
    @ApiOperation(value = "数据查询")
    public R search(@RequestBody String[] indexNames) {
        ElasticQueryDto queryDto = new ElasticQueryDto();
        queryDto.setIndices(indexNames);
        queryDto.setIncludeFields(new String[]{"test_code", "test_name", "_index"});

        // 构造查询条件
        List<ElasticQueryOperatorDto> operatorDtos = new ArrayList<>();
        ElasticQueryOperatorDto operatorDto1 = new ElasticQueryOperatorDto();
        operatorDto1.setKey("test_code");
        operatorDto1.setOperate(IN_TERMS.getValue());
        operatorDto1.setValue(new String[]{"test_code_1", "test_code_2", "test_code_3", "test_code_4", "test_code_5"});
        operatorDtos.add(operatorDto1);

        ElasticQueryOperatorDto operatorDto2 = new ElasticQueryOperatorDto();
        operatorDto2.setKey("test_name");
        operatorDto2.setOperate(ConstantsUtils.QueryTypeEnum.MUST_NOT_TERM.getValue());
        operatorDto2.setValue(new String[]{"test_name_3"});
        operatorDtos.add(operatorDto2);

        queryDto.setOperators(operatorDtos);

        // list查询
        List<Map<String, Object>> result = elasticSearchRequestUtils.search(queryDto);

        // 原始查询结果
        SearchResponse searchResponse = elasticSearchRequestUtils.searchResponse(queryDto);

        // 分页查询
        ElasticQueryPageDto pageDto = new ElasticQueryPageDto();
        pageDto.setLimit(5);
        queryDto.setQueryPageDto(pageDto);
        SpPageBase page = elasticSearchRequestUtils.searchPage(queryDto);

        Map map = new HashMap();
        map.put("searchResponse", searchResponse.getHits());
        map.put("list", result);
        map.put("page", page);
        return R.success(map);
    }

    /**
     * 批量插入
     * 1、bulk里为什么不支持get?
     * 　批量操作，里面放get操作，没啥用！官方也不支持。
     * 2、create 和index的区别
     * 　　如果数据存在，使用create操作失败，会提示文档已经存在，使用index则可以成功执行。
     * 3、bulk一次最大处理多少数据量？
     * 　　bulk会把将要处理的数据载入内存中，所以数据量是有限制的，最佳的数据量不是一个确定的数值，它取决于你的硬件，你的文档大小以及复杂性，你的索引以及搜索的负载。
     * 　　一般建议是1000-5000个文档，如果你的文档很大，可以适当减少队列，大小建议是5-15MB，默认不能超过100M，可以在es的配置文件（即$ES_HOME下的config下的elasticsearch.yml）中。
     *
     * @return
     */
    @GetMapping("/insertBulkASync")
    @ApiOperation(value = "异步批量插入")
    public R insertBulkASync(@RequestParam String indexName) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("test_code", "test_code_" + i);
            m.put("test_name", "test_name_" + i);
            data.add(m);
        }
        boolean result = elasticSearchRequestUtils.insertBulkASync(indexName, "type_" + indexName, data);
        return R.success(result);
    }

    @GetMapping("/insertBulkSync")
    @ApiOperation(value = "同步批量插入")
    public R insertBulkSync(@RequestParam String indexName) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("test_code", "test_code_b_" + i);
            m.put("test_name", "test_name_b_" + i);
            data.add(m);
        }

        long startTime = System.currentTimeMillis();
        log.info("开始时间：" + startTime);

        boolean result = elasticSearchRequestUtils.insertBulkSync(indexName, "type_" + indexName, data);

        long endTime = System.currentTimeMillis();
        log.info("结束时间：" + endTime);
        float excTime = (float) (endTime - startTime) / 1000;
        log.info("执行时间：" + excTime + "s");

        return R.success(result);
    }

    @GetMapping("/insertOne")
    @ApiOperation(value = "单条插入(同步)返回ID")
    public R insertOne(@RequestParam String indexName) {
        long startTime = System.currentTimeMillis();
        log.info("开始时间：" + startTime);

        Map<String, Object> m = new HashMap<>();
        m.put("test_code", "test_code_b_" + startTime);
        m.put("test_name", "test_name_b_" + startTime);

        String id = elasticSearchRequestUtils.insertOne(indexName, "type_" + indexName, m);

        long endTime = System.currentTimeMillis();
        log.info("结束时间：" + endTime);
        float excTime = (float) (endTime - startTime) / 1000;
        log.info("执行时间：" + excTime + "s");

        return R.success(id);
    }

    @GetMapping("/insertOneASync")
    @ApiOperation(value = "异步插入")
    public R insertOneASync(@RequestParam String indexName) {
        long startTime = System.currentTimeMillis();
        log.info("开始时间：" + startTime);

        Map<String, Object> m = new HashMap<>();
        m.put("test_code", "test_code_b_" + startTime);
        m.put("test_name", "test_name_b_" + startTime);

        boolean reslut = elasticSearchRequestUtils.insertOneASync(indexName, "type_" + indexName, m);

        long endTime = System.currentTimeMillis();
        log.info("结束时间：" + endTime);
        float excTime = (float) (endTime - startTime) / 1000;
        log.info("执行时间：" + excTime + "s");

        return R.success(reslut);
    }

    @GetMapping("/upsertOneSyncById")
    @ApiOperation(value = "单条更新(同步)")
    public R upsertOneSyncById(@RequestParam String indexName, @RequestParam String id) {
        long startTime = System.currentTimeMillis();
        log.info("开始时间：" + startTime);

        Map<String, Object> m = new HashMap<>();
        m.put("test_code", "test_code_b_" + startTime);
        m.put("test_name", "test_name_b_" + startTime);

        boolean reslut = elasticSearchRequestUtils.upsertOneSyncById(indexName, "type_" + indexName, m, id);

        long endTime = System.currentTimeMillis();
        log.info("结束时间：" + endTime);
        float excTime = (float) (endTime - startTime) / 1000;
        log.info("执行时间：" + excTime + "s");

        return R.success(reslut);
    }

    @GetMapping("/upsertOneForASyncResponse")
    @ApiOperation(value = "异步单条更新")
    public R upsertOneForASyncResponse(@RequestParam String indexName, @RequestParam String id) {
        long startTime = System.currentTimeMillis();
        log.info("开始时间：" + startTime);

        Map<String, Object> m = new HashMap<>();
        m.put("test_code", "test_code_b_" + startTime);
        m.put("test_name", "test_name_b_" + startTime);

        boolean reslut = elasticSearchRequestUtils.upsertOneForASyncResponse(indexName, "type_" + indexName, m, id);

        long endTime = System.currentTimeMillis();
        log.info("结束时间：" + endTime);
        float excTime = (float) (endTime - startTime) / 1000;
        log.info("执行时间：" + excTime + "s");

        return R.success(reslut);
    }

}

```




