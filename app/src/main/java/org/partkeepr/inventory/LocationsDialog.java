package org.partkeepr.inventory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.icu.util.CurrencyAmount;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.amrdeveloper.treeview.TreeViewHolderFactory;

import org.partkeepr.inventory.api.entities.Location;
import org.partkeepr.inventory.api.entities.LocationCategory;
import org.partkeepr.inventory.view.CategoryNodeHolder;
import org.partkeepr.inventory.view.CustomTreeManager;
import org.partkeepr.inventory.view.LocationNodeHolder;

import java.util.ArrayList;
import java.util.List;

public class LocationsDialog extends DialogFragment {

    public List<LocationCategory> locationTree;
    public Location selectedLocation;
    public Consumer<Location> onSelect;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        TreeViewHolderFactory factory = (v, layout) -> {
            if (layout == R.layout.tree_category) return new CategoryNodeHolder(v);
            else return new LocationNodeHolder(v, selectedLocation);
        };
        View root = inflater.inflate(R.layout.dialog_locations, null);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        TreeViewAdapter treeViewAdapter = new TreeViewAdapter(factory,
                new CustomTreeManager());
        recyclerView.setAdapter(treeViewAdapter);

        if (locationTree.size() == 1) {
            locationTree = locationTree.get(0).Subcategories;
        }
        List<TreeNode> nodes = ConvertRecursive(locationTree);
        treeViewAdapter.updateTreeNodes(nodes);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        treeViewAdapter.setTreeNodeClickListener((treeNode, view) -> {
            Object value = treeNode.getValue();
            if (value instanceof Location) {
                if (onSelect != null) {
                    onSelect.accept((Location)value);
                }
                dismiss();
            }
        });

        return builder
                .setTitle(R.string.select_location)
                .setView(root)
                .setNegativeButton(R.string.cancel, (dialog, id) -> dismiss())
                .create();
    }

    private List<TreeNode> ConvertRecursive(List<LocationCategory> list) {
        List<TreeNode> nodes = new ArrayList<>();
        for (LocationCategory category : list) {
            TreeNode newNode = new TreeNode(category, R.layout.tree_category);
            if (category.Subcategories != null) {
                for (TreeNode subNode : ConvertRecursive(category.Subcategories)) {
                    newNode.addChild(subNode);
                    if (subNode.isExpanded()) {
                        newNode.setExpanded(true);
                    }
                }
            }
            if (category.Locations != null) {
                for (Location location : category.Locations) {
                    TreeNode node = new TreeNode(location, R.layout.tree_location);
                    if (location == selectedLocation) {
                        node.setSelected(true);
                        newNode.setExpanded(true);
                    }
                    newNode.addChild(node);
                }
            }
            nodes.add(newNode);
        }
        return nodes;
    }
}
