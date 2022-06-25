package com.sp.infra.comp.excel.export;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ExcelDataVO<C, Y> implements Serializable {

    public static final String EXPORT_EXCEL_NAME = "EXPORT_EXCEL_NAME";

    public static final String EXCEL_DATA = "EXCEL_DATA";

    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    private static String DECIMAL_FORMAT = "###,###,###,##0.##";

    private String datePattern;

    private String decimalPattern;


    //一个excel有多个sheet
    private List<SheetExcelData<C, Y>> dataList = new ArrayList<>();

    public ExcelDataVO() {

        this.datePattern = DATE_FORMAT;

        this.decimalPattern = DECIMAL_FORMAT;
    }

    public ExcelDataVO(String datePattern) {
        this.datePattern = datePattern;
        this.decimalPattern = DECIMAL_FORMAT;
    }

    public ExcelDataVO(String datePattern, String decimalPattern) {
        this.datePattern = datePattern;
        this.decimalPattern = decimalPattern;
    }

    public SheetExcelData<C, Y> addSheet(SheetExcelData<C, Y> sheetExcelData) {
        dataList.add(sheetExcelData);
        return sheetExcelData;

    }

    public List<SheetExcelData<C, Y>> getDataList() {
        return dataList;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public String getDecimalPattern() {
        return decimalPattern;
    }

    public void setDecimalPattern(String decimalPattern) {
        this.decimalPattern = decimalPattern;
    }

    //excel sheet数据
    public static class SheetExcelData<C, Y> implements Serializable {

        //sheetName
        private String sheetName;

        //这里面可以添加表单与列表两种
        private List<BaseColumnExcelData> baseColumnExcelDataList = new ArrayList<>();

        public SheetExcelData() {

        }

        public SheetExcelData(String sheetName) {
            this.sheetName = sheetName;
        }

        //新增列表导出
        public ColumExcelData<C> addColumExcelData(ColumExcelData<C> columExcelData) {
            baseColumnExcelDataList.add(columExcelData);
            return columExcelData;
        }

        //新增表格导出
        public TableExcelData<Y> addTableColumExcelData(TableExcelData<Y> tableExcelData) {
            baseColumnExcelDataList.add(tableExcelData);
            return tableExcelData;
        }

        public List<BaseColumnExcelData> getBaseColumnExcelDataList() {
            return baseColumnExcelDataList;
        }

        public String getSheetName() {
            return sheetName;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }
    }

    public static class BaseColumnExcelData implements Serializable {
        protected String titleName;

        protected List<Pair> pairList = new ArrayList<>();

        public BaseColumnExcelData() {
        }

        public String getTitleName() {
            return titleName;
        }

        public BaseColumnExcelData setTitleName(String titleName) {
            this.titleName = titleName;
            return this;
        }

        public List<Pair> getPairList() {
            return pairList;
        }

        public BaseColumnExcelData addPairs(Pair... pair) {
            this.pairList.addAll(Arrays.asList(pair));
            return this;
        }

        public BaseColumnExcelData addPairs(List<Pair> pairList) {
            this.pairList.addAll(pairList);
            return this;
        }

        public BaseColumnExcelData addPair(Pair pair) {
            this.pairList.add(pair);
            return this;
        }

        public BaseColumnExcelData addTestPair(Class clazz) {
            Field[] fields = clazz.getDeclaredFields();
            int sort = 0;
            for (Field field : fields) {
                ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
                if (excelColumn != null) {
                    ExcelHandler.ExcelConvertHandler convert = null;

                    if (convert == null) {
                        Class handler = excelColumn.handler();
                        try {
                            convert = handler.isEnum() ? new ExcelHandler.MapConvertHandler(getEnumMap(handler)) :
                                    ExcelHandler.ExcelConvertHandler.class.isAssignableFrom(handler) ? (ExcelHandler.ExcelConvertHandler) handler.newInstance() : null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    this.pairList.add(new Pair(convert, field.getName(), excelColumn.value()));
                }
            }
            if (sort != 0) {
                //this.pairList.sort(Comparator.comparingInt(Pair::getSort));
            }
            return this;

        }

    }


    public static class ColumExcelData<C> extends BaseColumnExcelData {

        private List<C> dataList = new ArrayList<>();

        @Override
        public ColumExcelData<C> addPair(Pair pair) {
            this.pairList.add(pair);
            return this;
        }

        public ColumExcelData() {

        }


        @Override
        public String getTitleName() {
            return titleName;
        }

        @Override
        public ColumExcelData setTitleName(String titleName) {
            this.titleName = titleName;
            return this;
        }

        @Override
        public List<Pair> getPairList() {
            return pairList;
        }


        public List<C> getDataList() {
            return dataList;
        }

        public ColumExcelData addDataList(List<C> dataList) {
            this.dataList = dataList;
            return this;
        }
    }

    public static class TableExcelData<Y> extends BaseColumnExcelData {

        private Y data;

        private int size;


        public TableExcelData() {
            this.size = 3;
        }


        public int getSize() {
            return size;
        }

        public TableExcelData setSize(int size) {
            this.size = size;
            return this;
        }

        @Override
        public TableExcelData setTitleName(String titleName) {
            this.titleName = titleName;
            return this;
        }

        public Y getData() {
            return data;
        }

        public TableExcelData<Y> addData(Y data) {
            this.data = data;
            return this;
        }


    }

    public static class Pair<T> implements Serializable {

        //列名
        private String columnName;

        //属性名
        private String propertyName;


        private ExcelHandler.ExcelConvertHandler convertHandler;

        public Pair() {
        }

        public Pair(String columnName, String propertyName) {
            this(null, propertyName, columnName);
        }


        public Pair(ExcelHandler.ExcelConvertHandler convertHandler, String propertyName, String columnName) {
            this.columnName = columnName;
            this.propertyName = propertyName;
            this.convertHandler = convertHandler;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public ExcelHandler.ExcelConvertHandler getConvertHandler() {
            return convertHandler;
        }


    }

    public static Map<Object, String> getEnumMap(Class clazz) {
        Map<Object, String> map = new HashMap<>();
        try {
            Method getValue = clazz.getDeclaredMethod("getValue");
            Method getDescription = clazz.getDeclaredMethod("getDescription");
            for (Object enumConstant : clazz.getEnumConstants()) {
                map.put(getValue.invoke(enumConstant), String.valueOf(getDescription.invoke(enumConstant)));
            }
        } catch (Exception e) {
        }
        return map;
    }
}




