package bean;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
/**
 * Created by hadoop on 17-4-26.
 */
public class HttpClient {
    private CloseableHttpClient client;
    private String url;
    private String status = "";


    public String getStatus() {
        return status;
    }

    public HttpClient(String url) {
        client = HttpClients.createDefault();
        this.url = url;
    }

    public void close() throws IOException {
        client.close();
    }

    public String query(byte[] content)
            throws ClientProtocolException, IOException {
        StringBuffer sb = new StringBuffer();

        HttpPost httpPost = new HttpPost(url);
        HttpEntity entity = new ByteArrayEntity(content);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);

        try {
//            System.out.println(response.getStatusLine());
            HttpEntity entity2 = response.getEntity();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entity2.getContent()));
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append("\n" + line);
            }
            EntityUtils.consume(entity2);
        } finally {
            response.close();
        }

        if (sb.length() > 1)
            sb.delete(0, 1);
        return sb.toString();
    }

    public String query(String content)
            throws ClientProtocolException, IOException {
        return query(content.getBytes());
    }

    public String query(byte[] content, Properties header)
            throws ClientProtocolException, IOException {
        StringBuffer sb = new StringBuffer();

        HttpPost httpPost = new HttpPost(url);
        HttpEntity entity = new ByteArrayEntity(content);
        httpPost.setEntity(entity);

        for (Map.Entry<Object, Object> entry: header.entrySet())
            httpPost.setHeader(entry.getKey().toString(), entry.getValue().toString());

        CloseableHttpResponse response = client.execute(httpPost);
        try {
            status = response.getStatusLine().toString();
            System.out.println(response.getStatusLine());
            HttpEntity entity2 = response.getEntity();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entity2.getContent()));
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append("\n" + line);
            }
            EntityUtils.consume(entity2);
        } finally {
            response.close();
        }

        if (sb.length() > 1)
            sb.delete(0, 1);
        return sb.toString();
    }

    public String query(String content, Properties header)
            throws ClientProtocolException, IOException {
        return query(content.getBytes(), header);
    }

    public String query(Properties param, Properties header) throws ClientProtocolException, IOException {
        StringBuffer sb = new StringBuffer();
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();;
        HttpPost httpPost = new HttpPost(url);
//        HttpEntity entity = new ByteArrayEntity(content.getBytes());

        for (Map.Entry<Object, Object> entry: header.entrySet())
            httpPost.setHeader(entry.getKey().toString(), entry.getValue().toString());

        for (Map.Entry<Object, Object> entry: param.entrySet())
            postParameters.add(new BasicNameValuePair(entry.getKey().toString(),
                    URLEncoder.encode(entry.getValue().toString(), "UTF-8")));

        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));

        CloseableHttpResponse response = client.execute(httpPost);
        try {
            System.out.println(url + " " + response.getStatusLine());
            HttpEntity entity2 = response.getEntity();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entity2.getContent()));
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append("\n" + line);
            }
            EntityUtils.consume(entity2);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            response.close();
        }

        if (sb.length() > 1)
            sb.delete(0, 1);
        return sb.toString();
    }
}

