package services;

import conf.CmbConfig;
import conf.CmbConfiguration;
import localuse.Entity;

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

    private Entity cmbEntity;
        public CmbService(CmbConfig config)
        {
            cmbEntity = new Entity(config);
        }

        @POST
        @Path("/api/cmb0")
        public List<String> parse0(String data)
        {
            List<String> outList = cmbEntity.parse(data,"0");
            return outList;
        }

        @POST
        @Path("/api/cmb1")
        public List<String> parse1(String data)
        {
            List<String> outList = cmbEntity.parse(data,"1");
            return outList;
        }

        @POST
        @Path("/api/cmb2")
        public List<String> parse2(String data)
        {
            List<String> outList = cmbEntity.parse(data,"2");
            return outList;
        }
}
