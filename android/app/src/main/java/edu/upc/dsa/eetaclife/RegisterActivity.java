package edu.upc.dsa.eetaclife;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.upc.dsa.eetaclife.api.RetrofitClient;
import edu.upc.dsa.eetaclife.models.Credentials;
import edu.upc.dsa.eetaclife.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister, btnGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etRegUsername);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena usuario, email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        Credentials credentials = new Credentials(username, email, password);
        RetrofitClient.getApiService().registerUser(credentials).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 201 || response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registro correcto. Inicia sesión", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else if (response.code() == 409) {
                    Toast.makeText(RegisterActivity.this, "El usuario o el email ya están registrados", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 400) {
                    Toast.makeText(RegisterActivity.this, "Datos inválidos o campos vacíos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Sin conexión con el servidor: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}