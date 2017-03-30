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
    private String modelfile0;
    @JsonProperty()
    private String modelfile1;
    @JsonProperty()
    private String modelfile2;
    @JsonProperty()
    private String rulefile;
    @JsonProperty()
    private String hanlp;


    public CmbConfig getCmb() {
        return cmb;
    }

    public void setCmb(CmbConfig cmb) {
        this.cmb = cmb;
    }

    public String getHanlp() {
        return hanlp;
    }

    public void setHanlp(String hanlp) {
        this.hanlp = hanlp;
    }

    public String getModelfile0() {
        return modelfile0;
    }

    public void setModelfile0(String modelfile0) {
        this.modelfile0 = modelfile0;
    }

    public String getModelfile1() {
        return modelfile1;
    }

    public void setModelfile1(String modelfile1) {
        this.modelfile1 = modelfile1;
    }

    public String getModelfile2() {
        return modelfile2;
    }

    public void setModelfile2(String modelfile2) {
        this.modelfile2 = modelfile2;
    }

    public String getRulefile() {
        return rulefile;
    }

    public void setRulefile(String rulefile) {
        this.rulefile = rulefile;
    }
}
