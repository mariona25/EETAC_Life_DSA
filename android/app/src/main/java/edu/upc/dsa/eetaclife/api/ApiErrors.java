package edu.upc.dsa.eetaclife.api;

import android.content.Context;

import java.io.IOException;

import edu.upc.dsa.eetaclife.R;
import retrofit2.Response;

public final class ApiErrors {

    private ApiErrors() {}

    public static String describe(Context context, Response<?> response) {
        if (response.errorBody() != null) {
            try {
                String message = response.errorBody().string().trim();
                if (!message.isEmpty()) {
                    return message;
                }
            } catch (IOException ignored) {
                // Si no se puede leer la respuesta, mostramos el código HTTP.
            }
        }

        return context.getString(R.string.error_http, response.code());
    }
}