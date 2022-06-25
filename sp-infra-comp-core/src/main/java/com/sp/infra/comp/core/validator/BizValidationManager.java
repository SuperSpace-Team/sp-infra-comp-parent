/**
 * 
 */
package com.sp.infra.comp.core.validator;

import java.util.List;

/**
 * @description: 业务校验器管理
 * @author: luchao
 * @date: Created in 8/5/21 4:57 PM
 */
public interface BizValidationManager {
	/**
	 * 获取校验POJO属性结果
	 * @param data
	 * @return
	 */
	List<Error> validate(Object data);

	/**
	 * 仅获取校验的错误
	 * @param data
	 * @return
	 */
	List<Error> validateError(Object data);

	/**
	 * 仅获取校验的警告
	 * @param data
	 * @return
	 */
	List<Error> validateWarn(Object data);
}
