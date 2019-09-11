package testSite;

import com.bocse.perfume.requester.HttpRequester;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.time.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by bogdan.bocse on 26/07/16.
 */
public class SelfTest {
    private final static Logger logger = Logger.getLogger(SelfTest.class.toString());

    /*
    Email:
    test-user@scentsee.com
    Password:
    s0meL0ngPa$$word
    First Name:
    Test
    Last Name:
    User
    aceessKey:
    cRStXTxO54LtCR3AjhaV9MPq7tLTxegF
    secretKey:
    CnVThu44yz2ZmRWxjSWXmAhjLVyEPn7Gsg6GKnRBYE1JCw9v9MqyVbLycaTIoiKr
     */

    private Map<String, Long> timedURLs = new HashMap<>();

    public SelfTest() {

    }

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        try {
            new SelfTest().performAll();
        } catch (Exception ex) {
            logger.warning("Unknown error" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void performAll() throws IOException, InterruptedException {
        performBattery("http");
        //performBattery("https");
        logger.info("Times: " + timedURLs.toString());
    }

    private void performBattery(String protocol) throws IOException, InterruptedException {
        HttpRequester.getDocument(createPerfumeDocumentRequest(protocol + "://scentsee.com/rest/authentication/login?email=test-user@scentsee.com&password=s0meL0ngPa$$word"));
        //Advanced Search
        performRequest(protocol + "://scentsee.com/rest/recommendation/byAdvancedSearch?gender=MALE&affinityIds[]=2567305179982780&adversityIds[]=7508222889646533&priceMin=250&priceMax=550&maxResults=8&AQUATIC=4&mustBeInStock=true&acceptedAffiliates[]=AORO,STRAWBERRY,PARFUMEXPRESS&accessKey=cRStXTxO54LtCR3AjhaV9MPq7tLTxegF&secretKey=CnVThu44yz2ZmRWxjSWXmAhjLVyEPn7Gsg6GKnRBYE1JCw9v9MqyVbLycaTIoiKr");
        //Quiz Search
        performRequest(protocol + "://scentsee.com/rest/recommendation/byFavoriteFragranceId?gender=MALE&ids[]=969013414245411,7269055690140367&maxResults=8&accessKey=cRStXTxO54LtCR3AjhaV9MPq7tLTxegF&secretKey=CnVThu44yz2ZmRWxjSWXmAhjLVyEPn7Gsg6GKnRBYE1JCw9v9MqyVbLycaTIoiKr");
        //Simple Preference Search
        performRequest(protocol + "://scentsee.com/rest/recommendation/byQuestionnaireAnswersId?gender=MALE&intention=self-male&price=800&skin-complexion=average&skin-type-male=mixed&spicy=yes&diet=omnivore&smoker=yes&personality=extrovert&season=winter&lifestyle=calm-male&occasion=business&color=blue&vacation-spot=sea&music=classical&perfume-personality=strong&accessKey=cRStXTxO54LtCR3AjhaV9MPq7tLTxegF&secretKey=CnVThu44yz2ZmRWxjSWXmAhjLVyEPn7Gsg6GKnRBYE1JCw9v9MqyVbLycaTIoiKr");
    }

    private void performRequest(String url) throws IOException, InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        this.getDocument(createPerfumeDocumentRequest(url), true);
        stopWatch.stop();
        timedURLs.put(url, stopWatch.getTime());
    }

    private HttpUriRequest createPerfumeDocumentRequest(final String url) {
        String newUrl = url.replaceAll(" ", "%20");
        final HttpGet httpGet = new HttpGet(newUrl);
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(9000).setConnectTimeout(9000).setSocketTimeout(9000).build();
        httpGet.setConfig(requestConfig);
        logger.info("Executing request " + httpGet.getRequestLine());
        return httpGet;

    }


    private String getDocument(HttpUriRequest httpRequest, final Boolean throwOnEmptyDocument) throws IOException, InterruptedException {
        String doc = null;

        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
        httpRequest.addHeader("User-Agent", "ScentSee Test Agent");

        httpRequest.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpRequest.addHeader("Accept-Language", "en-US,en;q=0.8");

        CloseableHttpClient httpclient =
                HttpClients.custom().
                        setRetryHandler(new DefaultHttpRequestRetryHandler(5, true))

                        .build();


        try {

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {


                public String handleResponse(
                        final HttpResponse response) throws IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();

                        return entity != null ? EntityUtils.toString(entity) : null;

                    } else {
                        if (throwOnEmptyDocument)
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        else
                            return null;
                    }
                }

            };
            doc = httpclient.execute(httpRequest, responseHandler);
        } finally {
            httpclient.close();
        }


        return doc;
    }
}
