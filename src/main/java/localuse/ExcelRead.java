package localuse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.hanlp.utility.Predefine;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;



/**
 * Created by hpre on 17-3-4.
 */
public class ExcelRead {

    public static String excelPath = "/home/hpre/program/cmb/附：文本分析-授信报告样本-20170209-03系列 超算结果分析反馈2.xls";

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
            for (int i = 0; i < rows; i++)
            {

                Cell oCell= oFirstSheet.getCell(col,i);//需要注意的是这里的getCell方法的参数，第一个是指定第几列，第二个参数才是指定第几行
                String contents = oCell.getContents();
                System.out.println(contents);
                StandardTokenizer.SEGMENT.enableNameRecognize(true);
                StandardTokenizer.SEGMENT.enableOrganizationRecognize(true);
                StandardTokenizer.SEGMENT.enableJapaneseNameRecognize(false).enableIndexMode(false).
                        enableTranslatedNameRecognize(false).enablePlaceRecognize(false);

                List<Term> segStr = StandardTokenizer.segment(contents);
//                System.out.println(contents);
                for (Term term : segStr)
                {
//                    System.out.print(term.word+" ");
//                    if (term.nature.toString().equals("nt"))
//                    {
//                        System.out.println("公司-->"+term.word);
//                    }
//                    else if (term.nature.toString().equals("nr"))
//                        System.out.println("人名-->"+term.word);
//                    else if (term.nature.toString().equals("ntc"))
//                    {
//                        System.out.println("ss" +term.word);
//                    }
                }
//                System.out.println();
//                System.out.println(segStr);
//                    System.out.println(oCell.getContents()+"\r\n");

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
