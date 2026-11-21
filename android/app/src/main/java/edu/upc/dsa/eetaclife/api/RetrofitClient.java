package edu.upc.dsa.eetaclife.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 10.0.2.2 apunta al localhost de tu ordenador desde el emulador Android
    // Si Persona 1 usa prefijo (ej. /dsaApp/), añádelo detrás de 8080/
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static EetacApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(EetacApiService.class);
    }
}