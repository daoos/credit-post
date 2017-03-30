package conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

/**
 * Created by hpre on 16-12-16.
 */
public class CmbConfiguration extends Configuration{
    @JsonProperty()
    private CmbConfig cmb;
    @JsonProperty()

    private String cmbCom;

    @JsonProperty()
    private String cmbSenten;
    @JsonProperty()
    private String ruleFile;
    @JsonProperty()
    private String hanlp;


    public CmbConfig getCmb() {
        return cmb;
    }

    public void setCmb(CmbConfig cmb) {
        this.cmb = cmb;
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

    public String getRuleFile() {
        return ruleFile;
    }

    public void setRuleFile(String ruleFile) {
        this.ruleFile = ruleFile;
    }

    public String getHanlp() {
        return hanlp;
    }

    public void setHanlp(String hanlp) {
        this.hanlp = hanlp;
    }
}
