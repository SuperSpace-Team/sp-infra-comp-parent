package com.sp.infra.comp.excel;

import java.util.List;

/**
 * @author Liu Tao
 * @date 2018/7/19 上午10:13 数据处理器
 */
public interface ExcelDataHandler<T> {

    /**
     * 分页数据拉取
     */
    List<T> lastDate(String fileds, String params, Integer pageIndex, Integer pageSize);

    /**
     * 二进制文件上传处理
     */
    void upload(byte[] bytes);
}
