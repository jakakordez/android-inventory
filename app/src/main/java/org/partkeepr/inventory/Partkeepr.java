package org.partkeepr.inventory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.partkeepr.inventory.entities.Category;
import org.partkeepr.inventory.entities.StockEntry;
import org.partkeepr.inventory.entities.StorageLocation;
import org.partkeepr.inventory.entities.Part;

import java.net.URLEncoder;
import java.util.ArrayList;

public class Partkeepr {
    Client client;

    public Partkeepr(Client client){
        this.client = client;
    }

    public void GetParts(Client.OnResult<ArrayList<Part>> onParts){
        GetParts(onParts, null);
    }

    public void GetParts(Client.OnResult<ArrayList<Part>> onParts, StorageLocation location){
        String url = "/parts";
        if(location != null) url += "?filter=" + Filter(location);
        client.Request(url, json -> {
            try {
                ArrayList<Part> parts = new ArrayList<>();
                JSONArray jsonArray = json.getJSONArray("hydra:member");
                for(int i = 0; i < jsonArray.length(); i++){
                    parts.add(ParsePart(jsonArray.getJSONObject(i)));
                }
                onParts.Result(parts);
            }
            catch (Exception e){
                e.printStackTrace();
                onParts.Result(null);
            }
        });
    }

    public void GetStock(Client.OnResult<ArrayList<StockEntry>> onStock, Part part){
        client.Request("/stock_entries?filter=" + Filter(part), json -> {
            try {
                ArrayList<StockEntry> entries = new ArrayList<>();
                JSONArray jsonArray = json.getJSONArray("hydra:member");
                for(int i = 0; i < jsonArray.length(); i++){
                    entries.add(ParseStockEntry(jsonArray.getJSONObject(i)));
                }
                onStock.Result(entries);
            }
            catch (Exception e){
                e.printStackTrace();
                onStock.Result(null);
            }
        });
    }

    private String Filter(Part part){
        try {
            String url = "[{\"subfilters\":[],\"property\":\"part\",\"operator\":\"=\",\"value\":\"" + part.Id + "\"}]";
            return URLEncoder.encode(url, "utf-8");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private String Filter(StorageLocation location){
        try {
            String url = "[{\"subfilters\":[],\"property\":\"storageLocation\",\"operator\":\"=\",\"value\":\"" + location.Id + "\"}]";
            return URLEncoder.encode(url, "utf-8");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public void GetLocations(Client.OnResult<ArrayList<StorageLocation>> onLocations){
        client.Request("/storage_locations", json -> {
            try {
                ArrayList<StorageLocation> entries = new ArrayList<>();
                JSONArray jsonArray = json.getJSONArray("hydra:member");
                for(int i = 0; i < jsonArray.length(); i++){
                    entries.add(ParseLocation(jsonArray.getJSONObject(i)));
                }
                onLocations.Result(entries);
            }
            catch (Exception e){
                e.printStackTrace();
                onLocations.Result(null);
            }
        });
    }

    private StockEntry ParseStockEntry(JSONObject stockObject) throws Exception{
        StockEntry s = new StockEntry();
        s.Id = stockObject.getString("@id");
        if(stockObject.has("comment") && !stockObject.isNull("comment")) {
            s.Comment = stockObject.getString("comment");
        }
        s.StockLevel = stockObject.getInt("stockLevel");
        s.DateTime = stockObject.getString("dateTime");
        return s;
    }

    private Part ParsePart(JSONObject partObject) throws Exception{
        Part p = new Part();
        p.Id = partObject.getString("@id");
        p.Name = partObject.getString("name");
        p.Description = partObject.getString("description");
        p.StockLevel = partObject.getInt("stockLevel");
        p.PartCategory = ParseCategory(partObject.getJSONObject("category"));
        if(partObject.has("storageLocation") && !partObject.isNull("storageLocation")) {
            p.PartLocation = ParseLocation(partObject.getJSONObject("storageLocation"));
        }
        return p;
    }

    public Category ParseCategory(JSONObject categoryObject) throws Exception {
        Category c = new Category();
        c.Id = categoryObject.getString("@id");
        c.Name = categoryObject.getString("name");
        return c;
    }

    public StorageLocation ParseLocation(JSONObject locationObject) throws Exception {
        StorageLocation l = new StorageLocation();
        l.Id = locationObject.getString("@id");
        l.Name = locationObject.getString("name");
        return l;
    }

    public void SetStock(Part part, int quantity, String comment, Client.OnResult<Boolean> onResult) {
        client.Put(part.Id + "/setStock", "quantity=" + quantity + "&comment=" + comment, onResult);
    }

    public void AddStock(Part part, int quantity, String comment, Client.OnResult<Boolean> onResult) {
        client.Put(part.Id + "/addStock", "quantity=" + quantity + "&comment=" + comment, onResult);
    }
}
