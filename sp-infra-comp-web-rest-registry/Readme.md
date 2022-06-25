# sp-infra-comp-sc-rest-registry

    通过扫描MVC @RequestMapping相关注解对API元数据信息Metadata接口进行上报

## 引用依赖

如果项目使用这个工具包，它只需要引入以下maven的依赖项。

```xml
<dependency>
    <groupId>com.sp.infra.comp</groupId>
    <artifactId>sp-infra-comp-sc-rest-registry</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### API接口上报
1. 扫描代码工程的所有MVC @RequestMapping相关注解，上报到Consul KV的【api-metadata】目录下，以各个应用名称为子目录定义；
2. API元数据属性存储类MetadataStore定义了上报的内容：commitId和buildTime为当前git提交的版本记录数据,appName为应用名称(默认取spring.application.name)：
#### 前提：在POM.xml需依赖git maven插件:
   ```xml
      <plugin>
        <groupId>pl.project13.maven</groupId>
        <artifactId>git-commit-id-plugin</artifactId>
        <version>2.2.4</version>
      </plugin>
```
### 功能说明
该上报Consul KV功能可结合老限流SDK使用。