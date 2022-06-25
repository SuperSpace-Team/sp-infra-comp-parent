package com.sp.infra.comp.datasource;

import java.util.Stack;

/**
 * 数据源存储池
 * Created by luchao on 2021/8/7.
 */
public class DynamicDataSourceHolders {
    //跨线程请使用InheritableThreadLocal  跨线程池请使用TransmitableThreadLocal
    public static final ThreadLocal<Stack<String>> holder = new ThreadLocal<>();

    public static void pushDataSource(String name) {
        Stack<String> ss = holder.get();
        if(ss==null){
            Stack<String> newss = new Stack<>();
            newss.push(name);
            holder.set(newss);
            return;
        }
        ss.push(name);
    }

    public static String peekDataSource() {
        Stack<String> ss = holder.get();
        if(ss!=null && !ss.isEmpty()){
            return ss.peek();
        }
       return null;
    }

    public static void clear(){
        holder.remove();
    }

    public static String popDataSource(){
        Stack<String> ss = holder.get();
        if(ss!=null && !ss.isEmpty()){
            return ss.pop();
        }
        return null;
    }
}
