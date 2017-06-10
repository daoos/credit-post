package localuse.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hadoop on 17-5-13.
 */
public class Regextest {
    public static void main(String[] args) {
//        String s ="2014年，如果不将8290万元的财务费用资本化，则汇森煤业已是亏损运营；\n"+
//                "(一) 规范意见要素：\n" +
//                "1、发行人-中国文本集团有限公司\n" +
//                "2、业务品种-超短期融资券\n" +
//                "3、承销金额-不超过人民币10亿元\n" +
//                "4、期限- 不超过270天\n" +
//                "5、本次为新增\n" +
//                "6、发行方式-公开，可分期发行\n" +
//                "7、承销方式-余额包销\n" +
//                "8、我行角色-联席主承销商\n" +
//                "9、持券金额：不持券\n" +
//                "10、信用增进措施-无 \n" +
//                "（二）风险提示要点\n" +
//                "1、申请人刚性债务畸高，偿债依赖再发债、再融资，融资链如出现问题，易导致债务危机。\n" +
//                "2、目前，房地产市场走势不明朗，园区工业经济实体亦不景气，严重影响申请人预期的主要还款能力。\n" +
//                "（三）管理要求\n" +
//                "1、应要求申请人充分及时披露各项债务还款安排计划、土地整理、厂房开发以及其他平台单位往来占款状况等信息，其他信息按交易商协会要求进行信息充分披露；\n" +
//                "2、加强对募集资金用途管理；\n" +
//                "3、落实后续再发债计划，注意到期与新发时间的对接；\n" +
//                "4、关注同业授信续作动向和存量贷款分类变化情况；\n" +
//                "5、关注开发区土地市场变化及申请人土地处理、厂房开发进程；\n" +
//                "6、关注其他平台单位挤占挪用情况；\n" +
//                "7、在债务融资工具存续期内，通过各种有效方法对发行人进行跟踪、监测、调查，及时准确地掌握其风险状况及偿债能力，持续督导其履行信息披露、还本付息等义务，并按规定出具后续管理报告。\n" +
//                "（四）其他事项\n" +
//                "本次申报资料数据陈旧，质量较差。要求分行投行部严格把关。";
//        String[] sentences = s.split("[!?！；。，？\n]");
//        List<String> negtiveSentences = new ArrayList<String>();
//        for (String sentence: sentences) {
//            sentence=sentence.replaceAll("^（{0,1}\\({0,1}[一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾0-9]{1,2}\\){0,1}）{0,1}[\\.、]{0,1}","");
//            System.out.println(sentence);
//        }
        File dirInput = new File(args[0]);
        File[] files = dirInput.listFiles();
        int i=0;
        for (File file: files) {
 //           System.out.println(file);

            if(file.toString().endsWith("/73"))
                System.out.println();

//			FileWriter fileWriter = new FileWriter(file);

            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    // 要验证的字符串
                    String str = scanner.nextLine();
                    // 邮箱验证规则
                    String regEx = "((?![，。]).)*[要求]{2,}((?![，。]).)*：";//".{0,6}（{0,1}\\({0,1}[一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾0-9]{1,2}[）\\)\\.、,，]+\\D{0,3}.*";
                    // 编译正则表达式
                    Pattern pattern = Pattern.compile(regEx);
                    // 忽略大小写的写法
                    // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(str);
                    // 字符串是否与正则表达式相匹配
                    boolean rs = matcher.matches();
 //                   System.out.println(rs);

                    if(rs){
                        i++;
                        System.out.println(str);
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
}
