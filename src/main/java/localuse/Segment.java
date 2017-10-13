package localuse;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.hankcs.hanlp.utility.Predefine;

public class Segment
{


	private static String input = "/home/hpre/222/";
	private static String out = "/home/hpre/222-out/";

//	public static String predicates[] = {"严控","了解","争取","做好","关注","分析","办妥","办理","加强","审查","审核","建立",
//			"扩大","承诺","控制","提供","收集","明确","查询","核实","核查","检查","沟通","注意","监控","考虑","落实","要求",
//			"监测","监管","确保","纳入","统计","观察","超过","跟踪","跟进","防止","预防"
//										};
	public static void main(String[] args) throws IOException
	{
		normalSeg();
	}

	/*
	分词
	predicate PR 谓语
	attribute AT 定语
	object	  OB 宾语
	 */
	public static void normalSeg() throws IOException
	{
		List<String> predicateList = new ArrayList<>();
//		for (String predicate : predicates) {
//			if (!predicateList.contains(predicate))
//			{
//				predicateList.add(predicate);
//			}
//		}

		Predefine.HANLP_PROPERTIES_PATH = "/home/hpre/program/cmb/model/hanlp.properties";

		File dirPath = new File(input);
		File[] files = dirPath.listFiles();
		for (File file : files)
		{
			FileWriter fileWriter = new FileWriter(new File(out+file.getAbsolutePath().substring(input.length())));
			Scanner scanner = new Scanner(file);
			System.out.println(file);
			while (scanner.hasNext())
			{
				String temStr = scanner.nextLine();
				temStr=temStr.replaceAll(" ", "");
//				StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);
//				System.out.println(temStr);
				List<Term> segStr = StandardTokenizer.segment(temStr);
				String result = "";
				for (Term sTerm:segStr)
				{
//					if (predicateList.contains(sTerm.word))
//						result = result+"#PR#"+sTerm.word+"_"+sTerm.nature+"#PR#"+" ";
//					else
						if (sTerm.nature.equals("nis"))
						{
							System.out.println("公司名--->"+sTerm);
						}
						result = result+sTerm.word+"_"+sTerm.nature+" "; //
				}
				System.out.print(result);
				System.out.println();
//				System.out.println(result);
				fileWriter.write(result+"\n"); //"#SENT_BEG#/begin "++" #SENT_END#/end"
			}
			scanner.close();
			fileWriter.close();
		}
	}
}
