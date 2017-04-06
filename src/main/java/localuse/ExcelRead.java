package localuse;

import com.hankcs.hanlp.utility.Predefine;
import conf.CmbConfig;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import parse.CmbParse;

import java.io.*;
import java.util.List;

/**
 * Created by hpre on 17-3-4.
 */
public class ExcelRead {

    public static String excelPath = "/home/hpre/program/cmb/note/附-授信报告样本-招商银行20170321-系列4.xls";

    public static void main(String[] args) {
        excelRead(1);
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

            Sheet oFirstSheet = rwb.getSheet(0);// 使用索引形式获取第一个工作表，也可以使用rwb.getSheet(sheetName);其中sheetName表示的是工作表的名称
//        System.out.println("工作表名称：" + oFirstSheet.getName());
            int rows = oFirstSheet.getRows();//获取工作表中的总行数
            int columns = oFirstSheet.getColumns();//获取工作表中的总列数
            for (int i = 6; i < rows; i++)
            {
                Cell oCell= oFirstSheet.getCell(col,i);//需要注意的是这里的getCell方法的参数，第一个是指定第几列，第二个参数才是指定第几行
                String contents = oCell.getContents();
                System.out.println(i - 5);
                System.out.println(contents+"\n");
                CmbParse cmbParse = new CmbParse(new CmbConfig());
                List<String> parse = cmbParse.parse(contents);
                for (String eachParse : parse) {
                    System.out.println(eachParse);
                }
                System.out.println("--------------------");
                System.out.println();
//                FileWriter fileWriter = new FileWriter(new File("/home/hpre/program/cmb/1000份全文/" + oFirstSheet.getCell(0, i).getContents()));
//                fileWriter.write(contents);
//                fileWriter.close();
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
