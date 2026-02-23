package com.example.cfrepave.model;

public class InstanceInfo {

    private String instanceIndex;
    private String instanceId;
    private String instanceIp;
    private String instancePort;
    private long uptimeSeconds;
    private String appName;
    private String appId;
    private String timestamp;
    private int memoryLimitMb;
    private int diskLimitMb;
    private long memoryUsedMb;
    private long diskUsedMb;

    public String getInstanceIndex() {
        return instanceIndex;
    }

    public void setInstanceIndex(String instanceIndex) {
        this.instanceIndex = instanceIndex;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceIp() {
        return instanceIp;
    }

    public void setInstanceIp(String instanceIp) {
        this.instanceIp = instanceIp;
    }

    public String getInstancePort() {
        return instancePort;
    }

    public void setInstancePort(String instancePort) {
        this.instancePort = instancePort;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getMemoryLimitMb() {
        return memoryLimitMb;
    }

    public void setMemoryLimitMb(int memoryLimitMb) {
        this.memoryLimitMb = memoryLimitMb;
    }

    public int getDiskLimitMb() {
        return diskLimitMb;
    }

    public void setDiskLimitMb(int diskLimitMb) {
        this.diskLimitMb = diskLimitMb;
    }

    public long getMemoryUsedMb() {
        return memoryUsedMb;
    }

    public void setMemoryUsedMb(long memoryUsedMb) {
        this.memoryUsedMb = memoryUsedMb;
    }

    public long getDiskUsedMb() {
        return diskUsedMb;
    }

    public void setDiskUsedMb(long diskUsedMb) {
        this.diskUsedMb = diskUsedMb;
    }
}
