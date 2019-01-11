package com.hansight.springbootmybatis2.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vul")
public class VulScanConfig {

    private int clientCount;
    private String address;
    private boolean enableSSL;
    private int maxRetryTimeout;
    private int connectTimeout;
    private int socketTimeout;
    private int connectionRequestTimeout;
    private int toThreadCount;
    private int maxConnPerRoute;
    private int maxConnTotal;

    public int getClientCount() {
        return clientCount;
    }

    public void setClientCount(int clientCount) {
        this.clientCount = clientCount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isEnableSSL() {
        return enableSSL;
    }

    public void setEnableSSL(boolean enableSSL) {
        this.enableSSL = enableSSL;
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

    @Override
    public String toString() {
        return "VulScanConfig{" +
                "clientCount=" + clientCount +
                ", address='" + address + '\'' +
                ", enableSSL=" + enableSSL +
                ", maxRetryTimeout=" + maxRetryTimeout +
                ", connectTimeout=" + connectTimeout +
                ", socketTimeout=" + socketTimeout +
                ", connectionRequestTimeout=" + connectionRequestTimeout +
                ", toThreadCount=" + toThreadCount +
                ", maxConnPerRoute=" + maxConnPerRoute +
                ", maxConnTotal=" + maxConnTotal +
                '}';
    }
}
