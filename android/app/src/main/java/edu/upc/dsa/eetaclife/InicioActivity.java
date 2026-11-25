package edu.upc.dsa.eetaclife;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.upc.dsa.eetaclife.api.StudentProfileSource;
import edu.upc.dsa.eetaclife.models.User;
import edu.upc.dsa.eetaclife.session.SessionStore;
import edu.upc.dsa.eetaclife.ui.StudentStats;
import retrofit2.Call;

public class InicioActivity extends AppCompatActivity {

    private Call<User> profileCall;
    private TextView welcome;
    private TextView status;
    private View stats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        welcome = findViewById(R.id.tvInicioWelcome);
        status = findViewById(R.id.tvInicioStatus);
        stats = findViewById(R.id.statsInicio);

        findViewById(R.id.btnGoToTienda).setOnClickListener(v ->
                startActivity(new Intent(this, TiendaActivity.class)));
        findViewById(R.id.btnGoToPerfil).setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> SessionStore.signOut(this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (profileCall != null) {
            profileCall.cancel();
        }

        status.setText(R.string.loading_profile);
        profileCall = StudentProfileSource.load(this, new StudentProfileSource.Listener() {
            @Override
            public void onLoaded(User user) {
                welcome.setText(getString(R.string.home_welcome, user.getUsername()));
                StudentStats.render(stats, user);
                status.setText("");
            }

            @Override
            public void onError(String message) {
                stats.setVisibility(View.GONE);
                status.setText(message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (profileCall != null) {
            profileCall.cancel();
        }
        super.onDestroy();
    }
}