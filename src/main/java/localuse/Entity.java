package localuse;

import bean.ComNerTerm;
import bean.Frequence;
import bean.RichTerm;
import bean.SentenceTerm;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.hanlp.utility.Predefine;
import conf.CmbConfig;
import conf.CmbConfiguration;
import crfpp.CrfppRecognition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * Created by hpre on 16-12-16.
 *
 * 招行
 */

public class Entity
{

	private static Log log = LogFactory.getLog(Entity.class);
    private static CrfppRecognition rec;
	private static Map<String,String> ruleMap = null;

	public static String comVector[] = new String[]{"SU","PR", "AT", "OB"};

	public static String nullity[] = new String[]{"经审议","授信主体","授信币种及金额","授信种类","业务品种","授信期限",
			"价格条件", "还款条件","担保条件","分期还款","利率","还款方式","担保方式","同意","附原审批意见","发放要求",
			"小计","备用额度","合计", "注：","还款安排","编号","放款安排","保证条件","额度项","提用方式","额度启用条件",
			"使用要求", "启用要求","预留额度"," 成员企业名单","还款计划","借款人","信托金额","币种金额","额度内容",
			"额度期限", "增信方式","还款来源","分期还款","分行审批意见","保函受益人","费率","被担保人","贷款利率"};

	public static String specialSenten[] = new String[]{"若","如果","如","一旦","待","超过","在。。之前","存在变数"};

