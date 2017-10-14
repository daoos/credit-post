package localuse;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.*;
import java.util.*;

/**
 *1、将159个分行的测试集的表格中授信文本结构化结果分字段存入表格中；
 *2、计算授信文本结构化效果的准确率、召回率、F值
 * Created by hpre on 17-10-10.
 */
public class Random2 {

    public static void main(String[] args) throws WriteException, IOException, BiffException {
//        run();
        calculate();
    }

    private static void calculate() throws IOException, BiffException {
        File excelIn1 = new File("/home/hpre/program/cmb/note/fenhang/"+"random (复件).xls");
        InputStream is1 = new FileInputStream(excelIn1);
        Workbook wbIn1 = Workbook.getWorkbook(is1);
        Sheet sheet1 = wbIn1.getSheet(0);
        File excelIn2 = new File("/home/hpre/program/cmb/note/fenhang/"+"random-检查版 (复件).xls");
        InputStream is2 = new FileInputStream(excelIn2);
        Workbook wbIn2 = Workbook.getWorkbook(is2);
        Sheet sheet2 = wbIn2.getSheet(0);

        List<String> bigBigClassList = new ArrayList<>();
        bigBigClassList.add("贷款要求:");
        bigBigClassList.add("风险点:");
        bigBigClassList.add("关联关系:");

        int accuracy = 0;
        int sum1 = 0;
        int sum2 = 0;
        int rows = sheet1.getRows();

        for (int i = 0; i < rows; i++) {
            List<String> trueList = new ArrayList<>();
            Map<String, List<String>> trueMap = new HashMap<>();
            List<String> nowList = new ArrayList<>();
            Map<String, List<String>> nowMap = new HashMap<>();

            String num = sheet1.getCell(0, i).getContents();
            String nowResult = sheet1.getCell(2, i).getContents();
            String trueResult = sheet2.getCell(2, i).getContents();
            String[] twoSpaceSplit1 = nowResult.split("\n\n");
            for (String eachLine : twoSpaceSplit1) {
                if (eachLine.contains("\n")) {
                    String[] oneSpaceSplit = eachLine.split("\\n");
                    for (String each : oneSpaceSplit) {
                        if (bigBigClassList.contains(each)) {
                            continue;
                        }
                        if (each.contains("->")) {
                            continue;
                        }
                        System.out.println(each);
                        nowList.add(each);
                    }
                }
            }
            String[] twoSpaceSplit2 = trueResult.split("\n\n");
            for (String eachLine : twoSpaceSplit2) {
                if (eachLine.contains("\n")) {
                    String[] oneSpaceSplit = eachLine.split("\\n");
                    for (String each : oneSpaceSplit) {
                        if (bigBigClassList.contains(each)) {
                            continue;
                        }
                        if (each.contains("->")) {
                            continue;
                        }
                        System.out.println(each);
                        trueList.add(each);
                    }
                }
            }

            trueMap.put(num, trueList);
            nowMap.put(num, nowList);
            Set<String> keySet = trueMap.keySet();
            for (String key : keySet) {
                List<String> list1 = trueMap.get(key);
                List<String> list2 = nowMap.get(key);
                sum1 += list1.size();
                sum2 += list2.size();
                for (String eachResult : list1) {
                    if (list2.contains(eachResult)) {
                        accuracy++;
                    }
                }
            }
        }
        System.out.println(accuracy);
        System.out.println(sum1);
        System.out.println(sum2);

        wbIn1.close();
        is1.close();
        wbIn2.close();
        is2.close();
    }

