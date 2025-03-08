package org.partkeepr.inventory.api;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;

public class ConnectionInfo {
    public final String username;
    public final String password;
    public final String ip;

    private ConnectionInfo(String username, String password, String ip) {
        this.username = username;
        this.password = password;
        this.ip = ip;
    }

    public void Store(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user", username);
        editor.putString("password", password);
        editor.putString("ip", ip);
        editor.apply();
    }

    public static ConnectionInfo FromPreferences(SharedPreferences pref)
            throws InvalidObjectException
    {
        String user = pref.getString("user", "");
        String password = pref.getString("password", "");
        String ip = pref.getString("ip", "");
        if (user.isEmpty() || password.isEmpty() || ip.isEmpty())
        {
            throw new InvalidObjectException("No connection info found");
        }
        return new ConnectionInfo(user, password, ip);
    }

    public static ConnectionInfo FromQrCode(JSONObject code) throws InvalidObjectException
    {
        try {
            String username = code.getString("username");
            String password = code.getString("password");
            String ip = code.getString("ip");
            return new ConnectionInfo(username, password, ip);
        } catch (JSONException e) {
            throw new InvalidObjectException("Invalid QR code");
        }
    }
}
