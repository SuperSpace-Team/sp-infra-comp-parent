package com.sp.infra.comp.consul.watcher;

import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.KeyValueClient;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.sp.infra.comp.consul.common.CommonConsulClient;
import com.sp.infra.comp.consul.common.GenericRunnable;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description Consul KV数据监听器
 * @Author alexlu
 * @date 2021.08.13
 */
public class ConsulWatcher implements GenericRunnable.Watcher<String, String> {

    public static final ConsulWatcher INSTANCE = new ConsulWatcher();
    private static final KeyValueClient consulClient = CommonConsulClient.getDefaultConsulClient();

    // uri测试wait=-1s&index=0  api接口wait默认单位->秒
    // consul服务端默认时间5分钟，AbstractHttpTransport传输对象固定SocketTimeout为1分钟,
    // 为了避免java.net.SocketTimeoutException;客服端55s时返回当前最新配置，刷新周期默认55s,
    // 当然也可以自定义httpclient实现最长5分钟的SocketTimeout
    private static final long DEFAULT_WATCH_WAIT = 55;

    // uri测试index=-1 -> Invalid index
    private static final Map<String, Long> WATCH_INDEXES = new HashMap();
    private static final long DEFAULT_WATCH_INDEX = 0;

    public String watch(String key) {
        Long consulIndex = WATCH_INDEXES.get(key);
        if (consulIndex == null) {
            consulIndex = DEFAULT_WATCH_INDEX;
        }

        QueryParams queryParams =
                QueryParams.Builder.builder()
                        .setIndex(consulIndex)
                        .setWaitTime(DEFAULT_WATCH_WAIT) // 等待时间<60s,兼容一些老版本的consul-api
                        .build();

        Response<GetValue> kvValue = consulClient.getKVValue(key, queryParams);

        // IO Blocking是不响应中断的，这里再次校验
        GetValue value = kvValue.getValue();
        if (value == null) {
            return null;
        }

        consulIndex = kvValue.getConsulIndex();
        WATCH_INDEXES.put(key, consulIndex);
        return value.getDecodedValue(StandardCharsets.UTF_8);
    }
}

