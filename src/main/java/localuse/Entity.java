package localuse;

import bean.ComNerTerm;
import bean.Frequence;
import bean.RichTerm;
import bean.SentenceTerm;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.hanlp.utility.Predefine;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.Compile;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static CrfppRecognition rec0;
    private static CrfppRecognition rec1;
    private static CrfppRecognition rec2;
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

    public Entity(CmbConfig conf)
	{
		Predefine.HANLP_PROPERTIES_PATH = "/home/hpre/program/cmb/model/hanlp.properties";
		rec = new CrfppRecognition("/home/hpre/program/cmb/model/model2.crfpp");
		rec0 = new CrfppRecognition("/home/hpre/program/cmb/model/model0.crfpp");
		rec1 = new CrfppRecognition("/home/hpre/program/cmb/model/model1.crfpp");
		rec2 = new CrfppRecognition("/home/hpre/program/cmb/model/model2.crfpp");
//		Predefine.HANLP_PROPERTIES_PATH = conf.hanlp;
//		rec = new CrfppRecognition(conf.modelfile2);
//		rec0 = new CrfppRecognition(conf.modelfile0);
//		rec1 = new CrfppRecognition(conf.modelfile1);
//		rec2 = new CrfppRecognition(conf.modelfile2);

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

/*
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
//			if (file.toString().equals("/home/hpre/projects/cmbPython/20份测试/")) //program/cmb/200份授信报告红色部分/58
//				System.out.println();
			FileWriter fileWriter = new FileWriter(dirOut.getAbsolutePath()+file.getAbsolutePath().toString().substring(dirInput.getAbsolutePath().length()));

			StringBuffer sb = new StringBuffer();
			while (scanner.hasNextLine()) {
//				List<RichTerm> list = deal(scanner.nextLine());
//				for (RichTerm sent: list) {
//					System.out.println(sent);
//				}
				String strLine = scanner.nextLine();
				sb.append(strLine);
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


//				System.out.println();
//				}
//				List<Frequence> statistics = statistics(strLine);
//				System.out.println(manualStr);
			}
			List<String> inference = entity.parse(sb.toString(), "2");
			for (String s : inference)
			{
				System.out.println(s);
				fileWriter.write(s);
			}
			scanner.close();
			fileWriter.close();
		}
	}
*/

	public static void main(String[] args)
	{
		String s = "";
//		String out = clean_noise(s);
		Entity entity = new Entity(new CmbConfiguration().getCmb());
		List<String> parse_out = entity.parse(s,"2");
		System.out.println();
//		System.out.println(out);
	}

	/*
	预处理  主要删除空格，将英文符号转中文符号，去除一些黑色部分
	 */
	public static String clean_noise(String text)
	{
		String noises[] = new String[]{"经审议","担保条件","国内保理部分","具体授信主体","前提条件","保理业务要求","主要承若事项","鉴于"};

		text = text.replaceAll(" ","");
		text = text.replaceAll("\\(","（");
		text = text.replaceAll("\\)","）");

		boolean remove = false;

		String[] lineSplit = text.split("\n");
		for (int i = 0; i < lineSplit.length; i++)
		{
			for (String noise : noises)
			{
				if (lineSplit[i].startsWith(noise))
				{
					remove = true;
					break;
				}
			}

			if (remove)
			{
				StringBuffer sb = new StringBuffer();
				sb.append(lineSplit[i]);
				while (true)
				{
					i++;
					if (i == lineSplit.length )
					{
						text = text.replace(sb.toString(), "");
						break;
					}
					Matcher m = tipPattern.matcher(lineSplit[i]);
					if (m.find())
					{
						sb.append("\n"+lineSplit[i]);
					}
					else
					{
						text = text.replace(sb.toString(), "");
						remove = false;
						break;
					}
				}

			}
		}
//		System.out.println(text);

		String[] lineSplit2 = text.split("\n");
		for (String eachLine : lineSplit2)
		{
			StringBuffer sb = new StringBuffer();
			for (String eachNuilty : nullity)
			{ //无效句去除
				if (eachLine.startsWith(eachNuilty))
				{
					sb.append("\n"+eachLine);
					break;
				}
			}
			text = text.replace(sb.toString(), "");
		}

		return text;
	}

	/*
	入口
	 */
	public List<String> parse(String text,String type)
	{
		List<String> outList = new ArrayList<>();
		if (text == null) {
			return outList;
		}
		text = clean_noise(text);

		if (type.equals("0"))
		{
			rec = rec0;
		}
		else if (type.equals("1"))
		{
			rec = rec1;
		}
		else
		{
			rec = rec2;
		}

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
				for (String eachSenten : specialSenten)
				{
					if (commaSplit[i].contains(eachSenten) && (i+1) < commaSplit.length)
					{ //连句就不分开
						sentenceList.add(commaSplit[i++]+"，"+commaSplit[i]);
						continue tohere;
					}
				}
				if (!commaSplit[i].equals(""))
				{
					sentenceList.add(commaSplit[i]);
				}
			}
			if (sentenceList.size() == 0)
				continue;
			List<SentenceTerm> sentenceTerms = cmbServiceFuseJudou(sentenceList, dealList);
			List<String> inference = inference(sentenceTerms);
			for (String s : inference)
			{
				outList.add(s);
			}
		}

		List<String> normalization = normalization(outList);

		return normalization;
	}

	/*
    融合句子划分和成分识别
    */
	public static List<SentenceTerm> cmbServiceFuseJudou(List<String> judouList, List<ComNerTerm> comNerTermList)
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
//		log.info("分词结果：" + termList);

		rec.addTerms(termList);

		List<RichTerm> richTermList = rec.parse();
//		log.info("标注结果:"+richTermList);
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
				continue;
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

//			if (!ruleOut.toString().equals("") && ruleOut.toString().length() > 2)
//				System.out.println(ruleOut.toString());
			if (!ruleOut.toString().equals(""))
				in.add(ruleOut.toString());
		}
		return in;
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



	public static List<String> normalization(List<String> outList)
	{
		List<String> normalList = new ArrayList<>();

		for (String eachOut : outList)
		{
			String[] lineSplit = eachOut.split("\n");
			for (String eachLine : lineSplit)
			{
				if (!normalList.contains(eachLine))
				{
					normalList.add(eachLine);
				}
			}
		}
		return normalList;
	}



}
