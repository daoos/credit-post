package services;

import conf.CmbConfig;
import conf.CmbConfiguration;
import localuse.Entity;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by hpre on 17-3-1.
 */

@Path("/api/cmb")
@Produces(MediaType.APPLICATION_JSON)
public class CmbService {

    private Entity cmbEntity;
        public CmbService(CmbConfig config)
        {
            cmbEntity = new Entity(config);
        }

        @POST
        public List<String> parse(String data)
        {
            List<String> outList = cmbEntity.parse(data);
            return outList;
        }
}
