package com.example.destinyitemrandomizer.destinywrapper;

public class ItemLookupInfo {

    private String hash;
    private String instanceId;
    private String ownerId;
    private boolean isEquipped = false;

    public boolean isEquipped() {
        return isEquipped;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getHash() {
        return hash;
    }

    public String getInstanceId() {
        return instanceId;
    }


    public ItemLookupInfo(String h, String i, String o, Boolean e) {
        hash = h;
        instanceId = i;
        ownerId = o;
        isEquipped = e;
    }

    public ItemLookupInfo(String h, String i, String o) {
        hash = h;
        instanceId = i;
        ownerId = o;
    }

}
