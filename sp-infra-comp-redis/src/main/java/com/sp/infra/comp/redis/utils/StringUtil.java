package com.sp.infra.comp.redis.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 字符串帮助工具
 * @author  Wang Chong
 */
public class StringUtil {
    public static final String DEFAULT_TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    public static final String SHORT_FORMAT = "yyyyMMdd";
    /**
     * 替换占位符 {0} {1} 等，然后输出字符串
     *
     * @param text      包含占位符的文本
     * @param arguments 要替换的参数
     * @return 新的文本
     */
    public static String format(String text, Object... arguments) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        List<Object> args = new ArrayList<>(arguments.length);
        for (Object argument : arguments) {
            if (argument instanceof Number) {
                argument = String.valueOf(argument);
            } else if (argument instanceof Date) {
                argument =  DateFormatUtils.format((Date)argument, DEFAULT_TIME_FORMAT_STRING);
            }

            args.add(argument);
        }

        return MessageFormat.format(text, args.toArray());
    }
}
