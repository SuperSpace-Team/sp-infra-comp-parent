package com.sp.infra.comp.rest.registry.meta;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 元数据加载器
 */
public class MetadataLoader {
  private static MetadataLoader LOADER = new MetadataLoader();
  private static final String Separator = "|";

  private Map<String, Metadata> registryMetadatas = new TreeMap<>();
  private Map<String, List<Metadata>> subscriberMetadatas = new ConcurrentHashMap<>();

  private MetadataLoader() {}

  public static MetadataLoader getInstance() {
    return LOADER;
  }

  public void addRegistryMetadata(String path, String method) {
    Metadata bean = new Metadata(path, method);
    getInstance().getRegistryMetadatas().put(buildRegistryKey(method, path), bean);
  }

  public Map<String, Metadata> getRegistryMetadatas() {
    return registryMetadatas;
  }

  public Map<String, List<Metadata>> getSubscriberMetadatas() {
    return subscriberMetadatas;
  }

  public List<Metadata> getSubscriberMetadatasByServerName(String serverName) {
    return subscriberMetadatas == null || subscriberMetadatas.isEmpty()
        ? null
        : subscriberMetadatas.get(serverName);
  }

  public void addSubscriberMetadatasByServerName(String severName, List<Metadata> metadatas) {
    subscriberMetadatas.put(severName, metadatas);
  }

  public static final String buildRegistryKey(String method, String path) {
    return method + Separator + path;
  }
}
