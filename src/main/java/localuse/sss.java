package localuse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hpre on 17-3-22.
 */
public class sss {

    public static void main(String[] args) {
        String s = "青岛盛文综合开发集团有限公司、青岛农村商业银行股份有限公司城阳丹山支行、青岛国际工艺品城生产基地有限公司、青岛林之枫桦专业合作社、王裕锡：本院受理上诉人青岛盛文综合开发集团有限公司与被上诉人青岛农村商业银行股份有限公司城阳丹山支行及原审被告青岛国际工艺品城生产基地有限公司、青岛林之枫桦专业合作社、王裕锡金融借款合同纠纷一案，现依法向你方公告送达(2016)鲁民终940号的开庭传票、举证通知书、（受理）应诉通知书。自公告之日起60日内即视为送达，定于期满后第3日下午13:30分（遇节假日顺延），在本院公开审理，逾期不到庭，将依法处理。";
        String ss = "(2016)鲁民终940号";
        String ret = get_case_num(ss);
        System.out.println(ret);
    }

    public static String get_case_num(String jsonObject)
    {

        Pattern pattern = Pattern.compile("([(|（]{1}[\\d]{4}[)|）]{1})[\u4e00-\u9fa5]{1}[[\u4e00-\u9fa5]|\\d|（|(|）|)]{1,12}[字第|财保|行审|辖终|初|初字|保|[执|刑][\u4e00-\u9fa5]{0,1}|终|民[\u4e00-\u9fa5]{1}]{1}[\\d|－|-|、|-]+号+?");
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
