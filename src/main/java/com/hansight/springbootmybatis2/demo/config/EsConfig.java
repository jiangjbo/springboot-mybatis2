package com.hansight.springbootmybatis2.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "es")
public class EsConfig {

    private int clientCount;
    private String clusterName;
    private String addresses;
    private boolean sniff;
    private int sniffTimeout;
    private boolean enableSSL;
    private int maxRetryTimeout;
    private int connectTimeout;
    private int socketTimeout;
    private int connectionRequestTimeout;
    private int toThreadCount;
    private int maxConnPerRoute;
    private int maxConnTotal;
    private int buckSize;

    public int getClientCount() {
        return clientCount;
    }

    public void setClientCount(int clientCount) {
        this.clientCount = clientCount;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public int getMaxRetryTimeout() {
        return maxRetryTimeout;
    }

    public void setMaxRetryTimeout(int maxRetryTimeout) {
        this.maxRetryTimeout = maxRetryTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public boolean isEnableSSL() {
        return enableSSL;
    }

    public void setEnableSSL(boolean enableSSL) {
        this.enableSSL = enableSSL;
    }

    public int getToThreadCount() {
        return toThreadCount;
    }

    public void setToThreadCount(int toThreadCount) {
        this.toThreadCount = toThreadCount;
    }

    public int getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
    }

    public int getMaxConnTotal() {
        return maxConnTotal;
    }

    public void setMaxConnTotal(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
    }

    public int getBuckSize() {
        return buckSize;
    }

    public void setBuckSize(int buckSize) {
        this.buckSize = buckSize;
    }

    public boolean isSniff() {
        return sniff;
    }

    public void setSniff(boolean sniff) {
        this.sniff = sniff;
    }


    public int getSniffTimeout() {
        return sniffTimeout;
    }

    public void setSniffTimeout(int sniffTimeout) {
        this.sniffTimeout = sniffTimeout;
    }

    @Override
    public String toString() {
        return "EsConfig{" +
                "clientCount=" + clientCount +
                ", clusterName='" + clusterName + '\'' +
                ", addresses='" + addresses + '\'' +
                ", sniff=" + sniff +
                ", sniffTimeout=" + sniffTimeout +
                ", enableSSL=" + enableSSL +
                ", maxRetryTimeout=" + maxRetryTimeout +
                ", connectTimeout=" + connectTimeout +
                ", socketTimeout=" + socketTimeout +
                ", connectionRequestTimeout=" + connectionRequestTimeout +
                ", toThreadCount=" + toThreadCount +
                ", maxConnPerRoute=" + maxConnPerRoute +
                ", maxConnTotal=" + maxConnTotal +
                ", buckSize=" + buckSize +
                '}';
    }
}
