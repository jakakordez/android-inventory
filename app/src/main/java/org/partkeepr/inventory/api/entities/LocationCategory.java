package org.partkeepr.inventory.api.entities;

import androidx.annotation.NonNull;

import java.util.List;

public class LocationCategory {
    public int Id;
    public String Name;
    public List<Location> Locations;
    public List<LocationCategory> Subcategories;

    @NonNull
    @Override
    public String toString() {
        return Name;
    }
}
