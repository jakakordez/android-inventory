package org.partkeepr.inventory.api.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;

public class Part {
    public int Id;

    public int CategoryId;

    public String Name;

    public String Description;

    public int StockLevel;

    public int LocationId;

    public LocalDateTime LastEntry;

    public static Part FromJsonObject(JSONObject obj) throws JSONException {
        Part result = new Part();
        result.Id = obj.getInt("id");
        result.CategoryId = obj.getInt("categoryId");
        result.Name = obj.getString("name");
        result.Description = obj.optString("description", "");
        result.StockLevel = obj.getInt("stockLevel");
        result.LocationId = obj.getInt("locationId");
        result.LastEntry = LocalDateTime.parse(obj.getString("lastEntry"));
        return result;
    }

    public boolean CheckedRecently() {
        if (LastEntry == null) {
            return false;
        }
        Duration between = Duration.between(LastEntry, LocalDateTime.now());
        return between.toDays() < 14;
    }

    public JSONObject ToJson() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("Id", Id);
        result.put("CategoryId", CategoryId);
        result.put("Name", Name);
        result.put("Description",  Description);
        result.put("StockLevel", StockLevel);
        result.put("LocationId", LocationId);
        return result;
    }
}
