package com.sp.infra.comp.feign.definitions;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 无参对象定义
 * @author luchao
 * @create 2020/10/11.
 */
@Data
@NoArgsConstructor
public class Void {

    private static final Void EMPTY = new Void();

    private String data;

    public static Void ok() {
        return new Void();
    }

}
