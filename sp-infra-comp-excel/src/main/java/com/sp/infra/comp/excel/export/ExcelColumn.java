package com.sp.infra.comp.excel.export;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {

    String value();

    String handlerKey() default ""; // overwrite handler, default map key is fieldName

    Class handler() default Object.class; // implements ExcelConvertHandler or has field 'list' enum
}
