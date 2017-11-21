package bean;

import java.util.regex.Pattern;

/**
 * Created by hadoop on 17-11-7.
 */
public class RegRuleEntity {
    public String getRegStr() {
        return regStr;
    }

    private String regStr;
    private String type;
    private int index;
    private Pattern regx;
    private boolean isShort = false;
    public boolean isShort() {
        return isShort;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = Integer.parseInt(index);
    }

    public Pattern getRegx() {
        return regx;
    }

    public void setRegx(String regx) {
        regStr = regx;
        this.regx = Pattern.compile(regx);
        if("条件".equals(type) ){
            isShort = false;
        }else{
            isShort = true;
        }

    }


}