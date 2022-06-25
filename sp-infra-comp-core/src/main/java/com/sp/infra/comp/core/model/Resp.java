package com.sp.infra.comp.core.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * (原云超)返回数据对象
 *
 * @author luchao
 * @date 2018-09-05 17:30:43
 */
@Data
public class Resp<T> implements Serializable {
	private static final long serialVersionUID = 2523377728598055414L;
	/**
	 * 统一查询编码标识
	 */
	public static final int SUCCESS_CODE = 200000;
	/**
	 * 统一异常错误
	 */
	public static final int ERROR_CODE = 500000;

	public static final String SYS_ERROR_MSG = "system error";
	public static final String SYS_SUCC_MSG = "success";

	@Setter
	@Getter
	private int code;

	@Setter
	@Getter
	private String message = "";

	/**
	 * 设置响应数据
	 */
	@Setter
	@Getter
	private T result;

	public Resp() {
		super();
	}

	/**
	 * 请求结果信息
	 *
	 * @param code 响应码
	 * @param data 响应数据
	 * @param msg  相信信息说明
	 */
	private Resp(int code, T data, String msg) {
		this.setCode(code);
		this.setMessage(msg);
		this.result = data;
	}

	/**
	 * 请求失败
	 *
	 * @return
	 */
	public static <T> Resp<T> error() {
		Resp<T> response = new Resp<>();
		response.setCode(ERROR_CODE);
		response.setMessage(SYS_ERROR_MSG);
		return response;
	}

	/**
	 * 请求失败
	 *
	 * @param msg 自定义异常信息
	 * @return
	 */
	public static <T> Resp<T> error(String msg) {
		Resp<T> response = new Resp<>();
		response.setCode(ERROR_CODE);
		response.setMessage(msg);
		return response;
	}

	/**
	 * 请求失败
	 *
	 * @param error 自定义异常枚举
	 * @return
	 */
	public static <T> Resp<T> error(ExceptionEnum error) {
		Resp<T> response = new Resp<>();
		response.setCode(error.getCode());
		response.setMessage(error.getDesc());
		return response;
	}

	/**
	 * 请求失败
	 *
	 * @param error 自定义异常枚举
	 * @return
	 */
	public static <T> Resp<T> error(ExceptionEnum error, T result) {
		Resp<T> response = new Resp<>();
		response.setCode(error.getCode());
		response.setMessage(error.getDesc());
		response.setResult(result);
		return response;
	}

//	/**
//	 * 请求失败
//	 *
//	 * @param error 自定义异常
//	 * @return
//	 */
//	public static <T> R<T> error(RRException error) {
//		R<T> response = new R<>();
//		response.setCode(error.getCode());
//		response.setMessage(error.getMessage());
//		return response;
//	}


	/**
	 * 请求失败
	 *
	 * @param code 自定义异常编码
	 * @param msg  自定义异常信息
	 * @return
	 */
	public static <T> Resp<T> error(int code, String msg) {
		return error(code, msg, null);
	}

	/**
	 * 请求失败
	 *
	 * @param code 自定义异常编码
	 * @param msg  自定义异常信息
	 * @return
	 */
	public static <T> Resp<T> error(int code, String msg, T result) {
		Resp<T> response = new Resp<>();
		response.setCode(code);
		response.setMessage(msg);
		response.setResult(result);
		return response;
	}

	/**
	 * 请求成功 有响应数据
	 *
	 * @param data 响应数据
	 * @return
	 */
	public static <T> Resp<T> success(T data) {
		Resp<T> response = new Resp<>();
		response.setCode(SUCCESS_CODE);
		response.setMessage(SYS_SUCC_MSG);
		response.setResult(data);
		return response;
	}


	/**
	 * 请求成功,data为null
	 *
	 * @return
	 */
	public static <T> Resp<T> success() {
		Resp<T> response = new Resp<>();
		response.setCode(SUCCESS_CODE);
		response.setMessage(SYS_SUCC_MSG);
		return response;
	}


	public static Resp newInstance(int code, Object data, String msg) {
		return new Resp(code, data, msg);
	}

	public interface ExceptionEnum {
		int getCode();

		String getDesc();
	}
}
