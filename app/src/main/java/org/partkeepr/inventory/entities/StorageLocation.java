package org.partkeepr.inventory.entities;

import org.json.JSONObject;

public class StorageLocation {
    public String Id;
    public String Name;

    public JSONObject SourceObject;

    @Override
    public String toString() {
        return Name;
    }
}
