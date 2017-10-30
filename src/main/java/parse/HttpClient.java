package parse;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
/**
 * Created by hadoop on 17-4-26.
 */
public class HttpClient {
    private CloseableHttpClient client;
    private String url;


    public HttpClient(String url) {
        client = HttpClients.createDefault();
        this.url = url;
    }

}

