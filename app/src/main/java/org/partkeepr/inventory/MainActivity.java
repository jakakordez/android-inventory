package org.partkeepr.inventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;
import org.partkeepr.inventory.entities.Part;
import org.partkeepr.inventory.entities.StockEntry;
import org.partkeepr.inventory.entities.StorageLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ListView lstParts, lstStock;
    Partkeepr partkeepr;
    ArrayList<StockEntry> stockEntries;
    StockAdapter stockAdapter;
    ArrayList<Part> parts;
    PartsAdapter partAdapter;
    ArrayList<StorageLocation> locations;
    StorageLocation selectedLocation;
    Part selectedPart;
    ImageButton btnAdd;
    Set<String> checkedParts = new HashSet<String>();
    static final int LAUNCH_QR_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lstStock = findViewById(R.id.lstStock);
        lstParts = findViewById(R.id.lstParts);
        btnAdd = findViewById(R.id.btnAdd);
        Initialize(new Client());

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);
        }
    }

    private void Initialize(Client client){
        partkeepr = new Partkeepr(client);

        stockEntries = new ArrayList<>();
        stockAdapter = new StockAdapter(this, R.layout.item_stock, stockEntries);

        lstStock.setAdapter(stockAdapter);

        parts = new ArrayList<>();
        partAdapter = new PartsAdapter(this, R.layout.item_part, parts, checkedParts);
        lstParts.setAdapter(partAdapter);
        lstParts.setOnItemClickListener((parent, view, position, id) -> {
            selectedPart = partAdapter.getItem(position);
            partAdapter.SetSelected(selectedPart);
            LoadStock(partAdapter.getItem(position));
        });

        LoadParts();
        partkeepr.GetLocations(newLocations -> {
            if(newLocations == null){
                Log.e("GUI", "Loading locations failed");
                runOnUiThread(()-> {
                    Toast.makeText(MainActivity.this, "Loading locations failed", Toast.LENGTH_SHORT).show();
                });
            }
            else{
                Log.i("GUI", "Received " + newLocations.size() + " locations");
                locations = newLocations;
            }
        });


        btnAdd.setOnClickListener(v -> {
            if(selectedPart != null) {
                StockDialogFragment dialog = new StockDialogFragment();
                dialog.part = selectedPart;
                dialog.partkeepr = partkeepr;
                dialog.OnChange = this::LoadParts;
                dialog.show(getSupportFragmentManager(), null);
            }
        });
    }

    public void LoadParts(){
        final String id = selectedPart != null ? selectedPart.Id : null;
        selectedPart = null;
        partkeepr.GetParts(newParts -> {
            if(newParts == null){
                Log.e("GUI", "Loading parts failed");
                runOnUiThread(()-> {
                    Toast.makeText(MainActivity.this, "Loading parts failed", Toast.LENGTH_SHORT).show();
                });
            }
            else {
                Log.i("GUI", "Received " + newParts.size() + " parts");
                runOnUiThread(()->{
                    for (Part newPart : newParts) {
                        if(newPart.Id.equals(id)) {
                            selectedPart = newPart;
                            break;
                        }
                    }
                    parts.clear();
                    parts.addAll(newParts);
                    partAdapter.notifyDataSetChanged();

                    if(selectedPart != null) LoadStock(selectedPart);
                });
            }
        }, selectedLocation);
    }

    public void LoadStock(Part part){
        partkeepr.GetStock(stock -> {
            runOnUiThread(()->{
                part.StockEntries = stock;
                if(part.CheckedToday()) checkedParts.add(part.Id);
                stockEntries.clear();
                stockEntries.addAll(stock);
                stockAdapter.notifyDataSetChanged();
                partAdapter.notifyDataSetChanged();
            });
        }, part);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public void ShowLocationDialog(Consumer<StorageLocation> handler)
    {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_launcher_background);
        builderSingle.setTitle("Select location");

        final ArrayAdapter<StorageLocation> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, locations);

        builderSingle.setNegativeButton("All", (dialog, which) -> {
            dialog.dismiss();
            handler.accept(null);
        });

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            dialog.dismiss();
            handler.accept(arrayAdapter.getItem(which));
        });
        builderSingle.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_location){
            if(locations != null){
                ShowLocationDialog(l -> {
                    selectedLocation = l;
                    LoadParts();
                });
            }
            return true;
        }
        else if(item.getItemId() == R.id.menu_refresh){
            LoadParts();
            return true;
        }
        else if(item.getItemId() == R.id.menu_login){

            Intent i = new Intent(this, QrCodeScanner.class);
            startActivityForResult(i, LAUNCH_QR_ACTIVITY);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_QR_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra(QrCodeScanner.KEY_QR_CODE);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String username = jsonObject.getString("username");
                    String password = jsonObject.getString("password");
                    String ip = jsonObject.getString("ip");
                    Toast.makeText(getApplicationContext(), "Logging in as " + username, Toast.LENGTH_SHORT).show();
                    Initialize(new Client(ip, username, password));
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }
}