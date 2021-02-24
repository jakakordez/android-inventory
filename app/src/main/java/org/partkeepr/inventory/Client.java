package org.partkeepr.inventory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

public class Client {

    public interface OnResult<T>{

        void Result(T argument);
    }
    String ip;

    String user;
    String password;
    ExecutorService executorService;

    public Client(String ip, String username, String password) {
        executorService = Executors.newSingleThreadExecutor();
        this.ip = ip;
        this.user = username;
        this.password = password;
    }

    public Client(){
        executorService = Executors.newSingleThreadExecutor();
        ip = "192.168.1.1:8080";
        user = "user";
        password = "pass123";
    }

    public Future<JSONObject> Request(String address, OnResult<JSONObject> onResult){
        return executorService.submit(() -> {
            JSONObject jsonObject = RequestSync(address);
            onResult.Result(jsonObject);
            return jsonObject;
        });
    }

    private JSONObject RequestSync(String address){
        try {
            URL url = new URL("http://" + ip + "/api" + address);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", GetAuth());
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                int code = urlConnection.getResponseCode();
                Log.i("CLIENT", "GET " + url + ": got response code " + code);
                if(code < 200 || code >= 300) return null;
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }
                return new JSONObject(responseStrBuilder.toString());
            } finally {
                urlConnection.disconnect();
            }
        }
        catch (Exception e){
            return null;
        }
    }

    private String GetAuth(){
        String auth = user + ":" + password;
        String encodedAuth = Base64.encodeToString(auth.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        return "Basic " + encodedAuth;
    }

    public void Put(String address, String data, OnResult<Boolean> onResult){
        executorService.submit(() -> {
            try {
                URL url = new URL("http://" + ip + address);
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                httpCon.setRequestProperty("Authorization", GetAuth());
                httpCon.setDoOutput(true);
                httpCon.setRequestMethod("PUT");
                OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
                out.write(data);
                out.close();
                int code = httpCon.getResponseCode();
                Log.i("CLIENT", "PUT " + url + ": got response code " + code);
                if(code >= 200 && code < 300){
                    onResult.Result(true);
                    return true;
                }else{
                    InputStream in = new BufferedInputStream(httpCon.getInputStream());
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null) {
                        responseStrBuilder.append(inputStr);
                    }
                    Log.e("PUT", responseStrBuilder.toString());
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            onResult.Result(false);
            return false;
        });
    }
}
