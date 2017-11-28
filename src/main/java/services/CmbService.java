package services;

import conf.CmbConfig;
import org.json.JSONException;
import org.json.JSONObject;
import parse.CmbParse;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hpre on 17-3-1.
 */

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class CmbService {

        CmbConfig cmbConfig;
        public CmbService(CmbConfig cmbConfig)
        {
            this.cmbConfig = cmbConfig;
        }


        @POST
        @Path("/api/v1/cmb/java")
        public ResponseResult parse0(String  repStr)
        {


            JSONObject repObj = null;
            try {
                repObj = new JSONObject(repStr);
            }catch (JSONException je){
                je.printStackTrace();
            }
            ResponseResult response = null ;

            if(repObj ==null){
                response = new ResponseResult(0x1400,"error",new AppResult(new ArrayList<>()));

            }else{
                CmbParse cmbCmbParse;
                List<String> outList =null;
                try {
                    cmbCmbParse = new CmbParse(cmbConfig);
                    String text = repObj.getString("text");
                    outList = cmbCmbParse.parse(text);
                    response = new ResponseResult(0x1200,"success",new AppResult(outList));

                }catch (JSONException je){
                    je.printStackTrace();
                    response = new ResponseResult(0x1400,"error",new AppResult(new ArrayList<>()));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    response = new ResponseResult(0x2000,"error",new AppResult(new ArrayList<>()));

                }
                if(response ==null){
                    response = new ResponseResult(0x1100,"error",new AppResult(new ArrayList<>()));

                }
            }
            return response;
        }
}
