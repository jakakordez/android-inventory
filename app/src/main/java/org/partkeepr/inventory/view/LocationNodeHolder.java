package org.partkeepr.inventory.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewHolder;

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
        int txtColor;
        if (selectedLocation == location) {
            bgColor = R.color.design_default_color_primary;
            txtColor = R.color.white;
        }
        else {
            bgColor = R.color.white;
            txtColor = R.color.black;
        }
        Context ctx = itemView.getContext();
        itemView.setBackgroundColor(ContextCompat.getColor(ctx, bgColor));
        lblName.setTextColor(ContextCompat.getColor(ctx, txtColor));
    }
}
