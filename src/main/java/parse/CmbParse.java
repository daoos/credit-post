package parse;

import bean.ComNerTerm;
import bean.SentenceTerm;
import com.hankcs.hanlp.utility.Predefine;
import conf.CmbConfig;
import conf.CmbConfiguration;
import crfpp.CrfppRecognition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hpre on 16-12-16.
 *
 * 招行授信文本分析
 */

public class CmbParse
{
	private static Log log = LogFactory.getLog(CmbParse.class);
	private static CrfppRecognition rec;
	private static Map<String,String> ruleMap = null;

	public static Pattern tipPattern = Pattern.compile("（\\d{1,2}）|\\d{1,2}）|\\d{1,2}");

	public static String nullity[] = new String[]{"经审议","授信主体","授信币种及金额","业务品种","授信期限",
			"价格条件", "还款条件","担保条件","分期还款","利率","还款方式","担保方式","同意","附原审批意见","发放要求",
			"小计","备用额度","合计", "注：","还款安排","编号","放款安排","保证条件","额度项","提用方式","额度启用条件",
			"使用要求", "启用要求","预留额度"," 成员企业名单","还款计划","借款人","信托金额","币种金额","额度内容",
			"额度期限", "增信方式","还款来源","分期还款","分行审批意见","保函受益人","费率","被担保人","贷款利率",

			"建议","提款进度安排及相应条件","主要承诺事项","抄送","结论抄送"
	};

	public static String noises[] = new String[]{"经审议","担保条件","国内保理部分","具体授信主体","前提条件","保理业务要求","主要承若事项","鉴于"};


//	public static String specialSenten[] = new String[]{"若","如果","如","一旦","待","超过","在。。之前","存在变数"};

	private ComParse comParse = null;
	private SentenParse sentenParse = null;
	private String url = null;

