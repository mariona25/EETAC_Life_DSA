package edu.upc.dsa.eetaclife;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.upc.dsa.eetaclife.api.StudentProfileSource;
import edu.upc.dsa.eetaclife.models.User;
import edu.upc.dsa.eetaclife.ui.StudentStats;
import retrofit2.Call;

public class PerfilActivity extends AppCompatActivity {

    private Call<User> profileCall;
    private TextView username;
    private TextView email;
    private TextView graduation;
    private TextView passed;
    private TextView status;
    private View content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        username = findViewById(R.id.tvPerfilUsername);
        email = findViewById(R.id.tvPerfilEmail);
        graduation = findViewById(R.id.tvPerfilGraduation);
        passed = findViewById(R.id.tvPerfilPassed);
        status = findViewById(R.id.tvPerfilStatus);
        content = findViewById(R.id.perfilContent);

        findViewById(R.id.btnPerfilBack).setOnClickListener(v -> finish());
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
                username.setText(user.getUsername());
                email.setText(user.getEmail());
                graduation.setText(user.isGraduated()
                        ? R.string.profile_graduated : R.string.profile_in_progress);
                passed.setText(getString(R.string.profile_completed,
                        user.getPassedMinigames().size()));

                StudentStats.render(findViewById(R.id.statsPerfil), user);
                content.setVisibility(View.VISIBLE);
                status.setText("");
            }

            @Override
            public void onError(String message) {
                content.setVisibility(View.GONE);
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