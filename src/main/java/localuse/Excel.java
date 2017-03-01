package localuse;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Created by hpre on 17-2-20.
 */
public class Excel {

    public static void main(String[] args) {
        extractContentFromExcel();
    }

    //从表格中抽取内容
    public static void extractContentFromExcel()
    {
        Scanner scanner = null;
        FileWriter fileWriter = null;
        List<String> strList = new ArrayList<>();
        try {
            scanner = new Scanner(new File("/home/hpre/program/cmb/3"));
            fileWriter = new FileWriter(new File("/home/hpre/program/cmb/3-out"));
            while (scanner.hasNext())
            {
                String strLine = scanner.nextLine();
                System.out.println(strLine);
                if (strLine.length()<3)
                    continue;
//                if (strLine.startsWith("\""));
//                {
//                    strLine = strLine.substring(1);
//                }
                if (strLine.startsWith("1.")||strLine.startsWith("2.")||strLine.startsWith("3.")||strLine.startsWith("4.")||strLine.startsWith("5."))
                {
                    strLine = strLine.substring(2);
                }
                if (strLine.startsWith("."))
                {
                    strLine = strLine.substring(1);
                }
                if (!strList.contains(strLine))
                {
                    strList.add(strLine);
                }
                System.out.println(strLine);
//                fileWriter.write(strLine+"\n");
            }
            Collections.sort(strList);
            for (String str : strList) {
                fileWriter.write(str+"\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
    public static String getContent() throws Exception {
        //构建Workbook对象, 只读Workbook对象
        //直接从本地文件创建Workbook
        //从输入流创建Workbook

        FileInputStream fis = new FileInputStream("/home/hpre/program/cmb/附：文本分析-授信报告样本-20170209-03系列 超算结果分析反馈.xlsx");
        StringBuilder sb = new StringBuilder();
        jxl.Workbook rwb = Workbook.getWorkbook(fis);
        //一旦创建了Workbook，我们就可以通过它来访问
        //Excel Sheet的数组集合(术语：工作表)，
        //也可以调用getsheet方法获取指定的工资表
        Sheet[] sheet = rwb.getSheets();
        for (int i = 0; i < sheet.length; i++) {
            Sheet rs = rwb.getSheet(i);
            for (int j = 0; j < rs.getRows(); j++) {
                Cell[] cells = rs.getRow(j);
                for(int k=0;k<cells.length;k++)
                sb.append(cells[k].getContents());
            }
        }
        fis.close();
        return sb.toString();
    }
}
