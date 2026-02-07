package org.partkeepr.inventory;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.partkeepr.inventory.api.entities.Part;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PartsAdapter extends BaseAdapter {

    @NonNull
    private final Context context;
    @NonNull
    private final List<Part> objects;

    public PartsAdapter(@NonNull Context context,
                        @NonNull List<Part> objects)
    {
        this.context = context;
        this.objects = objects;
    }

    public boolean ShowZero = false;
    private Integer selected = null;

    private Stream<Part> GetStream()
    {
        return objects.stream()
                .filter(o -> o.StockLevel > 0 || ShowZero)
                .sorted(Comparator.comparing(o -> o.Name, String::compareToIgnoreCase));
    }

    @Override
    public int getCount() {
        return (int)GetStream().count();
    }

    @Override
    public Part getItem(int position) {
        Optional<Part> first = GetStream().skip(position).findFirst();
        return first.orElse(null);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int pos, View convertView, ViewGroup parent){
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.item_part, null);
        }

        Part part = getItem(pos);
        TextView tv = v.findViewById(R.id.lblName);
        tv.setText(part.Name);

        TextView lblStock = v.findViewById(R.id.lblStock);
        lblStock.setText(part.StockLevel+"");

        if (selected != null && part.Id == selected){
            v.setBackgroundColor(Color.GRAY);
        }
        else v.setBackgroundColor(ColorHelper.GetBackgroundColor(context));

        ImageView imgCheck = v.findViewById(R.id.imgCheck);
        if(part.CheckedRecently()){
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
