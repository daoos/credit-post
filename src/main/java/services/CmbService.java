package services;

import conf.CmbConfig;
import parse.CmbParse;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by hpre on 17-3-1.
 */

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class CmbService {

    private CmbParse cmbCmbParse;
        public CmbService(CmbConfig config)
        {
            cmbCmbParse = new CmbParse(config);
        }

        @POST

        @Path("/api/cmb")
        public List<String> parse0(String data)
        {
            List<String> outList = cmbCmbParse.parse(data);
            return outList;
        }
}
