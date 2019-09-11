package testSite;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Created by bocse on 22.11.2015.
 */
public class FLRequester {
    private final static Logger logger = Logger.getLogger(FLRequester.class.toString());
    private final static Long maxAttempts = 10L;
    private final static Long initialDelay = 1300L;
    private final static Double backoffExponent = 1.9;
    private final static String[] userAgents = {
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537."
    };

    public static Document getDocument(HttpUriRequest httpRequest) throws IOException, InterruptedException {
        return getDocument(httpRequest, true);
    }

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        PrintWriter writer = new PrintWriter("C:\\Temp\\fl.csv", "UTF-8");

        for (int page = 1; page <= 221; page++) {
            String newUrl = "https://fetlife.com/administrative_areas/2815/kinksters?page=" + page;
            final HttpGet httpGet = new HttpGet(newUrl);
            final RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(9000).setConnectTimeout(9000).setSocketTimeout(9000).build();
            httpGet.setConfig(requestConfig);
            Document doc = getDocument(httpGet);
            logger.info(doc.html());
            Elements elements = doc.select("span.quiet");
            for (Element element : elements) {
                String payload = element.text().trim();
                if (!payload.isEmpty()) {
//                    if (payload.toLowerCase().contains("f sub"))
//                    {
//                        logger.warning("Found a sub: "+payload);
//                    }
                    payload = payload.substring(0, 2) + " " + payload.substring(2, payload.length());
                    String[] parts = payload.split(" ");
                    if (parts.length == 1)
                        writer.println(page + "," + parts[0] + ",,");
                    else if (parts.length == 2) {
                        if (parts[1].toLowerCase().equals("m") || parts[1].toLowerCase().equals("f")) {
                            writer.println(page + "," + parts[0] + "," + parts[1] + ",");
                        } else {
                            writer.println(page + "," + parts[0] + ",," + parts[1]);
                        }
                    } else {
                        writer.println(page + "," + parts[0] + "," + parts[1] + "," + parts[2]);
                    }
                }
            }
        }


        writer.close();
    }

    public static Document getDocument(HttpUriRequest httpRequest, final Boolean throwOnEmptyDocument) throws IOException, InterruptedException {
        Document doc = null;
        Long delay = initialDelay;
        Long attemptIndex = 0L;
        Boolean isSuccess = false;

        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
        httpRequest.addHeader("User-Agent", userAgents[(int) (System.nanoTime() % userAgents.length)] + (System.nanoTime() % 99 + 1));
        httpRequest.addHeader("Referer", "https://www.google.com/");
        httpRequest.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        //httpRequest.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        httpRequest.addHeader("Accept-Language", "en-US,en;q=0.8");
        CookieStore cookieStore = new BasicCookieStore();

        // Create local HTTP context
        final HttpClientContext localContext = HttpClientContext.create();
        // Bind custom cookie store to the local context
        localContext.setCookieStore(cookieStore);

        BasicClientCookie cookie1 = new BasicClientCookie("FL", "00051d2c-15b4-3ced-3a03-17e2b7b289ec");
        cookie1.setDomain("fetlife.com");
        cookie1.setPath("/");
        cookieStore.addCookie(cookie1);

        BasicClientCookie cookie2 = new BasicClientCookie("_Fetlife_continuous_perving", "on");
        cookie2.setDomain("fetlife.com");
        cookie2.setPath("/");
        cookieStore.addCookie(cookie2);

        BasicClientCookie cookie3 = new BasicClientCookie("_fl_sessionid", "403b2766dac481400ab27c8955db0edc");
        cookie3.setDomain("fetlife.com");
        cookie3.setPath("/");
        cookieStore.addCookie(cookie3);

        while (!isSuccess & attemptIndex < maxAttempts) {
            try {
                CloseableHttpClient httpclient =
                        HttpClients.custom().
                                setRetryHandler(new DefaultHttpRequestRetryHandler(5, true))

                                .build();


                try {

                    // Create a custom response handler
                    ResponseHandler<Document> responseHandler = new ResponseHandler<Document>() {


                        public Document handleResponse(
                                final HttpResponse response) throws IOException {
                            int status = response.getStatusLine().getStatusCode();
                            if (status >= 200 && status < 300) {
                                HttpEntity entity = response.getEntity();
                                Document doc = Jsoup.parse(entity.getContent(), "UTF-8", "");
                                //return entity != null ? EntityUtils.toString(entity) : null;
                                return doc;
                            } else {
                                if (throwOnEmptyDocument)
                                    throw new ClientProtocolException("Unexpected response status: " + status);
                                else
                                    return null;
                            }
                        }

                    };
                    doc = httpclient.execute(httpRequest, responseHandler, localContext);
                } finally {
                    httpclient.close();
                }
                isSuccess = true;

            } catch (IOException ex) {
                logger.warning("Error in performing request for person  " + ex.getMessage());
                if (attemptIndex < maxAttempts) {
                    logger.warning("Retry attempt " + attemptIndex + " / " + maxAttempts + ", waiting before retrying" + delay);
                } else {
                    logger.warning("Aborting process, after  " + maxAttempts + " attempts over " + delay + "ms");
                    throw ex;
                }
                Thread.sleep((int) (delay.longValue()));
                attemptIndex++;
                delay = (long) (delay * backoffExponent + System.nanoTime() % 1000);
            }
        }
        return doc;
    }


}
