package bean;

import java.util.regex.Pattern;

/**
 * Created by hadoop on 17-11-7.
 */
public class RegRuleEntity {

    private String type;
    private int index;
    private Pattern regx;

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
        this.regx = Pattern.compile(regx);
    }


}