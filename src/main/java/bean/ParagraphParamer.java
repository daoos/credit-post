package bean;

import tools.CommonlyTools;

/**
 * Created by hadoop on 17-6-7.
 */
public class ParagraphParamer {
    public static String acWord[] =new String[]{"关注","落实"};//"要求",
    public ParagraphParamer(String text){
        if(text.contains("风险提示")) {
            //用于对风险点识别的判断入口讲后文出现的有风险提示的句子且后去判断为识别的地方做风险点判断
            setHasrisk(true);
        }
        else {
            setHasrisk(false);
        }
        setParagraghAC(null);
    }
    public boolean isHasrisk() {
        return hasrisk;
    }

    public void setHasrisk(boolean hasrisk) {
        this.hasrisk = hasrisk;
    }

    public String getParagraghAC() {
        return paragraghAC;
    }

    public void setParagraghAC(String paragraghAC) {
        this.paragraghAC = paragraghAC;
    }

    boolean hasrisk;
    String paragraghAC;

    /**
     * 正则判断句子是否出现  风险管理要求：  关注：  落实： 类型的问题
     * @param text
     */
    public void hasACDemon(String text){
        for(String word:acWord){
            String regExStr = "((?![，。]).)*["+word+"]{2,}((?![，。]).)*：";
            if(CommonlyTools.getTeacherList(text,regExStr) && !text.contains("提款要求") && !text.contains("贷前要求")){
                setParagraghAC(word);
            }
        }
    }

    public void isSerialSentense(String  text){
        String regExStr = ".{0,6}（{0,1}\\({0,1}[一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾0-9]{1,2}[）\\)\\.、,，]+\\D{0,3}.*";
        if(!CommonlyTools.regEx(text,regExStr)){
            setParagraghAC(null);
        }
    }
}
