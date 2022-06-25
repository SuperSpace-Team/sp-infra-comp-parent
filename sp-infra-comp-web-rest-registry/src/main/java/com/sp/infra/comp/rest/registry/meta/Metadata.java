package com.sp.infra.comp.rest.registry.meta;

import java.io.Serializable;

/**
 * API元数据信息
 */
public class Metadata implements Serializable{

  private static final long serialVersionUID = -5577147389148434125L;

  /**
   * 请求URI原始路径
   */
  private String path;

  /**
   * 请求URI转为.的路径
   */
  private String dotPath;

  /**
   * HTTP请求方法
   */
  private String httpMethod;

  public Metadata() {

  }

  public Metadata(String path, String httpMethod) {
    this.path = path;
    this.httpMethod = httpMethod;
    if (path != null && path.length() > 0) {
      String dot = path.replace("/", ".");
      if (dot.startsWith(".")) {
        dot = dot.substring(1);
      }
      this.dotPath = dot;
    }
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getDotPath() {
    return dotPath;
  }

  public void setDotPath(String dotPath) {
    this.dotPath = dotPath;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }
}
