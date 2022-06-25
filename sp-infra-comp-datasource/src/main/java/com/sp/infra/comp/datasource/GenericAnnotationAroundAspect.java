package com.sp.infra.comp.datasource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by luchao on 2017/9/7.
 */
@Slf4j
public abstract class GenericAnnotationAroundAspect {
    //(Method -> Final com.sp.infra.comp.datasource.DataSource)
    ConcurrentHashMap<Method,String> cached = new ConcurrentHashMap<>();

    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodInvocationProceedingJoinPoint mpjp = (MethodInvocationProceedingJoinPoint)pjp;
        boolean isAnnotated = false;

        try {
             isAnnotated= before(mpjp);
            return mpjp.proceed(mpjp.getArgs()) ;
        } finally {
            if(isAnnotated){
                DynamicDataSourceHolders.popDataSource();
            }
        }
    }

    /**
     * 查找到@DataSource注解的数据源定义方法签名加入本地缓存
     * @param mpjp
     * @return
     */
    public boolean before(MethodInvocationProceedingJoinPoint mpjp) {
            MethodSignature methodSignature = (MethodSignature) mpjp.getSignature();

            //获取当前的切面方法
            Method method = methodSignature.getMethod();
            Class target = mpjp.getTarget().getClass();

            String cachedDataSource = cached.get(method);
            if(StringUtils.isNotBlank(cachedDataSource)){
                DynamicDataSourceHolders.pushDataSource(cachedDataSource);
                log.debug("{}方法{}数据源缓存代理到{}",Thread.currentThread(),method.getName(),cachedDataSource);
                return true;
            }

            if(collectResult(target, method)){
                String confirmedDataSource = DynamicDataSourceHolders.peekDataSource();
                cached.put(method,confirmedDataSource);
                log.debug("{}方法{}数据源代理到{}",Thread.currentThread(),method.getName(),confirmedDataSource);
                return true;
            }

            log.debug("{}方法{}无数据源代理注解",Thread.currentThread(),method.getName());
            return false;
    }

    /**
     * 查找到@DataSource注解的方法放入数据源Stack集合
     * @param target
     * @param method
     * @return
     */
    private boolean collectResult(Class target, Method method){
        //遍历父类注解
        Method expectMethod= findoutAnnotatedMethodInTargetClz(target, method);
        if(expectMethod != null){
            boolean processed = process(expectMethod);
            if(processed){
                return true;
            }
        }

        //遍历基类
        Class superclass = target.getSuperclass();
        if(superclass != null){
            if(collectResult(superclass,method)){
                return true;
            }
        }

        //遍历实现的接口
        Class[] interfaces = target.getInterfaces();
        if(interfaces != null){
            for (Class anInterface : interfaces) {
                if(collectResult(anInterface,method)){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取类的方法签名
     * @param targetClass
     * @param method
     * @return
     */
    private Method findoutAnnotatedMethodInTargetClz(Class targetClass,Method method){
        Method superMethod=null;
        //无可追溯的父类
        if(targetClass==null||targetClass==Object.class){
            return null;
        }

        try {
            //这里不使用参数进行匹配，有可能父类是个抽象类，其中参数是泛型，无法获取方法，避免这种情况，方法不应该重载参数相同的方法,或者不要使用泛型
            superMethod = targetClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
            return superMethod;
        } catch (NoSuchMethodException e) {
            //如果无法准确获取遍历重名方法是否有直接获取,使用了泛型的方法往往不需要重载
            Method[] declaredMethods = targetClass.getDeclaredMethods();
            if(declaredMethods!=null){
                for (Method declaredMethod : declaredMethods) {
                    if(method.getName().equals(declaredMethod.getName())){
                        return declaredMethod;
                    }
                }
            }
        }
        return null;

    }

    public abstract boolean process(Method method);

}

