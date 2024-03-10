package org.partkeepr.inventory.entities;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Part {
    public String Id;
    public String Name;
    public String Description;
    public int StockLevel;
    public Category PartCategory;
    public StorageLocation PartLocation;

    public ArrayList<StockEntry> StockEntries;

    public JSONObject SourceObject;

    public boolean CheckedThisWeek(){
        if(StockEntries == null) return false;
        for (StockEntry stockEntry : StockEntries) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                long stamp = parser.parse(stockEntry.DateTime).getTime();
                long now = new Date().getTime();
                if (now - stamp < 1000 * 60 * 60 * 24 * 7) return true;
            }
            catch (ParseException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return Id + ": " + Name;
    }
}
