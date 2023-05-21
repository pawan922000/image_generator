package com.example.imagen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.airbnb.lottie.animation.content.Content;
import com.bumptech.glide.Glide;
import com.example.imagen.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    String imageUrl;
    String fileName= "code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        if(!isConnected()){
            Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
        }

        binding.btngen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = binding.inputText.getText().toString();
                callApi(text);
                closekeyboard();
            }
        });

        binding.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DownloadManager downloadManager= null;
                    downloadManager= (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri downloadUri= Uri.parse(imageUrl);
                    DownloadManager.Request request= new DownloadManager.Request(downloadUri);
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                            DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false)
                            .setTitle(fileName)
                            .setMimeType("image/jpeg")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator+
                                    fileName+".jpg");
                    downloadManager.enqueue(request);

                    Toast.makeText(MainActivity.this, "downloading", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void closekeyboard() {
        View view= this.getCurrentFocus();
                if(view!=null){
                    InputMethodManager imm= (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                }
    }

    private  Boolean isConnected(){
        ConnectivityManager connectivityManager= (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return  connectivityManager.getActiveNetworkInfo()!=null &&
                connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void callApi(String text) {
        progress(true);
        JSONObject object= new JSONObject();
        try {
            object.put("prompt",text);
            object.put("size","512x512");
        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        RequestBody requestBody= RequestBody.create(object.toString(),JSON);
        Request request= new Request.Builder().url("https://api.openai.com/v1/images/generations")
                .header("Authorization",
                        "Bearer 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")
                .post(requestBody).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"Can't Generate Image\n check network",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    imageUrl = jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
                    loadimage(imageUrl);

                    progress(false);
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"Sorry Could not generate Image",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });
    }

    private void loadimage(String imageUrl) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide
                        .with(MainActivity.this)
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .into(binding.genimage);
            }
        });
    }

    private void progress(Boolean inProgress){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (inProgress){
                    binding.animationView.setVisibility(View.VISIBLE);
                    binding.cardView.setVisibility(View.GONE);
                    binding.btnDownload.setVisibility(View.GONE);
                }
                else {
                    binding.animationView.setVisibility(View.GONE);
                    binding.cardView.setVisibility(View.VISIBLE);
                    binding.btnDownload.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}