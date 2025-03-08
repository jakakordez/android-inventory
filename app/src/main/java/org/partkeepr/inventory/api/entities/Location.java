package org.partkeepr.inventory.api.entities;

import androidx.annotation.NonNull;

public class Location {
    public int Id;

    public String Name;

    @NonNull
    @Override
    public String toString() {
        return Name;
    }
}
