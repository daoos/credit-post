package parse;

import bean.RichTerm;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import conf.CmbConfig;
import crfpp.CrfppRecognition;

import javax.ws.rs.POST;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by hpre on 17-3-28.
 */
public class SentenParse {
    private CrfppRecognition recCS;

    public SentenParse(CmbConfig cmbConfig) {
        recCS = new CrfppRecognition(cmbConfig.cmbSenten);
//        recCS = new CrfppRecognition("/home/hadoop/wnd/usr/cmb/learnModel/cmbSenten.crfpp");

    }

    /*
    将一篇授信报告进行句子划分，划分好的句子会存在一个List集合中
     */
    @POST
    public List<String> sentenService(String text) {
        List<String> resultList = new ArrayList<String>();
        StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);
        List<Term> termList = StandardTokenizer.segment(text);

        recCS.addTerms(termList);

        List<RichTerm> richTermList = recCS.parse();
        String tem_s = "";
        for (RichTerm richTerm:richTermList) {
            if (richTerm.pos==null||richTerm.comTypeStr==null)
                continue;
            if(richTerm.comTypeStr.toString().equals("CS_S")) {
                tem_s = tem_s.trim().replaceAll("\n", "");
                if (!tem_s.equals("")) {
                    resultList.add(tem_s);
                }

                tem_s = "";
            }
            else {
                if (!richTerm.pos.toString().equals("begin")&&!richTerm.pos.toString().equals("end")) {
                    tem_s = tem_s+richTerm.word;
                }
                if (richTerm.pos.toString().equals("end")) {
                    tem_s = tem_s.trim().replaceAll("\n", "");
                    if (!tem_s.equals("")) {
                        resultList.add(tem_s);
                    }
                    tem_s = "";
                }
            }
        }
        recCS.clear();

        return resultList;

    }

    public static void main(String[] args) throws FileNotFoundException {
        File[] files = new File(args[0]).listFiles();

        CmbConfig cmbConfig = new CmbConfig();
        SentenParse juDouParser = new SentenParse(cmbConfig);

        Set<String> sets = new HashSet<>();
        for (File file: files) {
            Scanner input = new Scanner(file);
            System.out.println(file.getAbsolutePath());
            while (input.hasNextLine()) {
                List<String> list = juDouParser.sentenService(input.nextLine());
                for (String s : list) {
                    System.out.println(s);
                }
//                for (String sent: list) {
//                    if (sent.contains(",") || sent.contains("，"))
//                        sets.add(sent);
//                }
            }
        }
//        for (String sent: sets) {
//            System.out.println(sent);
//        }
    }
}
