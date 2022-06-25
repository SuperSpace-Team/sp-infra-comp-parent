package com.sp.infra.comp.rest.registry.config;


import com.sp.infra.comp.rest.registry.meta.Metadata;

import java.io.Serializable;
import java.util.List;

/**
 * API元数据属性存储类
 */
public class MetadataStore implements Serializable {
  private static final long serialVersionUID = 5100548448741431957L;
  public static final String PREFIX_KEY = "api-metadata/";

  private String commitId;
  private Long buildTime;

  private List<Metadata> metadatas;

  public String getCommitId() {
    return commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  public Long getBuildTime() {
    return buildTime;
  }

  public void setBuildTime(Long buildTime) {
    this.buildTime = buildTime;
  }

  public List <Metadata> getMetadatas() {
    return metadatas;
  }

  public void setMetadatas(List <Metadata> metadatas) {
    this.metadatas = metadatas;
  }
}
