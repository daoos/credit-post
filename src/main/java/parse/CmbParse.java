package parse;

import bean.ComNerTerm;
import bean.ParagraphParamer;
import bean.RegRuleEntity;
import bean.SentenceTerm;
import com.hankcs.hanlp.utility.Predefine;
import conf.CmbConfig;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tools.CommonlyTools;
import tools.Levenshtein;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    public static String annotates[] =new String[]{"AC","OB"};//缺省成分补充
	public static String andWord[] = new String[]{"与", "或", "、", "及"};
	private ComParse comParse = null;
	private SentenParse sentenParse = null;
	private static OpinionMining mining = null;
	private static Map<String,String> ruleMap = null;
	private static List<RegRuleEntity> allRegRule = null;
    public CmbParse(CmbConfig cmbConfig) throws Exception {
		Predefine.HANLP_PROPERTIES_PATH = cmbConfig.hanlp;
		try {
			comParse = new ComParse(cmbConfig);
			sentenParse = new SentenParse(cmbConfig);
			mining = new OpinionMining(cmbConfig.getModel());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		allRegRule =CommonlyTools.getAllRegRule(cmbConfig.getRegexFile());
		ruleMap = new HashMap<>();
		Scanner scanner = null;
		try {

			scanner = new Scanner(new File(cmbConfig.ruleFile));
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

	/**
	 * 本地测试多份文本
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		CmbConfig cmbConfig = loadConfig();
		CmbParse cmbParse = null;
		try {
			cmbParse = new CmbParse(cmbConfig);
		} catch (Exception e) {
			e.printStackTrace();
		}
		File dirInput = new File(args[0]);
		File[] files = dirInput.listFiles();
		for (File file: files) {
			System.out.println(file);
			if(file.toString().endsWith("/73"))
				System.out.println();
			Scanner scanner = null;
			try {
				scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String strLine = scanner.nextLine();
					List<String> inference = cmbParse.parse(strLine);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			finally {
				scanner.close();
			}
		}
	}

	/**
	 * App调用入口
	 * @param text	请求的授信文本
	 * @return	结构化结果
	 */
	public synchronized 	List<String> parse(String text) {
		text=text.replace("\\(","）").replaceAll("\\)","）");//测试使用预处理修改至python部分
		ParagraphParamer paramer = new ParagraphParamer(text);
		List<String> outList = new ArrayList<>();
		for (String eachLine : text.split("\n")) {
			if (eachLine.length() < 2)
				continue;
			//获取是否有段落层面的AC需要记录
			if(paramer.getParagraghAC()==null) {
				paramer.hasACDemon(eachLine);
			}
			else{
				paramer.isSerialSentense(eachLine);
			}

			List<ComNerTerm> comNerTermList = comParse.comService(eachLine);
			// 成分标注

			List<String> sentenList = sentenParse.sentenService(eachLine);
			// 句子标注

			List<SentenceTerm> sentenceTerms = comFuseSenten(sentenList, comNerTermList);
			// 成分句子融合

			List<SentenceTerm> shortSentenceTerms =shortSentence(sentenceTerms);
            //长句化短句以及补充缺省

			System.out.println("原句：\t"+eachLine);
			System.out.println("成分：\t"+shortSentenceTerms);
			List<String> inference = inference(paramer,shortSentenceTerms,sentenceTerms);
			// 处理每一句
			for (String eachResult : inference) {
				outList.add(eachResult);
			}
		}

		List<String> normalization = normalization(outList);
		// 数据规范化

		if(normalization.size()>1)
			normalization = removeDuplicates(normalization);

		return normalization;
	}

	/**
	 * 根据逗号，句号，分号将sentence中长的变短
	 * @param sentenceTerms	句子集合
	 * @return	变短的句子集合
	 */
	private  List<SentenceTerm> shortSentence(List<SentenceTerm> sentenceTerms){
        List<SentenceTerm> sTermList = new ArrayList<>();
        for(int i=0;i<sentenceTerms.size();i++){
            SentenceTerm sentenceTerm = sentenceTerms.get(i);
            String sentence = sentenceTerm.getSentence();
            int offset=sentenceTerm.getOffset();
            boolean hasCoEXFlag=false;
			for(ComNerTerm comNerTerm:sentenceTerm.getComNerTermList()){//包含CO EX 长句不拆分
				if(comNerTerm.typeStr.equals("CO")||comNerTerm.typeStr.equals("EX")){
					hasCoEXFlag=true;
					break;
				}
			}
            if(!hasCoEXFlag&&(sentence.contains("，")||sentence.contains("。")||sentence.contains("；"))){
				List<SentenceTerm> tempTermList = new ArrayList<>();
                String[] shortsentences=sentence.split("[，。；,]");
                List<ComNerTerm> comNerTermList = sentenceTerms.get(i).getComNerTermList();

                for(int j=0;j<shortsentences.length;j++){
                    List<ComNerTerm> comNerTermArrayList = new ArrayList<>();
                    int sentenceLength=shortsentences[j].length();
                    int num=0;
                    boolean isEx=false;
                    for(ComNerTerm comNerTerm:comNerTermList){
                        if(shortsentences[j].contains(comNerTerm.word)&&(offset+sentenceLength>comNerTerm.offset)){
                            if(comNerTerm.typeStr.equals("EX")&&j>0&&tempTermList.size()>1){
                                //为补充EX句留到后面处理将与前一合并
                                isEx=true;
                            }
                            comNerTermArrayList.add(comNerTerm);
                            num++;
                            if(comNerTerm.typeStr.equals("CO")&&j<shortsentences.length-1&&!shortsentences[j+1].equals("")){
                                //为条件句补充后一句上来
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
                        //成分补充句与前一句合并
                        SentenceTerm sentenceTerm1=tempTermList.get(tempTermList.size()-1);
                        sentenceTerm1.getComNerTermList().addAll(comNerTermArrayList);
                        sentenceTerm1.setSentence(sentenceTerm1.getSentence()+"，"+shortsentences[j]);
                    }
                    else {
                        SentenceTerm sentenceTerm1=new SentenceTerm(shortsentences[j], comNerTermArrayList,offset);
                        offset+= sentenceLength+1;
						tempTermList.add(sentenceTerm1);
                    }

                }
				tempTermList=addedDefault(tempTermList);
				//判断缺省成分
				sTermList.addAll(tempTermList);
            }else {
                sTermList.add(sentenceTerm);
            }

        }
        return sTermList;
    }

	/**
	 * 缺省策略，就近取缺省属性补充上去
	 有待完善 策略本身存在偏差
	 目前是向前搜寻所缺省的成分
	 * @param sentenceTerms
	 * @param subscript
	 * @param defaultType
	 * @return
	 */
	private  List<SentenceTerm> dealDefault(List<SentenceTerm> sentenceTerms,int subscript,String defaultType)
	{
		int useSentenceTermsNum=1;
        int changeNum=subscript;
        int zhengfu=-1;
        boolean flag=true;
		while(changeNum<sentenceTerms.size()&&flag){//钟摆循环
         //   zhengfu=zhengfu*-1;//注销只前至搜索
            changeNum=changeNum+zhengfu*useSentenceTermsNum++;
            if(changeNum<0||changeNum>sentenceTerms.size()-1)
                continue;
			SentenceTerm sentenceTerm=sentenceTerms.get(changeNum);
			for(int x=0;x<sentenceTerm.getComNerTermList().size()&&flag;x++) {
				int i=x;
				if(zhengfu<0)
					i=sentenceTerm.getComNerTermList().size()-1-x;
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

	/**
	 * 补充缺省,处理办法
	 （2)。后半句缺失成分，给之加上
	 * @param sentenceTerms
	 * @return
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
			boolean hasADFlag=false;
			for (int j = 0; j < comNerTermList.size(); j++){
				for(int m=0;m<annotates.length;m++) {
					if(comNerTermList.get(j).typeStr.equals(annotates[m])){
						annotatesBoolean[m]=true;
						break;
					}
				}
				if(comNerTermList.get(j).typeStr.equals("AD")){
					hasADFlag=true;
				}
			}

			int flagNum=0;//AC OB都没用不补充了
			 for(Boolean b:annotatesBoolean){
				if(!b){
					flagNum++;
				}
			 }
			//假如AO OB都缺  就先不处理
			 if(flagNum!=annotatesBoolean.length)
			for(int j=0;j<annotatesBoolean.length;j++) {
				if(!annotatesBoolean[j]){//
					//处理缺省
					if(!annotatesBoolean[0]&&hasADFlag)//有OB少AC 但是短句有AD 情况不做缺省处理
						continue;
                    sentenceTerms=dealDefault(sentenceTerms,i,annotates[j]);//缺省处理
				}
			}
		}
		return sentenceTerms;
	}

	/**
	 * sentenceTerms预处理	合并相同成分
	 * @param sentenceTerms
	 * @return
	 */
	private static List<SentenceTerm> preDeal(List<SentenceTerm> sentenceTerms) {
		List<SentenceTerm> newSentenceTerms = new ArrayList<>();
            for (int i = 0; i < sentenceTerms.size(); i++) {
                List<ComNerTerm> comNerTermList = sentenceTerms.get(i).getComNerTermList();
                int offset = sentenceTerms.get(i).getOffset();
                String sentence = sentenceTerms.get(i).getSentence();
                List<ComNerTerm> newComNerTermList = new ArrayList<>();
                    for (int j = 0; j < comNerTermList.size() - 1; j++) {
                    	if(comNerTermList.get(j).typeStr.equals("NA")){
                    		//将NA周围未识别部分扩充进来
							int currentlyWordOffset=comNerTermList.get(j).offset;
							String currentlyWord=comNerTermList.get(j).word;
							if(j<comNerTermList.size() - 1){//获取之后的未定义成分
								int nextWordOffset=comNerTermList.get(j+1).offset;
								if(nextWordOffset-currentlyWordOffset>currentlyWord.length()) {
									try{
										comNerTermList.get(j).setWord(currentlyWord+
												sentence.substring(currentlyWordOffset+
														currentlyWord.length()-offset,nextWordOffset-offset));
									}catch ( Exception e){
										e.printStackTrace();
									}
								}
							}
							//前后NA合并
							if ( (j<comNerTermList.size() - 1&&comNerTermList.get(j + 1).typeStr.equals("NA"))||(j>0&&comNerTermList.get(j-1).typeStr.equals("NA") )) {
								int strStart = comNerTermList.get(j).offset + comNerTermList.get(j).word.length() - offset;
								int strEnd = comNerTermList.get(j + 1).offset - offset;
								String str = sentence.substring(strStart, strEnd);

								boolean andTag = false;
								for (String eachAndWord : andWord) {
									if (str.contains(eachAndWord))
										andTag = true;
								}
								if (!andTag) {
									ComNerTerm newComNerTerm=null;
									if(newComNerTermList.size()>0&&newComNerTermList.get(newComNerTermList.size()-1).typeStr.equals("NA")){
										newComNerTerm=newComNerTermList.get(newComNerTermList.size()-1);
										newComNerTermList.remove(newComNerTermList.size()-1);
										newComNerTerm.word = newComNerTerm.word + comNerTermList.get(j).word;
									}else{
										newComNerTerm = new ComNerTerm();
										newComNerTerm.typeStr = "NA";
										newComNerTerm.offset = comNerTermList.get(j).offset;
										newComNerTerm.word = comNerTermList.get(j).word + comNerTermList.get(j + 1).word;
									}

									newComNerTermList.add(newComNerTerm);
									if(j<comNerTermList.size()-1)
										j++;
								}
							}
							else{
								newComNerTermList.add(comNerTermList.get(j));
							}
						}
                        else {
                            newComNerTermList.add(comNerTermList.get(j));
                        }
                    }

                if (comNerTermList.size() >= 1) {
					newComNerTermList.add(comNerTermList.get(comNerTermList.size() - 1));
				}
                newSentenceTerms.add(new SentenceTerm(sentence, newComNerTermList, offset));
            }
		return newSentenceTerms;
	}

	/**
	 * 融合句子划分和成分识别
	 * @param sentenList
	 * @param comNerTermList
	 * @return
	 */
	private List<SentenceTerm> comFuseSenten(List<String> sentenList, List<ComNerTerm> comNerTermList) {
		int sentenceLength;
		int sentenceStart;
		int sentenceEnd = 0;
		int comNerTermLocation = 0;
		List<SentenceTerm> sTermList = new ArrayList<>();
		int offset = 0;

		for (int i=0;i<sentenList.size();i++) {
			String sentence=sentenList.get(i);
			List<ComNerTerm> cNerTermList = new ArrayList<>();
			sentenceStart = sentenceEnd;
			sentenceLength = sentence.length();
			sentenceEnd = sentenceStart + sentenceLength + 1; // 句读时，丢失分句的标点符号（逗号，分号，句号等），需要长度+1

			//判断是否句子有（）
			Pattern pattern = Pattern.compile("(?<=（)[^）]+");
			Matcher matcher = pattern.matcher(sentence);
			while(matcher.find())
			{
				for(int j = 0; j < comNerTermList.size(); j++){//将括号中以识别成分类型替换成EX
					if(matcher.group().contains(comNerTermList.get(j).word)){
						comNerTermList.get(j).setTypeStr("EX");
					}
				}
				//未识别出来括号中的东西先不处理 预留

			}
			for (int j = comNerTermLocation; j < comNerTermList.size(); j++) {
				ComNerTerm comNerTerm = comNerTermList.get(j);
				if (comNerTerm.offset < sentenceEnd && comNerTerm.offset >= sentenceStart) {
					cNerTermList.add(comNerTerm);
					if(sentenceLength<comNerTerm.word.length()&&i+1<sentenList.size()){
						sentence+=sentenList.get(i+1);
						i++;
						sentenceLength= sentence.length();
						sentenceEnd=sentenceStart+sentenceLength+1;
					}
				}
				else {
					comNerTermLocation = j;
					break;
				}
			}
			//将对应的成分加入到相应的句子中构建
			sTermList.add(new SentenceTerm(sentence, cNerTermList, offset));
			offset += sentenceLength + 1;
		}

		List<SentenceTerm> sTermListResult = new ArrayList<>();
		//将只有EX与CO   分词成分word与句子一样的时候 EX合并前一句，CO与后一句合并
		for(int i=0;i<sTermList.size();i++){
			SentenceTerm sentenceTerm = sTermList.get(i);
			if(sentenceTerm.getComNerTermList().size()==1){
				if(sentenceTerm.getComNerTermList().get(0).typeStr.equals("CO")&&i+1<sTermList.size()){
					SentenceTerm sentenceTerm1 = sTermList.get(i + 1);
					i++;
					sentenceTerm.setSentence(sentenceTerm.getSentence()+"，"+sentenceTerm1.getSentence());
					for(ComNerTerm comNerTerm:sentenceTerm1.getComNerTermList()){
						sentenceTerm.getComNerTermList().add(comNerTerm);
					}
				}
				else if(sentenceTerm.getComNerTermList().get(0).typeStr.equals("EX")&&i>0){
					SentenceTerm sentenceTerm1 = sTermList.get(i - 1);
					sentenceTerm.setSentence(sentenceTerm1.getSentence()+"，"+sentenceTerm.getSentence());
					sentenceTerm.setOffset(sentenceTerm1.getOffset());
					for(ComNerTerm comNerTerm:sentenceTerm.getComNerTermList()){
						sentenceTerm1.getComNerTermList().add(comNerTerm);
					}
					sentenceTerm.setComNerTermList(sentenceTerm1.getComNerTermList());
					sTermListResult.remove(sTermListResult.size()-1);
				}
			}
			sTermListResult.add(sentenceTerm);
		}
		sTermList.clear();
		return sTermListResult;
	}

	/**
	 * 本地测试加载cmb.yaml配置文件
	 * @return	CmbConfig对象
	 */
	public static CmbConfig loadConfig() {
		Scanner scanner = null;
		CmbConfig cmbConfig = new CmbConfig();
		try {
			scanner = new Scanner(new File("cmb.yaml"));
			while (scanner.hasNext()) {
				String strLine = scanner.nextLine();
				if (strLine.contains("hanlp")) {
					String[] hanlpSplit = strLine.split("hanlp: ");
					cmbConfig.setHanlp(hanlpSplit[1]);
				}
				if (strLine.contains("cmbSenten")) {
					String[] cmbSentenSplit = strLine.split("cmbSenten: ");
					cmbConfig.setCmbSenten(cmbSentenSplit[1]);
				}
				if (strLine.contains("cmbCom")) {
					String[] cmbComSplit = strLine.split("cmbCom: ");
					cmbConfig.setCmbCom(cmbComSplit[1]);
				}
				if (strLine.contains("ruleFile")) {
					String[] ruleFileSplit = strLine.split("ruleFile: ");
					cmbConfig.setRuleFile(ruleFileSplit[1]);
				}
				if (strLine.contains("modelPath")) {
					String[] ruleFileSplit = strLine.split("modelPath: ");
					cmbConfig.setModel(ruleFileSplit[1]);
				}
				if (strLine.contains("regexFile")) {
					String[] ruleFileSplit = strLine.split("regexFile: ");
					cmbConfig.setRegexFile(ruleFileSplit[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		return cmbConfig;
	}

	/**
	 * 处理条件句
	 * @param sentenceTerm
	 * @return	结果化结果
	 */
	public static String dealCondition(SentenceTerm sentenceTerm) {
		StringBuffer sbAction = new StringBuffer();
		List<ComNerTerm> comNerTermList = sentenceTerm.getComNerTermList();
		if (comNerTermList.size() < 2) {
			return sbAction.toString();
		}
		List<ComNerTerm> newComNerTerm = new ArrayList<>();
		StringBuffer sbCondition = new StringBuffer();
		sbCondition.append("条件:" );
		String sentence = sentenceTerm.getSentence();
		int location = sentenceTerm.getOffset();
		String[] symbolSplit = sentence.split("[,，.。;；:：、]");
		for (int i = 0; i < symbolSplit.length; i++) {
			int sentenEnd = location + symbolSplit[i].length();
			boolean appendTag = false;
			boolean coFlag=false; //是否条件成分被吸入sbCondition
			for (ComNerTerm eachComNerTerm : comNerTermList) {
				if (eachComNerTerm.typeStr.toString().equals("CO") ) {
					sbCondition.append(eachComNerTerm.word);
					location = eachComNerTerm.offset + eachComNerTerm.word.length();
					coFlag=true;
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
			for(int x=0;x<comNerTermList.size()&&coFlag;x++){
				ComNerTerm eachComNerTerm=comNerTermList.get(x);
				if(eachComNerTerm.typeStr.equals("CO")) {
					comNerTermList.remove(eachComNerTerm);
					x--;
				}
			}
			if (appendTag) {
				sbAction.append(",");

			}
			location = sentenEnd + 1;
		}
		if (sbAction.toString().length() > 0) {
			String substring = sbAction.toString().substring(0, sbAction.toString().length() - 1);
			String ret = sbCondition.toString() + " 动作:" + substring;
			return  ret;
		}
		else {
			return "";
		}
	}

	/**
	 * 将List集合中的每一个SentenceTerm分别处理
	 * @param paragraphParamer	段落对象
	 * @param sentenceTermList	句子集合
	 * @param longSentenceTerms	长句集合
	 * @return
	 */
	public static List<String> inference(ParagraphParamer paragraphParamer,List<SentenceTerm> sentenceTermList,List<SentenceTerm> longSentenceTerms) {
		List<String> in = new ArrayList<>();
		List<String>  longSentenceList=null;

		if(paragraphParamer.isHasrisk()){
			//将长句中句子部分组成list给予后面出现风险点获取，避免短句的效果不理想
			longSentenceList =new ArrayList<>();
			for(SentenceTerm longSentenceTerm:longSentenceTerms){
				longSentenceList.add(longSentenceTerm.getSentence());
			}
		}

		for (SentenceTerm sentenceTerm : sentenceTermList) {
			StringBuffer ruleOut = new StringBuffer();
			String sentence = sentenceTerm.getSentence();
			int offset = sentenceTerm.getOffset();
			List<ComNerTerm> comNerTermList = sentenceTerm.getComNerTermList();
			boolean tagCO = false;
			boolean tagEX = false;
			boolean tagAC = false;
			String EXOB = "";
			String EXWord = "";
			List<ComNerTerm> EXComNerTermList = new ArrayList<>();
			String EXSentence = sentence;
			int EXLength = 0;
			int currComNum=0;
			int firstComNeroffset = 0;
			if(comNerTermList.size()>0)
				firstComNeroffset = comNerTermList.get(0).getOffset();
			for (ComNerTerm eachComNerTerm : comNerTermList) {
				if (eachComNerTerm.typeStr.toString().equals("CO")) {
					tagCO = true;
				}
				if (eachComNerTerm.typeStr.toString().equals("AC")) {
					tagAC = true;
				}
				if (eachComNerTerm.typeStr.toString().equals("EX")) {

					tagEX = true;
					try {
						String behind = EXSentence.substring(0, eachComNerTerm.offset-firstComNeroffset);
						String front = EXSentence.substring(eachComNerTerm.offset-firstComNeroffset,
								EXSentence.length()).replaceFirst(StringEscapeUtils.unescapeJava(eachComNerTerm.word), "");
						EXSentence = behind + front;
					}catch (Exception e){
						e.printStackTrace();
					}
					if (EXWord.equals("")) {
						EXWord = eachComNerTerm.word;
						EXLength = EXWord.length();
					}
					else {
						EXLength = EXLength + eachComNerTerm.word.length();
						EXWord = EXWord + "," + eachComNerTerm.word;
					}
					for(int i=currComNum+1;i<comNerTermList.size();i++){
						ComNerTerm comNerTerm = comNerTermList.get(i);
						comNerTerm.setOffset(comNerTerm.getOffset()-eachComNerTerm.word.length());
					}
				}
				else {
					String typeStr = eachComNerTerm.typeStr;
					String word = eachComNerTerm.word;
					int offset1 = eachComNerTerm.offset;
					EXComNerTermList.add(new ComNerTerm(word, typeStr, offset1));
				}
				if (eachComNerTerm.typeStr.toString().equals("OB")) {
					EXOB += StringEscapeUtils.unescapeJava(eachComNerTerm.word);
				}
				currComNum++;
			}
			//判断无AC情况将段中要求 关注 落实加入成分中：
			//有段落AC 但是本局无AC情况
			if(paragraphParamer.getParagraghAC()!=null&&!tagAC&&comNerTermList.size()>0){
				int insertNum=paragraphParamer.getParagraghAC().length();
				ComNerTerm acAdd=new ComNerTerm();
				acAdd.setTypeStr("AC");
				acAdd.setOffset(sentenceTerm.getOffset());
				acAdd.setWord(paragraphParamer.getParagraghAC());

				comNerTermList.add(0,acAdd);
				for(int x=1;x<comNerTermList.size();x++){
					comNerTermList.get(x).setOffset(comNerTermList.get(x).getOffset()+insertNum);
				}
				sentenceTerm.setSentence(paragraphParamer.getParagraghAC()+sentenceTerm.getSentence());
			}
			if (tagCO) {
				sentenceTerm = new SentenceTerm(EXSentence, EXComNerTermList, offset);
				ruleOut.append(dealCondition(sentenceTerm)); // 处理条件句
			}
			else {
				if (tagEX) {
					sentenceTerm = new SentenceTerm(EXSentence, EXComNerTermList, offset);
					sentenceTerm.setSentence(EXSentence);
				}
				List<SentenceTerm> connect = connect(sentenceTerm);
                List<SentenceTerm> preRet = preDeal(connect);
				for (SentenceTerm eachSentenTerm : preRet) {//规则加载
					ruleOut.append(inferenceInside(eachSentenTerm));
				}
			}
			String strRuleOut = ruleOut.toString();
			if (tagEX && !EXOB.equals("") && strRuleOut.contains(EXOB)) {
				strRuleOut = strRuleOut.replace(EXOB, EXOB + "【" + EXWord + "】");
//				System.out.println("--------------->>"+strRuleOut);
			}
			boolean specTreatment = true;//需不需要特殊处理

			if (!ruleOut.toString().equals("")) {
				in.add(strRuleOut);
				specTreatment = false;
			}
			else{//正则模式判断
				for (SentenceTerm longSentenceTerm : longSentenceTerms) {
					String longSentence = longSentenceTerm.getSentence();
					if(longSentence.contains(sentence)){
						if(longSentenceTerm.getOffset()<=sentenceTerm.getOffset()){
							if(!longSentenceTerm.getRegex()){
								if(longSentence.length()>3){
									//进入正则模式
									String regRult = sentenRegParse(longSentence);
									//对正则的结果判断
									if(regRult!=null){
										in.add(regRult);
										specTreatment = false;
									}
								}
								longSentenceTerm.setRegex(true);
							}

						}

					}

				}
			}
			if (specTreatment) {

				//没有匹配到规则的处理
				//处理含有VE或VC不含有AC且没有匹配到规则的 直接加要求
				//处理不含AC只有OB 情况
				//处理只有AD OB 情况
				boolean hasVEFlag = false;
				boolean hasVCFlag = false;
				boolean hasACFlag = false;
				boolean hasOBFlag = false;
				List<ComNerTerm> comNerTermList1 = sentenceTerm.getComNerTermList();
				boolean hasADFlag = false;
				StringBuffer str = new StringBuffer();
				for (ComNerTerm comNerTerm : comNerTermList1) {
					if (comNerTerm.typeStr.equals("VE"))
						hasVEFlag = true;
					else if (comNerTerm.typeStr.equals("AC"))
						hasACFlag = true;
					else if (comNerTerm.typeStr.equals("VC"))
						hasVCFlag = true;
					else if (comNerTerm.typeStr.equals("OB"))
						hasOBFlag = true;
					else if (comNerTerm.typeStr.equals("AD"))
						hasADFlag = true;
					str.append(comNerTerm.word);
				}
				if ((hasVEFlag || hasVCFlag) && !hasACFlag && hasOBFlag) {

					in.add("要求 " + str.toString());
				} else {
					if (!hasACFlag && sentenceTerm.getSentence().contains("用途") && hasOBFlag && !str.toString().contains("用途")) {

						in.add("用于 " + str.toString());
					} else if (hasADFlag && hasOBFlag && comNerTermList1.size() == 2 && str.toString().contains("不得")) {
						in.add("不得 " + str.toString().replaceAll("不得", ""));
					} else if (paragraphParamer.isHasrisk()) {
						// 还未识别的入风险点句法树识别风险点
						String result = riskPaser(sentenceTerm.getSentence().replaceFirst("风险提示", ""));
						for (String longSentence : longSentenceList) {
							if (result != null && longSentence.contains(result)) {
								in.add(longSentence.replaceFirst("风险提示", "").replaceAll("^（{0,1}\\({0,1}[一二三四五六七八九十百壹贰叁肆伍陆柒捌玖拾0-9]{1,2}\\){0,1}）{0,1}[\\.、]{0,1}", "").replace("_x000D_", ""));
							}
						}
					}
				}
			}
		}
		return in;
	}

	/**
	 * 从SentenceTerm通过规则推导出最终结果
	 * @param sentenceTerm	句子对象
	 * @return	推导结果
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
		String ruleOut = ruleRecursion(type.toString().trim(), typeAndWord, sb);
		StringBuffer inside_out = new StringBuffer();
		if (!ruleOut.equals("")) {
			String[] lineSplit = ruleOut.split("\n");
			for (String eachLine : lineSplit) {
				String[] tabSplit = eachLine.split("\t");
				inside_out.append(tabSplit[0]+"\n");
			}
		}
		return inside_out.toString();
	}

	/**
	 * 将有并列成分的句子分开，分成多个短句
	 * @param sentenceTerm
	 * @return	短句结果list
	 */
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
			while (true) {
				outer:
				for (int q = 0;q < sentenceTermList.size();q++) {
					List<ComNerTerm> comNerTermList = sentenceTermList.get(q).getComNerTermList();
					for (int i = 0; i < comNerTermList.size() - 1; i++) {
						for (String doubleValue : doubleValues) {
							if (comNerTermList.get(i).typeStr.equals(doubleValue) && comNerTermList.get(i + 1).typeStr.equals(doubleValue)) {
								String dealSentens=sentenceTermList.get(q).getSentence().replace("\\","/");
								int l = comNerTermList.get(i + 1).offset - comNerTermList.get(i).offset;
								int subStart = comNerTermList.get(i).offset - sentenceTerm.getOffset();  //第一个NA的末尾位置
								String subSentence=null;
								try {
									subSentence = dealSentens.substring(subStart+comNerTermList.get(i).word.length(), subStart+l);

								}catch ( Exception e){
									e.printStackTrace();
								}
								String replaceStr = subSentence+comNerTermList.get(i+1).word;
								if ((subSentence.contains("、")||subSentence.contains("或")||subSentence.contains("及")||subSentence.contains("与"))) {
									//重组成新的２句话
									//左肺及右肺下见斑片影　　将“左肺及右肺下”分别替换为“左肺”和“右肺下”
									String senten1 = dealSentens.replaceFirst(replaceStr,"");
									List<ComNerTerm> comNerTerm1 = new ArrayList<>();
									for (int j = 0; j < comNerTermList.size(); j++) {
										if (j!=(i+1)) {
											int offset1 = comNerTermList.get(j).offset;
											int weiyi = sentenceTermList.get(q).getSentence().length()-senten1.length();
											if (comNerTermList.get(j).offset>=comNerTermList.get(i+1).offset) {
												offset1 -= weiyi;
											}
											comNerTerm1.add(new ComNerTerm(comNerTermList.get(j).word,comNerTermList.get(j).typeStr,offset1));
										}
									}
									SentenceTerm newSenterm1 = new SentenceTerm(senten1,comNerTerm1,sentenceTermList.get(q).getOffset());
									String senten2 = sentenceTermList.get(q).getSentence().replaceFirst(comNerTermList.get(i).word+subSentence,"");
									List<ComNerTerm> comNerTerm2 = new ArrayList<>();
									for (int j = 0; j < comNerTermList.size(); j++) {
										if (j != i) {
											int offset2 = comNerTermList.get(j).offset;
											int weiyi = sentenceTermList.get(q).getSentence().length()-senten2.length();
											if (comNerTermList.get(j).offset>=comNerTermList.get(i).offset) {
												offset2 -= weiyi;
											}
											comNerTerm2.add(new ComNerTerm(comNerTermList.get(j).word,comNerTermList.get(j).typeStr,offset2));
										}
									}
									SentenceTerm newSenterm2 = new SentenceTerm(senten2,comNerTerm2,sentenceTermList.get(q).getOffset());
									//移除原来的，加上分好的子句
									sentenceTermList.remove(q);
									sentenList.remove(q);
									if (!sentenList.contains(newSenterm1.getSentence())) {
										sentenceTermList.add(newSenterm1);
										sentenList.add(newSenterm1.getSentence());
									}
									if (!sentenList.contains(newSenterm2.getSentence())) {
										sentenceTermList.add(newSenterm2);
										sentenList.add(newSenterm2.getSentence());
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
				if (t == sentenceTermList.size())
					//如果都没有连续属性出现就退出
					break;
				count++;
				if (count>15) break;
				//防止陷入死循环
			}
		}
		return sentenceTermList;
	}

	/**
	 * 判断是否还存在需要连续的O或D或T
	 * @param comNerTermList
	 * @return true/false
	 */
	private static boolean hasDouble(List<ComNerTerm> comNerTermList)
	{
		StringBuffer sb = new StringBuffer();
		for (ComNerTerm comNerTerm : comNerTermList) {
			sb.append(comNerTerm.typeStr+" ");
		}
		if (sb.toString().contains("OB OB")||sb.toString().contains("NA NA")) {
			return true;
		}
		else {
			return false;
		}
	}
	//做风险点判断是否有风险点
	private static String riskPaser(String text)  {
		String[] sentences = text.split("[!?！；。？]");
		List<String> negtiveSentences = new ArrayList<String>();
		OUT:
		for (String sentence: sentences) {
			{
				OpinionMining.State state = null;
				try {
					state = mining.expansion(sentence);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				switch (state) {
					case negative:
						negtiveSentences.add(sentence);
					default:
						continue OUT;
				}
			}
		}
		if (negtiveSentences.isEmpty())
			return null;
		return StringUtils.join(negtiveSentences.toArray(new String[0]), "\n");
	}

	/**
	 * 规则递归推导
	 * @param rule
	 * @param lineStr
	 * @param sb
	 * @return
	 */
	public static String ruleRecursion(String rule,String lineStr,StringBuffer sb)
	{
		String ruleOut = ruleMap.get(rule);
		if (ruleOut == null) {

			if (rule.equals(""))
				return sb.toString();
			else {
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
					int count = 0;
					if (ss[i1].toCharArray()[0] > '9') {
						count = ss[i1].toCharArray()[0] - 55;
					}
					else {
						count = Integer.parseInt(ss[i1]);
					}
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


	/**
	 * 数据规范化
	 * @param outList
	 * @return 规范化后的list
	 */
	public static List<String> normalization(List<String> outList) {
		List<String> normalList = new ArrayList<>();
		for (String eachOut : outList) {
			String[] lineSplit = eachOut.split("\n");
			for (String eachLine : lineSplit) {
				if (eachLine.contains(" ")) {
					String[] commaSplit = eachLine.split(" ");
					if (commaSplit.length == 2) {
						if (commaSplit[1].length() < 2||commaSplit[0].length() < 2){
							continue; // 去除长度小于2的对象
						}
					}
				}
				if (!normalList.contains(eachLine)) {
					normalList.add(eachLine);
				}
			}
		}
		return normalList;
	}


	/**
	 * 数据去重
	 * @param outList
	 * @return 去重后的list
	 */
	public static List<String> removeDuplicates(List<String> outList) {
		List<String> normalList = new ArrayList<>();
		int[][] charSimilar=new int[outList.size()][outList.size()];
		for (int x=0;x<outList.size()-1;x++) {
			String eachLine=outList.get(x);
			for(int i=0;i<outList.size()-1;i++){
				if(!outList.get(i).equals(eachLine)){
					int diffLocation=Levenshtein.getCharLocation(eachLine,outList.get(i));
					if(diffLocation>Math.min(eachLine.length(),outList.get(i).length())-1){
						//不同的位置大于较小的长度，表示包含
						charSimilar[x][i]=1;
					}
					else {
						String diffstr1=eachLine.substring(diffLocation,eachLine.length());
						String diffstr2=outList.get(i).substring(diffLocation,outList.get(i).length());
						Levenshtein lt = new Levenshtein();
						if(lt.getSimilarityRatio(diffstr1, diffstr2)>0.8001){
							//剩余部分的相似度大于，判断为相同句子
							charSimilar[x][i]=1;
						}
					}
				}
			}
		}
		int [] needRemove=new int[outList.size()];
		for(int x=0;x<outList.size()-1;x++){
			for(int y=0;y<outList.size()-1;y++){
				if(charSimilar[x][y]==1){
					if(outList.get(x).length()>=outList.get(y).length()){
						charSimilar[y][x]=0;
						needRemove[y]=1;
					}
					else{
						charSimilar[x][y]=0;
						needRemove[x]=1;
					}

				}
			}
		}
		for(int i=0;i<needRemove.length;i++){
			if(needRemove[i]==0){
				normalList.add(outList.get(i));
			}
		}
		return normalList;
	}


	/**
	 * 正则模式匹配
	 * @param longSentence
	 * @return 去重后的list
	 */
	private static String sentenRegParse(String longSentence){
		String regResult =null;
		for (RegRuleEntity regRule : allRegRule) {

			String type = regRule.getType();
			int index = regRule.getIndex();
			Matcher m=regRule.getRegx().matcher(longSentence);
			if(m.find()){
				if("条件".equals(type)){
					if(index>20){
						regResult  ="条件:"+ m.group(2) + " 动作:" + m.group(1);
					}else{
						regResult= "条件:"+ m.group(1) + " 动作:"  + m.group(2);
					}
				}else {
							regResult = type+" " + m.group(index);
				}
				break;
			}
		}
		return regResult;
	}



}
