package edu.upc.dsa.eetaclife.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import edu.upc.dsa.eetaclife.LoginActivity;

public final class SessionStore {

    private static final String PREFS = "EetacLifePrefs";

    private SessionStore() {}

    public static void saveToken(Context context, String token) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString("token", token).apply();
    }

    public static String authorizationHeader(Context context) {
        String token = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("token", null);
        return token == null || token.isEmpty() ? null : "Bearer " + token;
    }

    public static void signOut(Activity activity) {
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}