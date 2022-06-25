package com.sp.infra.comp.excel.export;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WorkBookBuilder {
    public static Logger log = LoggerFactory.getLogger(WorkBookBuilder.class);
    public static ByteArrayOutputStream buildExcelDocument(Map<String, Object> model, Integer totalNum) throws Exception {
        //待到处的excel文件名
        Workbook workbook =new SXSSFWorkbook(totalNum);
        String excelFileName  = MapUtils.getString(model, ExcelDataVO.EXPORT_EXCEL_NAME);
        ExcelDataVO excelDataVO = (ExcelDataVO)model.get(ExcelDataVO.EXCEL_DATA);
        if(excelDataVO == null || CollectionUtils.isEmpty(excelDataVO.getDataList())){
            throw new Exception("参数错误");
        }
        if(!StringUtils.containsIgnoreCase(excelFileName,"xlsx")){
            excelFileName = excelFileName + ".xlsx";
        }
        List<ExcelDataVO.SheetExcelData<Object,Object>> sheetExcelDataVOList  = excelDataVO.getDataList();
        String dateFormat  = excelDataVO.getDatePattern();
        String decimalFormat  = excelDataVO.getDecimalPattern();
        CellStyle cellStyle  = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);

        Font headfont = workbook.createFont();
        headfont.setBold(true);
        headfont.setFontHeightInPoints((short) 10);// 设置字体大小
        CellStyle headerStyle  = workbook.createCellStyle();
        headerStyle.setFont(headfont);
        headerStyle.setAlignment(HorizontalAlignment.LEFT);
        headerStyle.setFillBackgroundColor(HSSFColor.GREY_50_PERCENT.index);
        for(ExcelDataVO.SheetExcelData sheetExcelDataVO  : sheetExcelDataVOList) {
            String sheetName  = sheetExcelDataVO.getSheetName();
            sheetName  = StringUtils.isNotBlank(sheetName) ? sheetName : StringUtils.substringBeforeLast(excelFileName,".xlsx");
            List<ExcelDataVO.BaseColumnExcelData> baseColumExcelDataVOList = sheetExcelDataVO.getBaseColumnExcelDataList();
            Sheet sheet = workbook.createSheet(sheetName);
            int rowIndex = 0;
            log.info("开始生成sheet {} 数量 {}", sheetName, sheetExcelDataVO.getBaseColumnExcelDataList().size());
            for(ExcelDataVO.BaseColumnExcelData baseColumnExcelData : baseColumExcelDataVOList){
                log.info("开始生成sheet数据 {}", baseColumnExcelData.getPairList().size());
                String titleName = baseColumnExcelData.getTitleName();
                if(StringUtils.isNotBlank(titleName)) {
                    log.info("开始生成sheet标题 {}", sheetName);
                    int tempIndex  = rowIndex;
                    Row titleRow = sheet.createRow(rowIndex++);
                    int columCount  = CollectionUtils.size(baseColumnExcelData.getPairList());
                    if(baseColumnExcelData instanceof ExcelDataVO.TableExcelData) {
                        ExcelDataVO.TableExcelData tableExcelDataVO  = (ExcelDataVO.TableExcelData) baseColumnExcelData;
                        columCount = tableExcelDataVO.getSize() * 3 -2 ;
                    }
                    for(int i = 0; i < columCount ; i++) {
                        titleRow.createCell(i);
                    }
                    CellRangeAddress cellRangeAddress  = new CellRangeAddress(tempIndex,tempIndex,0,columCount);
                    sheet.addMergedRegion(cellRangeAddress);
                    Cell rangeColumn = titleRow.getCell(0);
                    rangeColumn.setCellStyle(cellStyle);
                    rangeColumn.setCellValue(titleName);
                }
                if(baseColumnExcelData instanceof ExcelDataVO.TableExcelData) { //如果是按表格
                    log.info("开始生成sheet头数据 {}", sheetName);
                    ExcelDataVO.TableExcelData tableExcelDataVO  = (ExcelDataVO.TableExcelData) baseColumnExcelData;
                    int size  = tableExcelDataVO.getSize();
                    Object data =  tableExcelDataVO.getData();
                    List<ExcelDataVO.Pair> pairList  = baseColumnExcelData.getPairList();
                    int headrow=pairList.size()/size;
                    if(pairList.size()%size>0){headrow++;}
                    for(int  i = 0 ; i < headrow ; i++){
                        Row tableRow  = sheet.createRow(rowIndex++);
                        List<ExcelDataVO.Pair> subList  = pairList.subList(i * size,Math.min(pairList.size(),(i+ 1) * size) );
                        int tableIndex  = 0;
                        for(ExcelDataVO.Pair pair  : subList) {
                            Cell nameCell  = tableRow.createCell(tableIndex++);
                            nameCell.setCellStyle(headerStyle);
                            nameCell.setCellValue(pair.getColumnName());
                            Cell valueCell  = tableRow.createCell(tableIndex++);
                            valueCell.setCellStyle(cellStyle);
                            setCellValue(valueCell,data,pair.getConvertHandler(),pair.getPropertyName(),dateFormat,decimalFormat);
                            tableRow.createCell(tableIndex++);
                        }
                    }

                }else {
                    log.info("开始生成sheet行数据 {}", sheetName);
                    Row headRow  = sheet.createRow(rowIndex++);
                    ExcelDataVO.ColumExcelData<Object> columExcelDataVO = (ExcelDataVO.ColumExcelData<Object>) baseColumnExcelData;
                    int headIndex  = 0;
                    for(ExcelDataVO.Pair pair : baseColumnExcelData.getPairList()) {
                        Cell cell  = headRow.createCell(headIndex++);
                        cell.setCellStyle(headerStyle);
                        cell.setCellValue(pair.getColumnName());
//                        System.out.println(cell.getStringCellValue());
                    }
                    List<Object> dataList = columExcelDataVO.getDataList();
                    int i = 1;
                    for(Object t : dataList) {
                        Row row  = sheet.createRow(rowIndex++);
                        int dataIndex  =  0;
                        List<ExcelDataVO.Pair> pairList  = baseColumnExcelData.getPairList();
                        for(ExcelDataVO.Pair pair : pairList) {
                            Cell cell  = row.createCell(dataIndex++);
                            cell.setCellStyle(cellStyle);
                            String propertyName  = pair.getPropertyName();
                            if(StringUtils.equals("行号",pair.getColumnName())){
                                cell.setCellValue(i);
                                i++;
                            }else{
                                setCellValue(cell,t,pair.getConvertHandler(),propertyName,dateFormat,decimalFormat);
                            }
                        }
                    }
                }
                rowIndex ++;
            }
        }
        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return  outputStream;
    }

    private static void setCellValue(Cell cell, Object t ,  ExcelHandler.ExcelConvertHandler convertHandler , String propertyName, String dateFormat, String decimalFormat) throws Exception {
        if(t  == null || StringUtils.isEmpty(propertyName)) {
            return;
        }
        Object result  = PropertyUtils.getProperty(t, propertyName);
        if(convertHandler != null && result != null){
            result  = convertHandler.convert(result,t);
        }
        if(Number.class.isInstance(result)) {
            DecimalFormat df   =   new   DecimalFormat(decimalFormat);
            cell.setCellValue(df.format(result));
        }else if(Date.class.isInstance(result)){
            cell.setCellValue(DateFormatUtils.format((Date) result,dateFormat));
        }else if(Calendar.class.isInstance(result)){
            cell.setCellValue(DateFormatUtils.format((Calendar) result,dateFormat));
        }else{
            cell.setCellValue(result != null ? result.toString() : "");
        }
    }
}