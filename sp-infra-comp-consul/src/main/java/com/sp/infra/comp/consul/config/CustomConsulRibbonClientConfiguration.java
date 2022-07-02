package com.sp.infra.comp.consul.config;

import com.ecwid.consul.v1.ConsulClient;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.ConsulPing;
import org.springframework.cloud.consul.discovery.HealthServiceServerListFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.netflix.client.config.CommonClientConfigKey.DeploymentContextBasedVipAddresses;
import static com.netflix.client.config.CommonClientConfigKey.EnableZoneAffinity;

/**
 * @author 翟永超
 * @create 2018/3/19.
 * @blog http://blog.didispace.com
 */
@Configuration
public class CustomConsulRibbonClientConfiguration {

    @Autowired
    private ConsulClient client;

    // FIXME 有问题
    @Value("${ribbon.client.name}")
    private String serviceId = "client";

    protected static final String VALUE_NOT_SET = "__not__set__";

    protected static final String DEFAULT_NAMESPACE = "ribbon";

    public CustomConsulRibbonClientConfiguration() {
    }

    public CustomConsulRibbonClientConfiguration(String serviceId) {
        this.serviceId = serviceId;
    }

    @Bean
    public ServerList<?> ribbonServerList(IClientConfig config, ConsulDiscoveryProperties properties) {
        YhConsulServerList serverList = new YhConsulServerList(client, properties);
        serverList.initWithNiwsConfig(config);
        return serverList;
    }

    @Bean
    public ServerListFilter<Server> ribbonServerListFilter() {
        return new HealthServiceServerListFilter();
    }

    @Bean
    public IPing ribbonPing() {
        return new ConsulPing();
    }

    @PostConstruct
    public void preprocess() {
        setProp(this.serviceId, DeploymentContextBasedVipAddresses.key(), this.serviceId);
        setProp(this.serviceId, EnableZoneAffinity.key(), "true");
    }

    protected void setProp(String serviceId, String suffix, String value) {
        // how to set the namespace properly?
        String key = getKey(serviceId, suffix);
        DynamicStringProperty property = getProperty(key);
        if (property.get().equals(VALUE_NOT_SET)) {
            ConfigurationManager.getConfigInstance().setProperty(key, value);
        }
    }

    protected DynamicStringProperty getProperty(String key) {
        return DynamicPropertyFactory.getInstance().getStringProperty(key, VALUE_NOT_SET);
    }

    protected String getKey(String serviceId, String suffix) {
        return serviceId + "." + DEFAULT_NAMESPACE + "." + suffix;
    }

}