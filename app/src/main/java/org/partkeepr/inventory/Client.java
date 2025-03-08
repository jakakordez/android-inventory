package org.partkeepr.inventory;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.partkeepr.inventory.api.IPayloadProvider;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    String ip;
    String token;
    ExecutorService executorService;

    public Client(String ip) {
        executorService = Executors.newSingleThreadExecutor();
        this.ip = ip;
    }

    public CompletableFuture<Boolean> Login(String username, String password) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        IPayloadProvider<JSONObject> payload =() -> {
            JSONObject loginModel = new JSONObject();
            loginModel.put("Email", username);
            loginModel.put("Password", password);
            return loginModel;
        };

        Post("/api/auth/login", payload)
            .handle((model, ex) -> {
                if (ex != null) {
                    future.completeExceptionally(ex);
                }
                else {
                    try {
                        token = model.getString("token");
                        future.complete(true);
                    } catch (JSONException e) {
                        future.completeExceptionally(ex);
                    }
                }
                return null;
            });
        return future;
    }

    public CompletableFuture<JSONArray> List(String address) {
        return Request("GET", address, null)
            .thenCompose(data -> {
                try {
                    return CompletableFuture.completedFuture(new JSONArray(data));
                } catch (JSONException e) {
                    CompletableFuture<JSONArray> failedFuture = new CompletableFuture<>();
                    failedFuture.completeExceptionally(e);
                    return failedFuture;
                }
            });
    }

    public CompletableFuture<JSONObject> Post(String address,
                                              IPayloadProvider<JSONObject> payload)
    {
        return RequestWithJson("POST", address, payload)
            .thenCompose(data -> {
                try {
                    return CompletableFuture.completedFuture(new JSONObject(data));
                } catch (JSONException e) {
                    CompletableFuture<JSONObject> failedFuture = new CompletableFuture<>();
                    failedFuture.completeExceptionally(e);
                    return failedFuture;
                }
            });
    }

    public CompletableFuture<String> RequestWithJson(String method, String address,
                                             IPayloadProvider<JSONObject> payloadProvider)
    {
        return Request(method, address, () -> payloadProvider.Provide().toString());
    }

    public CompletableFuture<String> Request(String method, String address,
                                             IPayloadProvider<String> payloadProvider)
    {
        CompletableFuture<String> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                URL url = new URL("http://" + ip + address);
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                httpCon.setConnectTimeout(2000);
                if (token != null) {
                    httpCon.setRequestProperty("Authorization", "Bearer " + token);
                }
                httpCon.setRequestMethod(method);
                if (payloadProvider != null) {
                    httpCon.setRequestProperty("Content-Type", "application/json");
                    httpCon.setDoOutput(true);
                    OutputStream outStream = httpCon.getOutputStream();
                    OutputStreamWriter out = new OutputStreamWriter(outStream);
                    out.write(payloadProvider.Provide());
                    out.close();
                }
                int code = httpCon.getResponseCode();
                Log.i("CLIENT", method + " " + url + ": got " + code);

                try(InputStream in = new BufferedInputStream(httpCon.getInputStream());
                    InputStreamReader sr = new InputStreamReader(in, UTF_8);
                    BufferedReader br = new BufferedReader(sr)) {

                    StringBuilder stringBuilder = new StringBuilder();
                    String inputStr;
                    while ((inputStr = br.readLine()) != null) {
                        stringBuilder.append(inputStr);
                    }

                    if (code >= 200 && code < 300) {
                        future.complete(stringBuilder.toString());
                        return true;
                    }
                    Log.e("CLIENT", method + ": " + stringBuilder);
                    future.completeExceptionally(
                            new IOException("Server returned code " + code));
                    return false;
                }
            }
            catch (Exception e){
                future.completeExceptionally(e);
                return false;
            }
        });
        return future;
    }
}
