package com.computech.compras;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        // Defina o tempo de espera da splash screen (em milissegundos)
        int splashScreenTimeout = 3000; // 3 segundos

        new Handler().postDelayed(() -> {
            // Após o tempo de espera, inicie a MainActivity
            Intent intent = new Intent(splash.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, splashScreenTimeout);
    }
}
