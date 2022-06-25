package com.sp.infra.comp.redis.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SplitJoinUtil {

    private static final String COMMA = ",";

    public static List<String> split2String(String str) {
        return split2String(str, COMMA);
    }

    /**
     * 按 分隔符 切割字符(去掉字符前后的空格)
     *
     * @param str
     * @param sep 分隔符
     * @return
     */
    public static List<String> split2String(String str, String sep) {
        if (StringUtils.isBlank(str)) {
            return Collections.emptyList();
        }
        return Splitter.on(sep).omitEmptyStrings().trimResults().splitToList(str);
    }

    public static List<Integer> split2Int(String str) {
        return split2Int(str, COMMA);
    }

    /**
     * 按 分隔符 切割字符 并转成 Integer(去掉字符前后的空格)
     *
     * @param str
     * @param sep 分隔符
     * @return
     */
    public static List<Integer> split2Int(String str, String sep) {
        if (StringUtils.isBlank(str)) {
            return Collections.emptyList();
        }
        return split2String(str, sep).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public static List<Long> split2Long(String str) {
        return split2Long(str, COMMA);
    }

    /**
     * 按 分隔符 切割字符 并转成 Long(去掉字符前后的空格)
     *
     * @param str
     * @param sep 分隔符
     * @return
     */
    public static List<Long> split2Long(String str, String sep) {
        if (StringUtils.isBlank(str)) {
            return Collections.emptyList();
        }
        return split2String(str, sep).stream().map(Long::parseLong).collect(Collectors.toList());
    }

    public static <T> Optional<T> resolve(Supplier<T> resolver) {
        try {
            T result = resolver.get();
            return Optional.ofNullable(result);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    public static <T> String join(List<T> list, Function<T, String> keyExtractor, String delimeter) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        return Joiner.on(Optional.ofNullable(delimeter).orElse(",")).skipNulls().join(
                list.stream().map(keyExtractor).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
    }

    public static <T> String join(List<T> list, Function<T, String> keyExtractor) {
        return join(list, keyExtractor, ",");
    }

    public static <T> Map<String, T> map(List<T> list, Function<T, String> keyExtractor) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().filter(Objects::nonNull).collect(Collectors.toMap(keyExtractor, Function.identity(), (a, b) -> b));
    }

    public static <T, V> Map<String, V> map(List<T> list, Function<T, String> keyExtractor, Function<T, V> valueExtractor) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().filter(Objects::nonNull).collect(Collectors.toMap(keyExtractor, valueExtractor, (a, b) -> b));
    }
}
