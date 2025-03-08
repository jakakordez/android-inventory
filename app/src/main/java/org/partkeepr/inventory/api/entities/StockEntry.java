package org.partkeepr.inventory.api.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;

public class StockEntry {
    public int Id;

    public int PartId;

    public int UserId;

    public int Change;

    public LocalDateTime Timestamp;

    public String Comment;

    public String UserName;

    public static StockEntry FromJsonObject(JSONObject obj) throws JSONException {
        StockEntry result = new StockEntry();
        result.Id = obj.getInt("id");
        result.PartId = obj.optInt("partId", -1);
        result.UserId = obj.optInt("userId", -1);
        result.Change = obj.getInt("change");
        result.Timestamp = LocalDateTime.parse(obj.getString("timestamp"));
        result.Comment = obj.optString("comment", "");
        JSONObject user = obj.optJSONObject("user");
        if (user != null) {
            result.UserName = user.getString("username");
        }
        return result;
    }
}
