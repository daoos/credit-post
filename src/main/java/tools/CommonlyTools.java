package tools;

import bean.RegRuleEntity;
import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hadoop on 17-6-7.
 */
public class CommonlyTools {

    public static void printFile(String str, String outPath, Boolean append) {
        try {
            FileOutputStream e = new FileOutputStream(outPath, append.booleanValue());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(e);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(str);
            bufferedWriter.close();
            outputStreamWriter.close();
            e.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }
    public static boolean regEx(String sentense,String regExStr){
        // 要验证的字符串
        String str = sentense;
        // 邮箱验证规则
        String regEx =regExStr;
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regEx);
        // 忽略大小写的写法
        // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        // 字符串是否与正则表达式相匹配
        boolean rs = matcher.matches();
        //                   System.out.println(rs);
        if(rs){
            return true;
        }
        return false;
    }

    public static boolean getTeacherList(String managers, String regEx){
        List<String> ls=new ArrayList<String>();
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(managers);
        while(matcher.find())
            ls.add(matcher.group());
        if(ls.size()>0){
            ls.clear();
            ls=null;
            return true;
        }
        else
        {
            ls.clear();
            ls=null;
            return false;
        }

    }


    public static Vector<String> get_all_match(String content, String regex) {
        Vector all = new Vector();
        String matched = "";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);

        while(m.find()) {
            matched = m.group();
            all.add(matched);
        }
        return all;
    }
    public static String match(String str,Pattern pattern){
        String result = "";
        Matcher m=pattern.matcher(str);
        if(m.find()){
            result = m.group();
        }
        return result;
    }
    public static List<RegRuleEntity> getAllRegRule(String filePath) throws Exception {
        List<RegRuleEntity> list = new ArrayList<RegRuleEntity>();
        Scanner sc = new Scanner(new File(filePath));
        while(sc.hasNext()){
            String line = sc.nextLine();
            if(line.isEmpty() || line.startsWith("~")){
                continue;
            }

            String[] splits = line.split("#");

            if(line.startsWith("条件")){
                RegRuleEntity entity = new RegRuleEntity();
                entity.setType("条件");
                entity.setIndex(splits[splits.length-1]);
                entity.setRegx(splits[2]);
                list.add(entity);
            }else if(splits.length==3){
                RegRuleEntity entity = new RegRuleEntity();
                entity.setType(splits[0]);
                entity.setIndex("0");
                entity.setRegx(splits[2]);

                list.add(entity);
            }else if(splits.length==4){
                RegRuleEntity entity = new RegRuleEntity();
                entity.setType(splits[0]);
                entity.setIndex(splits[splits.length-1]);
                entity.setRegx(splits[2]);
                list.add(entity);

            }else{
                System.out.println(line);
            }
        }
        sc.close();
        return list;
    }
    /**
     * 根据List获取到对应的JSONArray
     * @param list
     * @return
     */
    public static JSONArray getJSONArrayByList(List<?> list){
        JSONArray jsonArray = new JSONArray();
        if (list==null ||list.isEmpty()) {
            return jsonArray;//nerver return null
        }

        for (Object object : list) {
            jsonArray.put(object);
        }
        return jsonArray;
    }
}
