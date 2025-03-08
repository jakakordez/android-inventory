package org.partkeepr.inventory;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.partkeepr.inventory.api.entities.StockEntry;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class StockAdapter extends ArrayAdapter<StockEntry> {
    public StockAdapter(@NonNull Context context, int resource,
                        @NonNull List<StockEntry> objects)
    {
        super(context, resource, objects);
    }

    public View getView(int pos, View convertView, ViewGroup parent){
        View v = convertView;
        if(v==null) {
            LayoutInflater vi = (LayoutInflater)getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.item_stock, null);
        }

        StockEntry entry = getItem(pos);
        TextView tv = v.findViewById(R.id.lblComment);
        tv.setText(entry.Comment);

        TextView lblUser = v.findViewById(R.id.lblUser);
        lblUser.setText(entry.UserName);

        TextView lblStock = v.findViewById(R.id.lblStock);
        String sign = entry.Change > 0 ? "+" : "";
        lblStock.setText(String.format(Locale.ENGLISH, "%s%d", sign, entry.Change));
        if(entry.Change > 0){
            lblStock.setTextColor(Color.GREEN);
        }
        else lblStock.setTextColor(Color.RED);

        TextView lblDateTime = v.findViewById(R.id.lblDateTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        lblDateTime.setText(entry.Timestamp.format(formatter));

        return v;
    }
}
