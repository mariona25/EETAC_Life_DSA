package edu.upc.dsa.eetaclife.api;

import android.app.Activity;

import edu.upc.dsa.eetaclife.R;
import edu.upc.dsa.eetaclife.models.User;
import edu.upc.dsa.eetaclife.session.SessionStore;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class StudentProfileSource {

    private StudentProfileSource() {}

    public interface Listener {
        void onLoaded(User user);
        void onError(String message);
    }

    public static Call<User> load(Activity activity, Listener listener) {
        String authorization = SessionStore.authorizationHeader(activity);
        if (authorization == null) {
            SessionStore.signOut(activity);
            return null;
        }

        Call<User> call = RetrofitClient.getApiService().getProfile(authorization);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> request, Response<User> response) {
                if (request.isCanceled()) return;

                if (response.code() == 401) {
                    SessionStore.signOut(activity);
                } else if (response.isSuccessful() && response.body() != null) {
                    listener.onLoaded(response.body());
                } else if (response.isSuccessful()) {
                    listener.onError(activity.getString(R.string.error_empty_response));
                } else {
                    listener.onError(ApiErrors.describe(activity, response));
                }
            }

            @Override
            public void onFailure(Call<User> request, Throwable error) {
                if (!request.isCanceled()) {
                    listener.onError(activity.getString(R.string.error_connection));
                }
            }
        });

        return call;
    }
}