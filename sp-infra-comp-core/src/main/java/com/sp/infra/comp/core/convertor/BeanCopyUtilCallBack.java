package com.sp.infra.comp.core.convertor;

/**
 * 定义默认回调方法
 *
 * @author alexlu on 2020/6/4
 */
@FunctionalInterface
public interface BeanCopyUtilCallBack<S, T> {

	void callBack(S t, T s);
}
