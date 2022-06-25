package com.sp.infra.comp.excel.export;


import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;

public class ExcelHandler {

    public interface ExcelConvertHandler<Field, Obj> {
        Object convert(Field field, Obj obj);
    }

    public static class MapConvertHandler implements ExcelConvertHandler {
        private final Map<Object, String> map;

        public MapConvertHandler(Map<Object, String> map) {
            this.map = map != null && map.size() > 0 ? map : Collections.emptyMap();
        }

        @Override
        public String convert(Object value, Object obj) {
            String description = map.get(value);
            return StringUtils.isEmpty(description) ? String.valueOf(value) : description;
        }
    }
}



