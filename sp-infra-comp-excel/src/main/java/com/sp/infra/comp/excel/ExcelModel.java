package com.sp.infra.comp.excel;

/**
 * @author Liu Tao
 * @date 2018/7/18 下午6:48
 */
public class ExcelModel {

    private String name;

    private ExcelType type;

    /**
     * 枚举类，值需实现方法getValue；value需实现方法getDescription
     * 如枚举类的获取值/描述的方法名不统一，请重写getEnumValueMethodName、getEnumDescMethodName
     */
    private Class enumClass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExcelType getType() {
        return type;
    }

    public void setType(ExcelType type) {
        this.type = type;
    }

    public Class getEnumClass() {
        return enumClass;
    }

    public void setEnumClass(Class enumClass) {
        this.enumClass = enumClass;
    }

    public ExcelModel(String name, ExcelType type, Class enumClass) {
        this.name = name;
        this.type = type;
        this.enumClass = enumClass;
    }

    public ExcelModel(String name, ExcelType type) {
        this.name = name;
        this.type = type;
    }

    public ExcelModel(String name) {
        this.name = name;
        this.type = ExcelType.OTHER;
    }

    public enum ExcelType {
        ENUM, BOOLEAN, WHETHER, DATE, OTHER
    }
}
