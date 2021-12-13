package com.example.pepperecycle.utils;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
 * Classe che gestisce la connessione lato client (invio delle immagini e ricezione della label corretta)
 */
public class ClientManager implements Runnable {

    private static final String TAG = "ClientManager" ;
    String photoPath;
    String postUrl;
    boolean garbageClassified; // Si può togliere
    String garbageType;

    public ClientManager(String photoPath, String postUrl, boolean garbageClassified, String garbageType) {
        this.photoPath = photoPath;
        this.postUrl = postUrl;
        this.garbageClassified = garbageClassified;
        this.garbageType = garbageType;
    }
    public ClientManager(String photoPath, String postUrl, String garbageType) {
        this.photoPath = photoPath;
        this.postUrl = postUrl;
        this.garbageType = garbageType;
    }

    public void run() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image",photoPath,
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File(photoPath)))
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
                .method("POST", body)
                .build();

        try {
            Log.d(TAG, "request: " + request);
            Response response = client.newCall(request).execute();
            this.setGarbageType( response.body().string());
            System.out.println("Tipo di rifiuto: " + garbageType);
            if(garbageType!=null) {
                Log.d(TAG, "Classificazione riuscita.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Classificazione non riuscita.");
        }
    }

    public void setGarbageType(String garbageType) {
        this.garbageType = garbageType;
        Log.d(TAG, "garbageType: " + this.garbageType);
    }

    public String getGarbageType() {
        return garbageType;
    }

    public boolean isGarbageClassified() {
        return garbageClassified;
    }
}

