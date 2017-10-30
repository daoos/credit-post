package localuse;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.hanlp.utility.Predefine;
import tools.CommonlyTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by hpre on 16-10-27.
 */
public class ChangeToFeature
{

    private static String train_file = "/home/hpre/program/cmb/cmbCom";
    private static String out_file = "/home/hpre/program/cmb/model/cmbComFeature.crfpp";

    public static String biaozhu_Senten[] = new String[]{"CS"};
    public static String biaozhu_com[] = new String[]{"VN","AC", "OB", "EX", "QU", "VC", "AD", "VE", "PP", "NA", "CO", "PE"};
    public static String sep = "_";

    /**
     * 主方法
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        readWriter();
    }

    /**
     * 文件读写和写入
     * @throws IOException
     */
    public static void readWriter() throws IOException
    {
        File[] files = new File(train_file).listFiles();
        Scanner scanner = null;
        FileWriter fileWriter = new FileWriter(new File(out_file));
        for (File file : files)
        {
            if(file.toString().endsWith("/63.txt"))
                System.out.println();
            try
            {
                scanner = new Scanner(file);
                String strLine;
                int i = 0;
                while (scanner.hasNext())
                {
                    strLine = scanner.nextLine();
                    System.out.println(file);
                    System.out.println(i+"行");
                    System.out.println(strLine);
                    i++;
                    String lineResult = dealLine(strLine, "senten"); // com
                    fileWriter.write("#SENT_BEG#\tbegin\tOUT"+"\n"+lineResult+"#SENT_END#\tend\tOUT"+"\n"+"\n");
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            scanner.close();
        }
        fileWriter.flush();
        fileWriter.close();
    }

    /**
     * 将标注文件转成可被crf_learn训练的feature文件
     * @param line  一行文本
     * @param type  转换类型  senten/com
     * @return  一行转换结果
     */
    public static String dealLine(String line, String type)
    {
        String result = "";
        String[] lineSpaceSplit = line.split(" ");
        boolean outTag = true;
        String strBiaoZhu = null;
        String biaoZhu[] = null;
        for (String splitedStr : lineSpaceSplit)
        {
            if (type.equals("senten")) {
                biaoZhu = biaozhu_Senten;
                strBiaoZhu = "CS";
            } else if (type.equals("com")){
                biaoZhu = biaozhu_com;
            } else {
                System.out.println("请输入正确的转换类型");
            }
            if (splitedStr.equals("#SENT_BEG#/begin")||splitedStr.equals("#SENT_END#/end"))
            {
                String[] slashSplit = splitedStr.split(sep);
                result = result+slashSplit[0]+"\t"+slashSplit[1]+"\t"+"OUT"+"\n";
                continue;
            }
            if (splitedStr.startsWith("#"))
            {
                for (int i = 0; i < biaoZhu.length; i++)
                {
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
                    String[] slashSplit = wordAndNature.split(sep);
                    result = result+slashSplit[0]+"\t"+slashSplit[1]+"\t"+strBiaoZhu+"_S"+"\n";
                    continue;
                } else {
                    //以#开头，但不以#结尾   //  #OPER#脓肿/a
                    outTag = false;
                    String wordAndNature = splitedStr.substring((strBiaoZhu.length()+2),
                            splitedStr.length());
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
                try {
                    String wordAndNature = splitedStr.substring(0,
                            splitedStr.length()-(strBiaoZhu.length()+2));
                    String[] slashSplit = wordAndNature.split(sep);
                    result = result+slashSplit[0]+"\t"+slashSplit[1]+"\t"+strBiaoZhu+"_E"+"\n";
                } catch (Exception e){
                    e.printStackTrace();
                }
                continue;
            }
            if (outTag)
            {
                String[] slashSplit = splitedStr.split(sep);
                if (slashSplit.length>1)
                {
                    result = result+slashSplit[0]+"\t"+slashSplit[1]+"\t"+"OUT"+"\n";
                }
            } else {
                String[] slashSplit = splitedStr.split(sep);
                result = result + slashSplit[0] + "\t" + slashSplit[1] + "\t" + strBiaoZhu + "_M" + "\n";
            }
        }
        return result;
    }

    /**
     * 将用新标注方法的句子转成用老标注方法的句子
     * @param inline    一行用新标注方法的句子原文
     * @param type  转换类型  senten/com
     * @return 一行用老标注方法的句子原文
     */
    private String old2new(String inline, String type){
        Predefine.HANLP_PROPERTIES_PATH = "/mnt/vol_0/wnd/ml/cmb/hanlp.properties";
        StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);
        List<Term> termList = StandardTokenizer.segment(inline);
        String result="";
        for (Term term : termList) {
            result+=term.word+"_"+term.nature+" ";
        }
        if(result.contains("][_w")){
            result=result.replace("][_w","]_w [_w");
        }
        result=result.trim();
        Vector<String> biaodians = CommonlyTools.get_all_match(result, "(?<=\\[_w ).");
        for (String biaodian : biaodians) {
            result=result.replaceFirst("\\[_w ._w _CS_nx\\ ]_w","#CS#"+biaodian+"_w#CS#");
        }
        return result;
    }

}
