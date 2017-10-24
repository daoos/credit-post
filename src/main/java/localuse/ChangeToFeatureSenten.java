package localuse;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.hanlp.utility.Predefine;
import tools.CommonlyTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by hadoop on 17-10-24.
 */
public class ChangeToFeatureSenten {
    private static String TRAINPATH = "/mnt/vol_0/wnd/usr/cmb/cmbSenten_new";
    private static String OUTFILEPATH = "/mnt/vol_0/wnd/usr/cmb/10月24日/cmbSenten.crfpp";


    public static void main(String[] args) {
        ChangeToFeatureSenten instance = new ChangeToFeatureSenten();
        try {
            instance.readWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
 文件读写和写入
  */
    public  void readWriter() throws IOException
    {
        List<String> filesPath = ChangeToFeature.listFile(TRAINPATH);
        Scanner scanner = null;
        File file;
        FileWriter fileWriter = new FileWriter(new File(OUTFILEPATH));
        for (String filePath : filesPath)
        {
            file = new File(filePath);
//            String result="";
            if(file.toString().endsWith("/192.txt"))
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

        }
        fileWriter.flush();
        fileWriter.close();
    }


/*
   *语句解析成feature格式
 */
    private String dealLine(String inline){
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
        //
        Vector<String> biaodians = CommonlyTools.get_all_match(result, "(?<=\\[_w ).");


        for (String biaodian : biaodians) {
            result=result.replaceFirst("\\[_w ._w _CS_nx\\ ]_w","#CS#"+biaodian+"_w#CS#");
        }
        String lineResult=ChangeToFeature.dealLine(result);
        return lineResult;
    }
}
