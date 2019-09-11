package com.bocse.perfume.requester;


import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * Created by bocse on 22.11.2015.
 */
public class HttpRequester {
    private final static Logger logger = Logger.getLogger(HttpRequester.class.toString());
    private final static Long maxAttempts = 10L;
    private final static Long initialDelay = 1300L;
    private final static Double backoffExponent = 1.9;
    private final static String[] userAgents = {
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.",
    };

    public static Document getDocument(HttpUriRequest httpRequest) throws IOException, InterruptedException {
        return getDocument(httpRequest, true);
    }


    public static Document getDocument(HttpUriRequest httpRequest, final Boolean throwOnEmptyDocument) throws IOException, InterruptedException {
        Document doc = null;
        Long delay = initialDelay;
        Long attemptIndex = 0L;
        Boolean isSuccess = false;

        String userAgent = userAgents[0];
        httpRequest.addHeader("User-Agent", userAgent);
        httpRequest.addHeader("Referer", "https://www.google.com/");
        httpRequest.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        //httpRequest.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        httpRequest.addHeader("Accept-Language", "en-US,en;q=0.8");
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
                    doc = httpclient.execute(httpRequest, responseHandler);
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


    public static Long downloadToFile(HttpUriRequest httpRequest, File downloadFile, final Boolean throwOnEmptyDocument) throws IOException, InterruptedException {
        Long length = -1L;
        Long delay = initialDelay;
        Long attemptIndex = 0L;
        Boolean isSuccess = false;

        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
        httpRequest.addHeader("User-Agent", userAgents[(int) (System.nanoTime() % userAgents.length)] + (System.nanoTime() % 99 + 1));
        httpRequest.addHeader("Referer", "https://www.google.com/");
        httpRequest.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        //httpRequest.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        httpRequest.addHeader("Accept-Language", "en-US,en;q=0.8");
        while (!isSuccess & attemptIndex < maxAttempts) {
            try {
                CloseableHttpClient httpclient =
                        HttpClients.custom().
                                setRetryHandler(new DefaultHttpRequestRetryHandler(5, true))

                                .build();


                try {

                    // Create a custom response handler
                    ResponseHandler<Long> responseHandler = new ResponseHandler<Long>() {


                        public Long handleResponse(
                                final HttpResponse response) throws IOException {
                            int status = response.getStatusLine().getStatusCode();
                            if (status >= 200 && status < 300) {
                                HttpEntity entity = response.getEntity();

                                int bufferSize = 1024 * 40;
                                long contentLength = entity.getContentLength();
                                if (entity.getContentLength() > 1) {
                                    bufferSize = (int) entity.getContentLength();
                                    bufferSize = bufferSize + 1024;
                                }
                                // 7.593 - 7.638
                                if (contentLength < 7590 || contentLength > 7640) {
                                    OutputStream output = new BufferedOutputStream(
                                            new FileOutputStream(downloadFile),
                                            bufferSize
                                    );
                                    IOUtils.copy(entity.getContent(), output);
                                    output.close();
                                }
                                return entity.getContentLength();
                            } else {
                                if (throwOnEmptyDocument)
                                    throw new ClientProtocolException("Unexpected response status: " + status);
                                else
                                    return null;
                            }
                        }

                    };
                    length = httpclient.execute(httpRequest, responseHandler);
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
        return length;
    }


    public static String getInputStreamFromGzipURL(HttpUriRequest httpRequest, final Boolean throwOnEmptyDocument, String username, String password) throws IOException, InterruptedException {
        //InputStream inputStream=null;
        String stringResult = null;
        Long delay = initialDelay;
        Long attemptIndex = 0L;
        Boolean isSuccess = false;

        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
        httpRequest.addHeader("User-Agent", userAgents[(int) (System.nanoTime() % userAgents.length)] + (System.nanoTime() % 99 + 1));
        httpRequest.addHeader("Referer", "https://www.google.com/");
        httpRequest.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        //httpRequest.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        httpRequest.addHeader("Accept-Language", "en-US,en;q=0.8");

        while (!isSuccess & attemptIndex < maxAttempts) {
            try {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(AuthScope.ANY),
                        new UsernamePasswordCredentials(username, password));
                CloseableHttpClient httpclient =
                        HttpClients.custom().
                                setRetryHandler(new DefaultHttpRequestRetryHandler(5, true))
                                .setDefaultCredentialsProvider(credsProvider)
                                .build();


                try {

                    // Create a custom response handler
                    ResponseHandler<String> responseHandler = new ResponseHandler<String>() {


                        public String handleResponse(
                                final HttpResponse response) throws IOException {
                            int status = response.getStatusLine().getStatusCode();
                            if (status >= 200 && status < 300) {
                                HttpEntity entity = response.getEntity();
                                BufferedInputStream bis = new BufferedInputStream(entity.getContent());
                                GzipCompressorInputStream gzIn = new GzipCompressorInputStream(bis);

                                String theString = IOUtils.toString(gzIn, "UTF-8");
                                return theString;

                            } else {
                                if (throwOnEmptyDocument)
                                    throw new ClientProtocolException("Unexpected response status: " + status);
                                else
                                    return null;
                            }
                        }

                    };
                    stringResult = httpclient.execute(httpRequest, responseHandler);
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
        return stringResult;
    }


    public static String getInputStreamURL(HttpUriRequest httpRequest, final Boolean throwOnEmptyDocument) throws IOException, InterruptedException {
        //InputStream inputStream=null;
        String stringResult = null;
        Long delay = initialDelay;
        Long attemptIndex = 0L;
        Boolean isSuccess = false;

        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
        httpRequest.addHeader("User-Agent", userAgents[(int) (System.nanoTime() % userAgents.length)] + (System.nanoTime() % 99 + 1));
        httpRequest.addHeader("Referer", "https://www.google.com/");
        httpRequest.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        //httpRequest.addHeader("Accept-Encoding", "gzip, deflate, sdch");
        httpRequest.addHeader("Accept-Language", "en-US,en;q=0.8");

        while (!isSuccess & attemptIndex < maxAttempts) {
            try {
                /*
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(AuthScope.ANY),
                        new UsernamePasswordCredentials(username, password));
                        */
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
                                BufferedInputStream bis = new BufferedInputStream(entity.getContent());


                                String theString = IOUtils.toString(bis, "UTF-8");
                                return theString;

                            } else {
                                if (throwOnEmptyDocument)
                                    throw new ClientProtocolException("Unexpected response status: " + status);
                                else
                                    return null;
                            }
                        }

                    };
                    stringResult = httpclient.execute(httpRequest, responseHandler);
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
        return stringResult;
    }


    public static String getInputStreamTLSURL(HttpUriRequest httpRequest, final Boolean throwOnEmptyDocument) throws IOException, InterruptedException {
        //InputStream inputStream=null;
        String stringResult = null;
        Long delay = initialDelay;
        Long attemptIndex = 0L;
        Boolean isSuccess = false;

//        String userAgent="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
//        httpRequest.addHeader("User-Agent", userAgents[(int)(System.nanoTime()%userAgents.length)]+(System.nanoTime()%99+1));
//        httpRequest.addHeader("Referer", "https://www.google.com/");
//        httpRequest.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//        //httpRequest.addHeader("Accept-Encoding", "gzip, deflate, sdch");
//        httpRequest.addHeader("Accept-Language","en-US,en;q=0.8");

        while (!isSuccess & attemptIndex < maxAttempts) {
            try {
                SSLContext sslContext = SSLContexts.custom().useProtocol("TLSv1.2").loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();

                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                        new String[]{"TLSv1.2"},
                        null,
                        new NoopHostnameVerifier());


                CloseableHttpClient httpclient = HttpClients.custom()
                        //.setDefaultCredentialsProvider(credsProvider)
                        .setSSLSocketFactory(sslsf)
                        .build();
//                CloseableHttpClient httpclient =
//                        HttpClients.custom().
//                                setRetryHandler(new DefaultHttpRequestRetryHandler(5, true))
//                                .build();


                try {

                    // Create a custom response handler
                    ResponseHandler<String> responseHandler = new ResponseHandler<String>() {


                        public String handleResponse(
                                final HttpResponse response) throws IOException {
                            int status = response.getStatusLine().getStatusCode();
                            if (status >= 200 && status < 300) {
                                HttpEntity entity = response.getEntity();
                                BufferedInputStream bis = new BufferedInputStream(entity.getContent());


                                String theString = IOUtils.toString(bis, "UTF-8");
                                return theString;

                            } else {
                                if (throwOnEmptyDocument)
                                    throw new ClientProtocolException("Unexpected response status: " + status);
                                else
                                    return null;
                            }
                        }

                    };
                    stringResult = httpclient.execute(httpRequest, responseHandler);
                } finally {
                    httpclient.close();
                }
                isSuccess = true;

            } catch (IOException ex) {
                logger.warning("Error in performing request for person  " + ex.getMessage());
                ex.printStackTrace();
                if (attemptIndex < maxAttempts) {
                    logger.warning("Retry attempt " + attemptIndex + " / " + maxAttempts + ", waiting before retrying" + delay);
                } else {
                    logger.warning("Aborting process, after  " + maxAttempts + " attempts over " + delay + "ms");
                    throw ex;
                }
                Thread.sleep((int) (delay.longValue()));
                attemptIndex++;
                delay = (long) (delay * backoffExponent + System.nanoTime() % 1000);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
        }
        return stringResult;
    }

}
