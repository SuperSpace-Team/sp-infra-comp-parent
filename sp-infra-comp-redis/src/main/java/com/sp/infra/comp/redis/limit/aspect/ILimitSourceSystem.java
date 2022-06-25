package com.sp.infra.comp.redis.limit.aspect;

public interface ILimitSourceSystem {

    /**
     * 调用系统编号, 拼接在prefix后面
     *
     * @return
     */
    String getSourceSystem();
}
