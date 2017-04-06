package localuse;



import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static parse.CmbParse.cleanNoise;
import static parse.CmbParse.normalization;
/**
 * Created by hadoop on 17-4-5.
 */
public class FindSentenError {
    /*
	本地测试多份文本
	 */
    public static void main(String[] args) {
        FindSentenError findSentenError=new FindSentenError();
        File dirInput = new File(args[0]);
        File[] files = dirInput.listFiles();
        for (File file: files) {
            System.out.println(file);
            if(file.toString().endsWith("/180"))
                System.out.println();
            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String strLine = scanner.nextLine();
                    List<String> inference = findSentenError.parse(strLine);
                    for (String eachResult : inference) {
                        	System.out.println(eachResult);
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


    public List<String> parse(String text) {


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

            // 处理每一句
            if(eachLine.contains("%")){
                outList.add(eachLine);
            }
        }

        List<String> normalization = normalization(outList);
        // 数据规范化
        return normalization;
    }
}
