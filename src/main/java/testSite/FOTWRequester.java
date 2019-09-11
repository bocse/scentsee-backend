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
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by bocse on 22.11.2015.
 */
public class FOTWRequester {
    private final static Logger logger = Logger.getLogger(FOTWRequester.class.toString());
    private final static Long maxAttempts = 3L;
    private final static Long initialDelay = 2000L;
    private final static Double backoffExponent = 2.191709456;
    private final static String[] userAgents = {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36"
            //"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:42.0) Gecko/20100101 Firefox/42.0",
    };

    final int requestTimeout = 5000;
    final String cookieDomain = "www.fragrancesoftheworld.info";
    //ASPSESSIONIDACCATDQB=NGMPHJGAHEIFOIKCAOOKCJCD; ASP.NET_SessionId=ii31q5v02a3vivitnunpru45
    final String cookie1Name = "ASPSESSIONIDACCATDQB";
    final String cookie1Value = "NGMPHJGAHEIFOIKCAOOKCJCD";
    final String cookie2Name = "ASP.NET_SessionId";
    final String cookie2Value = "ii31q5v02a3vivitnunpru45";

    final Map<String, Object> status = new Hashtable<>();
    final int maxThreads = 8;
    final int startPage = 3800;
    final int endPage = 33000;

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {

        new FOTWRequester().execute(args);


    }

    public String getDocument(HttpUriRequest httpRequest) throws IOException, InterruptedException {
        return getDocument(httpRequest, true);
    }
    //scanned up to: 42791
    //first found: 3820
    //last found: 32767
    //INFO: Status: {retrieved=1, density=0.0, tried=3822, speedPerHour=7939.009170204135, lastPage=3822}

    public void execute(String[] args) throws IOException, InterruptedException {
        AtomicInteger retrieved = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);
        Long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);

        for (int pageIndex = startPage; pageIndex <= endPage; pageIndex++) {
            final int pageIndexCopy = pageIndex;
            executorService.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    PrintWriter writer = null;
                    try {
                        String newUrl = "http://www.fragrancesoftheworld.info//main/perfumedetail.asp?contactid=" + pageIndexCopy;
                        final HttpGet httpGet = new HttpGet(newUrl);
                        final RequestConfig requestConfig = RequestConfig.custom()
                                .setConnectionRequestTimeout(requestTimeout).setConnectTimeout(requestTimeout).setSocketTimeout(requestTimeout).build();
                        httpGet.setConfig(requestConfig);
                        String doc = getDocument(httpGet);
                        int tried = pageIndexCopy - startPage + 1;
                        status.put("tried", tried);
                        status.put("lastPage", pageIndexCopy);
                        status.put("speedPerHour", new Double(tried * 3600.0 * 1000.0 / (System.currentTimeMillis() - startTime)));
                        status.put("density", new Double(retrieved.get() * 1.0 / tried));
                        if (doc != null) {
                            writer = new PrintWriter("/home/bocse/fotw/html_" + pageIndexCopy + ".html", "ISO-8859-1");
                            writer.println(doc);
                            retrieved.incrementAndGet();
                            status.put("retrieved", retrieved.get());
                        }
                    } catch (Exception ex) {
                        logger.severe("Failed to read " + pageIndexCopy);
                        ex.printStackTrace();
                        errors.incrementAndGet();
                        status.put("errors", errors.get());
                    } finally {

                        if (writer != null) {
                            writer.close();
                            writer = null;
                        }
                    }
                    logger.info("Status: " + status.toString());
                    return new Object();
                }
            });

        }
        executorService.shutdown();
        logger.info("created jobs, awaiting termination");
        executorService.awaitTermination(24, TimeUnit.HOURS);
        logger.info("Status: " + status.toString());
        logger.info("Done in " + (System.currentTimeMillis() - startTime) / 1000 + "s");
    }

    public String getDocument(HttpUriRequest httpRequest, final Boolean throwOnEmptyDocument) throws IOException, InterruptedException {
        String doc = null;
        Long delay = initialDelay;
        Long attemptIndex = 0L;
        Boolean isSuccess = false;

        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
        httpRequest.addHeader("User-Agent", userAgents[(int) (System.nanoTime() % userAgents.length)] + (System.nanoTime() % 99 + 1));
        httpRequest.addHeader("Referer", "https://www.google.com/");
        httpRequest.addHeader("Accept", "ext/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        httpRequest.addHeader("Accept-Language", "en-US,en;q=0.8");
        CookieStore cookieStore = new BasicCookieStore();

        // Create local HTTP context
        final HttpClientContext localContext = HttpClientContext.create();
        // Bind custom cookie store to the local context
        localContext.setCookieStore(cookieStore);
        //ASPSESSIONIDACADSCRA=LODPIBMDOHCDCHHOLHGGNCAI; ASP.NET_SessionId=r3klx555wptsi245fr3woy45
        //www.fragrancesoftheworld.info
        BasicClientCookie cookie1 = new BasicClientCookie(cookie1Name, cookie1Value);
        cookie1.setDomain(cookieDomain);
        cookie1.setPath("/");
        cookieStore.addCookie(cookie1);

        BasicClientCookie cookie2 = new BasicClientCookie(cookie2Name, cookie2Value);
        cookie2.setDomain(cookieDomain);
        cookie2.setPath("/");
        cookieStore.addCookie(cookie2);

        while (!isSuccess & attemptIndex < maxAttempts) {
            try {
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
                                //entity.getContentEncoding()
                                return entity != null ? EntityUtils.toString(entity, "ISO-8859-1") : null;
                                //return doc;
                            } else if (status == 500)
                                return null;
                            else {
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
                delay = (long) ((delay + (System.nanoTime() % 1000)) * backoffExponent);
            }
        }
        return doc;
    }


}
