package com.hansight.springbootmybatis2.common.vul.factory;

import com.hansight.springbootmybatis2.common.elasticsearch.factory.HanRestClient;
import com.hansight.springbootmybatis2.demo.config.VulScanConfig;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class VulRestClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VulScanConfig vulScanConfig;

    private List<RestClient> vulRestClients = new ArrayList<>();
    private int totalCount;
    private static int lowCount;
    private Lock lock = new ReentrantLock();

    private static ThreadFactory threadFactory =  new ThreadFactory() {
        private final AtomicLong COUNT = new AtomicLong(1);
        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "VulScanRestClient" + COUNT.getAndIncrement());
        }
    };

    @PostConstruct
    public void init() {
        HttpHost[] hosts = Arrays.stream(vulScanConfig.getAddress().split(","))
                .map(s->s.split(":"))
                .map(s->new HttpHost(s[0], Integer.parseInt(s[1]), vulScanConfig.isEnableSSL()?"https":"http"))
                .collect(Collectors.toList()).toArray(new HttpHost[0]);
        this.totalCount = vulScanConfig.getClientCount();
        for(int i=0;i<totalCount;i++){
            newLowRestClients(hosts);
        }
    }

    public RestClient getLowRestClient(){
        try {
            lock.lock();
            if(lowCount >= totalCount){
                lowCount = 0;
            }
            return vulRestClients.get(lowCount++);
        } finally {
            lock.unlock();
        }
    }

    private void newLowRestClients(HttpHost[] hosts) {
        RestClientBuilder restClientBuilder = RestClient.builder(hosts)
                .setMaxRetryTimeoutMillis(vulScanConfig.getMaxRetryTimeout())
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(vulScanConfig.getConnectTimeout())
                        .setSocketTimeout(vulScanConfig.getSocketTimeout())
                        .setConnectionRequestTimeout(vulScanConfig.getConnectionRequestTimeout()))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    private IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(vulScanConfig.getToThreadCount())
                            .setTcpNoDelay(true)
                            .setSoKeepAlive(true)
                            .build();
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        httpClientBuilder = httpClientBuilder.setMaxConnPerRoute(vulScanConfig.getMaxConnPerRoute())
                                .setMaxConnTotal(vulScanConfig.getMaxConnTotal())
                                .setDefaultIOReactorConfig(ioReactorConfig)
                                .setThreadFactory(threadFactory);
                        if(vulScanConfig.isEnableSSL()){
                            httpClientBuilder.setSSLStrategy(new SSLIOSessionStrategy(
                                    createIgnoreVerifySSL(),
                                    new String[] { "TLSv1.2", "TLSv1.1"},
                                    null,
                                    NoopHostnameVerifier.INSTANCE
                            ));
                            //.setSSLContext(createIgnoreVerifySSL())
                            //.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        }
                        return httpClientBuilder;
                    }
                });
        RestClient restClient= restClientBuilder.build();
        vulRestClients.add(restClient);
    }

    @PreDestroy
    public void destroy() {
        vulRestClients.forEach(restClient -> {
            try {
                restClient.close();
            } catch (IOException e) {
                logger.debug("", e);
            }
        });
    }

    private static SSLContext createIgnoreVerifySSL() {
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");

            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                        String paramString) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                        String paramString) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[] {};
                }
            };
            sc.init(null, new TrustManager[] { trustManager }, new java.security.SecureRandom());
            return sc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sc;
    }
}
