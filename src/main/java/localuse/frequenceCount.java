package localuse;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import java.io.*;
import java.util.Scanner;

/**
 * Created by hpre on 16-12-19.
 */
public class frequenceCount {
    private static String input = "/home/hpre/else/文档/frequence2";
    public static String excelPath = "/home/hpre/program/cmb/附：文本分析-授信报告样本-20170209-03系列 超算结果分析反馈2.xls";

    public static void main(String[] args) {
//        count1();
//        count2();
//        count3();
//        count200();
        System.out.println();
        calProportion();
    }

    /*
    计算占的比例
     */
    public static void calProportion() {
        String calProportionFile = "/home/hpre/比例";
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(calProportionFile));
            double sum = 0;
            while (scanner.hasNext()) {
                String strLine = scanner.nextLine();
                String[] tabSplit = strLine.split("\t");
                sum += Double.parseDouble(tabSplit[1]);
            }
            System.out.println(sum);
            scanner.close();
            scanner = new Scanner(new File(calProportionFile));
            while (scanner.hasNext()) {
                String strLine = scanner.nextLine();
                String[] tabSplit = strLine.split("\t");
//                System.out.print(tabSplit[0]+"\t");
//                System.out.println(Double.parseDouble(tabSplit[1])/sum);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    /*
    统计各分类在200份里的词频
     */
    public static void count200() {
        String classFile = "/home/hpre/分类";
        Scanner scanner1 = null;
        try {
            scanner1 = new Scanner(new File(classFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (scanner1.hasNext()) {
            String strLine = scanner1.nextLine();
            if (strLine.startsWith("#") || strLine.startsWith("---") || strLine.length() < 2)
                continue;
            String[] colonSplit = strLine.split("：");
            if (!colonSplit[1].contains("；"))
                System.out.println(strLine);
            String[] semicolonSplit = colonSplit[1].split("；");
            int count = 0;
            InputStream is = null;
            try {
                is = new FileInputStream(excelPath);
                // 2、声明工作簿对象
                Workbook rwb = Workbook.getWorkbook(is);
                // 3、获得工作簿的个数,对应于一个excel中的工作表个数
                rwb.getNumberOfSheets();

                Sheet oFirstSheet = rwb.getSheet(1);// 使用索引形式获取第一个工作表，也可以使用rwb.getSheet(sheetName);其中sheetName表示的是工作表的名称
//              System.out.println("工作表名称：" + oFirstSheet.getName());
                int rows = oFirstSheet.getRows();//获取工作表中的总行数
                int columns = oFirstSheet.getColumns();//获取工作表中的总列数
                for (int i = 0; i < rows; i++) {
                    Cell oCell = oFirstSheet.getCell(3, i);//需要注意的是这里的getCell方法的参数，第一个是指定第几列，第二个参数才是指定第几行
                    String contents = oCell.getContents();
                    for (String eachObj : semicolonSplit) {
                        if (contents.contains(eachObj)) {
                            count++;
                            continue;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(colonSplit[0] + "\t" + count);
        }
        scanner1.close();
    }


    private static void count1()
    {
        //        List<String> strList = new ArrayList<>();
        int count = 1;
        String string = "";
        try {
            Scanner scanner = new Scanner(new File(input));
            while (scanner.hasNext())
            {
                String strLine = scanner.nextLine();
                if (string.equals(strLine))
                {
                    count++;
                }
                else
                {
                    System.out.println(string+"\t"+count);
                    count = 1;
                    string = strLine;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void count2()
    {
        String dirIn = "/home/hpre/Workspaces/MyEclipse 2015/cmbPython/redLabeled/";
        String dirOut = "/home/hpre/";
        File[] fileIns = new File(dirIn).listFiles();
        String biaozhu = "";
        for (File fileIn : fileIns) {
            try {
                Scanner scanner = new Scanner(fileIn);
                System.out.println(fileIn);
                StringBuffer sb = new StringBuffer();
                while (scanner.hasNext())
                {
                    String strLine = scanner.nextLine();
                    String[] spaceSplit = strLine.split(" ");
                    for (String eachSpaceWord : spaceSplit) {
                        if (eachSpaceWord.startsWith("#O#"))
                        {
                            if (eachSpaceWord.endsWith("#O#"))
                            {
                                String wordPos = eachSpaceWord.substring(3,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                System.out.println(slashSplit[0]);
                            }
                            else
                            {
                                biaozhu = "O";
                                String wordPos = eachSpaceWord.substring(3);
                                String[] slashSplit = wordPos.split("/");
                                sb.append(slashSplit[0]);
                            }
                        }
                        else
                        {
                            if (eachSpaceWord.endsWith("#O#"))
                            {
                                String wordPos = eachSpaceWord.substring(0,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                sb.append(slashSplit[0]);
                                System.out.println(sb.toString());
                                sb.delete(0,sb.length());
                                biaozhu = "";
                                continue;
                            }
                            if (biaozhu.equals("O"))
                            {
                                String[] slashSplit = eachSpaceWord.split("/");
                                sb.append(slashSplit[0]);
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void count3()
    {
        String dirIn = "/home/hpre/Workspaces/MyEclipse 2015/cmbPython/redLabeled/";
        String dirOut = "/home/hpre/";
        File[] fileIns = new File(dirIn).listFiles();
        for (File fileIn : fileIns) {
            try {
                Scanner scanner = new Scanner(fileIn);
//                System.out.println(fileIn);
                StringBuffer Osb = new StringBuffer();
                StringBuffer cSB = new StringBuffer();
                while (scanner.hasNext())
                {
                    String Obiaozhu = "";
                    String Cbiaozhu = "";
                    String C = "";
                    String strLine = scanner.nextLine();
                    if (!strLine.contains("#O#"))
                        continue;
                    String[] spaceSplit = strLine.split(" ");
                    for (String eachSpaceWord : spaceSplit) {
                        if (eachSpaceWord.startsWith("#C#"))
                        {
                            if (eachSpaceWord.endsWith("#C#"))
                            {
                                String wordPos = eachSpaceWord.substring(3,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                C = slashSplit[0];
                            }
                            else
                            {
                                Cbiaozhu = "C";
                                String wordPos = eachSpaceWord.substring(3);
                                String[] slashSplit = wordPos.split("/");
                                cSB.append(slashSplit[0]);
                            }
                        }
                        else
                        {
                            if (eachSpaceWord.endsWith("#C#"))
                            {
                                String wordPos = eachSpaceWord.substring(0,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                cSB.append(slashSplit[0]);
                                C = cSB.toString();
                                cSB.delete(0,cSB.length());
                                Cbiaozhu = "";
                                continue;
                            }
                            if (Cbiaozhu.equals("C"))
                            {
                                String[] slashSplit = eachSpaceWord.split("/");
                                cSB.append(slashSplit[0]);
                            }
                        }

                        if (eachSpaceWord.startsWith("#O#"))
                        {
                            if (eachSpaceWord.endsWith("#O#"))
                            {
                                String wordPos = eachSpaceWord.substring(3,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                System.out.println(C + "\t" +slashSplit[0]);
                            }
                            else
                            {
                                Obiaozhu = "O";
                                String wordPos = eachSpaceWord.substring(3);
                                String[] slashSplit = wordPos.split("/");
                                Osb.append(slashSplit[0]);
                            }
                        }
                        else
                        {
                            if (eachSpaceWord.endsWith("#O#"))
                            {
                                String wordPos = eachSpaceWord.substring(0,eachSpaceWord.length()-3);
                                String[] slashSplit = wordPos.split("/");
                                Osb.append(slashSplit[0]);
                                System.out.println(C + "\t" +Osb.toString());
                                Osb.delete(0,Osb.length());
                                Obiaozhu = "";
                                continue;
                            }
                            if (Obiaozhu.equals("O"))
                            {
                                String[] slashSplit = eachSpaceWord.split("/");
                                Osb.append(slashSplit[0]);
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
