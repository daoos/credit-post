package localuse.test;

import org.json.JSONArray;
import org.json.JSONObject;
import parse.CmbParse;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by hadoop on 17-4-10.
 */
public class PaserTest {
    public static int num;
    public static void main(String[] args) {
        PaserTest paserTest = new PaserTest();
        File dirInput = new File(args[0]);
        File[] files = dirInput.listFiles();
        num=0;
        jishu = new int[second_class.length][third_class.length];
        for(int i=0;i<second_class.length;i++)
            for(int j=0;j<third_class.length;j++){
                PaserTest.jishu[i][j]=0;
            }
        jishu1 = new int[four_class.length];
        for(int i=0;i<four_class.length;i++)
            PaserTest.jishu1[i]=0;
        for (File file: files) {
            System.out.println(file);

            if(file.toString().endsWith("/23.txt"))
                System.out.println();

//			FileWriter fileWriter = new FileWriter(file);
;
            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
                while (scanner.hasNextLine()) {

                    String strLine = scanner.nextLine();
//                    paserTest.parse(strLine);
                    paserTest.parse1(strLine);
//                    for (String eachResult : inference) {
//                        System.out.println("eachResult:\t"+eachResult);
//                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            finally {
                scanner.close();
//				fileWriter.flush();
//				fileWriter.close();
            }
        }
        paserTest.outPer();
    }
    public static int[][] jishu=null;
    public static int[] jishu1=null;
    public static String four_class[]= new String[]{"经营","财务", "融资"};
    public static String second_class[] = new String[]{"经营","财务", "担保", "行业", "投资", "贷款用途", "其他"};
    public static String third_class[] = new String[]{"上市类","经营情况类", "股权类", "效益类", "客户合作关系类", "资金类", "业务类", "结算类", "经营类", "融资类", "财务类", "负债类","信用类","经济类","行业类","战略类","投资类","贷后管理类","日常经营周转类","原材料采购类","授信类","信息类"};
    public static final String PATH_1="/home/hadoop/wnd/usr/cmb/统计/贷款要求";
    public static final String PATH_2="/home/hadoop/wnd/usr/cmb/统计/风险点";
    private void outPer(){
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();

        // 设置精确到小数点后1位
        numberFormat.setMaximumFractionDigits(1);

        double ss=0.0;
        double last=0.0;
        String lastStr="";
        for(int i=0;i<second_class.length;i++)
            for(int j=0;j<third_class.length;j++){
                if(jishu[i][j]>0){
                    String result = numberFormat.format((float) jishu[i][j] / (float) num * 100);
                    last=Double.parseDouble(result);
                    ss+=last;
                    lastStr=second_class[i]+"->"+third_class[j];
                    System.out.println(lastStr+"\t百分比=\t"+result);
                }
            }
        for(int i=0;i<four_class.length;i++){
            if(jishu1[i]>0){
                String result = numberFormat.format((float) jishu1[i]/ (float) num * 100);
                last=Double.parseDouble(result);
                ss+=last;
                lastStr=four_class[i];
                System.out.println(lastStr+"\t百分比=\t"+result+"\t"+jishu1[i]);
            }
        }
        if(ss!=100){
            System.out.println(lastStr+"\t百分比=\t"+numberFormat.format(100-ss+last));
        }

    }
    private void parse(String testJson){
        JSONObject jsonObject = new JSONObject(testJson);
        if(jsonObject!=null){
            JSONArray jsonArray = jsonObject.getJSONArray("贷款要求");
            if(jsonArray!=null){
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    if(jsonObject1!=null){
                        String second_str = jsonObject1.getString("second_class");
                        if(second_str!=null&&!second_str.equals("")){
                            String third_str = jsonObject1.getString("third_class");
                            String object = jsonObject1.getString("object").replaceAll("<br/>","");
                            String action = jsonObject1.getString("action");
                            printFile(action+"\t"+object+"\n",PATH_1+"/"+second_str+"/"+third_str,true);// action object写文件
                            for(int x=0;x<second_class.length;x++)
                                for(int y=0;y<third_class.length;y++) {
                                    if(second_class[x].equals(second_str)&&third_class[y].equals(third_str)) {
                                        jishu[x][y]++;
                                        num++;//文本数量;
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
    private void parse1(String testJson){
        JSONObject jsonObject = new JSONObject(testJson);
        if(jsonObject!=null){
            JSONArray jsonArray = jsonObject.getJSONArray("风险点");
            if(jsonArray!=null){
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    if(jsonObject1!=null){
                        String second_str = jsonObject1.getString("second_class");
                        if(second_str!=null&&!second_str.equals("")){
                            if(jsonObject.getJSONArray("关联关系")!=null&&jsonObject.getJSONArray("关联关系").length()>0){
                                JSONObject jsonObject2 = jsonObject.getJSONArray("关联关系").getJSONObject(0);

                                String name = jsonObject2.getString("Name");
                                String third_str = jsonObject1.getString("third_class");
                                String object = jsonObject1.getString("object").replaceAll("<br/>","");
                                String action = jsonObject1.getString("action");
                                printFile(name+"\t"+action+"\t"+object+"\n",PATH_2+"/"+second_str,true);// action object写文件
                                for(int x=0;x<four_class.length;x++) {
                                    if(four_class[x].equals(second_str)) {
                                        jishu1[x]++;
                                        num++;//文本数量;
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
    public static void printFile(String s, String outPath, Boolean append) {
        try {
            FileOutputStream e = new FileOutputStream(outPath, append.booleanValue());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(e);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(s);
            bufferedWriter.close();
            outputStreamWriter.close();
            e.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }
}
