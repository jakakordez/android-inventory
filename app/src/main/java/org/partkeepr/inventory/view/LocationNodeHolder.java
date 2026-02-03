package org.partkeepr.inventory.view;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewHolder;

import org.partkeepr.inventory.ColorHelper;
import org.partkeepr.inventory.R;
import org.partkeepr.inventory.api.entities.Location;

public class LocationNodeHolder extends TreeViewHolder {

    private final Location selectedLocation;

    public LocationNodeHolder(@NonNull View itemView, Location selectedLocation) {
        super(itemView);
        this.selectedLocation = selectedLocation;
    }

    @Override
    public void bindTreeNode(TreeNode node) {
        super.bindTreeNode(node);
        TextView lblName = itemView.findViewById(R.id.lblName);
        Location location = (Location) node.getValue();
        lblName.setText(location.Name);
        int bgColor;
        Context ctx = itemView.getContext();
        if (selectedLocation == location) {
            bgColor = Color.GRAY;
        }
        else {
            bgColor = ColorHelper.GetBackgroundColor(ctx);
        }
        itemView.setBackgroundColor(bgColor);
    }
}
