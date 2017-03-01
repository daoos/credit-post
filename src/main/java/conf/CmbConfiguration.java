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
    private String modelfile;
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

    public String getModelfile() {
        return modelfile;
    }

    public void setModelfile(String modelfile) {
        this.modelfile = modelfile;
    }

    public String getRulefile() {
        return rulefile;
    }

    public void setRulefile(String rulefile) {
        this.rulefile = rulefile;
    }
}
