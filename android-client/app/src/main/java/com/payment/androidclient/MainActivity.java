package com.payment.androidclient;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ApiClient apiClient;
    private LinearLayout container;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiClient = new ApiClient();
        container = findViewById(R.id.container);
        progressBar = findViewById(R.id.progressBar);
        Button btnLoad = findViewById(R.id.btnLoad);

        btnLoad.setOnClickListener(v -> loadData());
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        container.removeAllViews();

        apiClient.getAllSubscriptions(new ApiClient.Callback<Subscription[]>() {
            @Override
            public void onSuccess(Subscription[] result) {
                progressBar.setVisibility(View.GONE);
                if (result.length == 0) {
                    Toast.makeText(MainActivity.this, "Данных нет", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Subscription sub : result) {
                    TextView tv = new TextView(MainActivity.this);
                    String text = "ID подписки: " + sub.id + "\n" +
                            "Сервис ID: " + sub.serviceId + "\n" +
                            "Метод: " + sub.methodName + "\n" +
                            "Токен: " + sub.token + "\n" +
                            "Действует до: " + sub.validTo;
                    tv.setText(text);
                    tv.setPadding(16, 16, 16, 16);
                    tv.setTextSize(14f);

                    View divider = new View(MainActivity.this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 2));
                    divider.setBackgroundColor(0xFFCCCCCC);

                    container.addView(tv);
                    container.addView(divider);
                }
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Ошибка: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}