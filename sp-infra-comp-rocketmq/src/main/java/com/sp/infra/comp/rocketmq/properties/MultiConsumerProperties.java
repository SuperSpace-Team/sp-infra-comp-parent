package com.sp.infra.comp.rocketmq.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Wang Chong
 * @Date 2019/10/27
 **/
@Data
@ConfigurationProperties(prefix = "rocketmq.multi.consumer")
public class MultiConsumerProperties {
    List<MultiConsumer> consumers;
}
