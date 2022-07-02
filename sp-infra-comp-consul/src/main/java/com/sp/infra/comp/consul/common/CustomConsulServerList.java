package com.sp.infra.comp.consul.common;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.coordinate.model.Datacenter;
import com.ecwid.consul.v1.health.model.HealthService;
import com.netflix.client.config.IClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.ConsulServer;
import org.springframework.cloud.consul.discovery.ConsulServerList;

import java.util.Collections;
import java.util.List;

/**
 * 自定义Consul服务列表操作
 * @author luchao
 * @create 2022/05/31
 */
@Slf4j
public class CustomConsulServerList extends ConsulServerList {

    private String serfDataCenter;

    public CustomConsulServerList(ConsulClient client, ConsulDiscoveryProperties properties) {
        super(client, properties);
        this.serfDataCenter = getClient().getAgentSelf().getValue().getConfig().getDatacenter();
        log.info("Serf DataCenter : " + this.serfDataCenter);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        super.initWithNiwsConfig(clientConfig);
    }

    @Override
    public List<ConsulServer> getInitialListOfServers() {
        return getServers();
    }

    @Override
    public List<ConsulServer> getUpdatedListOfServers() {
        return getServers();
    }

    private List<ConsulServer> getServers() {
        if (getClient() == null) {
            return Collections.emptyList();
        }

        // 当前dc的实例
        String tag = getTag(); // null is ok
        Response<List<HealthService>> response = getClient().getHealthServices(
                getServiceId(), tag, getProperties().isQueryPassing(),
                QueryParams.DEFAULT, getProperties().getAclToken());
        if (response.getValue() != null && !response.getValue().isEmpty()) {
            log.debug("DataCenter=" + serfDataCenter + ", Service=" + getServiceId() + ", Instances=" + response.getValue().size());
            return transformResponse(response.getValue());
        }

        // 当前dc没有实例可用，看看其他dc的实例
        List<Datacenter> dataCenterList = getClient().getDatacenters().getValue();
        for (Datacenter datacenter : dataCenterList) {
            if (datacenter.getDatacenter().equals(serfDataCenter)) {
                continue;
            }

            response = getClient().getHealthServices(
                    getServiceId(), tag, getProperties().isQueryPassing(),
                    QueryParams.Builder.builder().setDatacenter(datacenter.getDatacenter()).build(),
                    getProperties().getAclToken());
            if (response.getValue() != null && !response.getValue().isEmpty()) {
                log.debug("DataCenter=" + datacenter.getDatacenter() + ", Service=" + getServiceId() + " Instances=" + response.getValue().size());
                return transformResponse(response.getValue());
            }
        }

        // 所有dc都没有实例
        return Collections.emptyList();
    }

    @Override
    protected List<ConsulServer> transformResponse(List<HealthService> healthServices) {
        return super.transformResponse(healthServices);
    }

    @Override
    protected QueryParams createQueryParamsForClientRequest() {
        return QueryParams.DEFAULT;
    }

}