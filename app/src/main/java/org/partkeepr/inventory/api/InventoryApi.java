package org.partkeepr.inventory.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.partkeepr.inventory.Client;
import org.partkeepr.inventory.api.entities.Location;
import org.partkeepr.inventory.api.entities.LocationCategory;
import org.partkeepr.inventory.api.entities.Part;
import org.partkeepr.inventory.api.entities.StockEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InventoryApi {
    Client client;

    private InventoryApi(Client client){
        this.client = client;
    }

    public static CompletableFuture<InventoryApi> Connect(ConnectionInfo info)
    {
        Client client = new Client(info.ip);
        return client.Login(info.username, info.password)
                .thenApply(result -> new InventoryApi(client));
    }

    public CompletableFuture<List<LocationCategory>> GetLocationTree() {
        return client.List("/api/locations")
                .thenApply(array -> {
                    try {
                        return GetRecursive(array);
                    } catch (JSONException e) {
                        throw new RuntimeException("Unable to parse locations", e);
                    }
                });
    }

    private List<LocationCategory> GetRecursive(JSONArray array) throws JSONException {
        List<LocationCategory> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            LocationCategory category = new LocationCategory();
            category.Id = obj.getInt("id");
            category.Name = obj.getString("name");
            category.Locations = GetLocations(obj.getJSONArray("locations"));
            category.Subcategories = GetRecursive(obj.getJSONArray("subcategories"));
            result.add(category);
        }
        return result;
    }

    private List<Location> GetLocations(JSONArray array) throws JSONException {
        List<Location> result = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Location location = new Location();
            location.Id = obj.getInt("id");
            location.Name = obj.getString("name");
            result.add(location);
        }

        return result;
    }

    public CompletableFuture<List<Part>> GetParts(int locationId) {
        return client.List("/api/locations/" + locationId + "/parts")
            .thenApply(array -> {
                try {
                    List<Part> parts = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        parts.add(Part.FromJsonObject(array.getJSONObject(i)));
                    }
                    return parts;
                } catch (JSONException e) {
                    throw new RuntimeException("Unable to parse locations", e);
                }
            });
    }

    public CompletableFuture<List<StockEntry>> GetStock(int partId) {
        return client.List("/api/parts/" + partId + "/history")
                .thenApply(array -> {
                    try {
                        List<StockEntry> entries = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            entries.add(StockEntry.FromJsonObject(array.getJSONObject(i)));
                        }
                        return entries;
                    } catch (JSONException e) {
                        throw new RuntimeException("Unable to parse locations", e);
                    }
                });
    }

    public CompletableFuture<?> SetStock(int partId, int newLevel, String comment) {
        return client.RequestWithJson(
                "PUT",
                "/api/parts/" + partId + "/history",
                () -> {
                    JSONObject model = new JSONObject();
                    model.put("NewLevel", newLevel);
                    model.put("Comment", comment);
                    return model;
                });
    }

    public CompletableFuture<?> PutPart(Part part) {
        return client.RequestWithJson(
                "PUT",
                "/api/parts/" + part.Id,
                part::ToJson);
    }
}
