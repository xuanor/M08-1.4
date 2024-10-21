package com.example.m08;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    Button button;
    final String url = "https://api.myip.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Configuración del OnClickListener del botón
        button.setOnClickListener(v -> {
            // Ejecutar la tarea en segundo plano
            executeTask(url);
        });
    }

    // Método para ejecutar la tarea en segundo plano usando ExecutorService
    public void executeTask(String url) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Ejecuta la llamada a la API en un hilo de fondo
                String result = apiCall(url);

                // Ahora maneja el resultado en el hilo principal
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Actualiza la UI con el resultado en el hilo principal
                        if (result != null) {
                            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Error al obtener la respuesta.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    // Método para realizar la llamada a la API
    private String apiCall(String myUrl) {
        String result = null;
        InputStream in;

        try {
            URL url = new URL(myUrl);
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setAllowUserInteraction(false);
            httpsConn.setInstanceFollowRedirects(true);
            httpsConn.setRequestMethod("GET");
            httpsConn.connect();

            int resCode = httpsConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpsConn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8")); // Cambiado a UTF-8
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                in.close();
                result = sb.toString();
            } else {
                result = "Error: " + resCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = "Error: " + e.getMessage();
        }
        return result;
    }
}
