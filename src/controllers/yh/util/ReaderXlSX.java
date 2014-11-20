package controllers.yh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
@SuppressWarnings({"rawtypes","unused","deprecation"})
public class ReaderXlSX implements Cell{
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
        		//rowData.put(xlsxTitle[j], row.getCell((short) j).getStringCellValue().trim());
        		rowData.put(xlsxTitle[j], getCellFormatValue(row.getCell((short) j),xlsxTitle[j]).trim());
            	j++;
            	
            }
            xlsxContent.add(rowData);
        }
        return xlsxContent;
    }

    /**
     * 获取单元格数据内容为字符串类型的数据
     * 
     * @param cell Excel单元格
     * @return String 单元格数据内容
     */
	private String getStringCellValue(XSSFCell cell) {
        String strCell = "";
        switch (cell.getCellType()) {
        case XSSFCell.CELL_TYPE_STRING:
            strCell = cell.getStringCellValue();
            break;
        case XSSFCell.CELL_TYPE_NUMERIC:
            strCell = String.valueOf(cell.getNumericCellValue());
            break;
        case XSSFCell.CELL_TYPE_BOOLEAN:
            strCell = String.valueOf(cell.getBooleanCellValue());
            break;
        case XSSFCell.CELL_TYPE_BLANK:
            strCell = "";
            break;
        default:
            strCell = "";
            break;
        }
        if (strCell.equals("") || strCell == null) {
            return "";
        }
        if (cell == null) {
            return "";
        }
        return strCell;
    }
    /**
     * 获取单元格数据内容为日期类型的数据
     * 
     * @param cell
     *            Excel单元格
     * @return String 单元格数据内容
     */
	private String getDateCellValue(XSSFCell cell) {
		
        String result = "";
        try {
            int cellType = cell.getCellType();
            if (cellType == XSSFCell.CELL_TYPE_NUMERIC) {
                Date date = cell.getDateCellValue();
                result = (date.getYear() + 1900) + "-" + (date.getMonth() + 1)
                        + "-" + date.getDate();
            } else if (cellType == XSSFCell.CELL_TYPE_STRING) {
                String date = cell.getStringCellValue();
                result = date.replaceAll("[年月]", "-").replace("日", "").trim();
            } else if (cellType == XSSFCell.CELL_TYPE_BLANK) {
                result = "";
            }
        } catch (Exception e) {
            System.out.println("日期格式不正确!");
            e.printStackTrace();
        }
        return result;
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
    public static String[] getXlsTitle(String xlsPath) throws Exception {
    	
    	InputStream is = new FileInputStream(xlsPath);
    	ReaderXlSX excelReader = new ReaderXlSX();
    	excelReader.readExcelTitle(is);
    	return xlsxTitle;
    }
    
    //读取Excel表格标题
    public static String[] getXlsTitle(File xlsFile) throws Exception {
    	
    	InputStream is = new FileInputStream(xlsFile);
    	ReaderXlSX excelReader = new ReaderXlSX();
        excelReader.readExcelTitle(is);
    	return xlsxTitle;
    }
    
    //读取Excel表格内容
    public static List<Map<String,String>> getXlsContent(String xlsPath) throws Exception {
    	InputStream is = new FileInputStream(xlsPath);
    	ReaderXlSX excelReader = new ReaderXlSX();
    	xlsxContent = excelReader.readExcelContent(is);
    	return xlsxContent;
    }
    
    //读取Excel表格内容
    public static List<Map<String,String>> getXlsContent(File xlsFile) throws Exception {
    	InputStream is = new FileInputStream(xlsFile);
    	ReaderXlSX excelReader = new ReaderXlSX();
    	xlsxContent = excelReader.readExcelContent(is);
    	return xlsxContent;
    }
    
    //获取xls文件内容
    public static Map<String, Object> readerXLS(String xlsPath) throws Exception {
    	Map<String, Object> xlsData = new HashMap<String, Object>();
		//读取Excel表格标题
    	InputStream is = new FileInputStream(xlsPath);
    	ReaderXlSX excelReader = new ReaderXlSX();
        excelReader.readExcelTitle(is);
        //读取Excel表格内容
        InputStream is2 = new FileInputStream(xlsPath);
        excelReader.readExcelContent(is2);
        //设值
        xlsData.put("title", xlsxTitle);
        xlsData.put("content", xlsxContent);
	        
    	return xlsData;
    	
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
        	
            // 对读取Excel表格标题测试
            InputStream is = new FileInputStream("d:\\广电运通.xlsx");
            ReaderXlSX excelReader = new ReaderXlSX();
            String[] title = excelReader.readExcelTitle(is);
            System.out.println("获得Excel表格的标题:");
            for (String s : title) {
                System.out.print(s + " ");
            }

            // 对读取Excel表格内容测试
            InputStream is2 = new FileInputStream("d:\\广电运通.xlsx");
            //Map<Integer, String> map = excelReader.readExcelContent(is2);
            List<Map<String,String>> content = excelReader.readExcelContent(is2);
        	for (Map map2 : content) {
            	System.out.println();
            	for (int i = 0; i <= map2.size()-1; i++) {
                    System.out.print(title[i]+":"+map2.get(title[i])+"  ");
                }
			}
				
        } catch (FileNotFoundException e) {
            System.out.println("未找到指定路径的文件!");
            e.printStackTrace();
        }
    }

	@Override
	public CellRangeAddress getArrayFormulaRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getBooleanCellValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getCachedFormulaResultType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Comment getCellComment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCellFormula() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellStyle getCellStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCellType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getColumnIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Date getDateCellValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getErrorCellValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Hyperlink getHyperlink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getNumericCellValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RichTextString getRichStringCellValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Row getRow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRowIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Sheet getSheet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStringCellValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPartOfArrayFormulaGroup() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeCellComment() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAsActiveCell() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellComment(Comment arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellErrorValue(byte arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellFormula(String arg0) throws FormulaParseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellStyle(CellStyle arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellType(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellValue(double arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellValue(Date arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellValue(Calendar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellValue(RichTextString arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellValue(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellValue(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHyperlink(Hyperlink arg0) {
		// TODO Auto-generated method stub
		
	}
}
