package edu.upc.dsa.eetaclife;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.upc.dsa.eetaclife.api.RetrofitClient;
import edu.upc.dsa.eetaclife.models.AuthResponse;
import edu.upc.dsa.eetaclife.models.Credentials;
import edu.upc.dsa.eetaclife.session.SessionStore;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnGoToRegister, btnForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etLoginUsername);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);

        btnLogin.setOnClickListener(v -> loginUser());
        btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        btnForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Contacta con soporte / Próximamente", Toast.LENGTH_SHORT).show());
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        Credentials credentials = new Credentials(username, password);
        RetrofitClient.getApiService().loginUser(credentials).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SessionStore.saveToken(LoginActivity.this, response.body().getToken());
                    Toast.makeText(LoginActivity.this,
                            "¡Bienvenido, " + username + "!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, InicioActivity.class));
                    finish();
                } else if (response.code() == 401 || response.code() == 404) {
                    Toast.makeText(LoginActivity.this,
                            "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable error) {
                Toast.makeText(LoginActivity.this,
                        "Sin conexión con el servidor: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}