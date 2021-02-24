package org.partkeepr.inventory;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.partkeepr.inventory.entities.Part;

public class StockDialogFragment extends DialogFragment {

    public Part part;
    public Partkeepr partkeepr;
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
        txtNumber.setText(part.StockLevel+"");

        btnPlus.setOnClickListener(v1 -> {
            int amount = Integer.parseInt(txtNumber.getText().toString());
            amount++;
            txtNumber.setText(amount+"");
        });

        btnMinus.setOnClickListener(v1 -> {
            int amount = Integer.parseInt(txtNumber.getText().toString());
            amount--;
            txtNumber.setText(amount+"");
        });

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setView(v)
            .setPositiveButton("Save", (dialog, id) -> {
                int amount = Integer.parseInt(txtNumber.getText().toString());
                if(amount == part.StockLevel){
                    partkeepr.AddStock(part, 0, txtComment.getText().toString(), argument -> {
                        if(argument) {
                            if(OnChange != null) OnChange.run();
                            dialog.dismiss();
                        }
                        else Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();

                    });
                }else{
                    partkeepr.SetStock(part, amount, txtComment.getText().toString(), argument -> {
                        if(argument) {
                            if(OnChange != null) OnChange.run();
                            dialog.dismiss();
                        }
                        else Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    });
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
