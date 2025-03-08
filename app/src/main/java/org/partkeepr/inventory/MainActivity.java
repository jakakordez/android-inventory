package org.partkeepr.inventory;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;
import org.partkeepr.inventory.api.ConnectionInfo;
import org.partkeepr.inventory.api.InventoryApi;
import org.partkeepr.inventory.api.entities.Location;
import org.partkeepr.inventory.api.entities.LocationCategory;
import org.partkeepr.inventory.api.entities.Part;
import org.partkeepr.inventory.api.entities.StockEntry;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<LocationCategory> locationTree;
    ListView lstParts, lstStock;
    InventoryApi inventoryApi;
    ArrayList<StockEntry> stockEntries;
    StockAdapter stockAdapter;
    ArrayList<Part> parts;
    PartsAdapter partAdapter;
    Location selectedLocation;
    Part selectedPart;
    ImageButton btnAdd;
    ImageButton btnMove;
    static final int LAUNCH_QR_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lstStock = findViewById(R.id.lstStock);
        lstParts = findViewById(R.id.lstParts);
        btnAdd = findViewById(R.id.btnAdd);
        btnMove = findViewById(R.id.btnMove);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA }, 1);
        }
    }

    private void TryToConnect(ConnectionInfo info) {
        InventoryApi.Connect(info)
                .thenAccept(api -> {
                    info.Store(getPreferences(MODE_PRIVATE));
                    runOnUiThread(() -> Initialized(api));
                })
                .handle((inventoryApi, throwable) -> {
                    if (throwable != null) {
                        if (throwable.getCause() instanceof SocketTimeoutException) {
                            Toast("Unable to reach the server");
                        }
                        else {
                            Toast("Unable to login");
                        }
                    }
                    return null;
                });
    }

    private void Initialized(InventoryApi api) {
        inventoryApi = api;

        stockEntries = new ArrayList<>();
        stockAdapter = new StockAdapter(this, R.layout.item_stock, stockEntries);

        lstStock.setAdapter(stockAdapter);

        parts = new ArrayList<>();
        partAdapter = new PartsAdapter(this, R.layout.item_part, parts);
        lstParts.setAdapter(partAdapter);
        lstParts.setOnItemClickListener((parent, view, position, id) -> {
            selectedPart = partAdapter.getItem(position);
            partAdapter.SetSelected(selectedPart);
            LoadStock(partAdapter.getItem(position));
        });

        inventoryApi
            .GetLocationTree()
            .thenAccept(locations -> {
                Log.i("GUI", "Received " + locations.size() + " root locations");
                locationTree = locations;
                OpenLocation();
            })
            .handle((a, ex) -> {
                if (ex != null) {
                    Log.e("GUI", "Loading locations failed");
                    runOnUiThread(() -> {
                        Toast("Loading locations failed");
                    });
                }
                return null;
            });

        btnAdd.setOnClickListener(v -> {
            if(selectedPart != null) {
                StockDialogFragment dialog = new StockDialogFragment();
                dialog.part = selectedPart;
                dialog.inventoryApi = inventoryApi;
                dialog.OnChange = this::LoadParts;
                dialog.show(getSupportFragmentManager(), null);
            }
        });

        btnMove.setOnClickListener(v -> {
            if (selectedPart != null) {
                ShowLocationDialog(location -> {
                    selectedPart.LocationId = location.Id;
                    inventoryApi.PutPart(selectedPart).thenAccept(r -> {
                        LoadParts();
                    });
                });
            }
        });

        OpenLocation();
    }

    public void LoadParts(){
        final int id = selectedPart != null ? selectedPart.Id : -1;
        selectedPart = null;
        inventoryApi.GetParts(selectedLocation.Id).thenAccept(newParts -> {
            if(newParts == null){
                Log.e("GUI", "Loading parts failed");
                runOnUiThread(()-> Toast("Loading parts failed"));
            }
            else {
                Log.i("GUI", "Received " + newParts.size() + " parts");
                runOnUiThread(()->{
                    for (Part newPart : newParts) {
                        if(newPart.Id == id) {
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
        });
    }

    public void LoadStock(Part part){
        inventoryApi.GetStock(part.Id).thenAccept(stock -> {
            runOnUiThread(()->{
                stock.sort((o1, o2) -> o2.Timestamp.compareTo(o1.Timestamp));
                stockEntries.clear();
                stockEntries.addAll(stock);
                stockAdapter.notifyDataSetChanged();
                partAdapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public void ShowLocationDialog(Consumer<Location> handler)
    {
        LocationsDialog dialog = new LocationsDialog();
        dialog.locationTree = locationTree;
        dialog.selectedLocation = selectedLocation;
        dialog.onSelect = handler;
        dialog.show(getSupportFragmentManager(), "Locations");
    }

    public void OpenLocation()
    {
        if(locationTree != null){
            ShowLocationDialog(l -> {
                selectedLocation = l;
                setTitle(l.Name);
                LoadParts();
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_location){
            OpenLocation();
            return true;
        }
        else if(item.getItemId() == R.id.menu_refresh){
            if (inventoryApi == null) {
                try {
                    SharedPreferences pref = getPreferences(MODE_PRIVATE);
                    TryToConnect(ConnectionInfo.FromPreferences(pref));
                } catch (Exception e) {
                    Toast("Unable to login");
                }
            }
            else {
                LoadParts();
            }
            return true;
        }
        else if(item.getItemId() == R.id.menu_login){

            Intent i = new Intent(this, QrCodeScanner.class);
            startActivityForResult(i, LAUNCH_QR_ACTIVITY);
        }
        return false;
    }

    @Override
    protected void onResume() {
        if (inventoryApi == null) {
            try {
                SharedPreferences pref = getPreferences(MODE_PRIVATE);
                TryToConnect(ConnectionInfo.FromPreferences(pref));
            } catch (Exception e) {
                Toast("Unable to login");
            }
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_QR_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra(QrCodeScanner.KEY_QR_CODE);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    ConnectionInfo info = ConnectionInfo.FromQrCode(jsonObject);
                    Toast("Logging in as " + info.username);
                    TryToConnect(info);
                }
                catch (Exception e){
                    Toast("Unable to login: " + e.getMessage());
                }
            }
        }
    }

    private void Toast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }
}