package conf;

/**
 * Created by hpre on 17-3-1.
 */
public class CmbConfig {
    public String hanlp;
    public String ruleFile;
    public String cmbCom;
    public String cmbSenten;
    public String modelPath;

    public String getRegexFile() {
        return regexFile;
    }

    public void setRegexFile(String regexFile) {
        this.regexFile = regexFile;
    }

    public String regexFile;
    public String getModel() {
        return modelPath;
    }

    public void setModel(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getHanlp() {
        return hanlp;
    }

    public void setHanlp(String hanlp) {
        this.hanlp = hanlp;
    }

    public String getRuleFile() {
        return ruleFile;
    }

    public void setRuleFile(String ruleFile) {
        this.ruleFile = ruleFile;
    }

    public String getCmbCom() {
        return cmbCom;
    }

    public void setCmbCom(String cmbCom) {
        this.cmbCom = cmbCom;
    }

    public String getCmbSenten() {
        return cmbSenten;
    }

    public void setCmbSenten(String cmbSenten) {
        this.cmbSenten = cmbSenten;
    }

}
