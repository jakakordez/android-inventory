package org.partkeepr.inventory.view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewHolder;

import org.partkeepr.inventory.R;
import org.partkeepr.inventory.api.entities.LocationCategory;

public class CategoryNodeHolder extends TreeViewHolder {

    public CategoryNodeHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void bindTreeNode(TreeNode node) {
        super.bindTreeNode(node);
        TextView lblName = itemView.findViewById(R.id.lblName);
        LocationCategory category = (LocationCategory) node.getValue();
        lblName.setText(category.Name);
    }
}
