package edu.upc.dsa.eetaclife;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InicioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        SharedPreferences prefs = getSharedPreferences("EetacLifePrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Estudiante");

        TextView tvWelcome = findViewById(R.id.tvInicioWelcome);
        tvWelcome.setText("¡Hola, " + username + "!");

        Button btnGoToTienda = findViewById(R.id.btnGoToTienda);
        btnGoToTienda.setOnClickListener(v -> {
            startActivity(new Intent(InicioActivity.this, TiendaActivity.class));
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            startActivity(new Intent(InicioActivity.this, LoginActivity.class));
            finish();
        });
    }
}