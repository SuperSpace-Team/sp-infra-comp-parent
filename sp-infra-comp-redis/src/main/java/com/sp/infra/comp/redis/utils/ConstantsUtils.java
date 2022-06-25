package com.sp.infra.comp.redis.utils;

/**
 * 常量,枚举定义
 *
 * @author zhanghai
 */
public class ConstantsUtils {

	private static String illegalStateException = "Utility class";

	/**
	 * redis set 路径
	 */
	public abstract class RedisSet {
		private RedisSet() {
			throw new IllegalStateException(illegalStateException);
		}

		/**
		 * 默认过期时长，单位：秒
		 * 60 * 60 * 24
		 */
		public static final long DEFAULT_EXPIRE = 86400;

		/**
		 * 不设置过期时长
		 */
		public static final long NOT_EXPIRE = -1;
	}

}


