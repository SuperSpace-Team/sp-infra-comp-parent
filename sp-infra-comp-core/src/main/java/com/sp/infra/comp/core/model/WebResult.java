package com.sp.infra.comp.core.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * (原云创)返回数据对象
 * Created by luchao on 2021/10/29.
 */
public class WebResult<T> implements Serializable {
    /**
     * 返回码
     */
    private int code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 当前时间
     */
    private Date now;

    public static final int ERRORCODE_OK = 0;

    public WebResult() {
        super();
    }

    public WebResult(int code, String message, T data) {
        super();
        this.code = code;
        this.data = data;
        this.message = message;
        this.now = new Date();
    }

    public WebResult(T data) {
        super();
        this.code = ERRORCODE_OK;
        this.message = StringUtils.EMPTY;
        this.data = data;
        this.now = new Date();
    }

    public static <T extends Serializable> WebResult<T> ok(T data) {
        WebResult<T> ret = new WebResult<>(ERRORCODE_OK, "", data);
        return ret;
    }

    public static WebResult ok(Object data) {
        WebResult<Object> ret = new WebResult<>(ERRORCODE_OK, "", data);
        return ret;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public <T> T data() {
        return (T) data;
    }

    public Date getNow() {
        return now;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}