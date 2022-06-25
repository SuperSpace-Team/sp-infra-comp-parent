package com.sp.infra.comp.excel;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Liu Tao
 * @date 2018/7/18 下午6:38
 */
public abstract class ExcelExport<T> {

    private Logger logger = LoggerFactory.getLogger(ExcelExport.class);
    private static final String DEFAULT_TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    protected abstract LinkedHashMap<String, ExcelModel> getPropertyAndColumnNameMap(String fileds);

    protected abstract Class<T> getModelClass();

    protected String getEnumValueMethodName() {
        return "getValue";
    }

    protected String getEnumDescMethodName() {
        return "getDescription";
    }

    protected void putMap(LinkedHashMap<String, ExcelModel> map, String key, ExcelModel excelModel, String fileds) {
        if (fileds.contains(key) || "all".equals(fileds)) {
            map.put(key, excelModel);
        }
    }

    private Cell getCell(Sheet sheet, int row, int col) {
        Row sheetRow = sheet.getRow(row);
        if (sheetRow == null) {
            sheetRow = sheet.createRow(row);
        }
        Cell cell = sheetRow.getCell(col);
        if (cell == null) {
            cell = sheetRow.createCell(col);
        }
        return cell;
    }


    public void excelExport(int count, String fileds, String params, ExcelDataHandler<T> excelDataHandler) {

        SXSSFWorkbook wb = new SXSSFWorkbook(100);
        Sheet sheet = wb.createSheet();
        Map<String, ExcelModel> propertyAndColumnNameMap = getPropertyAndColumnNameMap(fileds);
        Map<String, Integer> propertyNameAndColIndexMap = new LinkedHashMap<>();

        int colIndex = 0;
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        Font font = wb.createFont();
        // 加粗
        font.setBold(true);
        cellStyle.setFont(font);
        for (Map.Entry<String, ExcelModel> entry : propertyAndColumnNameMap.entrySet()) {
            String propertyName = entry.getKey();
            ExcelModel excelModel = entry.getValue();
            propertyNameAndColIndexMap.put(propertyName, colIndex);
            Cell cell = getCell(sheet, 0, colIndex);
            cell.setCellValue(excelModel.getName());
            cell.setCellStyle(cellStyle);
            colIndex++;
        }
        int rowIndex = 0, pageIndex = 1, pageSize = 500;
        //数据拉取器
        while (true) {
            int sum = pageIndex * pageSize;
            //是否大于导出总行数，是则退出
            if (pageIndex > 1 && sum > count) {
                break;
            }
            //当最后一页大于总行数时，则使用导出最大数量作为分页规则
            int temp = count - sum;
            if (temp < 0) {
                temp = count;
            }
            //数据拉取，分页进行读取数据，防止oom溢出
            List<T> data = excelDataHandler.lastDate(fileds, params, pageIndex, temp > 0 && temp < pageSize ? temp : pageSize);
            //拉取数据失败，退出
            if (data == null || data.isEmpty()) {
                break;
            }
            for (int i = 0; i < data.size(); i++, rowIndex++) {
                T record = data.get(i);
                setPropertiesValueByReflection(sheet, rowIndex, record, propertyAndColumnNameMap, propertyNameAndColIndexMap);
            }
            pageIndex++;
        }

        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try {
            wb.write(arrayOutputStream);
            excelDataHandler.upload(arrayOutputStream.toByteArray());
        } catch (IOException e) {
            logger.error("导出数据异常", e);
            excelDataHandler.upload(null);
        } finally {
            try {
                arrayOutputStream.close();
            } catch (IOException e) {
                logger.error("导出数据异常", e);
            }
            wb.dispose();
            try {
                wb.close();
            } catch (IOException e) {
                logger.error("导出数据异常", e);
            }
        }
    }


    private void setPropertiesValueByReflection(Sheet sheet, Integer rowIndex, T obj,
                                                Map<String, ExcelModel> propertyAndColumnNameMap,
                                                Map<String, Integer> propertyNameAndColIndexMap) {
        for (Map.Entry<String, ExcelModel> entry : propertyAndColumnNameMap.entrySet()) {
            String propertyName = entry.getKey();
            ExcelModel excelModel = entry.getValue();
            Object value = getPropertyValue(propertyName, excelModel, obj);
            getCell(sheet, rowIndex + 1, propertyNameAndColIndexMap.get(propertyName)).setCellValue(value != null ? value.toString() : null);

        }
    }


    private Object getPropertyValue(String propertyName, ExcelModel excelModel, T obj) {
        Class<T> objcls = getModelClass();
        Object value = null;
        try {
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, objcls);
            Method rM = pd.getReadMethod();//获得读方法
            value = rM.invoke(obj);
            if (excelModel.getType().equals(ExcelModel.ExcelType.ENUM)) {
                //枚举
                if (value != null) {
                    value = getEnumValue(excelModel.getEnumClass(), value);
                }
            } else if (excelModel.getType().equals(ExcelModel.ExcelType.BOOLEAN)) {
                //布尔值
                value = Boolean.valueOf(value != null ? value.toString() : "") ? "是" : "否";
            } else if (excelModel.getType().equals(ExcelModel.ExcelType.WHETHER)) {
                //是否值
                if (value != null) {
                    value = Byte.valueOf(value.toString()) == 1 ? "是" : "否";
                }
            } else if (excelModel.getType().equals(ExcelModel.ExcelType.DATE)) {
                //日期
                SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TIME_FORMAT_STRING);
                value = sdf.format(value).trim();
            }
            //后续可添加更多类型
        } catch (Exception e) {
            logger.error("设置excel属性异常", e);
        }
        return value;
    }


    private String getEnumValue(Class aClass, Object value) {
        try {
            Method getCode = aClass.getMethod(getEnumValueMethodName());
            Method getDescription = aClass.getMethod(getEnumDescMethodName());
            Object[] objs = aClass.getEnumConstants();
            for (Object enu : objs) {
                Object code = getCode.invoke(enu);
                if (code.equals(value)) {
                    return value.toString() + "-" + getDescription.invoke(enu).toString();
                }
            }
        } catch (Exception e) {
            logger.error("获取枚举信息异常", e);
        }
        return value.toString();
    }


}
