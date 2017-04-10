package localuse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**

 * Created by hpre on 16-10-27.
 */
public class ChangeToFeature
{
    private static String train_file = "/home/hpre/program/cmb/cmbSenten/";
    private static String out_file = "/home/hpre/program/cmb/model/280cmbSenten.crfpp";//280cmbCom.crfpp";

//    public static String biaoZhu[] = new String[]{"OPER","ANES","DATE","TIME","DIET","STYL","MEAS","OTHE"};
    public static String biaoZhu[] = new String[]{"CS"};
//    public static String biaoZhu[] = new String[]{"O","T","D","P","C","S","N","A","Q","De"};
//    public static String biaoZhu[] = new String[]{"VN","AC", "OB", "EX", "QU", "VC", "AD", "VE", "PP", "NA", "CO", "PE"};
    public static String sep = "_"; // "_"  "/"

//    private static String train_file = "/home/hpre/else/文档/out/";
//    private static String out_file = "/home/hpre/xiangya/pythonChengfen/featureCom3.com.qdcz.crfpp";
    public static void main(String[] args) throws IOException

    {
        readWriter();

    }
    /*
    文件读写和写入
     */
    public static void readWriter() throws IOException
    {
        List<String> filesPath = listFile(train_file);
        Scanner scanner = null;
        File file;
        FileWriter fileWriter = new FileWriter(new File(out_file));
        for (String filePath : filesPath)
        {
            file = new File(filePath);
//            String result="";
            if(file.toString().endsWith("/145.txt"))
                System.out.println();
            try
            {
                scanner = new Scanner(file);
                String strLine;
                int i = 0;
                while (scanner.hasNext())
                {
                    strLine = scanner.nextLine();
                    System.out.println(filePath);
                    System.out.println(i+"行");
                    System.out.println(strLine);
                    i++;

//                    String lineResult = dealLine1(strLine);
//                    result+=lineResult+"\n";
                    String lineResult = dealLine(strLine);
                    fileWriter.write("#SENT_BEG#\tbegin\tOUT"+"\n"+lineResult+"#SENT_END#\tend\tOUT"+"\n"+"\n");
                    System.out.println(lineResult);
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            scanner.close();
//            FileWriter fileWriter = new FileWriter(new File(filePath));
//            fileWriter.write(result);
//            fileWriter.flush();
//            fileWriter.close();
        }
        fileWriter.flush();
        fileWriter.close();
    }

    public static String dealLine1(String line)
    {
        String result = "";
        if(line.equals(result))
            return result;
        String biaozhu="#CS#";
        String[] lineSpaceSplit = line.split("\t");
        boolean outTag = true;
        String strBiaoZhu = null;
        //标注是否为OUT的标志，为true则为OUT。
        for (String splitedStr : lineSpaceSplit)
        {
            String[] slashSplit = splitedStr.split(sep);
            if(slashSplit[1].equals("w")&&slashSplit[0].equals("。"))
                System.out.println();
            if(slashSplit[1].equals("w")&&(slashSplit[0].equals("。")||slashSplit[0].equals("；")))
                splitedStr=biaozhu+splitedStr+biaozhu;

            result+=splitedStr+"\t";
        }
        return result;
    }
    /*
    处理每一行
    #SENT_BEG#/begin 拟/v #TIME#16/m :/w 30/m#TIME# 送/v OR/nx 行/ng #STYL#腹腔镜/n#STYL# l/nx #OPER#阑尾/n 切除/v 术/ng#OPER#
     #SENT_END#/end
     */
    public static String dealLine(String line)
    {
        String result = "";
        line=line.replaceAll(" ","\t");
        String[] lineSpaceSplit = line.split("\t");
        boolean outTag = true;
        String strBiaoZhu = null;
        //标注是否为OUT的标志，为true则为OUT。
        for (String splitedStr : lineSpaceSplit)
        {

//            strBiaoZhu = "CS";
            if (splitedStr.equals("#SENT_BEG#/begin")||splitedStr.equals("#SENT_END#/end"))
            {
                String[] slashSplit = splitedStr.split(sep);
                result = result+slashSplit[0]+"\t"+slashSplit[1]+"\t"+"OUT"+"\n";
//                System.out.println(result);
                continue;
            }

            if (splitedStr.startsWith("#"))
            {

                for (int i = 0; i < biaoZhu.length; i++)
                {
                    if (splitedStr.contains("De"))
                    {
                        strBiaoZhu = "De";
                        break;
                    }
                    //搜索是哪个标注 //DATE
                    if (splitedStr.contains(biaoZhu[i]))
                    {
                        strBiaoZhu = biaoZhu[i];
                        break;
                    }
                }
                if (splitedStr.endsWith("#"))
                {
                    //以#开头且以#结尾，那么肯定是单个的 //  #DATE#今日/t#DATE#
                    String wordAndNature = splitedStr.substring((strBiaoZhu.length()+2),
                            splitedStr.length()-(strBiaoZhu.length()+2));
                    //  今日/t
                    String[] slashSplit = wordAndNature.split(sep);
                    result = result+slashSplit[0]+"\t"+slashSplit[1]+"\t"+strBiaoZhu+"_S"+"\n";
                    continue;
                }

                else
                {
                    //以#开头，但不以#结尾   //  #OPER#脓肿/a
                    outTag = false;
                    String wordAndNature = splitedStr.substring((strBiaoZhu.length()+2),
                            splitedStr.length());
                    //  今日/t
                    String[] slashSplit = wordAndNature.split(sep);
                    result = result+slashSplit[0]+"\t"+slashSplit[1]+"\t"+strBiaoZhu+"_B"+"\n";
                    continue;
                }
            }
            if (splitedStr.endsWith("#"))
            {
                System.out.println(splitedStr);
                //不以#开头，但以#结尾   引流术/n#OPER#
                outTag = true;
                String wordAndNature = splitedStr.substring(0,
                        splitedStr.length()-(strBiaoZhu.length()+2));
                //  今日/t
                String[] slashSplit = wordAndNature.split(sep);
                result = result+slashSplit[0]+"\t"+slashSplit[1]+"\t"+strBiaoZhu+"_E"+"\n";
                continue;

            }

            if (outTag)
            {
                //  拟/v
//                System.out.println(splitedStr);
                String[] slashSplit = splitedStr.split(sep);
                if (slashSplit.length>1)
                {

                    result = result+slashSplit[0]+"\t"+slashSplit[1]+"\t"+"OUT"+"\n";
                }
            }
            else
            {
                //  切开/v     //  #OPER#脓肿/a 切开/v 引流术/n#OPER#
                String[] slashSplit = splitedStr.split(sep);
                result = result+slashSplit[0]+"\t"+slashSplit[1]+"\t"+strBiaoZhu+"_M"+"\n";
            }

        }
//        System.out.println(result);
        return result;
    }


    public static List<String> listFile(String dirFile)
    {
        List<String> fileList = new ArrayList<>();
        File listFile = new File(dirFile);
        String absolutePath;
        if (listFile.isDirectory())
        {
            File[] files = listFile.listFiles();
            for (File file : files)
            {
                if (file.isFile())
                {
                    absolutePath = file.getAbsolutePath();
                    fileList.add(absolutePath);
                }
                else
                {
                    listFile(file.getAbsolutePath());
                }
            }
        }
        else
        {
            absolutePath = listFile.getAbsolutePath();
            fileList.add(absolutePath);
        }
        return fileList;
    }
}
