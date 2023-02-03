package org.partkeepr.inventory;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.partkeepr.inventory.entities.StockEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StockAdapter extends ArrayAdapter<StockEntry> {
    public StockAdapter(@NonNull Context context, int resource, @NonNull List<StockEntry> objects) {
        super(context, resource, objects);
    }

    public View getView(int pos, View convertView, ViewGroup parent){
        View v = convertView;
        if(v==null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.item_stock, null);
        }

        StockEntry entry = getItem(pos);
        TextView tv = (TextView)v.findViewById(R.id.lblComment);
        tv.setText(entry.Comment);

        TextView lblUser = v.findViewById(R.id.lblUser);
        lblUser.setText(entry.User.Username);

        TextView lblStock = v.findViewById(R.id.lblStock);
        lblStock.setText((entry.StockLevel > 0?"+":"")+entry.StockLevel);
        if(entry.StockLevel > 0){
            lblStock.setTextColor(Color.GREEN);
        }
        else lblStock.setTextColor(Color.RED);

        TextView lblDateTime = v.findViewById(R.id.lblDateTime);
        String dtStart = "2010-10-15T09:27:37Z";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat outFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        try {
            Date date = format.parse(entry.DateTime.substring(0, 19));
            lblDateTime.setText(outFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
            lblDateTime.setText(entry.DateTime);
        }


        return v;
    }
}
