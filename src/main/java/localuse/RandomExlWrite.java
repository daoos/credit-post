package localuse;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * usage:从159个分行中随机抽取出一篇文本，并记录是哪一篇，并访问授信文本结构化接口，将结果也存入xls
 * Created by hpre on 17-10-10.
 */
public class RandomExlWrite {

    public static void main(String[] args) throws IOException, BiffException, WriteException {
        run();
    }

    /**
     * 将混乱的动作、对象和分类整理好
     * @param object    对象
     * @param action    动作
     * @return  整理好的动作+对象+分类
     */
    private static String fetchStr(String object, String action) {
        int i1 = object.indexOf("<span");
        int i2 = object.indexOf("</span");
        int i3 = object.indexOf("@>");
        System.out.println(object);
        System.out.println(i3 + " " + i2);
        if (object.equals("")) {
            System.out.println(object);
        }
        String objClass = object.substring(i3, i2);
        object = object.substring(0, i1 - 1);
        if (!object.equals("")) {
            int i4 = objClass.indexOf("(");
            int i5 = objClass.indexOf(")");
            objClass = objClass.substring(i4, i5 + 1);
            System.out.println(objClass);
        } else {
            objClass = "(->)";
            System.out.println(objClass);
        }
        String resultStr = action + object + "  " + objClass;
        return resultStr;
    }

    /**
     * 从一个表格中读原文，将原文访问授信文本结构化接口，将结果解析好存入表格中
     * @throws IOException
     * @throws BiffException
     * @throws WriteException
     */
    private static void run() throws IOException, BiffException, WriteException {
        String excelDir = "/home/hpre/program/cmb/note/fenhang/random (复件).xls";
        InputStream is = null;
        is = new FileInputStream(excelDir);
        Workbook workbookIs = Workbook.getWorkbook(is);
        Sheet sheetIs = workbookIs.getSheet(0);

        OutputStream os = null;
        File excel = new File("/home/hpre/program/cmb/note/fenhang/"+"random.xls");
        os = new FileOutputStream(excel);
        WritableWorkbook workbook = Workbook.createWorkbook(os);
        WritableSheet wrsheet = workbook.createSheet("First Sheet", 0);

        int j = 0;
        int rowsIs = sheetIs.getRows();
        for (int k = 0; k < rowsIs; k++) {
            String num = sheetIs.getCell(0, k).getContents();
            String contents = sheetIs.getCell(1, k).getContents();
            System.out.println(contents);
            String url = "http://0.0.0.0:5004/cmb";
            String result = query(contents, url);
            JSONObject jsonObject = new JSONObject(result);

            String resultStr = "风险点:";

            JSONArray riskJsonArr = jsonObject.getJSONArray("风险点");
            for (int i = 0; i < riskJsonArr.length(); i++) {
                JSONObject riskJson = riskJsonArr.getJSONObject(i);
                String action = riskJson.getString("action");
                String object = riskJson.getString("object");
                String fetchStr = "";
                object = object.replace("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", "");
                object = object.replace("<br/>动作", "动作");
                if (object.contains("<br/>")) {
                    String[] split = object.split("<br/>");
                    for (String eachObj : split) {
                        if (eachObj.equals("")) {
                            System.out.println(eachObj);
                        }
                        String eachFetchStr = fetchStr(eachObj, action);
                        if (fetchStr.equals("")) {
                            fetchStr = eachFetchStr;
                        } else {
                            fetchStr = fetchStr + "\n" + eachFetchStr;
                        }
                    }
                } else {
                    if (object.equals("")) {
                        fetchStr = object + " ";
                        System.out.println(fetchStr);
                    } else {
                        fetchStr = fetchStr(object, action);
                        System.out.println(fetchStr);
                    }
                }
                resultStr = resultStr + "\n" + fetchStr;
            }

            resultStr = resultStr + "\n\n" + "贷款要求:";

            JSONArray loanJsonArr = jsonObject.getJSONArray("贷款要求");
            for (int i = 0; i < loanJsonArr.length(); i++) {
                JSONObject loanJson = loanJsonArr.getJSONObject(i);
                String action = loanJson.getString("action");
                String object = loanJson.getString("object");
                System.out.println("object:" + object);
                String fetchStr = "";
                object = object.replace("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", "");
                object = object.replace("<br/>动作", "动作");
                if (object.contains("<br/>")) {
                    String[] split = object.split("<br/>");
                    for (String eachObj : split) {
                        if (eachObj.equals("")) {
                            System.out.println(eachObj);
                        }
                        String eachFetchStr = fetchStr(eachObj, action);
                        System.out.println(eachFetchStr);

                        if (fetchStr.equals("")) {
                            fetchStr = eachFetchStr;
                        } else {
                            fetchStr = fetchStr + "\n" + eachFetchStr;
                        }
                    }
                } else {
                    fetchStr = fetchStr(object, action);
                    System.out.println(fetchStr);
                }

                resultStr = resultStr + "\n"  + fetchStr;
            }

            resultStr = resultStr + "\n\n" + "关联关系:";

            JSONArray relationJsonArr = jsonObject.getJSONArray("关联关系");
            for (int i = 0; i < relationJsonArr.length(); i++) {
                JSONObject relationJson = relationJsonArr.getJSONObject(i);
                String relation = relationJson.getString("relation");
                String name = relationJson.getString("Name");
                resultStr = resultStr + "\n" + name + " (" + relation + ")";
            }
            System.out.println(resultStr);
            wrsheet.addCell(new Label(0, j, num));
            wrsheet.addCell(new Label(1, j, contents));
            wrsheet.addCell(new Label(2, j++, resultStr));
        }
        workbook.write();
        workbook.close();
        os.close();
    }


    /**
     * 向url发送post请求
     * @param content   请求主体
     * @param url   请求地址
     * @return  响应结果
     * @throws JSONException
     * @throws IOException
     */
    public static String query(String content, String url) throws JSONException, IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        StringBuffer sb = new StringBuffer();
        try {
            HttpPost httpPost = new HttpPost(url);
            HttpEntity entity = new ByteArrayEntity(content.getBytes());
            httpPost.setEntity(entity);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                HttpEntity entity2 = response.getEntity();
                BufferedReader reader = new BufferedReader(new InputStreamReader(entity2.getContent()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line+"\n");
                }
                EntityUtils.consume(entity2);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        return sb.toString();
    }

}
