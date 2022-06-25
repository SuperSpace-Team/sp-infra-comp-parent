package com.sp.infra.comp.redis.lock.exeption;

import com.sp.infra.comp.redis.utils.StringUtil;

/**
 * 锁失败的异常
 * @author  Wang Chong
 */
public class LockFailException extends RuntimeException {
    private static final long serialVersionUID = 3424011621396881667L;
    private String lockKey;

    public LockFailException(String lockKey) {
        super(StringUtil.format("锁失败，key:{0}", lockKey));
        this.lockKey = lockKey;
    }

    /**
     * 获取失败锁对应的key
     *
     * @return key
     */
    public String getLockKey() {
        return lockKey;
    }
}
