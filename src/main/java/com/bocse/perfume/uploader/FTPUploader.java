package com.bocse.perfume.uploader;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Created by bocse on 27.11.2015.
 */
public class FTPUploader {
    private final static Logger logger = Logger.getLogger(FTPUploader.class.toString());
    private final ExecutorService executor;
    private FTPClient ftp = new FTPClient();
    private FTPClientConfig config = new FTPClientConfig();
    private String hostname;
    private int port = 21;
    private String username;
    private String password;

    public FTPUploader(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;

        ftp.setConnectTimeout(15000);
        ftp.setControlKeepAliveReplyTimeout(5000);
        ftp.setControlKeepAliveTimeout(121);
        config.setLenientFutureDates(true);
        // for example config.setServerTimeZoneId("Pacific/Pitcairn")
        ftp.configure(config);
        executor = Executors.newSingleThreadExecutor();
    }

    public void init() throws IOException {

        boolean error = false;
        try {
            int reply;

            ftp.connect(hostname, port);
            logger.info("Connected to " + hostname + ".");
            logger.info(ftp.getReplyString());

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                logger.warning("FTP server refused connection.");
                System.exit(1);
            }
            ftp.login(username, password);
            logger.info(ftp.getReplyString());
            ftp.enterLocalPassiveMode();
            logger.info(ftp.getReplyString());
            //ftp.setFileType(FTP.ASCII_FILE_TYPE);
            // transfer files
            //ftp.logout();
        } finally {


        }
    }

    public boolean uploadFile(final File localFile) throws IOException {
        return this.uploadFile("/data/" + localFile.getName(), localFile);
    }

    public synchronized boolean uploadFile(final String remotePath, final File localFile) throws IOException {
        if (!ftp.isConnected()) {
            this.init();
        }
        boolean result = ftp.storeFile(remotePath, new FileInputStream(localFile));
        logger.info(ftp.getReplyString());
        return result;

    }

    public Future<Boolean> uploadFileAsync(final String remotePath, final File localFile) {
        synchronized (executor) {
            return executor.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    return uploadFile(remotePath, localFile);
                }
            });
        }
    }

    public void disconnect() throws InterruptedException, IOException {
        executor.shutdown();
        logger.info("Waiting for upload to finish.");
        executor.awaitTermination(10, TimeUnit.HOURS);
        logger.info("Finished uploads");
        if (ftp.isConnected()) {

            ftp.disconnect();
        }
        logger.info("Terminated FTP connection.");
    }
}
