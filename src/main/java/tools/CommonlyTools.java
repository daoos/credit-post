package tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hadoop on 17-6-7.
 */
public class CommonlyTools {


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
}
