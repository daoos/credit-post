package localuse;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.hankcs.hanlp.utility.Predefine;

import conf.CmbConfig;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.*;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;
import parse.CmbParse;
import localuse.test.QuickStart;

import static localuse.test.QuickStart.query;


/**
 * Created by hpre on 17-3-4.
 */
public class ExcelRead {

    public static String excelPath = "/home/hpre/program/cmb/note/附-授信报告样本-招商银行2017531-系列6.xls";

    public static String writerPath = "/home/hpre/program/cmb/note/招行授信报告120份样本结果-20170602.xls";

    public static void main(String[] args) throws IOException, WriteException, BiffException {
//        excelRead(2);
        excelWriter2();
    }

    public static String excelWriter2() throws IOException, WriteException, BiffException {
        String s = "";

        InputStream is = null;
        is = new FileInputStream(excelPath);
        // 2、声明工作簿对象

        Workbook rwb = Workbook.getWorkbook(is);

        // 3、获得工作簿的个数,对应于一个excel中的工作表个数
        rwb.getNumberOfSheets();

        Sheet oFirstSheet = rwb.getSheet(0);// 使用索引形式获取第一个工作表，也可以使用rwb.getSheet(sheetName);其中sheetName表示的是工作表的名称
//          System.out.println("工作表名称：" + oFirstSheet.getName());
        int rows = oFirstSheet.getRows();//获取工作表中的总行数
        int columns = oFirstSheet.getColumns();//获取工作表中的总列数

        List<String> numList = new ArrayList<>();
        for (int i = 5; i < rows; i++) {
            Cell numCell = oFirstSheet.getCell(1, i);
            String num = numCell.getContents();
            if (!numList.contains(num)) {
                numList.add(num);
            }
        }

        CmbConfig cmbConfig;
        cmbConfig = CmbParse.loadConfig();
        CmbParse cmbParse = new CmbParse(cmbConfig);


        for (String eachFenHang : numList) {
            int j = 0;
            OutputStream os = null;
            File excel = new File("/home/hpre/program/cmb/note/fenhang/"+eachFenHang+".xls");
            os = new FileOutputStream(excel);
            WritableWorkbook workbook = Workbook.createWorkbook(os);
            WritableFont bold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);//设置字体种类和黑体显示,字体为Arial,字号大小为10,采用黑体显示
            WritableCellFormat titleFormate = new WritableCellFormat(bold);//生成一个单元格样式控制对象
            titleFormate.setAlignment(jxl.format.Alignment.LEFT);//单元格中的内容水平方向居中
            titleFormate.setVerticalAlignment(VerticalAlignment.TOP);//单元格的内容垂直方向居中
//          Label title = new Label(0,0,"JExcelApi支持数据类型详细说明",titleFormate);
            //创建新的一页
            WritableSheet sheet = workbook.createSheet("First Sheet", 0);
            for (int i = 0; i < rows; i++) {
                Cell numCell = oFirstSheet.getCell(1, i);//需要注意的是这里的getCell方法的参数，第一个是指定第几列，第二个参数才是指定第几行
                String num = numCell.getContents();
                if (num.equals(eachFenHang)) {
                    Cell oCell = oFirstSheet.getCell(2, i);
                    String contents = oCell.getContents();
                    List<String> parse = cmbParse.parse(contents);

                    for (String eachParse : parse) {
                        System.out.println("eachParse:"+eachParse);
                    }
                    Label nCell = new Label(1, j, contents, titleFormate);

                    Label sCell = new Label(2, j++, contents, titleFormate);
                    sheet.addCell(nCell);
                    sheet.addCell(sCell);
                }
            }
            workbook.write();
            workbook.close();
            os.close();
        }
        return s;
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
//          System.out.println("工作表名称：" + oFirstSheet.getName());
            int rows = oFirstSheet.getRows();//获取工作表中的总行数
            int columns = oFirstSheet.getColumns();//获取工作表中的总列数
            for (int i = 5; i < rows; i++)
            {
                Cell oCell= oFirstSheet.getCell(col,i);//需要注意的是这里的getCell方法的参数，第一个是指定第几列，第二个参数才是指定第几行
                String contents = oCell.getContents();
                System.out.println(i - 5);
                System.out.println(contents+"\n");
//                CmbParse cmbParse = new CmbParse(new CmbConfig());
//                List<String> parse = cmbParse.parse(contents);
//                for (String eachParse : parse) {
//                    System.out.println(eachParse);
//                }
                System.out.println("--------------------");
                System.out.println();
                FileWriter fileWriter = new FileWriter(new File("/home/hpre/program/cmb/4000份全文/" + oFirstSheet.getCell(0, i).getContents()));
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


    public static String excelWriter() throws IOException, WriteException {
        String s = "";
        OutputStream os = null;
        File dir = new File("/home/hpre/program/cmb/120份测试文本/");
        File[] files = dir.listFiles();
        int i = 1;
        os = new FileOutputStream(writerPath);
        WritableWorkbook workbook = Workbook.createWorkbook(os);
        WritableFont bold = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD);//设置字体种类和黑体显示,字体为Arial,字号大小为10,采用黑体显示
        WritableCellFormat titleFormate = new WritableCellFormat(bold);//生成一个单元格样式控制对象
        titleFormate.setAlignment(jxl.format.Alignment.LEFT);//单元格中的内容水平方向居中
        titleFormate.setVerticalAlignment(VerticalAlignment.TOP);//单元格的内容垂直方向居中
//        Label title = new Label(0,0,"JExcelApi支持数据类型详细说明",titleFormate);
        //创建新的一页
        WritableSheet sheet = workbook.createSheet("First Sheet",0);
        for (File file : files) {
            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
                String content = "";
                while (scanner.hasNext()) {
                    String strLine = scanner.nextLine();
                    if (content.equals("")) {
                        content = strLine;
                    }
                    else {
                        content = content + "\n" + strLine;
                    }
                }
                String query = query(content, "http://0.0.0.0:5004/cmb");
                System.out.println(query);
                String result = "审批授信风险点：";
                String tagArr[] = new String[]{"风险点", "贷款要求", "关联关系"};
                JSONObject jsonObject = new JSONObject(query);
                JSONArray riskArr = jsonObject.getJSONArray("风险点");
                JSONArray loanArr = jsonObject.getJSONArray("贷款要求");
                JSONArray relationArr = jsonObject.getJSONArray("关联关系");

                List<String> List1 = new ArrayList<>();
                List<String> List2 = new ArrayList<>();
                List<String> List3 = new ArrayList<>();



                for (int j = 1; j <= riskArr.length(); j++) {
                    JSONObject riskJson = riskArr.getJSONObject(j-1);
                    String action = riskJson.getString("action");
                    String object = riskJson.getString("object");
                    String second_class = riskJson.getString("second_class");
                    String third_class = riskJson.getString("third_class");
                    if (!List1.contains(action)) {
                        List1.add(action);
                        result = result + "\n" + j + "." + action + ":\n     " + object + " (" +second_class + "->" + third_class +")";
                    }
                    else {
                        String newStr = action+":\n    "+object+"("+second_class+"->"+third_class +")";
                        result = result.replace(action+":", newStr);
                    }
//                    System.out.println("riskJson-->"+riskJson);
                }
                result = result + "\n" + "贷后要求：";
                for (int j = 1; j <= loanArr.length(); j++) {
                    JSONObject loanJson = loanArr.getJSONObject(j-1);
                    String action = loanJson.getString("action");
                    String object = loanJson.getString("object");
                    String second_class = loanJson.getString("second_class");
                    String third_class = loanJson.getString("third_class");
                    if (!List2.contains(action)) {
                        List2.add(action);
                        result = result + "\n" + j + "." + action + ":\n     " + object + " (" +second_class + "->" + third_class +")";
                    }
                    else {
                        String newStr = action+":\n    "+object+"("+second_class+"->"+third_class +")";
                        result = result.replace(action+":", newStr);
                    }
//                    result = result + "\n" + j + "." + action + " " + object + " （" +second_class + "->" + third_class +")";
//                    System.out.println("loanJson-->"+loanJson);
                }
                result = result + "\n" + "关联关系：";
                for (int j = 1; j <= relationArr.length(); j++) {
                    JSONObject relationJson = relationArr.getJSONObject(j-1);
                    String Name = relationJson.getString("Name");
                    String relation = relationJson.getString("relation");
                    result = result + "\n" + j + "." + Name + " （" +relation +")";
//                    System.out.println("relationJson-->"+relationJson);
                }
                System.out.println("result----->" + result);



                Label numCell = new Label(0, i, "" + i, titleFormate);
                Label contentCell = new Label(1, i, content, titleFormate);
                Label resultCell = new Label(2, i++, result.replace(" <br/>", "\n  "), titleFormate);
                sheet.addCell(numCell);
                sheet.addCell(contentCell);
                sheet.addCell(resultCell);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RowsExceededException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            } finally {
                scanner.close();
            }
        }
        workbook.write();
        try {
            workbook.close();
        } catch (WriteException e) {
            e.printStackTrace();
        }
        os.close();
        return s;
    }


}
