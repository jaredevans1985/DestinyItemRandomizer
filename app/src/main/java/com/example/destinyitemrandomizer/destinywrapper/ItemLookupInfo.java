package com.example.destinyitemrandomizer.destinywrapper;

public class ItemLookupInfo {

    private String hash;
    private String instanceId;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    ItemLookupInfo(String h, String i) {
        hash = h;
        instanceId = i;
    }

}
