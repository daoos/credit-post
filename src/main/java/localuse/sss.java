package localuse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hpre on 17-3-22.
 */
public class sss {

    public static void main(String[] args) {
        String s = "//    宋广成：本院受理徐旭岚诉你离婚纠纷一案，已审理终结。现依法向你公告送达（2016）浙0523民初6366号民事判决书。自公告之日起， 60日内来本院领取民事判决书，逾期则视为送达。如不服本判决，可在公告期满后15日内，向本院递交上诉状及副本，上诉于湖州市中级人民法院。逾期本判决即发生法律效力。";
        String ret = get_case_num(s);
        System.out.println(ret);
    }

    public static String get_case_num(String jsonObject)
    {

        Pattern pattern = Pattern.compile("([(|（]{1}[\\d]{4}[)|）]{1})[\u4e00-\u9fa5]{1}[[\u4e00-\u9fa5]|\\d|（|(|）|)]{2,12}[字第|财保|行审|辖终|初|初字|保|[执|刑][\u4e00-\u9fa5]{0,1}|终|民[\u4e00-\u9fa5]{1}]{1}[\\d|－|-|、|-]+号+?");
        String result = null;
        String text = null;//
        String[] invalids = {"鉴","征补","贷","检","建验","调","执他字","国土","证经","证字","证民字","证民内字","内民证字","交认字","市政","咨字","个人借","减建","土地使用"};
        //todo 1.无效案号关键字数组
        //2.不以(年份)开头
        //3.除开年份和号，其它均为数字
        try {
            text = jsonObject;
            text = text.replaceAll("\n", "");
            Matcher matcher = pattern.matcher(text);
            tohere:while (matcher.find())
            {
                String group = matcher.group();
                if (!group.startsWith("(")&&!group.startsWith("（"))
                { //不以年份开头的
                    continue tohere;
                }
                if (Pattern.compile("[(|（)]{1}[\\d]{4}[)|）]{1}[字第|第]{1}\\d{1,}号{1}").matcher(group).find())
                { //房产证号
                    continue tohere;
                }
                for (String invalid : invalids)
                { //非案号类型
                    if (group.contains(invalid))
                    {
                        continue tohere;
                    }
                }

//                if (group.length() < 8)
//                    continue tohere;
//                String whetherNumber = group.substring(7,group.length()-1);
//                if (!Pattern.compile("[\\u4e00-\\u9fa5]").matcher(whetherNumber).find())
//                    continue tohere;
//                if (Pattern.compile("[(|（]{1}\\d{4}[(|（]{1}字第|第\\d{1,}号").matcher(group).find())
//                    continue tohere;

                if (result == null)
                {
                    result = group;
                }
                else if (result!=null && !result.contains(group))
                {
                    result = result + "," + group;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
