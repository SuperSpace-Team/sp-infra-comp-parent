package com.sp.infra.comp.core.convertor;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.sp.framework.common.enums.SystemErrorCodeEnum;
import com.sp.framework.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

import static org.springframework.beans.BeanUtils.copyProperties;

/**
 * 类型转换辅助类
 *
 * @author alexlu
 */
@Slf4j
public class ConvertHelper {

	private static final Map<String, DestinClassData> CONVERT_MAP = new HashMap<>();

	static final String SPRING_PROXY_CLASS = "EnhancerBySpringCGLIB";

	private ConvertHelper() {

	}

	/**
	 * 集合数据的拷贝
	 * List<UserVO> userVOList = copyListProperties(userDOList, UserVO::new)
	 *
	 * @param sources: 数据源类
	 * @param target:  目标类::new(eg: UserVO::new)
	 * @return
	 */
	public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target) {
		return copyListProperties(sources, target, null);
	}

	/**
	 * 带回调函数的集合数据的拷贝（可自定义字段拷贝规则）
	 * List<UserVO> userVOList = copyListProperties(userDOList, UserVO::new, (userDO, userVO) ->{
	 * // 这里可以定义特定的转换规则
	 * userVO.setSex(func(userDO.getSex());
	 * });
	 *
	 * @param sources:  数据源类
	 * @param target:   目标类::new(eg: UserVO::new)
	 * @param callBack: 回调函数
	 * @return
	 */
	public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target, BeanCopyUtilCallBack<S, T> callBack) {
		List<T> list = new ArrayList<>(sources.size());
		for (S source : sources) {
			T t = target.get();
			copyProperties(source, t);
			list.add(t);
			if (callBack != null) {
				callBack.callBack(source, t);
			}
		}
		return list;
	}

	/**
	 * 数组合并
	 * <p>
	 * String[] both = concatAllArr(first, second);
	 * String[] more = concatAllArr(first, second, third, fourth);
	 *
	 * @param first
	 * @param rest
	 * @param <T>
	 * @return
	 */
	public static <T> T[] concatAllArr(T[] first, T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			offset += array.length;
		}
		return result;
	}

	/**
	 * list去重
	 *
	 * @param list
	 */
	private static void removeDuplicate(List<String> list) {
		LinkedHashSet<String> set = new LinkedHashSet<>(list.size());
		set.addAll(list);
		list.clear();
		list.addAll(set);
	}

	/**
	 * 利用fastjson实现对象转换
	 * Dto，Entity 对象属性更新转换工具
	 *
	 * @param source 要转换的源实体
	 * @param target 元转的目标实体
	 * @param <T>
	 * @return
	 */
	public static <T> T convert(Object source, Object target) {
		ObjectMapper objectMapper = new ObjectMapper();
		// 配置该objectMapper在反序列化时，忽略目标对象没有的属性。凡是使用该objectMapper反序列化时，都会拥有该特性。
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// 读入需要更新的目标实体
		ObjectReader objectReader = objectMapper.readerForUpdating(target);
		// 将源实体的值赋值到目标实体上
		try {
			return objectReader.readValue(JSON.toJSONString(source));
		} catch (IOException e) {
			log.error("IOException", e);
			throw new SystemException(SystemErrorCodeEnum.IO_ERROR.getCode(), "dto entity 转换异常");
		}
	}

	/**
	 * list的转换
	 * 注意: destin实体类中必须有无参数构造器
	 *
	 * @param listSource 要转换的list集合
	 * @param destin     要转换的目标类型的Class
	 * @param <T>        要转换的目标类型
	 * @return 转换的后list集合
	 */
	public static <T> List<T> convertList(final List listSource, Class<T> destin) {
		if (listSource == null) {
			return Collections.emptyList();
		}
		if (listSource.isEmpty()) {
			return new ArrayList<>(0);
		}
		Class<?> source = listSource.get(0).getClass();
		if (source.getTypeName().contains(SPRING_PROXY_CLASS)) {
			source = source.getSuperclass();
		}
		final DestinClassData destinClassData = getDestinClassData(source, destin);
		List<T> list = new ArrayList<>(listSource.size());
		for (Object object : listSource) {
			T t = invokeConvert(destinClassData, object);
			list.add(t);
		}
		return list;
	}

	/**
	 * 单个对象的类型转换
	 *
	 * @param obj    要转换的对象
	 * @param destin 要转换的类型CLass
	 * @param <T>    要转换的类型
	 * @return 转换的后的对象
	 */
	public static <T> T convert(final Object obj, Class<T> destin) {
		if (obj == null) {
			return null;
		}
		Class<?> source = obj.getClass();
		if (source.getTypeName().contains(SPRING_PROXY_CLASS)) {
			source = source.getSuperclass();
		}
		DestinClassData destinClassData = getDestinClassData(source, destin);
		return invokeConvert(destinClassData, obj);
	}

	@SuppressWarnings("unchecked")
	static <T> T invokeConvert(final DestinClassData destinClassData, final Object obj) {
		T t = null;
		try {
			destinClassData.method.setAccessible(true);
			Object result = destinClassData.method.invoke(destinClassData.convertorI, obj);
			if (result != null) {
				t = (T) result;
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.warn("error.ConvertHelper.invokeConvert {}", e.toString());
		}
		return t;
	}

	static DestinClassData getDestinClassData(final Class source, final Class destin) {
		String key = source.getTypeName() + destin.getTypeName();
		//1. 获取要使用的Convertor对象。
		DestinClassData destinClassData = CONVERT_MAP.get(key);
		if (destinClassData == null) {
			destinClassData = new DestinClassData();
			CONVERT_MAP.put(key, destinClassData);
		}

		if (destinClassData.convertorClass == null) {
			destinClassData.convertorClass = getConvertClass(source, destin);
		}

		//2. 获取要调用的covert对象实例。

		if (destinClassData.convertorI == null) {
			destinClassData.convertorI = ApplicationContextHelper.getSpringFactory().getBean(destinClassData.convertorClass);
		}

		//3. 获取真正要调用的方法。
		Method method = destinClassData.method;
		if (method == null) {
			destinClassData.method = getMethod(source, destin, destinClassData.convertorClass);
		}
		return destinClassData;
	}

	private static Method getMethod(final Class source, final Class destin, final Class<? extends ConvertorI> convertorClass) {
		Method[] methods = convertorClass.getDeclaredMethods();
		for (Method method : methods) {
			Class[] paramTypes = method.getParameterTypes();
			if (method.getReturnType().equals(destin)
					&& paramTypes.length == 1
					&& paramTypes[0].equals(source)) {
				return method;
			}
		}
		throw new SystemException("error.convertHelper.getMethod, sourceClass: "
				+ source.getName() + " destinClass: " + destin.getName());
	}

	private static Class<? extends ConvertorI> getConvertClass(final Class source, final Class destin) {
		Map<String, ConvertorI> convertorIMap = ApplicationContextHelper.getSpringFactory()
				.getBeansOfType(ConvertorI.class);
		for (ConvertorI i : convertorIMap.values()) {
			Type[] interfacesTypes = i.getClass().getGenericInterfaces();
			for (Type t : interfacesTypes) {
				if (t instanceof ParameterizedType && isConvertorI(t, source, destin)) {
					return i.getClass();
				}
			}
		}
		throw new SystemException("error.convertHelper.getConvertClass, sourceClass: "
				+ source.getName() + " destinClass: " + destin.getName());
	}

	private static boolean isConvertorI(final Type t, final Class source, final Class destin) {
		Type rawType = ((ParameterizedType) t).getRawType();
		if (rawType == null) {
			return false;
		}
		if (!ConvertorI.class.getTypeName().equals(rawType.getTypeName())) {
			Type[] superInterfaces = ((Class) rawType).getGenericInterfaces();
			for (Type type : superInterfaces) {
				if (!ConvertorI.class.getTypeName().equals(((ParameterizedType) type).getRawType().getTypeName())) {
					return false;
				}
			}
		}
		Type[] genericType2 = ((ParameterizedType) t).getActualTypeArguments();
		int num = 0;
		for (Type t2 : genericType2) {
			if (source.getTypeName().equals(t2.getTypeName())
					|| destin.getTypeName().equals(t2.getTypeName())) {
				num++;
			}
		}
		return num > 1;
	}


	static class DestinClassData {

		private Class<? extends ConvertorI> convertorClass;

		private Method method;

		private ConvertorI convertorI;

	}

}
