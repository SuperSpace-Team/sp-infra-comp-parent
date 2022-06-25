package com.sp.infra.comp.rocketmq.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Wang Chong
 * @Date 2019/10/26
 **/
@Data
@ConfigurationProperties(prefix = "rocketmq.multi.producer")
public class MultiProducerProperties {
    List<MultiProducer> producers;
    boolean enable = false;
}