    private static void run() throws IOException, BiffException, WriteException {
        File excelIn = new File("/home/hpre/program/cmb/note/fenhang/"+"random.xls");
        InputStream is = new FileInputStream(excelIn);
        Workbook wbIn = Workbook.getWorkbook(is);
        Sheet sheet = wbIn.getSheet(0);
        int rows = sheet.getRows();

        File excelOut = new File("/home/hpre/program/cmb/note/fenhang/"+"random2.xls");
        OutputStream os = new FileOutputStream(excelOut);
        WritableWorkbook wbOut = Workbook.createWorkbook(os);
        WritableSheet wrsheet = wbOut.createSheet("First Sheet", 0);

        int l = 0;

        Map<Integer, Integer> mergeMap = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            Cell sheet1Cell = sheet.getCell(2, i);
            String result = sheet1Cell.getContents();
            System.out.println(result);
            String[] twoLineSplit = result.split("\n\n");
            System.out.println(twoLineSplit);
            int q = 0;
            for (String eachLines : twoLineSplit) {
                String[] oneLineSplit = eachLines.split("\n");
                System.out.println(oneLineSplit);
                for (int i1 = 0; i1 < oneLineSplit.length; i1++) {
                    String bigBigClass = oneLineSplit[0].substring(0, oneLineSplit[0].length() - 1); // 大大类
                    if (i1 > 0) {
                        String eachLine = oneLineSplit[i1];
                        // eachLine : 用于:日常流动资金周转  (贷款用途->日常经营周转类)

                        if (eachLine.equals("不得用于:") || eachLine.contains("<span style") || eachLine.endsWith(":")) {
                            System.out.println(sheet.getCell(0, i).getContents());
                            System.out.println(eachLine);
                            continue;
                        }

                        Label numCell = new Label(0, l , sheet.getCell(0, i).getContents());
                        Label contentCell = new Label(1, l, sheet.getCell(1, i).getContents());
                        Label resultCell = new Label(2, l, result);
                        wrsheet.addCell(numCell);
                        wrsheet.addCell(contentCell);
                        wrsheet.addCell(resultCell);

                        if (bigBigClass.contains("关联关系")) {
                            String[] twoSpaceSplit = eachLine.split(" ");
                            String name = twoSpaceSplit[0];
                            String relation = twoSpaceSplit[1].substring(1, twoSpaceSplit[1].length() - 1);
                            Label nameLabel = new Label(3, l, name);
                            Label relationLabel = new Label(4, l,relation);
                            Label bigBigClassLabel = new Label(5, l, "关联关系");
                            wrsheet.addCell(nameLabel);
                            wrsheet.addCell(relationLabel);
                            wrsheet.addCell(bigBigClassLabel);
                            q++;
                            l++;
                        } else if (bigBigClass.contains("风险点") || bigBigClass.contains("贷款要求")) {
                            String condition = ""; // 条件
                            String bigClass = ""; // 大类
                            String smallClass = ""; // 小类
                            String action = ""; // 动作
                            String object = "";// 对象
                            String explation = ""; // 补充

                            if (eachLine.contains("【") && eachLine.contains("】")) {
                                int leftIndex = eachLine.indexOf("【");
                                int rightIndex = eachLine.indexOf("】");
                                explation = eachLine.substring(leftIndex + 1, rightIndex);
                                eachLine = eachLine.replace("【" + explation + "】", "");
                            }

                            if (eachLine.contains("条件:")) {
                                int objectIndex = eachLine.indexOf("动作:");
                                action = eachLine.substring(0, objectIndex);
                                condition = action.substring(3);
                                String objectAndClass = eachLine.substring(objectIndex);
                                String[] twoSpaceSplit = objectAndClass.split("  ");
                                object = twoSpaceSplit[0];
                                String bigClassAndSmallCalss = twoSpaceSplit[1].substring(1, twoSpaceSplit[1].length() - 1);
                                if (!bigClassAndSmallCalss.equals("->")) {
                                    String[] bigClassAndSmallCalssSplit = bigClassAndSmallCalss.split("->");
                                    bigClass = bigClassAndSmallCalssSplit[0];
                                    smallClass = bigClassAndSmallCalssSplit[1];
                                }
                            } else {
                                String[] contentAndClass = eachLine.split("  ");
                                String[] actionAndObject = contentAndClass[0].split(":");
                                if (!contentAndClass[0].contains(":")) {
                                    action = contentAndClass[0];
                                } else {
                                    action = actionAndObject[0];
                                    System.out.println(contentAndClass[0]);
                                    object = actionAndObject[1];
                                }
                                String bigClassAndSmallCalss = contentAndClass[1].substring(1, contentAndClass[1].length() - 1);
                                if (!bigClassAndSmallCalss.equals("->")) {
                                    String[] bigClassAndSmallCalssSplit = bigClassAndSmallCalss.split("->");
                                    bigClass = bigClassAndSmallCalssSplit[0];
                                    smallClass = bigClassAndSmallCalssSplit[1];
                                }
                            }

                            Label conditionCell = new Label(3, l, condition);
                            Label actionCell = new Label(4, l, action);
                            Label objectCell = new Label(5, l, object);
                            Label bigClassCell = new Label(6, l, bigClass);
                            Label smallClassCell = new Label(7, l, smallClass);
                            Label explationCell = new Label(8,l, explation);
                            Label bigBigClassCell = new Label(9, l, bigBigClass);
                            wrsheet.addCell(conditionCell);
                            wrsheet.addCell(actionCell);
                            wrsheet.addCell(objectCell);
                            wrsheet.addCell(bigClassCell);
                            wrsheet.addCell(smallClassCell);
                            wrsheet.addCell(explationCell);
                            wrsheet.addCell(bigBigClassCell);
                            q++;
                            l++;
                        }
                    }
                }
            }

//            wbOut.close();
//
//            wrsheet.mergeCells(0, 0, i, i + q);
//            wrsheet.mergeCells(1, 1, i, i + q);
//            wrsheet.mergeCells(2, 2, i, i + q);
            mergeMap.put(i, q);
//            l += q;
        }

        int p = 0;
        for (int i = 0; i < rows; i++) {
            int q = mergeMap.get(i);
            wrsheet.mergeCells(0, p, 0, p + q);
            wrsheet.mergeCells(1, p, 1, p + q);
            wrsheet.mergeCells(2, p, 2, p + q);
            p += q;
        }

        wbOut.write();
        is.close();
        wbIn.close();
        wbOut.close();
        os.close();
    }

}
