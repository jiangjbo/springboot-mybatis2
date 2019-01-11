package com.hansight.springbootmybatis2.common.elasticsearch.factory;

import com.hansight.springbootmybatis2.common.elasticsearch.util.EsInfoManagement;
import com.hansight.springbootmybatis2.demo.config.EsConfig;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.sniff.SniffOnFailureListener;
import org.elasticsearch.client.sniff.Sniffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @Author: jiangbo
 * @Description:
 * @Date: Created in 16:42 2018/6/15
 * @Modified By:
 */
@Component
public class ESRestClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private EsConfig esConfig;

    private List<HanRestClient> restClients = new ArrayList<>();
    private List<HanRestHighLevelClient> restHighLevelClients = new ArrayList<>();
    private List<Sniffer> sniffers = new ArrayList<>();
    private int totalCount;
    private static int lowCount;
    private static int highCount;
    private Lock lock = new ReentrantLock();

    private static ThreadFactory threadFactory =  new ThreadFactory() {
        private final AtomicLong COUNT = new AtomicLong(1);
        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "ElasticsearchRestClient" + COUNT.getAndIncrement());
        }
    };

    @PostConstruct
    public void init() {
        HttpHost[] hosts = Arrays.stream(esConfig.getAddresses().split(","))
                .map(s->s.split(":"))
                .map(s->new HttpHost(s[0], Integer.parseInt(s[1]), esConfig.isEnableSSL()?"https":"http"))
                .collect(Collectors.toList()).toArray(new HttpHost[0]);
        this.totalCount = esConfig.getClientCount();
        for(int i=0;i<totalCount;i++){
            newLowRestClients(hosts);
        }
        String version = EsInfoManagement.initVersion(this);
        logger.info(String.format("es version %s", version));
        try {
            String cluster = getRestHighLevelClient().info().getClusterName().value();
            if (!esConfig.getClusterName().equals(cluster)) {
                logger.error("cluster name {%s},{%s} does not match", cluster, esConfig.getClusterName());
            }
        } catch (IOException e) {
            logger.error("connect to es fail", e);
        }

    }

    public HanRestClient getLowRestClient(){
        try {
            lock.lock();
            if(logger.isDebugEnabled())
                logger.debug("get elasticsearch low rest client.");
            if(lowCount >= totalCount){
                lowCount = 0;
            }
            return restClients.get(lowCount++);
        } finally {
            lock.unlock();
        }
    }

    public HanRestHighLevelClient getRestHighLevelClient() {
        try {
            lock.lock();
            if(logger.isDebugEnabled())
                logger.debug("get elasticsearch rest high level client.");
            if(highCount >= totalCount)
                highCount = 0;
            return restHighLevelClients.get(highCount++);
        } finally {
            lock.unlock();
        }
    }

    private void newLowRestClients(HttpHost[] hosts) {
        RestClientBuilder restClientBuilder = RestClient.builder(hosts)
                .setMaxRetryTimeoutMillis(esConfig.getMaxRetryTimeout())
                .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                    @Override
                    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                        return requestConfigBuilder.setConnectTimeout(esConfig.getConnectTimeout())
                                .setSocketTimeout(esConfig.getSocketTimeout())
                                .setConnectionRequestTimeout(esConfig.getConnectionRequestTimeout());
                    }
                })
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    private IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(esConfig.getToThreadCount())
                            .setTcpNoDelay(true)
                            .setSoKeepAlive(true)
                            .build();
                    private SSLContext createIgnoreVerifySSL() {
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
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        httpClientBuilder = httpClientBuilder.setMaxConnPerRoute(esConfig.getMaxConnPerRoute())
                                .setMaxConnTotal(esConfig.getMaxConnTotal())
                                .setDefaultIOReactorConfig(ioReactorConfig)
                                .setThreadFactory(threadFactory);
                        if(esConfig.isEnableSSL()){
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
        RestClient restClient;
        Sniffer sniffer = null;
        if(esConfig.isSniff()){
            SniffOnFailureListener sniffOnFailureListener = new SniffOnFailureListener();
            restClient = restClientBuilder.setFailureListener(sniffOnFailureListener).build();
            sniffer = Sniffer.builder(restClient)
                    .setSniffAfterFailureDelayMillis(30000)
                    .build();
            sniffOnFailureListener.setSniffer(sniffer);
        } else{
            restClient = restClientBuilder.build();
        }
        restClients.add(new HanRestClient(restClient));
        restHighLevelClients.add(new HanRestHighLevelClient(restClient));
        sniffers.add(sniffer);
    }

    @PreDestroy
    public void destroy() {
        restClients.forEach(hanRestClient -> {
            try {
                hanRestClient.close();
            } catch (IOException e) {
                logger.debug("", e);
            }
        });
        sniffers.forEach(sniffer -> {
            try {
                sniffer.close();
            } catch (IOException e) {
                logger.debug("", e);
            }
        });
    }
}
