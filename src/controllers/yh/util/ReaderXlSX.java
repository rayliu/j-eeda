package controllers.yh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReaderXlSX{
    private XSSFWorkbook wb;
    private XSSFSheet sheet;
    private XSSFRow row;
    private static String[] xlsxTitle;
	private static List<Map<String,String>> xlsxContent = new ArrayList<Map<String,String>>();;

    /**
     * 读取Excel表格表头的内容
     * @param InputStream
     * @return String 表头内容的数组
     */
    private String[] readExcelTitle(InputStream is) {
        try {
            wb = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sheet = wb.getSheetAt(0);
        row = sheet.getRow(0);
        // 标题总列数
        int colNum = row.getPhysicalNumberOfCells();
        System.out.println("colNum:" + colNum);
        //String[] title = new String[colNum];
        xlsxTitle = new String[colNum];
        for (int i = 0; i < colNum; i++) {
            //title[i] = getStringCellValue(row.getCell((short) i));
            xlsxTitle[i] = row.getCell((short) i).getStringCellValue().trim();
        }
        return xlsxTitle;
    }

    /**
     * 读取Excel数据内容
     * @param InputStream
     * @return Map 包含单元格数据内容的Map对象
     */
    private List<Map<String,String>> readExcelContent(InputStream is) {
        //String str = "";
        try {
            wb = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sheet = wb.getSheetAt(0);
        // 得到总行数
        int rowNum = sheet.getLastRowNum();
        row = sheet.getRow(0);
        /*
        getPhysicalNumberOfCells 是获取不为空的列个数。 
        getLastCellNum 是获取最后一个不为空的列是第几个。
        */
        int colNum = row.getPhysicalNumberOfCells();
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 1; i <= rowNum; i++) {
        	Map<String, String> rowData = new HashMap<String, String>();
            row = sheet.getRow(i);
            int j = 0;
            while (j < colNum) {
        		rowData.put(xlsxTitle[j], getCellFormatValue(row.getCell((short) j),xlsxTitle[j]).trim());
            	j++;
            	
            }
            xlsxContent.add(rowData);
        }
        return xlsxContent;
    }
    
	/**
     * 根据XSSFCell类型设置数据
     * @param cell
     * @return
     */
    private String getCellFormatValue(XSSFCell cell,String cellHead) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
            // 如果当前Cell的Type为NUMERIC
            case XSSFCell.CELL_TYPE_NUMERIC:
            case XSSFCell.CELL_TYPE_FORMULA: {
                // 判断当前的cell是否为Date
                if ("计划日期".equals(cellHead) || "预计到货日期".equals(cellHead)) {
                    // 如果是Date类型则，转化为Data格式
                    
                    //方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
                    //cellvalue = cell.getDateCellValue().toLocaleString();
                    
                    //方法2：这样子的data格式是不带带时分秒的：2011-10-12
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    cellvalue = sdf.format(date);
                }
                // 如果是纯数字
                else {
                    // 取得当前Cell的数值,返回：3.000008976E8
                    //cellvalue = String.valueOf(cell.getNumericCellValue());
                	cellvalue = String.format("%.0f", cell.getNumericCellValue());
                }
                break;
            }
            // 如果当前Cell的Type为STRING
            case XSSFCell.CELL_TYPE_STRING:
                // 取得当前的Cell字符串
                cellvalue = cell.getRichStringCellValue().getString();
                break;
            // 默认的Cell值
            default:
                cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }
    
    //读取Excel表格标题
    public static String[] getXlsTitle(File xlsFile) throws Exception {
    	
    	InputStream is = new FileInputStream(xlsFile);
    	ReaderXlSX excelReader = new ReaderXlSX();
        excelReader.readExcelTitle(is);
    	return xlsxTitle;
    }
    
    //读取Excel表格内容
    public static List<Map<String,String>> getXlsContent(File xlsFile) throws Exception {
    	InputStream is = new FileInputStream(xlsFile);
    	ReaderXlSX excelReader = new ReaderXlSX();
    	xlsxContent = excelReader.readExcelContent(is);
    	return xlsxContent;
    }
    
    //获取xls文件内容
    public static Map<String, Object> readerXLS(File xlsFile) throws Exception {
    	Map<String, Object> xlsData = new HashMap<String, Object>();
		//读取Excel表格标题
    	InputStream is = new FileInputStream(xlsFile);
    	ReaderXlSX excelReader = new ReaderXlSX();
        excelReader.readExcelTitle(is);
        //读取Excel表格内容
        InputStream is2 = new FileInputStream(xlsFile);
        excelReader.readExcelContent(is2);
        //设值
        xlsData.put("title", xlsxTitle);
        xlsData.put("content", xlsxContent);
	        
    	return xlsData;
    	
    }
    public static void main(String[] args) {
    	try {
    		File file = new File("d:\\广电运通.xlsx");
    		getXlsTitle(file);
			getXlsContent(file);
			for (String str: xlsxTitle) {
				System.out.print(str + " ");
			}
			System.out.println();
			for (Map map : xlsxContent) {
				for (int i = 0; i < xlsxTitle.length; i++) {
					System.out.print(xlsxTitle[i] + ":" + xlsxContent.get(i).get(xlsxTitle[i]) + "	");
				}
				System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    
}
