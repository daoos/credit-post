package parse;

import bean.ComNerTerm;
import bean.RichTerm;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import conf.CmbConfig;
import crfpp.CrfppRecognition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.POST;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by hpre on 17-3-28.
 */
public class ComParse {
    private static Log log = LogFactory.getLog(ComParse.class);

    private CrfppRecognition recCom = null;

    public static String comVector[] = new String[]{"VN","AC", "OB", "EX", "QU", "VC", "AD", "VE", "PP", "NA", "CO", "PE"};

    public ComParse(CmbConfig config) throws FileNotFoundException {
        recCom = new CrfppRecognition(config.cmbCom);
    }

    /**
     * 主方法
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        File dir = new File(args[0]);
        CmbConfig config = new CmbConfig();
        FileWriter fileWriter = new FileWriter(new File("/home/hpre/program/cmb/成分测试结果"));
        ComParse com = new ComParse(config);
        for (File f : dir.listFiles()) {
            Scanner input = new Scanner(f);
            System.out.println(f.getAbsolutePath());
            while (input.hasNext()) {
                String string = input.nextLine();
                int tagUse = 1;

                fileWriter.write(string+"\n");
                List<ComNerTerm> comServiceOut = com.comService(string);
                if (comServiceOut.size() != 0) {
                    System.out.println(comServiceOut);
                    fileWriter.write(comServiceOut + "\n");
                }
            }
            input.close();
        }
        fileWriter.close();
    }

    /**
     * 实体识别
     * @param text  一篇授信报告
     * @return  存了成分的List集合
     */
    @POST
    public List<ComNerTerm> comService(String text) {
        List<ComNerTerm> TermsList = new LinkedList<>();
        StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);

        List<Term> termList = StandardTokenizer.segment(text);
		log.info("分词结果：" + termList);

        recCom.addTerms(termList);
        System.out.println(termList.toString());
        List<RichTerm> richTermList = recCom.parse();
		log.info("标注结果:"+richTermList);
        StringBuffer sb = new StringBuffer();
        int offset = 0;
        for (RichTerm richTerm : richTermList) {
            String word = richTerm.word;
            if (word.startsWith("#SENT")) {
                continue;
            }

            if (richTerm.comTypeStr.equals("OUT")) {
                offset += richTerm.word.length();
                continue;
            }

            for (int i = 0; i < comVector.length; i++) {
                String strCase = comVector[i];
                if (richTerm.comTypeStr.equals(strCase + "_S")) {
                    TermsList.add(new ComNerTerm(richTerm.word,strCase,offset));
                    offset += richTerm.word.length();
                    break;
                }
                else if (richTerm.comTypeStr.equals(strCase + "_B")) {
                    sb.append(richTerm.word);
                    break;
                }
                else if (richTerm.comTypeStr.equals(strCase + "_M")) {
                    sb.append(richTerm.word);
                    break;
                }
                else if (richTerm.comTypeStr.equals(strCase + "_E")) {
                    sb.append(richTerm.word);

                    TermsList.add(new ComNerTerm(sb.toString(),strCase,offset));
                    offset += sb.length();
                    sb.delete(0, sb.length());
                    break;
                }
            }
        }
        recCom.clear();
        return TermsList;
    }

    /**
     * 此处为还原部分，从计算机标注还原到人为标注
     * @param data  原始文本
     * @return  机器标注结果
     */
    public static String manualBiaozhu(String data)
    {
        CrfppRecognition rec;
        StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);
        List<Term> termList = StandardTokenizer.segment(data);
        rec = new CrfppRecognition("/home/hpre/projects/cmbPython/model.crfpp");
        rec.addTerms(termList);

        List<RichTerm> richTermList = rec.parse();
        String manualBiaoZhu = "";
        for (RichTerm rTerm : richTermList)
        {
            if (rTerm.word.toString().equals("#SENT_BEG#") || rTerm.word.toString().equals("#SENT_END#"))
                continue;
            if (rTerm.comTypeStr.toString().equals("OUT"))
            {
                manualBiaoZhu = manualBiaoZhu + rTerm.word + "_" + rTerm.pos + " ";
            }
            else
            {
                if (rTerm.comTypeStr.toString().contains("_S"))
                {
                    manualBiaoZhu = manualBiaoZhu + "#" + rTerm.comTypeStr.toString().substring(0, rTerm.comTypeStr.toString().length() - 2) +
                            "#" + rTerm.word + "_" + rTerm.pos + "#" +
                            rTerm.comTypeStr.toString().substring(0, rTerm.comTypeStr.toString().length() - 2) + "#" + " ";
                }
                else if (rTerm.comTypeStr.toString().contains("_B"))
                {
                    manualBiaoZhu = manualBiaoZhu +"#" + rTerm.comTypeStr.toString().substring(0, rTerm.comTypeStr.toString().length() - 2)+
                            "#"+rTerm.word+"_"+rTerm.pos+" ";
                }
                else if (rTerm.comTypeStr.toString().contains("_E"))
                {
                    manualBiaoZhu = manualBiaoZhu +rTerm.word+"_"+rTerm.pos+"#" +
                            rTerm.comTypeStr.toString().substring(0, rTerm.comTypeStr.toString().length() - 2)+ "#"+" ";
                }
                else if (rTerm.comTypeStr.toString().contains("_M"))
                {
                    manualBiaoZhu = manualBiaoZhu + rTerm.word+"_"+
                            rTerm.pos+" ";
                }
            }
        }
        return manualBiaoZhu;
    }

}
