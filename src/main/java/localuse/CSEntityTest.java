package localuse;

import bean.RichTerm;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.hanlp.utility.Predefine;
import conf.CmbConfig;
import conf.CmbConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by hpre on 16-10-29.
 */
public class CSEntityTest
{


//    private static String input = "/home/hpre/xiangya/views_ComTest/";
    private static String input = "/home/hpre/program/cmb/lengthTest/";
    private static String out = "/home/hpre/program/cmb/lengthTest-out/";
    private static CrfppComRecognition recCS = null;
    static JuDouConfig juDouConfig = null;
    static
    {

        Predefine.HANLP_PROPERTIES_PATH = new CmbConfiguration().getHanlp();
        CmbConfig cmbConfig = new CmbConfig();
        recCS = new CrfppComRecognition("/home/hpre/program/cmb/model/80cmbFeature.crfpp");
        Predefine.HANLP_PROPERTIES_PATH = "/model/judou/hanlp.properties";
    }

    public static void main(String[] args) throws IOException {
        CSEntityTest csEntityTest = new CSEntityTest();
        List<String> filePaths = listFile(input);
        for (String filePath : filePaths)
        {
            Scanner scanner = new Scanner(new File(filePath));
            System.out.println(filePath+" "+filePath.length());
            System.out.println(input+" "+input.length());
            FileWriter fileWriter = new FileWriter(new File(out+filePath.substring(input.length(),filePath.length())));
            while (scanner.hasNext())
            {
                String str = scanner.nextLine();
//                System.out.println(str);
                String strings = csEntityTest.doJudou2(str);
                System.out.println(strings);//"strings="
                fileWriter.write(strings+"\n");
            }
            scanner.close();
            fileWriter.flush();
            fileWriter.close();
        }

    }

    /*
    短句标注
     */
    public String doJudou2(String text)
    {
        List<String> resultList = new ArrayList<String>();
        StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);
        List<Term> termList = StandardTokenizer.segment(text);

        recCS.addTerms(termList);

        List<RichTerm> richTermList = recCS.parse();
        String tem_s = "";
        for (RichTerm richTerm:richTermList)
        {
//            System.out.println(richTerm);
            if (richTerm.word.startsWith("#SEN"))
            {
                continue;
            }
            if(richTerm.comTypeStr.toString().equals("CS_S"))
            {
                tem_s = tem_s+"#CS#"+richTerm.word+"#CS#";
            }
            else
            {
                tem_s = tem_s+richTerm.word;
            }
        }
        recCS.clear();

        return tem_s;

    }

    public List<String> doJudou(String text)
    {
        List<String> resultList = new ArrayList<String>();
        StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);
        List<Term> termList = StandardTokenizer.segment(text);

        recCS.addTerms(termList);

        List<RichTerm> richTermList = recCS.parse();
        String tem_s = "";
        for (RichTerm richTerm:richTermList)
        {
            System.out.println(richTerm);
            if(richTerm.comTypeStr.toString().equals("CS_S"))
            {
                tem_s = tem_s.trim().replaceAll("\n", "");
                if (!tem_s.equals(""))
                {
                    resultList.add(tem_s);
                }

                tem_s = "";
            }
            else
            {
                if (!richTerm.pos.toString().equals("begin")&&!richTerm.pos.toString().equals("end"))
                {
                    tem_s = tem_s+richTerm.word;
                }
                if (richTerm.pos.toString().equals("end"))
                {
                    tem_s = tem_s.trim().replaceAll("\n", "");
                    if (!tem_s.equals(""))
                    {
                        resultList.add(tem_s);
                    }
                    tem_s = "";
                }
            }
        }
        recCS.clear();

        return resultList;

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
