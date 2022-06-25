package com.sp.infra.comp.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.sp.infra.comp.dubbo.utils.MdcUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Activate(group = {Constants.CONSUMER})
public class DubboConsumerTraceFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceId = MdcUtil.getOrDefaultMdc();
        RpcContext.getContext().setAttachment(MdcUtil.TRACE_ID, traceId);
        return invoker.invoke(invocation);
    }
}