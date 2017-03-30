package localuse;

import bean.Frequence;
import bean.RichTerm;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import crfpp.CrfppRecognition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import parse.CmbParse;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by hpre on 17-3-3.
 */
public class SomeBiaozhu {


    private static Log log = LogFactory.getLog(CmbParse.class);
    private static CrfppRecognition rec;
    private static Map<String,String> ruleMap = null;

    public static Pattern tipPattern = Pattern.compile("（\\d{1,2}）|\\d{1,2}）|\\d{1,2}");

    public static String comVector[] = new String[]{"SU","PR", "AT", "OB"};

    public static String nullity[] = new String[]{"经审议","授信主体","授信币种及金额","业务品种","授信期限",
            "价格条件", "还款条件","担保条件","分期还款","利率","还款方式","担保方式","同意","附原审批意见","发放要求",
            "小计","备用额度","合计", "注：","还款安排","编号","放款安排","保证条件","额度项","提用方式","额度启用条件",
            "使用要求", "启用要求","预留额度"," 成员企业名单","还款计划","借款人","信托金额","币种金额","额度内容",
            "额度期限", "增信方式","还款来源","分期还款","分行审批意见","保函受益人","费率","被担保人","贷款利率",

            "建议","提款进度安排及相应条件","主要承诺事项","抄送","结论抄送"
    };

    public static String specialSenten[] = new String[]{"若","如果","如","一旦","待","超过","在。。之前","存在变数"};

    public SomeBiaozhu()
    {

    }


    /*
	统计
     */
    public static List<Frequence> statistics(String text)
    {
        List<Frequence> TermsList = new LinkedList<>();
        List<String> wordList = new ArrayList<>();
        StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);
        List<Term> termList = StandardTokenizer.segment(text);
        rec.addTerms(termList);

        List<RichTerm> richTermList = rec.parse();
        StringBuffer sb = new StringBuffer();
        for (RichTerm richTerm : richTermList)
        {
            String word = richTerm.word;
            if (word.startsWith("#SENT"))
            {
                continue;
            }

            for (int i = 0; i < comVector.length; i++)
            {
                String strCase = comVector[i];
                if (strCase.equals("C"))
                    continue;
                if (richTerm.comTypeStr.equals(strCase + "_S"))
                {
//					if (wordList.contains(richTerm.word))
//					{
//
//					}
//					break;
//					System.out.println(richTerm.word);
                }
                else if (richTerm.comTypeStr.equals(strCase + "_B"))
                {
                    sb.append(richTerm.word);
                    break;
                }
                else if (richTerm.comTypeStr.equals(strCase + "_M"))
                {
                    sb.append(richTerm.word);
                    break;
                }
                else if (richTerm.comTypeStr.equals(strCase + "_E"))
                {
                    sb.append(richTerm.word);
//					System.out.println(sb.toString());
//					TermsList.add(new RichTerm(sb.toString(),richTerm.pos,strCase));
                    sb.delete(0, sb.length());
                    break;
                }
            }
        }
        rec.clear();
//      log.info("归并后的结果：" + termsList);
        return TermsList;
    }

    /*
    此处为还原部分，从计算机标注还原到人为标注，以便看出计算机哪里标注错了
    */
    public static String manualBiaozhu(String data)
    {
        List<RichTerm> richTermsList = new LinkedList<>();
        StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);

        List<Term> termList = StandardTokenizer.segment(data);
//            log.info("分词结果：" + termList);
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

    //自制比较器
    static class lengthComparator implements Comparator {
        public int compare(Object object1, Object object2) {// 实现接口中的方法
            return new Double(object1.toString().length()).compareTo(new Double(object2.toString().length()));
        }
    }



    /*
	将分好词的短语装进List集合
	 */
    /*
    public static List<String> loadWordToList(List<String> tmpList,List<String> biaozhuList)
    {
        Collections.sort(tmpList,new SomeBiaozhu().lengthComparator());
        for (int i = tmpList.size()-1; i > 0; i--)
        {
            String str = tmpList.get(i);
            List<Term> segment = StandardTokenizer.segment(str);
            StringBuffer sb = new StringBuffer();
            for (Term term : segment)
            {
                sb.append(term.word+"_"+term.nature+" ");
            }
            biaozhuList.add(sb.toString());
        }
        return biaozhuList;
    }

    static List<String> ATList = new ArrayList<>();
    static List<String> OBList = new ArrayList<>();
    static List<String> PRList = new ArrayList<>();




    public static void loadBiaozhu()
    {
        List<RichTerm> richTermsList = new LinkedList<>();
        StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);

        Scanner scanner1 = null;
        Scanner scanner2 = null;
        Scanner scanner3 = null;
        try {
            scanner1 = new Scanner(new File("/home/hpre/program/cmb/note/AT"));
            scanner2 = new Scanner(new File("/home/hpre/program/cmb/note/OB"));
            scanner3 = new Scanner(new File("/home/hpre/program/cmb/note/PR"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<String> tmpList = new ArrayList<>();
        while (scanner1.hasNext())
        {
            String strLine = scanner1.nextLine();
            tmpList.add(strLine);
        }
        ATList = loadWordToList(tmpList, ATList);
        tmpList = new ArrayList<>();
        while (scanner2.hasNext())
        {
            String strLine = scanner2.nextLine();
            tmpList.add(strLine);
        }
        OBList = loadWordToList(tmpList, OBList);
        tmpList = new ArrayList<>();
        while (scanner3.hasNext())
        {
            String strLine = scanner3.nextLine();
            tmpList.add(strLine);
        }
        PRList = loadWordToList(tmpList, PRList);
        scanner1.close();
        scanner2.close();
        scanner3.close();
    }

    /*
	机器标注，就是替换
	 */
    /*
    public static String automaticBiaozhu(String strLine)
    {
        loadBiaozhu();
        for (String s : ATList)
        {
            if (strLine.contains(s))
            {
                strLine = strLine.replaceAll(s,("#AT#"+s.substring(0,s.length()-2)+"#AT# "));
            }
        }
        for (String s : OBList)
        {
            if (strLine.contains(s))
            {
                strLine = strLine.replaceAll(s,("#OB#"+s.substring(0,s.length()-2)+"#OB# "));
            }
        }
        for (String s : PRList)
        {
            if (strLine.contains(s))
            {
                strLine = strLine.replaceAll(s,("#PR#"+s.substring(0,s.length()-2)+"#PR# "));
            }
        }
        return strLine;
    }
    */
}
