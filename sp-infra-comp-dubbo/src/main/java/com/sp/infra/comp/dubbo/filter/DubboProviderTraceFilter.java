package com.sp.infra.comp.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.sp.infra.comp.dubbo.utils.MdcUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Activate(group = {Constants.PROVIDER})
public class DubboProviderTraceFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        String traceId = RpcContext.getContext().getAttachment(MdcUtil.TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            MdcUtil.getOrDefaultMdc();
        } else {
            MdcUtil.insertMDC(traceId);
        }
        return invoker.invoke(invocation);
    }
}