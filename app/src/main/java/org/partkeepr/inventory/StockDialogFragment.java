package org.partkeepr.inventory;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.partkeepr.inventory.api.InventoryApi;
import org.partkeepr.inventory.api.entities.Part;

import java.util.Locale;

public class StockDialogFragment extends DialogFragment {

    public Part part;
    public InventoryApi inventoryApi;
    public Runnable OnChange;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_stock, null);

        TextView lblTitle = v.findViewById(R.id.lblTitle);
        Button btnPlus = v.findViewById(R.id.btnPlus);
        Button btnMinus = v.findViewById(R.id.btnMinus);
        EditText txtNumber = v.findViewById(R.id.txtNumber);
        EditText txtComment = v.findViewById(R.id.txtComment);

        lblTitle.setText(part.Name);
        txtNumber.setText(String.format(Locale.ENGLISH, "%d", part.StockLevel));

        btnPlus.setOnClickListener(v1 -> {
            int amount = Integer.parseInt(txtNumber.getText().toString());
            amount++;
            txtNumber.setText(String.format(Locale.ENGLISH, "%d", amount));
        });

        btnMinus.setOnClickListener(v1 -> {
            int amount = Integer.parseInt(txtNumber.getText().toString());
            amount--;
            txtNumber.setText(String.format(Locale.ENGLISH, "%d", amount));
        });

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setView(v)
            .setPositiveButton("Save", (dialog, id) -> {
                int amount = Integer.parseInt(txtNumber.getText().toString());
                String comment = txtComment.getText().toString();
                inventoryApi.SetStock(part.Id, amount, comment)
                        .thenAccept(r -> {
                            if(OnChange != null) OnChange.run();
                            dialog.dismiss();
                        });
            })
            .setNegativeButton("Cancel", (dialog, id) -> {
                // User cancelled the dialog
            });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
