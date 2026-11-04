package com.payment.androidclient;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.*;

public class ApiClient {
    //  вставить IP сервера (192.168.1.13)
    private static final String BASE_URL = "http://192.168.1.13:8081";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getAllSubscriptions(Callback<Subscription[]> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/payments")
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resBody = response.body().string();
                mainHandler.post(() -> {
                    if (response.isSuccessful()) {
                        Subscription[] subs = gson.fromJson(resBody, Subscription[].class);
                        callback.onSuccess(subs);
                    } else {
                        callback.onError("Ошибка сервера: " + response.code());
                    }
                });
            }
        });
    }
}