    public CmbParse(CmbConfig cmbConfig) {
		Predefine.HANLP_PROPERTIES_PATH = "/home/hadoop/wnd/ml/cmb/hanlp.properties";
//		Predefine.HANLP_PROPERTIES_PATH = cmbConfig.hanlp;
		try {
			comParse = new ComParse(cmbConfig);
			sentenParse = new SentenParse(cmbConfig);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	//	url = cmbConfig.url;
		url= "";//测试使用 记得注释
		ruleMap = new HashMap<>();
		Scanner scanner = null;
		try {
//			scanner = new Scanner(new File(cmbConfig.ruleFile));
			scanner = new Scanner(new File("/home/hadoop/wnd/usr/cmb/learnModel/ruleFile"));
			while (scanner.hasNext()) {
				String ruleStr = scanner.nextLine();
				if (ruleStr != null && ruleStr.length() < 2) {
					continue;
				}
				if (ruleStr.startsWith("-----")) {
					break;
				}
				if (ruleStr.contains("->")) {
					String[] split = ruleStr.split("->");
					ruleMap.put(split[0],split[1]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}


	/*
	App调用入口
	 */
	public List<String> parse(String text) {
//		try {
//			String objStr = query(text, url);
//			// 从python接口获取关联关系
//			System.out.println(objStr);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		text = cleanNoise(text);
		// 数据预处理

		List<String> outList = new ArrayList<>();
		if (text.equals("")) {
			return outList;
		}

		for (String eachLine : text.split("\n")) {
			eachLine = eachLine.replaceAll(" ","");

			if (eachLine.length() < 2)
				continue;

			List<ComNerTerm> comNerTermList = comParse.comService(eachLine);
			// 成分标注
			if (comNerTermList.size() == 0)
				continue;
			List<String> sentenList = sentenParse.sentenService(eachLine);
			// 句子标注
			List<SentenceTerm> sentenceTerms = comFuseSenten(sentenList, comNerTermList);
			// 成分句子融合

			addedDefault(sentenceTerms);
			//补充缺省

			sentenceTerms = preDeal(sentenceTerms);
			// sentenceTerms预处理

			List<String> inference = inference(sentenceTerms);
			// 处理每一句
			for (String eachResult : inference) {
				outList.add(eachResult);
			}
		}

		List<String> normalization = normalization(outList);
		// 数据规范化
		return normalization;
	}


	/*
	访问python接口获取关联关系
	 */
	public static String query(String content, String url) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		StringBuffer sb = new StringBuffer();
		try {
			HttpPost httpPost = new HttpPost(url);
			HttpEntity entity = new ByteArrayEntity(content.getBytes());
			httpPost.setEntity(entity);
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
				HttpEntity entity2 = response.getEntity();
				BufferedReader reader = new BufferedReader(new InputStreamReader(entity2.getContent()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
					sb.append(line+"\n");
				}
				EntityUtils.consume(entity2);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return sb.toString();
	}
	/*
	补充缺省,处理办法
	（1)。长句逗号直接改顿号
	（2)。后半句缺失AC，给之加上
	 */
	private  List<SentenceTerm> addedDefault(List<SentenceTerm> sentenceTerms){

		return null;
	}
	/*
	sentenceTerms预处理	合并相同成分
	 */
	private List<SentenceTerm> preDeal(List<SentenceTerm> sentenceTerms) {
		String annotates[] = new String[]{"NA"};
		List<SentenceTerm> newSentenceTerms = new ArrayList<>();
		for (int i = 0; i < sentenceTerms.size(); i++) {
			List<ComNerTerm> comNerTermList = sentenceTerms.get(i).getComNerTermList();
			int offset = sentenceTerms.get(i).getOffset();
			String sentence = sentenceTerms.get(i).getSentence();
			List<ComNerTerm> newComNerTermList = new ArrayList<>();
			for (int j = 0; j < comNerTermList.size() - 1; j++) {
				if (comNerTermList.get(j).typeStr.equals("NA") && comNerTermList.get(j + 1).typeStr.equals("NA")) {
					int strStart = comNerTermList.get(j).offset + comNerTermList.get(j).word.length()-comNerTermList.get(0).offset;
					int strEnd = comNerTermList.get(j + 1).offset-comNerTermList.get(0).offset;

					String str = sentence.substring(strStart, strEnd);
					System.out.println(str);
					String andWord[] = new String[]{"与", "或", "和", "、", "及"};
					boolean andTag = false;
					for (String eachAndWord : andWord) {
						if (str.contains(eachAndWord))
							andTag = true;
					}
					if (!andTag) {
						ComNerTerm newComNerTerm = new ComNerTerm();
						newComNerTerm.typeStr = "NA";
						newComNerTerm.offset = comNerTermList.get(j).offset;
						newComNerTerm.word = comNerTermList.get(j).word + comNerTermList.get(++j).word;
						newComNerTermList.add(newComNerTerm);
					}
				}
				else {
					newComNerTermList.add(comNerTermList.get(j));
				}
			}
			if (comNerTermList.size() >= 1)
				newComNerTermList.add(comNerTermList.get(comNerTermList.size() - 1));
			newSentenceTerms.add(new SentenceTerm(sentence, newComNerTermList, offset));
		}
		return newSentenceTerms;
	}


	/*
    融合句子划分和成分识别
    */
	private List<SentenceTerm> comFuseSenten(List<String> sentenList, List<ComNerTerm> comNerTermList) {
		int sentenceLength;
		int sentenceStart;
		int sentenceEnd = 0;
		int comNerTermLocation = 0;
		List<SentenceTerm> sTermList = new ArrayList<>();
		int offset = 0;

		for (String sentence : sentenList) {
			List<ComNerTerm> cNerTermList = new ArrayList<>();
			sentenceStart = sentenceEnd;
			sentenceLength = sentence.length();
			sentenceEnd = sentenceStart + sentenceLength + 1; // 句读时，丢失分句的标点符号（逗号，分号，句号等），需要长度+1
			for (int j = comNerTermLocation; j < comNerTermList.size(); j++) {
				ComNerTerm comNerTerm = comNerTermList.get(j);
				if (comNerTerm.offset < sentenceEnd &&
						comNerTerm.offset >= sentenceStart) {
					cNerTermList.add(comNerTerm);
				}
				else {
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
	本地测试多份文本
	 */
	public static void main(String[] args) {
		CmbParse cmbParse = new CmbParse(new CmbConfiguration().getCmb());
		File dirInput = new File(args[0]);
		File[] files = dirInput.listFiles();
		for (File file: files) {
			System.out.println(file);
			if(file.toString().endsWith("/197"))
				System.out.println();
			Scanner scanner = null;
			try {
				scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String strLine = scanner.nextLine();
					List<String> inference = cmbParse.parse(strLine);
					for (String eachResult : inference) {
					//	System.out.println(eachResult);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			finally {
				scanner.close();
			}
		}
	}


//	/*
//	本地测试单份文本
//	 */
//	public static void main(String[] args) {
//		String text = "";
////		String out = clean_noise(text);
//		CmbParse cmbParse = new CmbParse(new CmbConfiguration().getCmb());
//		List<String> parse_out = cmbParse.parse(text);
//		System.out.println();
////		System.out.println(out);
//	}


	/*
	预处理  主要删除空格，将英文符号转中文符号，去除一些黑色部分
	 */
	public static String cleanNoise(String text) {
		if (text == null || (text != null && text.length() < 2)) {
			return "";
		}

		text = text.replaceAll(" ","");
		text = text.replaceAll("\\(","（");
		text = text.replaceAll("\\)","）");

		boolean remove = false;

		String[] lineSplit = text.split("\n");
		for (int i = 0; i < lineSplit.length; i++) {
			for (String noise : noises) {
				if (lineSplit[i].startsWith(noise)) {
					remove = true;
					break;
				}
			}

			if (remove) {
				StringBuffer sb = new StringBuffer();
				sb.append(lineSplit[i]);
				while (true) {
					i++;
					if (i == lineSplit.length ) {
						text = text.replace(sb.toString(), "");
						break;
					}
					Matcher m = tipPattern.matcher(lineSplit[i]);
					if (m.find()) {
						sb.append("\n"+lineSplit[i]);
					}
					else {
						text = text.replace(sb.toString(), "");
						remove = false;
						break;
					}
				}

			}
		}
//		System.out.println(text);

		String[] lineSplit2 = text.split("\n");
		for (String eachLine : lineSplit2) {
			StringBuffer sb = new StringBuffer();
			for (String eachNuilty : nullity) { //无效句去除
				if (eachLine.startsWith(eachNuilty)) {
					sb.append("\n"+eachLine);
					break;
				}
			}
			text = text.replace(sb.toString(), "");
		}

		return text;
	}


	/*
	将List集合中的每一个SentenceTerm分别处理
	 */
	public static List<String> inference(List<SentenceTerm> sentenceTermList) {
		List<String> in = new ArrayList<>();
		for (SentenceTerm sentenceTerm : sentenceTermList) {
			StringBuffer ruleOut = new StringBuffer();
			String sentence = sentenceTerm.getSentence();
			int offset = sentenceTerm.getOffset();
			List<ComNerTerm> comNerTermList = sentenceTerm.getComNerTermList();
			if (sentence.contains("，")) {

				String[] split = sentence.split("，");
				int splitInt = offset + split[0].length();
				List<ComNerTerm> comNerTermList1 = new ArrayList<>();
				List<ComNerTerm> comNerTermList2 = new ArrayList<>();
				for (ComNerTerm comNerTerm : comNerTermList) {
					if (comNerTerm.offset < splitInt)
						comNerTermList1.add(comNerTerm);
					else
						comNerTermList2.add(comNerTerm);
				}
				SentenceTerm sentenceTerm1 = new SentenceTerm(split[0],comNerTermList1,offset);
				SentenceTerm sentenceTerm2 = new SentenceTerm(split[1],comNerTermList2,splitInt+1);
				String ininside1 = inferenceInside(sentenceTerm1);
				String ininside2 = inferenceInside(sentenceTerm2);
				if (!ininside1.equals("") && !ininside2.equals("")) {
					if (ininside1.contains("\n")) {
						ininside1 = ininside1.replace("\n","，");
						ruleOut.append(ininside1);
					}

					else
						ruleOut.append(ininside1+"，");
					ruleOut.append(ininside2);
				}
				else {
					if (!ininside1.equals(""))
						ruleOut.append(ininside1);
					if (!ininside2.equals(""))
						ruleOut.append(ininside2);
				}
			}
			else
				ruleOut.append(inferenceInside(sentenceTerm));

			if (!ruleOut.toString().equals(""))
				in.add(ruleOut.toString());
		}
		return in;
	}


	/*
	从SentenceTerm通过规则推导出最终结果
	 */
	public static String inferenceInside(SentenceTerm sentenceTerm) {
		List<ComNerTerm> comNerTermList = sentenceTerm.getComNerTermList();
		StringBuffer sb = new StringBuffer();
		StringBuffer type = new StringBuffer();
		StringBuffer word = new StringBuffer();
		for (ComNerTerm comNerTerm : comNerTermList) {
			type.append(comNerTerm.typeStr + " ");
			word.append(comNerTerm.word + " ");
		}
		String typeAndWord = type.toString() + "\t" + word.toString();
		if (!type.toString().equals("")) {
//				System.out.println();
		}
		String ruleOut = ruleRecursion(type.toString().trim(), typeAndWord, sb);
		StringBuffer inside_out = new StringBuffer();
		if (!ruleOut.equals("")) {
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
		if (ruleOut==null) {
			if (rule.equals(""))
				return sb.toString();
			else {
//				System.out.println(rule);
				System.out.println(lineStr);
				return sb.toString();
			}
		}

		String[] tabSplit = lineStr.split("\t");
		String[] spaceSplit2 = tabSplit[1].split(" ");
		String[] labelLocation = ruleOut.split("#");
		if (ruleOut.contains(",")) {
			String[] ruleArray = labelLocation[0].split(",");
			String[] locationArray = labelLocation[1].split(",");

			for (int i = 0; i < locationArray.length; i++) {
				StringBuffer lineStr2 = new StringBuffer();
				String[] ss = locationArray[i].split(" ");
				lineStr2.append(ruleArray[i].trim());
				lineStr2.append("\t");
				for (int i1 = 0; i1 < ss.length; i1++) {
					int count = Integer.parseInt(ss[i1]);
					lineStr2.append(spaceSplit2[count]+" ");
				}
				ruleRecursion(ruleArray[i].trim(),lineStr2.toString(),sb);
			}
		}
		else {
			String word = "";
			char[] chars = labelLocation[1].toCharArray();
			for (int i = 0; i < chars.length; i++) {
				int aChar = chars[i]; //unicode中　0是４８，空格是３２
				if (aChar==32) {
					word = word+" ";
				}
				else {
					word = word+spaceSplit2[aChar-48];
				}
			}
			sb.append(word+"\t"+labelLocation[0]+"\n");
		}
		return sb.toString();
	}


	// 数据规范化
	public static List<String> normalization(List<String> outList) {
		List<String> normalList = new ArrayList<>();

		for (String eachOut : outList) {
			String[] lineSplit = eachOut.split("\n");
			for (String eachLine : lineSplit) {
				if (!normalList.contains(eachLine)) {
					normalList.add(eachLine);
				}
			}
		}
		return normalList;
	}

}
