package localuse;

import com.hankcs.hanlp.utility.Predefine;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.*;

/**
 * Created by hpre on 17-3-4.
 */
public class ExcelRead {

    public static String excelPath = "/home/hpre/program/cmb/note/280份授信报告样本——类型提取2.xls";

    public static void main(String[] args) {
        excelRead(2);
    }


    public static String excelRead(int col)
    {
        Predefine.HANLP_PROPERTIES_PATH = "/home/hpre/program/cmb/model/hanlp.properties";

        String s = "";
        InputStream is = null;
        try {
            is = new FileInputStream(excelPath);
            // 2、声明工作簿对象
            Workbook rwb = Workbook.getWorkbook(is);
            // 3、获得工作簿的个数,对应于一个excel中的工作表个数
            rwb.getNumberOfSheets();

            Sheet oFirstSheet = rwb.getSheet(1);// 使用索引形式获取第一个工作表，也可以使用rwb.getSheet(sheetName);其中sheetName表示的是工作表的名称
//        System.out.println("工作表名称：" + oFirstSheet.getName());
            int rows = oFirstSheet.getRows();//获取工作表中的总行数
            int columns = oFirstSheet.getColumns();//获取工作表中的总列数
            for (int i = 4; i < rows; i++)
            {
                Cell oCell= oFirstSheet.getCell(col,i);//需要注意的是这里的getCell方法的参数，第一个是指定第几列，第二个参数才是指定第几行
                String contents = oCell.getContents();
                FileWriter fileWriter = new FileWriter(new File("/home/hpre/program/cmb/280份全文/" + oFirstSheet.getCell(0, i).getContents()));
                fileWriter.write(contents);
                fileWriter.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

}
