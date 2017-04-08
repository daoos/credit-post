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

//	public static String nullity[] = new String[]{"经审议","授信主体","授信币种及金额","业务品种","授信期限",
//			"价格条件", "还款条件","担保条件","分期还款","利率","还款方式","担保方式","同意","附原审批意见","发放要求",
//			"小计","备用额度","合计", "注：","还款安排","编号","放款安排","保证条件","额度项","提用方式","额度启用条件",
//			"使用要求", "启用要求","预留额度"," 成员企业名单","还款计划","借款人","信托金额","币种金额","额度内容",
//			"额度期限", "增信方式","还款来源","分期还款","分行审批意见","保函受益人","费率","被担保人","贷款利率",
//
//			"建议","提款进度安排及相应条件","主要承诺事项","抄送","结论抄送"
//	};

	public static String nullity[] = new String[]{"抄送","结论抄送"
	};


	public static String noises[] = new String[]{"经审议","担保条件","国内保理部分","具体授信主体","前提条件","保理业务要求","主要承若事项","鉴于"};

//	public static String specialSenten[] = new String[]{"若","如果","如","一旦","待","超过","在。。之前","存在变数"};

    public static String annotates[] =new String[]{"AC","OB"};//缺省成分补充
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

            sentenceTerms=shortSentence(sentenceTerms);
            //长句化短句

            sentenceTerms=addedDefault(sentenceTerms);
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
    根据逗号，句号，分号将sentence中长的变短
    */
    private  List<SentenceTerm> shortSentence(List<SentenceTerm> sentenceTerms){
        List<SentenceTerm> sTermList = new ArrayList<>();
        for(int i=0;i<sentenceTerms.size();i++){
            SentenceTerm sentenceTerm = sentenceTerms.get(i);
            String sentence = sentenceTerm.getSentence();
            int offset=sentenceTerm.getOffset();

            if(sentence.contains("，")||sentence.contains("。")||sentence.contains("；")){
                String[] shortsentences=sentence.split("[，。；]");
                List<ComNerTerm> comNerTermList = sentenceTerms.get(i).getComNerTermList();

                for(int j=0;j<shortsentences.length;j++){
                    List<ComNerTerm> comNerTermArrayList = new ArrayList<>();
                    int sentenceLength=shortsentences[j].length();
                    int num=0;
                    boolean isEx=false;
                    for(ComNerTerm comNerTerm:comNerTermList){
                        if(shortsentences[j].contains(comNerTerm.word)&&(offset+sentenceLength>comNerTerm.offset)){
                            if(comNerTerm.typeStr.equals("EX")&&j>0&&sTermList.size()>1){
                                isEx=true;
                            }
                            comNerTermArrayList.add(comNerTerm);
                            num++;
                            if(comNerTerm.typeStr.equals("CO")&&j<shortsentences.length-1&&!shortsentences[j+1].equals("")){//为条件句补充后一句上来
                                shortsentences[j]+="，"+shortsentences[j+1];
                                sentenceLength+=shortsentences[j+1].length()+1;
                                shortsentences[j+1]="";
                                continue;
                            }
                        }
                        else
                            break;
                    }
                    for(int m=num-1;m>=0;m--){
                        comNerTermList.remove(m);
                    }
                    if(isEx) {
                        SentenceTerm sentenceTerm1=sTermList.get(sTermList.size()-1);
                        sentenceTerm1.getComNerTermList().addAll(comNerTermArrayList);
                        sentenceTerm1.setSentence(sentenceTerm1.getSentence()+"，"+shortsentences[j]);
                    }
                    else {
                        SentenceTerm sentenceTerm1=new SentenceTerm(shortsentences[j], comNerTermArrayList,offset);
                        offset+= sentenceLength+1;
                        sTermList.add(sentenceTerm1);
                    }

                }
            }else {
                sTermList.add(sentenceTerm);
            }

        }
        sentenceTerms.clear();
        sentenceTerms=null;
        return sTermList;
    }

	/*
	缺省策略，就近取缺省属性补充上去
	 */
	private  List<SentenceTerm> dealDefault(List<SentenceTerm> sentenceTerms,int subscript,String defaultType)
	{
		int useSentenceTermsNum=1;
        int changeNum=subscript;
        int zhengfu=1;
        boolean flag=true;
		while(changeNum<sentenceTerms.size()&&flag){//钟摆循环
            zhengfu=zhengfu*-1;
            changeNum=changeNum+zhengfu*useSentenceTermsNum++;
            if(changeNum<0||changeNum>sentenceTerms.size()-1)
                continue;
			SentenceTerm sentenceTerm=sentenceTerms.get(changeNum);


			for(int i=0;i<sentenceTerm.getComNerTermList().size()&&flag;i++) {
				List<ComNerTerm> newComNerTermList=sentenceTerm.getComNerTermList();
				if(newComNerTermList.get(i).typeStr.equals(defaultType)) {//匹配所需元组
                    ComNerTerm comNerTerm = newComNerTermList.get(i);//所需要的元组

                    if(i>0&&newComNerTermList.get(i-1).typeStr.equals("AD")) {//所需为AC,前面带了AD的话，带上AD一起
                        comNerTerm=new ComNerTerm(newComNerTermList.get(i-1).word+comNerTerm.word,comNerTerm.typeStr,newComNerTermList.get(i-1).offset);
                    }
                    List<ComNerTerm> comNerTermList = sentenceTerms.get(subscript).getComNerTermList();
                    StringBuffer sb = new StringBuffer(sentenceTerms.get(subscript).getSentence());
                    if(i>comNerTermList.size()-1){//添加元组位置大于短句成分长度，末尾添加
                        int newComNerTermOffset=sentenceTerms.get(subscript).getOffset()+sentenceTerms.get(subscript).getSentence().length();//添加到句子末尾
                        ComNerTerm newComNerTerm=new ComNerTerm(comNerTerm.word, comNerTerm.typeStr, newComNerTermOffset);
                        comNerTermList.add(newComNerTerm);
                        sb.insert(sb.length(),comNerTerm.word);
                    }else{//反之，中间相应位置添加
                        int newComNerTermOffset=comNerTermList.get(0).offset;//sentenceTerms.get(subscript).getOffset();
                        for(int j=0;j<i;j++) {
                            newComNerTermOffset+=comNerTermList.get(j).word.length();
                        }
                        if(newComNerTermOffset>sentenceTerms.get(subscript).getOffset()+sentenceTerms.get(subscript).getSentence().length())
                            newComNerTermOffset=sentenceTerms.get(subscript).getOffset()+sentenceTerms.get(subscript).getSentence().length();
                        ComNerTerm newComNerTerm=new ComNerTerm(comNerTerm.word, comNerTerm.typeStr, newComNerTermOffset);
                        comNerTermList.add(i,newComNerTerm);
                        sb.insert(newComNerTermOffset-sentenceTerms.get(subscript).getOffset(),comNerTerm.word);
                        for(int j=i+1;j<comNerTermList.size();j++) {
                            comNerTermList.get(j).offset=comNerTermList.get(j).offset+comNerTerm.word.length();
                        }
                    }
                    sentenceTerms.get(subscript).setSentence(sb.toString());


                    for(int j=subscript+1;j<sentenceTerms.size();j++) {//修改后续下标
                        sentenceTerms.get(j).setOffset(sentenceTerms.get(j).getOffset()+comNerTerm.word.length());//后续句子结构下标改变
                        List<ComNerTerm> comNerTermList1 = sentenceTerms.get(j).getComNerTermList();
                        for(int m=0;m<comNerTermList1.size();m++){
                            comNerTermList1.get(m).offset=comNerTermList1.get(m).offset+comNerTerm.word.length();
                        }
                    }
                    flag=false;
				}
			}

		}
        return sentenceTerms;
	}
	/*
	补充缺省,处理办法
	（2)。后半句缺失成分，给之加上
	 */
	private  List<SentenceTerm> addedDefault(List<SentenceTerm> sentenceTerms){

		for(int i=0;i<sentenceTerms.size();i++) {
			List<ComNerTerm> comNerTermList = sentenceTerms.get(i).getComNerTermList();
			if(comNerTermList.size()==0)
			    continue;
			Boolean annotatesBoolean[] = new Boolean[annotates.length];//ob,ac
            for(int j=0;j<annotatesBoolean.length;j++){//初始化
                annotatesBoolean[j]=false;
            }

			for (int j = 0; j < comNerTermList.size(); j++){
				for(int m=0;m<annotates.length;m++) {
					if(comNerTermList.get(j).typeStr.equals(annotates[m])){
						annotatesBoolean[m]=true;
						break;
					}
				}
			}
			for(int j=0;j<annotatesBoolean.length;j++) {
				if(!annotatesBoolean[j]){
					//处理que省
					//  sentenceTerms i annotates[j]返回新的 newcomNerTermList
                    sentenceTerms=dealDefault(sentenceTerms,i,annotates[j]);
				}
			}
		}
		return sentenceTerms;
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

//					int strStart = comNerTermList.get(j).offset + comNerTermList.get(j).word.length()-comNerTermList.get(0).offset;
//					int strEnd = comNerTermList.get(j + 1).offset-comNerTermList.get(0).offset;

					int strStart = comNerTermList.get(j).offset + comNerTermList.get(j).word.length() - offset;
					int strEnd = comNerTermList.get(j + 1).offset - offset;

					String str = sentence.substring(strStart, strEnd);
//					System.out.println(str);
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
	public static void main(String[] args) throws IOException {
		CmbParse cmbParse = new CmbParse(new CmbConfiguration().getCmb());
		File dirInput = new File(args[0]);
		File[] files = dirInput.listFiles();
		for (File file: files) {
			System.out.println(file);

			if(file.toString().endsWith("/40"))
				System.out.println();


//			FileWriter fileWriter = new FileWriter(file);

			Scanner scanner = null;
			try {
				scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String strLine = scanner.nextLine();
					List<String> inference = cmbParse.parse(strLine);
					for (String eachResult : inference) {
						System.out.println("eachResult:\t"+eachResult);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			finally {
				scanner.close();
//				fileWriter.flush();
//				fileWriter.close();
			}
		}
	}



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
	处理条件句
	 */
	public static String dealCondition(SentenceTerm sentenceTerm) {
		StringBuffer sbAction = new StringBuffer();
		List<ComNerTerm> comNerTermList = sentenceTerm.getComNerTermList();
		if (comNerTermList.size() < 2) {
			return sbAction.toString();
		}
		List<ComNerTerm> newComNerTerm = new ArrayList<>();
		StringBuffer sbCondition = new StringBuffer();
		String sentence = sentenceTerm.getSentence();
		int location = sentenceTerm.getOffset();
		String[] symbolSplit = sentence.split("[,，.。;；:：、]");
		for (int i = 0; i < symbolSplit.length; i++) {
			int sentenEnd = location + symbolSplit[i].length();
			boolean appendTag = false;
			for (ComNerTerm eachComNerTerm : comNerTermList) {
				if (eachComNerTerm.typeStr.toString().equals("CO") && sbCondition.toString().equals("")) {
					sbCondition.append("条件:" + eachComNerTerm.word);
					location = eachComNerTerm.offset + eachComNerTerm.word.length();
					continue;
				}
				if (eachComNerTerm.offset < location) {
					continue;
				}
				if (location <= eachComNerTerm.offset && eachComNerTerm.offset < sentenEnd && !eachComNerTerm.typeStr.toString().equals("CO")) {
					if (!newComNerTerm.contains(eachComNerTerm)) {
						newComNerTerm.add(eachComNerTerm);
						sbAction.append(eachComNerTerm.word);
						appendTag = true;
					}
				}
				else if (eachComNerTerm.offset > sentenEnd) {
					break;
				}
			}
//			comNerTermList = newComNerTerm;
			if (appendTag) {
				sbAction.append(",");

			}
			location = sentenEnd + 1;
		}
		if (sbAction.toString().length() > 0) {
			String substring = sbAction.toString().substring(0, sbAction.toString().length() - 1);
			String ret = sbCondition.toString() + " 动作:" + substring;
//			System.out.println(ret);
			return  ret;
		}
		else {
			return "";
		}
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
			boolean tagCO = false;
			boolean tagEX = false;
			String EXOB = "";
			String EXWord = "";
			List<ComNerTerm> EXComNerTermList = new ArrayList<>();
			String EXSentence = sentence;
			int EXLength = 0;
			for (ComNerTerm eachComNerTerm : comNerTermList) {
				if (eachComNerTerm.typeStr.toString().equals("CO")) {
					tagCO = true;
				}
				if (eachComNerTerm.typeStr.toString().equals("EX")) {
					tagEX = true;
					EXSentence = EXSentence.replace(eachComNerTerm.word, "");
					if (EXWord.equals("")) {
						EXWord = eachComNerTerm.word;
						EXLength = EXWord.length();
					}
					else {
						EXLength = EXLength + eachComNerTerm.word.length();
						EXWord = EXWord + "," + eachComNerTerm.word;
					}
				}
				else {
					String typeStr = eachComNerTerm.typeStr;
					String word = eachComNerTerm.word;
					int offset1 = eachComNerTerm.offset;
					EXComNerTermList.add(new ComNerTerm(word, typeStr, offset1 - EXLength));
				}
				if (eachComNerTerm.typeStr.toString().equals("OB")) {
					EXOB = eachComNerTerm.word;
				}
			}
			if (tagCO) {
				sentenceTerm = new SentenceTerm(EXSentence, EXComNerTermList, offset);
				ruleOut.append(dealCondition(sentenceTerm));
			}
			else {
				if (tagEX) {
					sentenceTerm = new SentenceTerm(EXSentence, EXComNerTermList, offset);
				}
				List<SentenceTerm> connect = connect(sentenceTerm);
				for (SentenceTerm eachSentenTerm : connect) {
					ruleOut.append(inferenceInside(eachSentenTerm));
				}
			}
			String strRuleOut = ruleOut.toString();
			if (tagEX && !EXOB.equals("") && strRuleOut.contains(EXOB)) {
				strRuleOut = strRuleOut.replace(EXOB, EXOB + "【" + EXWord + "】");
//				System.out.println("--------------->>"+strRuleOut);
			}
			if (!ruleOut.toString().equals("")) {
				in.add(strRuleOut);
			}
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


	private static List<SentenceTerm> connect(SentenceTerm sentenceTerm)
	{
		List<SentenceTerm> sentenceTermList = new ArrayList<>();
		List<String> sentenList = new ArrayList<>();  //用于排除相同的句子
		sentenceTermList.add(sentenceTerm);
		sentenList.add(sentenceTerm.getSentence());
		if (hasDouble(sentenceTerm.getComNerTermList()))
		{
			String doubleValues[] = new String[]{"OB", "NA"};
			int count = 0;
			while (true)
			{
				System.out.println(sentenceTerm.getSentence());
				outer:
				for (int q = 0;q < sentenceTermList.size();q++)
				{
					List<ComNerTerm> comNerTermList = sentenceTermList.get(q).getComNerTermList();
					for (int i = 0; i < comNerTermList.size() - 1; i++)
					{
						for (String doubleValue : doubleValues)
						{
							if (comNerTermList.get(i).typeStr.equals(doubleValue) && comNerTermList.get(i + 1).typeStr.equals(doubleValue))
							{

								int l = comNerTermList.get(i + 1).offset - comNerTermList.get(i).offset;
								int subStart = comNerTermList.get(i).offset - sentenceTerm.getOffset();  //第一个D的起始位置-句子的起始位置
								String subSentence = sentenceTermList.get(q).getSentence().substring(subStart, subStart + l);
								String replaceStr = subSentence+comNerTermList.get(i+1).word;
								if ((subSentence.contains("、")||subSentence.contains("或")||subSentence.contains("及")||subSentence.contains("与")))
								{
									//重组成新的２句话
									//左肺及右肺下见斑片影　　将“左肺及右肺下”分别替换为“左肺”和“右肺下”
									String senten1 = sentenceTermList.get(q).getSentence().replace(replaceStr,comNerTermList.get(i).word);
									List<ComNerTerm> comNerTerm1 = new ArrayList<>();
									for (int j = 0; j < comNerTermList.size(); j++)
									{
										if (j!=(i+1))
										{
											int offset1 = comNerTermList.get(j).offset;
											int weiyi = sentenceTermList.get(q).getSentence().length()-senten1.length();
											if (comNerTermList.get(j).offset>=comNerTermList.get(i+1).offset)
											{
												offset1 -= weiyi;
											}
											comNerTerm1.add(new ComNerTerm(comNerTermList.get(j).word,comNerTermList.get(j).typeStr,offset1));
										}
									}
									SentenceTerm newSenterm1 = new SentenceTerm(senten1,comNerTerm1,comNerTerm1.get(0).offset);
									String senten2 = sentenceTermList.get(q).getSentence().replace(replaceStr,comNerTermList.get(i+1).word);
									List<ComNerTerm> comNerTerm2 = new ArrayList<>();
									for (int j = 0; j < comNerTermList.size(); j++)
									{
										if (j != i)
										{
											int offset2 = comNerTermList.get(j).offset;
											int weiyi = sentenceTermList.get(q).getSentence().length()-senten2.length();
											if (comNerTermList.get(j).offset>=comNerTermList.get(i).offset)
											{
												offset2 -= weiyi;
											}
											comNerTerm2.add(new ComNerTerm(comNerTermList.get(j).word,comNerTermList.get(j).typeStr,offset2));
										}
									}
									SentenceTerm newSenterm2 = new SentenceTerm(senten2,comNerTerm2,comNerTerm2.get(0).offset);
									//移除原来的，加上分好的子句
									sentenceTermList.remove(q);
									sentenList.remove(q);
									if (!sentenList.contains(newSenterm1.getSentence()))
									{
										sentenceTermList.add(newSenterm1);
										sentenList.add(newSenterm1.getSentence());
									}
									if (!sentenList.contains(newSenterm2.getSentence()))
									{
										sentenceTermList.add(newSenterm2);
										sentenList.add(newSenterm2.getSentence());
									}
									break outer;
								}
								else
								{
									//重新生成一个句子
									String newsentence = sentenceTermList.get(q).getSentence();
									int newoffset = sentenceTermList.get(q).getOffset();
									List<ComNerTerm> newComNerTerm = new ArrayList<>();

									for (int i1 = 0; i1 < comNerTermList.size(); i1++)
									{
										if (i1 == i)
										{
											newComNerTerm.add(new ComNerTerm(replaceStr,comNerTermList.get(i).typeStr,comNerTermList.get(i).offset));
											continue ;
										}
										if (i1 != (i+1))
										{
											newComNerTerm.add(new ComNerTerm(comNerTermList.get(i1).word,comNerTermList.get(i1).typeStr,comNerTermList.get(i1).offset));
										}
									}
									SentenceTerm newSentenceTerm = new SentenceTerm(newsentence,newComNerTerm,newoffset);
									sentenceTermList.remove(q);
									sentenList.remove(q);
									if (!sentenList.contains(newSentenceTerm.getSentence()));
									{
										sentenceTermList.add(newSentenceTerm);
										sentenList.add(newSentenceTerm.getSentence());
									}
									break outer;
								}
							}
						}
					}
				}


				int t = 0;
				for (SentenceTerm term : sentenceTermList) {
					if (!hasDouble(term.getComNerTermList())) {
						t++;
					}
				}
				if (t == sentenceTermList.size()) //如果都没有连续属性出现就退出
					break;
				count++;
				if (count>15)break;  //防止陷入死循环
			}
		}

		return sentenceTermList;
	}


	//判断是否还存在需要连续的O或D或T
	private static boolean hasDouble(List<ComNerTerm> comNerTermList)
	{
		StringBuffer sb = new StringBuffer();
		for (ComNerTerm comNerTerm : comNerTermList)
		{
			sb.append(comNerTerm.typeStr+" ");
		}
//		if (ruleMap.get(sb.toString().trim())!=null)
//		{ //如果有这样的规则，则不需要分句
//			return false;
//		}
		if (sb.toString().contains("OB OB")||sb.toString().contains("NA NA"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	/*
    规则递归推导
     */
	public static String ruleRecursion(String rule,String lineStr,StringBuffer sb)
	{
		String ruleOut = ruleMap.get(rule);
		if (ruleOut == null) {
			if (rule.equals(""))
				return sb.toString();
			else if (!rule.contains("AC") && rule.contains("OB")) {
//				System.out.println(rule);
				System.out.println(lineStr);
				// TODO: 17-3-30  加缺失成分后面要仔细处理
					rule = "AC " + rule;
					lineStr = "AC" + lineStr;
					lineStr = lineStr.replace("\t", "\t要求 ");
					ruleOut = ruleMap.get(rule);
					if (ruleOut == null) {
						return sb.toString();
					}
			}
			else {
                System.out.println(sb.toString());
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
					if (aChar > '9') {
						aChar = aChar - 7;
					}
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
					if (eachLine.contains("，") && !eachLine.contains("【") && !eachLine.contains("条件")) {
						String[] commaSplit = eachLine.split("，");
						for (String eachCommaSplit : commaSplit) {
							normalList.add(eachCommaSplit);
//							System.out.println(eachCommaSplit);
						}
					}
					else {
						normalList.add(eachLine);
//						System.out.println(eachLine);
					}
				}
			}
		}
		return normalList;
	}

}
