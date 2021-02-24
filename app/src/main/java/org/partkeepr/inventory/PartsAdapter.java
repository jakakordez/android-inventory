package org.partkeepr.inventory;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.partkeepr.inventory.entities.Part;

import java.util.List;
import java.util.Set;

public class PartsAdapter extends ArrayAdapter<Part> {
    Set<String> checkedParts;

    public PartsAdapter(@NonNull Context context, int resource, @NonNull List<Part> objects, Set<String> checkedParts) {
        super(context, resource, objects);
        this.checkedParts = checkedParts;
    }

    String selected = null;

    public View getView(int pos, View convertView, ViewGroup parent){
        View v = convertView;
        if(v==null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.item_part, null);
        }

        Part part = getItem(pos);
        TextView tv = (TextView)v.findViewById(R.id.lblName);
        tv.setText(part.Name);

        TextView lblStock = v.findViewById(R.id.lblStock);
        lblStock.setText(part.StockLevel+"");

        if(part.Id.equals(selected)){
            v.setBackgroundColor(Color.LTGRAY);
        }
        else v.setBackgroundColor(Color.WHITE);

        ImageView imgCheck = v.findViewById(R.id.imgCheck);
        if(checkedParts.contains(part.Id)){
            imgCheck.setVisibility(View.VISIBLE);
        }
        else imgCheck.setVisibility(View.INVISIBLE);

        return v;
    }

    public void SetSelected(Part item){
        selected = item.Id;
        notifyDataSetChanged();
    }
}