    public Entity(CmbConfig conf)
	{
//		Predefine.HANLP_PROPERTIES_PATH = conf.hanlp;
		Predefine.HANLP_PROPERTIES_PATH = "/home/hpre/program/cmb/model/hanlp.properties";
//		rec = new CrfppRecognition(conf.modelfile);
		rec = new CrfppRecognition("/home/hpre/program/cmb/model/model.crfpp");
		ruleMap = new HashMap<>();
		Scanner scanner = null;
		try
		{
//			scanner = new Scanner(new File(conf.rulefile));
			scanner = new Scanner(new File("/home/hpre/program/cmb/model/rulefile"));
			while (scanner.hasNext())
			{
				String ruleStr = scanner.nextLine();
				if (ruleStr.contains("->"))
				{
					String[] split = ruleStr.split("->");
					ruleMap.put(split[0],split[1]);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}

	public static String inferenceInside(SentenceTerm sentenceTerm)
	{
		List<ComNerTerm> comNerTermList = sentenceTerm.getComNerTermList();
		StringBuffer sb = new StringBuffer();
		StringBuffer type = new StringBuffer();
		StringBuffer word = new StringBuffer();
		for (ComNerTerm comNerTerm : comNerTermList) {
			type.append(comNerTerm.typeStr + " ");
			word.append(comNerTerm.word + " ");
		}
		String typeAndWord = type.toString() + "\t" + word.toString();
		if (!type.toString().equals(""))
		{
//				System.out.println();
		}
		String ruleOut = ruleRecursion(type.toString().trim(), typeAndWord, sb);
		StringBuffer inside_out = new StringBuffer();
		if (!ruleOut.equals(""))
		{
			String[] lineSplit = ruleOut.split("\n");
			for (String eachLine : lineSplit) {
				String[] tabSplit = eachLine.split("\t");
				inside_out.append(tabSplit[0]+"\n");
			}
//			System.out.println(ruleOut);
		}
		return inside_out.toString();
	}


	public static List<String> inference(List<SentenceTerm> sentenceTermList)
	{
		List<String> in = new ArrayList<>();
		for (SentenceTerm sentenceTerm : sentenceTermList) {
			StringBuffer ruleOut = new StringBuffer();
			String sentence = sentenceTerm.getSentence();
			int offset = sentenceTerm.getOffset();
			List<ComNerTerm> comNerTermList = sentenceTerm.getComNerTermList();
			if (sentence.contains("，"))
			{

				String[] split = sentence.split("，");
				int splitInt = offset + split[0].length();
				List<ComNerTerm> comNerTermList1 = new ArrayList<>();
				List<ComNerTerm> comNerTermList2 = new ArrayList<>();
				for (ComNerTerm comNerTerm : comNerTermList)
				{
					if (comNerTerm.offset < splitInt)
						comNerTermList1.add(comNerTerm);
					else
						comNerTermList2.add(comNerTerm);
				}
				SentenceTerm sentenceTerm1 = new SentenceTerm(split[0],comNerTermList1,offset);
				SentenceTerm sentenceTerm2 = new SentenceTerm(split[1],comNerTermList2,splitInt+1);
				String ininside1 = inferenceInside(sentenceTerm1);
				String ininside2 = inferenceInside(sentenceTerm2);
				if (!ininside1.equals("") && !ininside2.equals(""))
				{
					if (ininside1.contains("\n"))
					{
						ininside1 = ininside1.replace("\n","，");
						ruleOut.append(ininside1);
					}

					else
						ruleOut.append(ininside1+"，");
					ruleOut.append(ininside2);
				}
				else
				{
					if (!ininside1.equals(""))
						ruleOut.append(ininside1);
					if (!ininside2.equals(""))
						ruleOut.append(ininside2);
				}
			}
			else
				ruleOut.append(inferenceInside(sentenceTerm));

			if (!ruleOut.toString().equals("") && ruleOut.toString().length() > 2)
				System.out.println(ruleOut.toString());
			if (!ruleOut.toString().equals(""))
				in.add(ruleOut.toString());
		}
		return in;
	}

	/*
    规则递归推导
     */
	public static String ruleRecursion(String rule,String lineStr,StringBuffer sb)
	{
		String ruleOut = ruleMap.get(rule);
		if (ruleOut==null)
		{
			if (rule.equals(""))
				return sb.toString();
			else
			{
//				System.out.println(rule);
//				System.out.println(lineStr);
				return sb.toString();
			}
		}

		String[] tabSplit = lineStr.split("\t");
		String[] spaceSplit2 = tabSplit[1].split(" ");
		String[] labelLocation = ruleOut.split("#");
		if (ruleOut.contains(","))
		{
			String[] ruleArray = labelLocation[0].split(",");
			String[] locationArray = labelLocation[1].split(",");

			for (int i = 0; i < locationArray.length; i++)
			{
				StringBuffer lineStr2 = new StringBuffer();
				String[] ss = locationArray[i].split(" ");
				lineStr2.append(ruleArray[i].trim());
				lineStr2.append("\t");
				for (int i1 = 0; i1 < ss.length; i1++)
				{
					int count = Integer.parseInt(ss[i1]);
					lineStr2.append(spaceSplit2[count]+" ");
				}
				ruleRecursion(ruleArray[i].trim(),lineStr2.toString(),sb);
			}
		}
		else
		{
			String word = "";
			char[] chars = labelLocation[1].toCharArray();
			for (int i = 0; i < chars.length; i++)
			{
				int aChar = chars[i]; //unicode中　0是４８，空格是３２
				if (aChar==32)
				{
					word = word+" ";
				}
				else
				{
					word = word+spaceSplit2[aChar-48];
				}
			}
			sb.append(word+"\t"+labelLocation[0]+"\n");
		}
		return sb.toString();
	}


	public static void main(String[] args) throws IOException {
		Entity entity = new Entity(new CmbConfiguration().getCmb());
		File dirInput = new File(args[0]);
		File[] files = dirInput.listFiles();
		File dirOut = new File(args[1]);
		for (File file: files) {
//			System.out.println();
//			System.out.println();
			System.out.println(file);
			Scanner scanner = new Scanner(file);
			if (file.toString().equals("/home/hpre/program/cmb/200份授信报告红色部分/20")) //program/cmb/200份授信报告红色部分/58
				System.out.println();
			FileWriter fileWriter = new FileWriter(dirOut.getAbsolutePath()+file.getAbsolutePath().toString().substring(dirInput.getAbsolutePath().length()));
			while (scanner.hasNextLine()) {
//				List<RichTerm> list = deal(scanner.nextLine());
//				for (RichTerm sent: list) {
//					System.out.println(sent);
//				}
				String strLine = scanner.nextLine();
//				List<Term> segment = StandardTokenizer.segment(strLine);
//				StringBuffer sb = new StringBuffer();
//				for (Term term : segment)
//				{
//					sb.append(term.word+"_"+term.nature+" ");
//				}
//				String manualStr = automaticBiaozhu(sb.toString());
//				String manualStr = manualBiaozhu(strLine);
//				String[] split = strLine.split("[。；，]");
//				for (String s : split)
//				{

				strLine = strLine.trim();
				strLine = strLine.replaceAll(" ","");
				List<ComNerTerm> dealList = deal(strLine);
				String[] commaSplit = strLine.split("[，；。]");
				List<String> sentenceList = new ArrayList<>();
				tohere:for (int i = 0; i < commaSplit.length; i++)
				{
					for (String eachNuilty : nullity)
					{ //无效句去除
						if (commaSplit[i].startsWith(eachNuilty))
							continue;
					}
					for (String eachSenten : specialSenten)
					{
						if (commaSplit[i].contains(eachSenten) && (i+1) < commaSplit.length)
						{ //连句就不分开
							sentenceList.add(commaSplit[i++]+"，"+commaSplit[i]);
							continue tohere;
						}
					}
					sentenceList.add(commaSplit[i]);
				}
				List<SentenceTerm> sentenceTerms = comServiceFuseJudou(sentenceList, dealList);
				List<String> inference = inference(sentenceTerms);
//				System.out.println();
//				}
//				List<Frequence> statistics = statistics(strLine);
				for (String s : inference)
				{
					fileWriter.write(s);
				}
//				System.out.println(manualStr);
			}
			scanner.close();
			fileWriter.close();
		}
	}

	/*
	入口
	 */
	public List<String> parse(String text)
	{
		List<String> outList = new ArrayList<>();
		String[] lineSplit = text.split("\n");
		for (String eachLine : lineSplit)
		{
			eachLine = eachLine.trim();
			eachLine = eachLine.replaceAll(" ","");
			List<ComNerTerm> dealList = deal(eachLine);
			String[] commaSplit = eachLine.split("[，；。]");
			List<String> sentenceList = new ArrayList<>();
			tohere:for (int i = 0; i < commaSplit.length; i++)
			{
				for (String eachNuilty : nullity)
				{ //无效句去除
					if (commaSplit[i].startsWith(eachNuilty))
						continue;
				}
				for (String eachSenten : specialSenten)
				{
					if (commaSplit[i].contains(eachSenten) && (i+1) < commaSplit.length)
					{ //连句就不分开
						sentenceList.add(commaSplit[i++]+"，"+commaSplit[i]);
						continue tohere;
					}
				}
				sentenceList.add(commaSplit[i]);
			}
			List<SentenceTerm> sentenceTerms = comServiceFuseJudou(sentenceList, dealList);
			List<String> inference = inference(sentenceTerms);
			for (String s : inference)
			{
				outList.add(s);
			}
		}

		return outList;
	}

	/*
    融合句子划分和成分识别
    */
	public static List<SentenceTerm> comServiceFuseJudou(List<String> judouList, List<ComNerTerm> comNerTermList)
	{
		int sentenceLength;
		int sentenceStart;
		int sentenceEnd = 0;
		int comNerTermLocation = 0;
		List<SentenceTerm> sTermList = new ArrayList<>();
		int offset = 0;

		for (String sentence : judouList)
		{
			List<ComNerTerm> cNerTermList = new ArrayList<>();
			sentenceStart = sentenceEnd;
			sentenceLength = sentence.length();
			sentenceEnd = sentenceStart + sentenceLength + 1; // 句读时，丢失分句的标点符号（逗号，分号，句号等），需要长度+1
			for (int j = comNerTermLocation; j < comNerTermList.size(); j++)
			{
				ComNerTerm comNerTerm = comNerTermList.get(j);
				if (comNerTerm.offset < sentenceEnd &&
						comNerTerm.offset >= sentenceStart)
				{
					cNerTermList.add(comNerTerm);
				}
				else
				{
					comNerTermLocation = j;
					break;
				}
			}
			sTermList.add(new SentenceTerm(sentence, cNerTermList, offset));
			offset += sentenceLength + 1;
		}
		return sTermList;
	}

	/*
    实体识别处理
     */
    public static List<ComNerTerm> deal(String text)
	{
		List<ComNerTerm> TermsList = new LinkedList<>();
		StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);


		List<Term> termList = StandardTokenizer.segment(text);
		log.info("分词结果：" + termList);

		rec.addTerms(termList);

		List<RichTerm> richTermList = rec.parse();
		log.info("标注结果:"+richTermList);
		StringBuffer sb = new StringBuffer();
		int offset = 0;
		for (RichTerm richTerm : richTermList)
		{
			String word = richTerm.word;
			if (word.startsWith("#SENT"))
			{
				continue;
			}

			if (richTerm.comTypeStr.equals("OUT")) {
				offset += richTerm.word.length();
				continue; //减少循环次数，提高效率
			}

			for (int i = 0; i < comVector.length; i++)
			{
				String strCase = comVector[i];
				if (richTerm.comTypeStr.equals(strCase + "_S"))
				{
					TermsList.add(new ComNerTerm(richTerm.word,strCase,offset));
					offset += richTerm.word.length();
					break;
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
					TermsList.add(new ComNerTerm(sb.toString(),strCase,offset));
					offset += sb.length();
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
	public static List<String> loadWordToList(List<String> tmpList,List<String> biaozhuList)
	{
		Collections.sort(tmpList,new lengthComparator());
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

}
